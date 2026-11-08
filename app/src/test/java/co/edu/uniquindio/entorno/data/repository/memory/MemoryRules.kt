package co.edu.uniquindio.entorno.data.repository.memory

import co.edu.uniquindio.entorno.domain.model.AppNotification
import co.edu.uniquindio.entorno.domain.model.Badge
import co.edu.uniquindio.entorno.domain.model.NotificationType
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import java.time.LocalDate
import kotlinx.coroutines.flow.update

/*
 * Reglas de negocio que en la Fase 3 vivirán en el backend (puntos, niveles, insignias y
 * notificaciones). Aquí se aplican en memoria cada vez que ocurre la acción.
 */

private const val VERIFIED_FOR_BADGE = 10
private const val MIN_VOTES_TOP_REPORT = 10

/** Crea una notificación para [userId]. */
internal fun MemoryDatabase.pushNotification(
    userId: String,
    type: NotificationType,
    title: String,
    message: String,
    reportId: String? = null
) {
    val notification = AppNotification(
        id = newId("n"), userId = userId, type = type, title = title, message = message,
        reportId = reportId, read = false, createdAt = System.currentTimeMillis()
    )
    notifications.update { it + notification }
}

/** Suma (o resta) puntos y avisa si el usuario sube de nivel. */
internal fun MemoryDatabase.addPoints(userId: String, delta: Int) {
    val before = users.value.firstOrNull { it.id == userId } ?: return
    users.update { list ->
        list.map { user ->
            if (user.id == userId) user.copy(points = (user.points + delta).coerceAtLeast(0)) else user
        }
    }
    val after = users.value.firstOrNull { it.id == userId } ?: return
    if (after.level.ordinal > before.level.ordinal) {
        pushNotification(
            userId = userId, type = NotificationType.LEVEL_UP,
            title = "Nuevo nivel: ${after.level.label}.",
            message = "Llegaste a ${after.points} puntos. ¡Sigue ayudando a tu comunidad!"
        )
    }
}

/** Entrega una insignia una sola vez y avisa al usuario. */
internal fun MemoryDatabase.awardBadge(userId: String, badge: Badge) {
    val user = users.value.firstOrNull { it.id == userId } ?: return
    if (badge in user.badges) return
    users.update { list ->
        list.map { u -> if (u.id == userId && badge !in u.badges) u.copy(badges = u.badges + badge) else u }
    }
    pushNotification(
        userId = userId, type = NotificationType.BADGE_EARNED,
        title = "Nueva insignia: ${badge.label}", message = badge.description
    )
}

/** Insignia "Ciudadano confiable" al llegar a 10 reportes verificados o resueltos. */
internal fun MemoryDatabase.checkVerifiedBadge(ownerId: String) {
    val count = reports.value.count {
        it.ownerId == ownerId && (it.status == ReportStatus.VERIFIED || it.status == ReportStatus.RESOLVED)
    }
    if (count >= VERIFIED_FOR_BADGE) awardBadge(ownerId, Badge.TEN_VERIFIED_REPORTS)
}

/** Insignia "Destacado del mes" para el reporte más votado del mes en curso. */
internal fun MemoryDatabase.checkTopReportOfMonth() {
    val today = LocalDate.now()
    val top = reports.value
        .filter { it.date.year == today.year && it.date.month == today.month }
        .filter { it.status == ReportStatus.VERIFIED || it.status == ReportStatus.RESOLVED }
        .maxByOrNull { it.importantCount } ?: return
    if (top.importantCount >= MIN_VOTES_TOP_REPORT) awardBadge(top.ownerId, Badge.TOP_REPORT_OF_MONTH)
}
