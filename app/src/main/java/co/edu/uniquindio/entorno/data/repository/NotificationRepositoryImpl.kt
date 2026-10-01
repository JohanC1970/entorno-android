package co.edu.uniquindio.entorno.data.repository

import co.edu.uniquindio.entorno.core.util.safeCall
import co.edu.uniquindio.entorno.data.model.toDomain
import co.edu.uniquindio.entorno.data.model.toDto
import co.edu.uniquindio.entorno.data.remote.NotificationRemoteDataSource
import co.edu.uniquindio.entorno.domain.model.AppNotification
import co.edu.uniquindio.entorno.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val source: NotificationRemoteDataSource
) : NotificationRepository {

    override fun observeNotifications(userId: String): Flow<List<AppNotification>> =
        source.observe(userId).map { list -> list.map { it.toDomain() } }

    override fun observeUnreadCount(userId: String): Flow<Int> = source.observeUnreadCount(userId)

    override suspend fun setRead(notificationId: String, read: Boolean): Result<Unit> =
        safeCall { source.setRead(notificationId, read) }

    override suspend fun markAllAsRead(userId: String): Result<Unit> =
        safeCall { source.markAllAsRead(userId) }

    override suspend fun create(notification: AppNotification): Result<Unit> =
        safeCall { source.create(notification.toDto()) }
}