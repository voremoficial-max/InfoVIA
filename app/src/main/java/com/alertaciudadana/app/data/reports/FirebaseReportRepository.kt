package com.alertaciudadana.app.data.reports

import android.content.Context
import com.alertaciudadana.app.util.AppConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.Timestamp

class FirebaseNotConfiguredException(message: String) : IllegalStateException(message)

class FirebaseReportRepository(context: Context) {
    private val appContext = context.applicationContext
    private val reportLimitPreferences = appContext.getSharedPreferences("infovia_report_limits", Context.MODE_PRIVATE)

    private fun firebaseAppOrNull(): FirebaseApp? = runCatching {
        FirebaseApp.getApps(appContext).firstOrNull() ?: FirebaseApp.initializeApp(appContext)
    }.getOrNull()

    private fun firestoreOrNull(): FirebaseFirestore? =
        firebaseAppOrNull()?.let { runCatching { FirebaseFirestore.getInstance(it) }.getOrNull() }

    private fun authOrNull(): FirebaseAuth? =
        firebaseAppOrNull()?.let { runCatching { FirebaseAuth.getInstance(it) }.getOrNull() }

    suspend fun createReport(
        type: ReportType,
        level: ReportLevel,
        latitude: Double,
        longitude: Double,
        city: String,
        date: String,
        time: String
    ): String {
        require(latitude in -90.0..90.0) { "Latitud fuera de rango." }
        require(longitude in -180.0..180.0) { "Longitud fuera de rango." }
        require(city.isNotBlank()) { "Debes seleccionar una ciudad." }

        val nowMillis = System.currentTimeMillis()
        val lastReportMillis = reportLimitPreferences.getLong("last_report_at", 0L)
        val elapsedSeconds = (nowMillis - lastReportMillis) / 1000L
        if (lastReportMillis > 0L && elapsedSeconds < AppConfig.REPORT_MIN_INTERVAL_SECONDS) {
            val remaining = AppConfig.REPORT_MIN_INTERVAL_SECONDS - elapsedSeconds
            throw IllegalStateException("Espera $remaining segundos antes de enviar otro reporte.")
        }

        val db = firestoreOrNull() ?: throw FirebaseNotConfiguredException(
            "Firebase no se pudo inicializar. Verifica app/google-services.json."
        )
        val auth = authOrNull() ?: throw FirebaseNotConfiguredException(
            "Firebase Authentication no está disponible."
        )

        if (auth.currentUser == null) {
            runCatching { auth.signInAnonymously().await() }.getOrElse { error ->
                throw IllegalStateException(
                    "No se pudo iniciar sesión anónimamente. Activa Authentication → Anonymous en Firebase.",
                    error
                )
            }
        }

        val id = UUID.randomUUID().toString()
        val data = hashMapOf<String, Any?>(
            "id" to id,
            "type" to type.firestoreValue,
            "level" to level.firestoreValue,
            "latitude" to latitude,
            "longitude" to longitude,
            "city" to city,
            "date" to date,
            "time" to time,
            "serverTimestamp" to FieldValue.serverTimestamp(),
            "expiresAt" to Timestamp.now().let { Timestamp(it.seconds + AppConfig.REPORT_TTL_MINUTES * 60, it.nanoseconds) },
            "status" to ReportStatus.ACTIVO.firestoreValue
        )

        runCatching { db.collection(AppConfig.REPORTS_COLLECTION).document(id).set(data).await() }
            .getOrElse { error ->
                throw IllegalStateException(
                    "No se pudo guardar el reporte en Firestore. Verifica Firestore y sus reglas.", error
                )
            }
        reportLimitPreferences.edit().putLong("last_report_at", System.currentTimeMillis()).apply()
        return id
    }

    fun observeActiveReports(city: String): Flow<List<Report>> = callbackFlow {
        val db = firestoreOrNull()
        if (db == null) { close(FirebaseNotConfiguredException("Firebase no está disponible.")); return@callbackFlow }

        val registration: ListenerRegistration = db.collection(AppConfig.REPORTS_COLLECTION)
            // Solo escucha reportes activos, de la ciudad seleccionada y que todavía
            // no vencieron. Esto reduce documentos transferidos y trabajo del cliente.
            .whereEqualTo("city", city)
            .whereEqualTo("status", ReportStatus.ACTIVO.firestoreValue)
            .whereGreaterThan("expiresAt", Timestamp.now())
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val now = Timestamp.now()
                val reports = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    runCatching {
                        val expires = doc.getTimestamp("expiresAt")
                        if (expires != null && expires <= now) return@runCatching null
                        if (doc.getString("status") != ReportStatus.ACTIVO.firestoreValue) return@runCatching null
                        Report(
                            id = doc.getString("id") ?: doc.id,
                            type = if (doc.getString("type") == "accidente") ReportType.ACCIDENTE else ReportType.TRANSITO,
                            level = when (doc.getString("level")) {
                                "medio" -> ReportLevel.MEDIO
                                "alto" -> ReportLevel.ALTO
                                else -> ReportLevel.BAJO
                            },
                            latitude = doc.getDouble("latitude") ?: return@runCatching null,
                            longitude = doc.getDouble("longitude") ?: return@runCatching null,
                            city = doc.getString("city") ?: city,
                            date = doc.getString("date") ?: "",
                            time = doc.getString("time") ?: "",
                            serverTimestamp = doc.getTimestamp("serverTimestamp"),
                            expiresAt = expires
                        )
                    }.getOrNull()
                }
                trySend(reports)
            }
        awaitClose { registration.remove() }
    }
}
