package co.edu.uniquindio.entorno.domain.model

enum class ThemeMode(val label: String) {
    SYSTEM("Según el sistema"),
    LIGHT("Claro"),
    DARK("Oscuro")
}

/** Preferencias de la app (Configuración). En la Fase 2 viven solo en memoria. */
data class AppSettings(
    val notifyNearbyReports: Boolean = true,
    val notifyCommentsOnMyReports: Boolean = true,
    val notifyLevelAndBadges: Boolean = true,
    /** Radio de alertas en kilómetros (1 a 10). */
    val alertRadiusKm: Int = 2,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)
