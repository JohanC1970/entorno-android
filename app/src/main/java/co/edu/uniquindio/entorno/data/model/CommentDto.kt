package co.edu.uniquindio.entorno.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CommentDto(
    @DocumentId val id: String = "",
    val reportId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
