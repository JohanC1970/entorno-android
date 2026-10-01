package co.edu.uniquindio.entorno.domain.repository

import co.edu.uniquindio.entorno.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(userId: String): Flow<List<AppNotification>>
    fun observeUnreadCount(userId: String): Flow<Int>
    suspend fun setRead(notificationId: String, read: Boolean): Result<Unit>
    suspend fun markAllAsRead(userId: String): Result<Unit>
    suspend fun create(notification: AppNotification): Result<Unit>
}