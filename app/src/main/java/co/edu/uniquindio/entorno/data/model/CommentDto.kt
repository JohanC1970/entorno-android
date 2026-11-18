package co.edu.uniquindio.entorno.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Keep // Firestore llena esta clase por reflexión, buscando sus atributos por nombre
data class CommentDto(
    @DocumentId val id: String = "",
    val reportId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "USER",
    val text: String = "",
    val parentCommentId: String? = null,
    @ServerTimestamp val createdAt: Date? = null
)
