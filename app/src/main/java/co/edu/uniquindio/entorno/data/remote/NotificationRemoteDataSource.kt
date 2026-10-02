package co.edu.uniquindio.entorno.data.remote

import co.edu.uniquindio.entorno.core.util.asFlow
import co.edu.uniquindio.entorno.data.model.NotificationDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@Singleton
class NotificationRemoteDataSource @Inject constructor(private val db: FirebaseFirestore){

    private val notifications = db.collection("notifications")

    fun observe(userId: String): Flow<List<NotificationDto>> =
        notifications.whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .asFlow<NotificationDto>()

    fun observeUnreadCount(userId: String): Flow<Int> =
        notifications.whereEqualTo("userId", userId).whereEqualTo("read", false)
            .asFlow<NotificationDto>().map { it.size }

    suspend fun setRead(id: String, read: Boolean) {
        notifications.document(id).update("read", read).await()
    }

    suspend fun markAllAsRead(userId: String){
        val unread = notifications.whereEqualTo("userId",userId).whereEqualTo("read",false).get().await()
        val batch = db.batch()
        unread.documents.forEach { batch.update(it.reference, "read", true) }
        batch.commit().await()
    }

    suspend fun create(dto: NotificationDto){
        notifications.document().set(dto).await()
    }

}