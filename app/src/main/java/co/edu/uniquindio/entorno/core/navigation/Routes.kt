package co.edu.uniquindio.entorno.core.navigation

import kotlinx.serialization.Serializable

// ---- Autenticación (sin sesión)
@Serializable data object Welcome
@Serializable data object Login
@Serializable data object Register
@Serializable data object ForgotPassword

// ---- Compartidas por ambos grafos
@Serializable data object Terms
@Serializable data object Privacy

// ---- Pestañas de la barra inferior
@Serializable data object Home
@Serializable data class MapRoute(val focus: String? = null)
@Serializable data object MyReports
@Serializable data object Profile
@Serializable data object ModeratorPanel

// ---- Reportes
@Serializable data class ReportDetail(val reportId: String)
@Serializable data class Comments(val reportId: String)
@Serializable data class ReviewReport(val reportId: String)

/** reportId = null crea un reporte nuevo; con valor servirá para editarlo (pendiente). */
@Serializable data class CreateReport(val reportId: String? = null)
@Serializable data object PickLocation

// ---- Cuenta y comunidad
@Serializable data object Notifications
@Serializable data object EditProfile
@Serializable data object Settings
@Serializable data object ChangePassword
@Serializable data object Heroes
@Serializable data object Stats
@Serializable data object Badges
@Serializable data object HeatMap