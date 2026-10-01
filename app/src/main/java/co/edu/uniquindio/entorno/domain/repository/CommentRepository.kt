package co.edu.uniquindio.entorno.domain.repository

import co.edu.uniquindio.entorno.domain.model.Badge
import co.edu.uniquindio.entorno.domain.model.User
import kotlinx.coroutines.flow.Flow

interface CommentRepository {

    suspend fun findById(id: String): Result<User>
    fun observeUser(id: String): Flow<User?>
    suspend fun update(user: User): Result<Unit>
    suspend fun addPoints(userId: String, points: Int): Result<Unit>
    suspend fun awardBadge(userId: String, badge: Badge): Result<Unit>

}