package co.edu.uniquindio.entorno.domain.model

data class Comment(
    val id: String = "",
    val reportId: String,
    val authorId: String,
    val authorName: String,
    val text: String,
    val createdAt: Long = 0L,
    /** null = comentario raíz; con valor = respuesta al comentario con ese id (un solo nivel). */
    val parentCommentId: String? = null
)
