package co.edu.uniquindio.entorno.domain.repository

import co.edu.uniquindio.entorno.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val currentUserId: String?
    fun observeAuthState(): Flow<String?>
    /** El rol siempre queda como USER; los moderadores están precargados. */
    suspend fun register(user: User, password: String): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun deleteAccount(currentPassword: String): Result<Unit>
    fun logout()

}