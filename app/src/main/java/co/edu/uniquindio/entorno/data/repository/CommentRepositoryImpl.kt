package co.edu.uniquindio.entorno.data.repository

import co.edu.uniquindio.entorno.core.util.safeCall
import co.edu.uniquindio.entorno.data.model.toDomain
import co.edu.uniquindio.entorno.data.model.toDto
import co.edu.uniquindio.entorno.data.remote.ReportRemoteDataSource
import co.edu.uniquindio.entorno.domain.model.Comment
import co.edu.uniquindio.entorno.domain.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject
import jakarta.inject.Singleton


@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val source: ReportRemoteDataSource
) : CommentRepository {

    override fun observeComments(reportId: String): Flow<List<Comment>> =
        source.observeComments(reportId).map { list -> list.map { it.toDomain() } }

    override suspend fun addComment(comment: Comment): Result<Unit> = safeCall {
        require(comment.text.isNotBlank()) { "El comentario no puede estar vacío" }
        source.addComment(comment.reportId, comment.toDto())
    }
}