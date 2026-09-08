package com.alertaciudadana.app.util

import com.alertaciudadana.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

/** Comprueba una vez cada cierto tiempo la última release pública de GitHub. */
object UpdateChecker {
    data class UpdateInfo(
        val versionName: String,
        val downloadUrl: String,
        val releaseUrl: String,
        val notes: String
    )

    private const val PREFS = "infovia_updates"
    private const val LAST_CHECK = "last_check_at"

    suspend fun checkIfDue(context: android.content.Context, force: Boolean = false): UpdateInfo? = withContext(Dispatchers.IO) {
        val repository = BuildConfig.UPDATE_REPOSITORY.trim()
        if (repository.isBlank() || repository.startsWith("TU_USUARIO/")) return@withContext null

        val prefs = context.applicationContext.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val last = prefs.getLong(LAST_CHECK, 0L)
        if (!force && last > 0L && now - last < TimeUnit.HOURS.toMillis(AppConfig.UPDATE_CHECK_INTERVAL_HOURS)) {
            return@withContext null
        }
        prefs.edit().putLong(LAST_CHECK, now).apply()

        val connection = (URL("https://api.github.com/repos/$repository/releases/latest").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 7000
            readTimeout = 7000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "InfoVia/${BuildConfig.VERSION_NAME}")
        }

        try {
            if (connection.responseCode !in 200..299) return@withContext null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val tag = json.optString("tag_name").removePrefix("v").trim()
            val current = BuildConfig.VERSION_NAME.trim()
            if (!isNewer(tag, current)) return@withContext null

            val assets = json.optJSONArray("assets") ?: return@withContext null
            var apkUrl: String? = null
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                val name = asset.optString("name")
                if (name.endsWith(".apk", ignoreCase = true)) {
                    apkUrl = asset.optString("browser_download_url").takeIf { it.isNotBlank() }
                    if (apkUrl != null) break
                }
            }
            val download = apkUrl ?: return@withContext null
            UpdateInfo(
                versionName = tag,
                downloadUrl = download,
                releaseUrl = json.optString("html_url", download),
                notes = json.optString("body").trim()
            )
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun isNewer(remote: String, current: String): Boolean {
        fun parts(value: String): List<Int> = value.split('.', '-', '_').mapNotNull { it.toIntOrNull() }.take(4).let {
            if (it.isEmpty()) listOf(0) else it
        }
        val a = parts(remote)
        val b = parts(current)
        for (i in 0 until maxOf(a.size, b.size)) {
            val av = a.getOrElse(i) { 0 }
            val bv = b.getOrElse(i) { 0 }
            if (av != bv) return av > bv
        }
        return false
    }
}
