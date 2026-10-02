package co.edu.uniquindio.entorno.domain.model

enum class NotificationType{
    NEW_COMMENT, REPORT_VERIFIED, REPORT_REJECTED, REPORT_RESOLVED, BADGE_EARNED, LEVEL_UP
}

data class AppNotification(
    val id: String = "",
    val userId: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val reportId: String? = null,
    val read: Boolean = false,
    val createdAt: Long = 0L
)
