package co.edu.uniquindio.entorno.data.repository.memory

import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.core.util.safeCall
import co.edu.uniquindio.entorno.domain.model.AppNotification
import co.edu.uniquindio.entorno.domain.model.AppSettings
import co.edu.uniquindio.entorno.domain.model.Badge
import co.edu.uniquindio.entorno.domain.model.Comment
import co.edu.uniquindio.entorno.domain.model.EmailAlreadyInUseException
import co.edu.uniquindio.entorno.domain.model.InvalidCredentialsException
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.NotificationType
import co.edu.uniquindio.entorno.domain.model.PointsRule
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.model.UserRole
import co.edu.uniquindio.entorno.domain.model.UserSession
import co.edu.uniquindio.entorno.domain.model.WeakPasswordException
import co.edu.uniquindio.entorno.domain.repository.AuthRepository
import co.edu.uniquindio.entorno.domain.repository.CommentRepository
import co.edu.uniquindio.entorno.domain.repository.ImageRepository
import co.edu.uniquindio.entorno.domain.repository.NotificationRepository
import co.edu.uniquindio.entorno.domain.repository.ReportRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import co.edu.uniquindio.entorno.domain.repository.SettingsRepository
import co.edu.uniquindio.entorno.domain.repository.UserRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

private const val MIN_PASSWORD = 6

/** Reportes visibles en el feed público: sin verificar o verificados. */
private fun Report.isPublic(): Boolean = status == ReportStatus.PENDING || status == ReportStatus.VERIFIED

// ======================================================================= Autenticación

@Singleton
class InMemoryAuthRepository @Inject constructor(
    private val db: MemoryDatabase
) : AuthRepository {

    override val currentUserId: String? get() = db.authUserId.value

    override fun observeAuthState(): Flow<String?> = db.authUserId

    override suspend fun register(user: User, password: String): Result<User> = safeCall {
        val email = user.email.trim()
        if (db.users.value.any { it.email.equals(email, ignoreCase = true) }) {
            throw EmailAlreadyInUseException()
        }
        if (password.length < MIN_PASSWORD) throw WeakPasswordException()
        // El rol siempre es USER y los puntos empiezan en cero.
        val created = user.copy(
            id = db.newId("u"), name = user.name.trim(), email = email,
            city = user.city.trim(), address = user.address.trim(), phoneNumber = user.phoneNumber.trim(),
            role = UserRole.USER, points = 0, badges = emptyList(),
            createdAt = System.currentTimeMillis()
        )
        db.users.update { it + created }
        db.passwords.update { it + (created.id to password) }
        created
    }

    override suspend fun login(email: String, password: String): Result<User> = safeCall {
        val user = db.users.value.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
            ?: throw InvalidCredentialsException()
        if (db.passwords.value[user.id] != password) throw InvalidCredentialsException()
        db.authUserId.value = user.id
        user
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = safeCall {
        val exists = db.users.value.any { it.email.equals(email.trim(), ignoreCase = true) }
        check(exists) { "No existe una cuenta con ese correo" }
        // En la Fase 2 no se envía un correo real: la solicitud se da por enviada.
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = safeCall {
        val userId = db.authUserId.value ?: error("No hay sesión activa")
        if (db.passwords.value[userId] != currentPassword) {
            throw IllegalArgumentException("La contraseña actual es incorrecta")
        }
        if (newPassword.length < MIN_PASSWORD) throw WeakPasswordException()
        db.passwords.update { it + (userId to newPassword) }
    }

    override suspend fun deleteAccount(currentPassword: String): Result<Unit> = safeCall {
        val userId = db.authUserId.value ?: error("No hay sesión activa")
        if (db.passwords.value[userId] != currentPassword) {
            throw IllegalArgumentException("La contraseña es incorrecta")
        }
        db.users.update { list -> list.filterNot { it.id == userId } }
        db.passwords.update { it - userId }
        db.notifications.update { list -> list.filterNot { it.userId == userId } }
        db.authUserId.value = null
    }

    override fun logout() {
        db.authUserId.value = null
    }
}

@Singleton
class InMemorySessionRepository @Inject constructor(
    private val db: MemoryDatabase
) : SessionRepository {

    override val session: Flow<UserSession?> = db.session

    override suspend fun saveSession(userId: String, role: UserRole) {
        db.session.value = UserSession(userId, role)
    }

    override suspend fun clearSession() {
        db.session.value = null
    }
}

// ======================================================================= Usuarios

@Singleton
class InMemoryUserRepository @Inject constructor(
    private val db: MemoryDatabase
) : UserRepository {

    override suspend fun findById(id: String): Result<User> = safeCall {
        db.users.value.firstOrNull { it.id == id } ?: throw NoSuchElementException("Usuario no encontrado")
    }

    override fun observeUser(id: String): Flow<User?> =
        db.users.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun update(user: User): Result<Unit> = safeCall {
        val existing = db.users.value.firstOrNull { it.id == user.id }
            ?: throw NoSuchElementException("Usuario no encontrado")
        val email = user.email.trim()
        if (db.users.value.any { it.id != user.id && it.email.equals(email, ignoreCase = true) }) {
            throw EmailAlreadyInUseException()
        }
        val name = user.name.trim()
        // Rol, puntos e insignias no se editan desde el perfil.
        val updated = existing.copy(
            name = name, email = email, city = user.city.trim(), address = user.address.trim(),
            location = user.location, phoneNumber = user.phoneNumber.trim(),
            profilePictureUrl = user.profilePictureUrl
        )
        db.users.update { list -> list.map { if (it.id == user.id) updated else it } }
        if (name != existing.name) {
            db.reports.update { list -> list.map { if (it.ownerId == user.id) it.copy(ownerName = name) else it } }
            db.comments.update { list -> list.map { if (it.authorId == user.id) it.copy(authorName = name) else it } }
        }
    }

    override suspend fun addPoints(userId: String, points: Int): Result<Unit> = safeCall {
        db.addPoints(userId, points)
    }

    override suspend fun awardBadge(userId: String, badge: Badge): Result<Unit> = safeCall {
        db.awardBadge(userId, badge)
    }

    override fun observeRanking(): Flow<List<User>> =
        db.users.map { list -> list.sortedByDescending { it.points } }
}

// ======================================================================= Reportes

@Singleton
class InMemoryReportRepository @Inject constructor(
    private val db: MemoryDatabase
) : ReportRepository {

    override fun observeFeed(category: ReportCategory?): Flow<List<Report>> =
        db.reports.map { list ->
            list.filter { it.isPublic() && (category == null || it.category == category) }
                .sortedByDescending { it.createdAt }
        }

    override fun observeReport(id: String): Flow<Report?> =
        db.reports.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun findById(id: String): Result<Report?> = safeCall {
        db.reports.value.firstOrNull { it.id == id }
    }

    override fun observeUserReports(userId: String): Flow<List<Report>> =
        db.reports.map { list -> list.filter { it.ownerId == userId }.sortedByDescending { it.createdAt } }

    override fun observeModerationQueue(status: ReportStatus?): Flow<List<Report>> =
        db.reports.map { list ->
            list.filter { status == null || it.status == status }.sortedByDescending { it.createdAt }
        }

    override suspend fun getNearbyReports(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
        category: ReportCategory?
    ): Result<List<Report>> = safeCall {
        val center = Location(latitude, longitude)
        db.reports.value
            .filter { it.isPublic() && (category == null || it.category == category) }
            .filter { Geo.distanceMeters(center, it.location) <= radiusMeters }
            .sortedBy { Geo.distanceMeters(center, it.location) }
    }

    override suspend fun createReport(report: Report): Result<String> = safeCall {
        require(report.title.isNotBlank()) { "El título es obligatorio" }
        val owner = db.users.value.firstOrNull { it.id == report.ownerId }
            ?: throw NoSuchElementException("Usuario no encontrado")
        val now = System.currentTimeMillis()
        val id = db.newId("r")
        // Todo reporte nace PENDING y sin votos.
        val created = report.copy(
            id = id, title = report.title.trim(), description = report.description.trim(),
            status = ReportStatus.PENDING, ownerName = owner.name, importantCount = 0, commentCount = 0,
            rejectionReason = null, date = LocalDate.now(), createdAt = now,
            neighborhood = report.neighborhood.ifBlank { Geo.nearestNeighborhood(report.location) }
        )
        db.reports.update { it + created }
        db.addPoints(owner.id, PointsRule.CREATE_REPORT)
        db.awardBadge(owner.id, Badge.FIRST_REPORT)
        id
    }

    override suspend fun updateReport(report: Report): Result<Unit> = safeCall {
        check(db.reports.value.any { it.id == report.id }) { "El reporte no existe" }
        db.reports.update { list -> list.map { if (it.id == report.id) report else it } }
    }

    override suspend fun deleteReport(id: String): Result<Unit> = safeCall {
        db.reports.update { list -> list.filterNot { it.id == id } }
        db.comments.update { list -> list.filterNot { it.reportId == id } }
        db.votes.update { set -> set.filterNot { it.first == id }.toSet() }
    }

    override suspend fun toggleImportant(reportId: String, userId: String): Result<Boolean> = safeCall {
        val report = db.reports.value.firstOrNull { it.id == reportId }
            ?: throw NoSuchElementException("El reporte no existe")
        val key = reportId to userId
        val alreadyVoted = key in db.votes.value
        val delta = if (alreadyVoted) -1 else 1
        db.votes.update { if (alreadyVoted) it - key else it + key }
        db.reports.update { list ->
            list.map {
                if (it.id == reportId) it.copy(importantCount = (it.importantCount + delta).coerceAtLeast(0)) else it
            }
        }
        // El autor gana un punto por cada voto ajeno.
        if (report.ownerId != userId) db.addPoints(report.ownerId, delta * PointsRule.RECEIVE_IMPORTANT_VOTE)
        if (!alreadyVoted) db.checkTopReportOfMonth()
        !alreadyVoted
    }

    override fun observeHasVoted(reportId: String, userId: String): Flow<Boolean> =
        db.votes.map { (reportId to userId) in it }

    override suspend fun verifyReport(reportId: String, moderatorId: String): Result<Unit> = safeCall {
        requireModerator(moderatorId)
        val report = requirePendingReport(reportId)
        db.reports.update { list ->
            list.map { if (it.id == reportId) it.copy(status = ReportStatus.VERIFIED, rejectionReason = null) else it }
        }
        db.addPoints(report.ownerId, PointsRule.REPORT_VERIFIED)
        db.pushNotification(
            userId = report.ownerId, type = NotificationType.REPORT_VERIFIED,
            title = "Tu reporte fue verificado.",
            message = "«${report.title}» ya es visible para todos.", reportId = reportId
        )
        db.checkVerifiedBadge(report.ownerId)
    }

    override suspend fun rejectReport(reportId: String, moderatorId: String, reason: String): Result<Unit> = safeCall {
        requireModerator(moderatorId)
        require(reason.isNotBlank()) { "El motivo del rechazo es obligatorio" }
        val report = requirePendingReport(reportId)
        db.reports.update { list ->
            list.map {
                if (it.id == reportId) it.copy(status = ReportStatus.REJECTED, rejectionReason = reason.trim()) else it
            }
        }
        db.pushNotification(
            userId = report.ownerId, type = NotificationType.REPORT_REJECTED,
            title = "Reporte no publicado.", message = reason.trim(), reportId = reportId
        )
    }

    override suspend fun markResolved(reportId: String, actorId: String): Result<Unit> = safeCall {
        val report = db.reports.value.firstOrNull { it.id == reportId }
            ?: throw NoSuchElementException("El reporte no existe")
        val actor = db.users.value.firstOrNull { it.id == actorId }
            ?: throw NoSuchElementException("Usuario no encontrado")
        check(actor.id == report.ownerId || actor.role == UserRole.MODERATOR) {
            "Solo el autor o un moderador pueden resolver el reporte"
        }
        check(report.status == ReportStatus.VERIFIED) { "Solo se puede resolver un reporte verificado" }
        db.reports.update { list ->
            list.map { if (it.id == reportId) it.copy(status = ReportStatus.RESOLVED) else it }
        }
        if (actor.id != report.ownerId) {
            db.pushNotification(
                userId = report.ownerId, type = NotificationType.REPORT_RESOLVED,
                title = "Tu reporte fue resuelto.",
                message = "«${report.title}» se marcó como resuelto.", reportId = reportId
            )
        }
    }

    override fun observeAllReports(): Flow<List<Report>> =
        db.reports.map { list -> list.sortedByDescending { it.createdAt } }

    override fun observeVotesGiven(userId: String): Flow<Int> =
        db.votes.map { set -> set.count { it.second == userId } }

    private fun requireModerator(moderatorId: String) {
        val user = db.users.value.firstOrNull { it.id == moderatorId }
        check(user?.role == UserRole.MODERATOR) { "Acción reservada a moderadores" }
    }

    private fun requirePendingReport(reportId: String): Report {
        val report = db.reports.value.firstOrNull { it.id == reportId }
            ?: throw NoSuchElementException("El reporte no existe")
        check(report.status == ReportStatus.PENDING) { "Este reporte ya fue revisado" }
        return report
    }
}

// ======================================================================= Comentarios

@Singleton
class InMemoryCommentRepository @Inject constructor(
    private val db: MemoryDatabase
) : CommentRepository {

    override fun observeComments(reportId: String): Flow<List<Comment>> =
        db.comments.map { list -> list.filter { it.reportId == reportId } }

    override suspend fun addComment(comment: Comment): Result<Unit> = safeCall {
        require(comment.text.isNotBlank()) { "El comentario no puede estar vacío" }
        val author = db.users.value.firstOrNull { it.id == comment.authorId }
            ?: throw NoSuchElementException("Usuario no encontrado")
        val report = db.reports.value.firstOrNull { it.id == comment.reportId }
            ?: throw NoSuchElementException("El reporte no existe")

        // Las respuestas son de un solo nivel: si se responde a una respuesta, se cuelga de la raíz.
        val parent = comment.parentCommentId?.let { id -> db.comments.value.firstOrNull { it.id == id } }
        val rootId = parent?.let { it.parentCommentId ?: it.id }

        val created = comment.copy(
            id = db.newId("c"), text = comment.text.trim(), authorName = author.name,
            authorRole = author.role, parentCommentId = rootId, createdAt = System.currentTimeMillis()
        )
        db.comments.update { it + created }
        db.reports.update { list ->
            list.map { if (it.id == report.id) it.copy(commentCount = it.commentCount + 1) else it }
        }
        db.addPoints(author.id, PointsRule.COMMENT)
        db.awardBadge(author.id, Badge.FIRST_COMMENT)

        if (report.ownerId != author.id) {
            db.pushNotification(
                userId = report.ownerId, type = NotificationType.NEW_COMMENT,
                title = "${author.name} comentó", message = "en tu reporte «${report.title}».",
                reportId = report.id
            )
        }
        if (parent != null && parent.authorId != author.id && parent.authorId != report.ownerId) {
            db.pushNotification(
                userId = parent.authorId, type = NotificationType.NEW_COMMENT,
                title = "${author.name} te respondió", message = "en «${report.title}».",
                reportId = report.id
            )
        }
    }
}

// ======================================================================= Notificaciones

@Singleton
class InMemoryNotificationRepository @Inject constructor(
    private val db: MemoryDatabase
) : NotificationRepository {

    /** Aplica las preferencias de Configuración: tipos silenciados no se muestran. */
    private fun visibleFor(userId: String): Flow<List<AppNotification>> =
        combine(db.notifications, db.settings) { all, settings ->
            all.filter { it.userId == userId }
                .filter { n ->
                    when (n.type) {
                        NotificationType.NEW_COMMENT -> settings.notifyCommentsOnMyReports
                        NotificationType.BADGE_EARNED, NotificationType.LEVEL_UP -> settings.notifyLevelAndBadges
                        else -> true
                    }
                }
                .sortedByDescending { it.createdAt }
        }

    override fun observeNotifications(userId: String): Flow<List<AppNotification>> = visibleFor(userId)

    override fun observeUnreadCount(userId: String): Flow<Int> =
        visibleFor(userId).map { list -> list.count { !it.read } }

    override suspend fun setRead(notificationId: String, read: Boolean): Result<Unit> = safeCall {
        db.notifications.update { list ->
            list.map { if (it.id == notificationId) it.copy(read = read) else it }
        }
    }

    override suspend fun markAllAsRead(userId: String): Result<Unit> = safeCall {
        db.notifications.update { list ->
            list.map { if (it.userId == userId) it.copy(read = true) else it }
        }
    }

    override suspend fun create(notification: AppNotification): Result<Unit> = safeCall {
        val created = notification.copy(id = db.newId("n"), createdAt = System.currentTimeMillis())
        db.notifications.update { it + created }
    }
}

// ======================================================================= Imágenes y ajustes

/** Sin servicio externo en la Fase 2: se conserva la URI local como "URL pública". */
@Singleton
class InMemoryImageRepository @Inject constructor() : ImageRepository {
    override suspend fun uploadImage(localUri: String): Result<String> = Result.success(localUri)
    override suspend fun uploadImages(localUris: List<String>): Result<List<String>> = Result.success(localUris)
}

@Singleton
class InMemorySettingsRepository @Inject constructor(
    private val db: MemoryDatabase
) : SettingsRepository {
    override fun observeSettings(): Flow<AppSettings> = db.settings
    override fun update(transform: (AppSettings) -> AppSettings) {
        db.settings.update(transform)
    }
}
