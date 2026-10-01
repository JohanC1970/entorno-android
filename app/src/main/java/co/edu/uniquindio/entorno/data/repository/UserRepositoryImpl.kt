package co.edu.uniquindio.entorno.data.repository

import co.edu.uniquindio.entorno.core.util.safeCall
import co.edu.uniquindio.entorno.data.model.toDomain
import co.edu.uniquindio.entorno.data.remote.UserRemoteDataSource
import co.edu.uniquindio.entorno.domain.model.Badge
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val source: UserRemoteDataSource
) : UserRepository {

    override suspend fun findById(id: String): Result<User> = safeCall {
        source.get(id)?.toDomain() ?: error("Usuario no encontrado")
    }

    override fun observeUser(id: String): Flow<User?> = source.observe(id).map { it?.toDomain() }

    /** Solo actualiza campos editables: nunca rol, correo ni puntos. */
    override suspend fun update(user: User): Result<Unit> = safeCall {
        source.update(
            user.id,
            mapOf(
                "name" to user.name.trim(),
                "city" to user.city.trim(),
                "address" to user.address.trim(),
                "latitude" to user.location?.latitude,
                "longitude" to user.location?.longitude,
                "phoneNumber" to user.phoneNumber.trim(),
                "profilePictureUrl" to user.profilePictureUrl
            )
        )
    }

    override suspend fun addPoints(userId: String, points: Int): Result<Unit> =
        safeCall { source.addPoints(userId, points) }

    override suspend fun awardBadge(userId: String, badge: Badge): Result<Unit> =
        safeCall { source.addBadge(userId, badge.name) }
}
