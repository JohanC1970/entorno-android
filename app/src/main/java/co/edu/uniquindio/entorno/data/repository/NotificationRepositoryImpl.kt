package co.edu.uniquindio.entorno.data.repository

import co.edu.uniquindio.entorno.core.util.safeCall
import co.edu.uniquindio.entorno.data.model.toDomain
import co.edu.uniquindio.entorno.data.model.toDto
import co.edu.uniquindio.entorno.data.remote.NotificationRemoteDataSource
import co.edu.uniquindio.entorno.domain.model.AppNotification
import co.edu.uniquindio.entorno.domain.model.NotificationType
import co.edu.uniquindio.entorno.domain.repository.NotificationRepository
import co.edu.uniquindio.entorno.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Repositorio de notificaciones con persistencia en Firestore (NotificationRemoteDataSource)
 * y filtrado dinámico según las preferencias de configuración del usuario (SettingsRepository).
 */
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val source: NotificationRemoteDataSource,
    private val settings: SettingsRepository
) : NotificationRepository {

    private fun visibleFor(userId: String): Flow<List<AppNotification>> =
        combine(
            source.observe(userId).map { list -> list.map { it.toDomain() } },
            settings.observeSettings()
        ) { all, prefs ->
            all.filter { n ->
                when (n.type) {
                    NotificationType.NEW_COMMENT -> prefs.notifyCommentsOnMyReports
                    NotificationType.BADGE_EARNED, NotificationType.LEVEL_UP -> prefs.notifyLevelAndBadges
                    else -> true
                }
            }
        }

    override fun observeNotifications(userId: String) = visibleFor(userId)

    override fun observeUnreadCount(userId: String): Flow<Int> =
        visibleFor(userId).map { list -> list.count { !it.read } }

    override suspend fun setRead(notificationId: String, read: Boolean): Result<Unit> =
        safeCall { source.setRead(notificationId, read) }

    override suspend fun markAllAsRead(userId: String): Result<Unit> =
        safeCall { source.markAllAsRead(userId) }

    override suspend fun create(notification: AppNotification): Result<Unit> =
        safeCall { source.create(notification.toDto()) }
}
