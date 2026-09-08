package com.alertaciudadana.app.data.reports

import com.google.firebase.Timestamp

enum class ReportType(val firestoreValue: String) {
    TRANSITO("transito"),
    ACCIDENTE("accidente")
}

enum class ReportLevel(val firestoreValue: String) {
    BAJO("bajo"),
    MEDIO("medio"),
    ALTO("alto")
}

enum class ReportStatus(val firestoreValue: String) {
    ACTIVO("activo"),
    EXPIRADO("expirado")
}

data class Report(
    val id: String,
    val type: ReportType,
    val level: ReportLevel,
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val date: String,
    val time: String,
    val serverTimestamp: Timestamp? = null,
    val expiresAt: Timestamp? = null,
    val status: ReportStatus = ReportStatus.ACTIVO
)
