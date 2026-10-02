package co.edu.uniquindio.entorno.domain.repository

import co.edu.uniquindio.entorno.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun observeComments(reportId: String): Flow<List<Comment>>
    /** Crea el comentario y notifica al autor del reporte (si es otra persona). */
    suspend fun addComment(comment: Comment): Result<Unit>
}