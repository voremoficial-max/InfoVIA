package com.alertaciudadana.app.util

object AppConfig {
    const val DEFAULT_LATITUDE = 4.7110
    const val DEFAULT_LONGITUDE = -74.0721

    // En Fase 3 todos los reportes tienen una vigencia fija de 2 horas.
    const val REPORT_TTL_MINUTES = 120L
    const val REPORTS_COLLECTION = "reports"
    const val REPORT_MIN_INTERVAL_SECONDS = 30L

    // GitHub Releases se usa para avisar de nuevas versiones sin depender de
    // volver a distribuir manualmente el APK. GitHub Actions rellena este
    // valor automáticamente con el repositorio donde se compila.
    const val UPDATE_CHECK_INTERVAL_HOURS = 12L
}
