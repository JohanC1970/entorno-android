package co.edu.uniquindio.entorno.data.repository

import co.edu.uniquindio.entorno.core.util.safeCall
import co.edu.uniquindio.entorno.core.util.toEnumOrDefault
import co.edu.uniquindio.entorno.data.model.toDomain
import co.edu.uniquindio.entorno.data.model.toDto
import co.edu.uniquindio.entorno.data.remote.ReportRemoteDataSource
import co.edu.uniquindio.entorno.data.remote.UserRemoteDataSource
import co.edu.uniquindio.entorno.domain.model.Comment
import co.edu.uniquindio.entorno.domain.model.UserRole
import co.edu.uniquindio.entorno.domain.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject
import jakarta.inject.Singleton


@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val source: ReportRemoteDataSource,
    private val users: UserRemoteDataSource
) : CommentRepository {

    override fun observeComments(reportId: String): Flow<List<Comment>> =
        source.observeComments(reportId).map { list -> list.map { it.toDomain() } }

    override suspend fun addComment(comment: Comment): Result<Unit> = safeCall {
        require(comment.text.isNotBlank()) { "El comentario no puede estar vacío" }
        val author = users.get(comment.authorId) ?: error("Usuario no encontrado")

        // Respuestas de un solo nivel: si se responde a una respuesta, se cuelga de la raíz.
        val parent = comment.parentCommentId?.let { source.findComment(comment.reportId, it) }
        val rootId = parent?.let { it.parentCommentId ?: it.id }

        source.addComment(
            comment.reportId,
            comment.copy(
                authorName = author.name,
                authorRole = author.role.toEnumOrDefault(UserRole.USER),
                parentCommentId = rootId
            ).toDto()
        )
    }
}