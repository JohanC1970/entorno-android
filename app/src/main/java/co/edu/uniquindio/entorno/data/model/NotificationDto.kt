package co.edu.uniquindio.entorno.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class NotificationDto(
    @DocumentId val id: String = "",
    val userId: String = "",
    val type: String = "NEW_COMMENT",
    val title: String = "",
    val message: String = "",
    val reportId: String? = null,
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Date? = null
)
