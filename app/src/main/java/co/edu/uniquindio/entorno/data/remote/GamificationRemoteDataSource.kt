package co.edu.uniquindio.entorno.data.remote

import co.edu.uniquindio.entorno.data.model.NotificationDto
import co.edu.uniquindio.entorno.data.model.ReportDto
import co.edu.uniquindio.entorno.data.model.UserDto
import co.edu.uniquindio.entorno.domain.model.Badge
import co.edu.uniquindio.entorno.domain.model.NotificationType
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.domain.model.UserLevel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@Singleton
class GamificationRemoteDataSource @Inject constructor(private val db: FirebaseFirestore) {

    private val users = db.collection("users")
    private val reports = db.collection("reports")
    private val notifications = db.collection("notifications")
    private val countedStatuses = listOf(ReportStatus.VERIFIED.name, ReportStatus.RESOLVED.name)

    /** Suma (o resta) puntos y/o entrega una insignia una sola vez. Avisa de nivel e insignia. */
    suspend fun apply(userId: String, points: Int = 0, badge: Badge? = null) {
        val ref = users.document(userId)
        db.runTransaction { tx ->
            val user = tx.get(ref).toObject(UserDto::class.java) ?: return@runTransaction Unit
            val isNewBadge = badge != null && badge.name !in user.badges
            if (points == 0 && !isNewBadge) return@runTransaction Unit

            val newPoints = (user.points + points).coerceAtLeast(0)
            val updates = mutableMapOf<String, Any>("points" to newPoints)
            if (badge != null && isNewBadge) updates["badges"] = FieldValue.arrayUnion(badge.name)
            tx.update(ref, updates)

            val newLevel = UserLevel.fromPoints(newPoints)
            if (newLevel.ordinal > UserLevel.fromPoints(user.points).ordinal) {
                tx.set(
                    notifications.document(),
                    NotificationDto(
                        userId = userId, type = NotificationType.LEVEL_UP.name,
                        title = "Nuevo nivel: ${newLevel.label}.",
                        message = "Llegaste a $newPoints puntos. ¡Sigue ayudando a tu comunidad!"
                    )
                )
            }
            if (badge != null && isNewBadge) {
                tx.set(
                    notifications.document(),
                    NotificationDto(
                        userId = userId, type = NotificationType.BADGE_EARNED.name,
                        title = "Nueva insignia: ${badge.label}", message = badge.description
                    )
                )
            }
            Unit
        }.await()
    }

    /** Reportes del usuario que ya fueron verificados o resueltos. */
    suspend fun verifiedCount(ownerId: String): Int =
        reports.whereEqualTo("ownerId", ownerId).get().await()
            .toObjects(ReportDto::class.java)
            .count { it.status in countedStatuses }

    /** Reporte verificado o resuelto con más votos del mes en curso. */
    suspend fun topReportOfMonth(): ReportDto? {
        val start = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        return reports.whereGreaterThanOrEqualTo("createdAt", Date.from(start)).get().await()
            .toObjects(ReportDto::class.java)
            .filter { it.status in countedStatuses }
            .maxByOrNull { it.importantCount }
    }
}