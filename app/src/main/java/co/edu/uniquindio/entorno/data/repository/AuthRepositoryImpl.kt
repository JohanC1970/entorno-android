package co.edu.uniquindio.entorno.data.repository

import co.edu.uniquindio.entorno.core.util.safeCall
import co.edu.uniquindio.entorno.data.model.UserDto
import co.edu.uniquindio.entorno.data.model.toDomain
import co.edu.uniquindio.entorno.data.remote.AuthRemoteDataSource
import co.edu.uniquindio.entorno.data.remote.UserRemoteDataSource
import co.edu.uniquindio.entorno.domain.model.EmailAlreadyInUseException
import co.edu.uniquindio.entorno.domain.model.InvalidCredentialsException
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.model.UserRole
import co.edu.uniquindio.entorno.domain.model.WeakPasswordException
import co.edu.uniquindio.entorno.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.Flow
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authSource: AuthRemoteDataSource,
    private val userSource: UserRemoteDataSource
): AuthRepository{

    override val currentUserId: String? get() = authSource.currentUserId

    override fun observeAuthState(): Flow<String?> = authSource.observeAuthState()

    override suspend fun register(
        user: User,
        password: String
    ): Result<User> = safeCall {
        val uid = try {
            authSource.register(user.email.trim(), password)
        } catch (e: FirebaseAuthUserCollisionException) {
            throw EmailAlreadyInUseException()
        } catch (e: FirebaseAuthWeakPasswordException) {
            throw WeakPasswordException()
        }
        try {
            // El rol siempre es USER y la contraseña nunca se guarda en Firestore.
            userSource.create(
                uid,
                UserDto(
                    name = user.name.trim(),
                    email = user.email.trim(),
                    city = user.city.trim(),
                    address = user.address.trim(),
                    latitude = user.location?.latitude,
                    longitude = user.location?.longitude,
                    phoneNumber = user.phoneNumber.trim(),
                    profilePictureUrl = user.profilePictureUrl,
                    role = UserRole.USER.name
                )
            )
        } catch (e: Exception) {
            authSource.deleteCurrentUser() // evita cuentas de Auth sin perfil
            throw e
        }
        userSource.get(uid)?.toDomain() ?: error("No se pudo crear el perfil")

    }


    override suspend fun login(email: String, password: String): Result<User> = safeCall {
        val uid = try {
            authSource.login(email.trim(), password)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw InvalidCredentialsException() // contraseña incorrecta
        } catch (e: FirebaseAuthInvalidUserException) {
            throw InvalidCredentialsException() // cuenta inexistente o deshabilitada
        }
        userSource.get(uid)?.toDomain() ?: error("La cuenta no tiene un perfil asociado")
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = safeCall {
        authSource.sendPasswordReset(email.trim())
    }

    override suspend fun deleteAccount(currentPassword: String): Result<Unit> = safeCall {
        val uid = authSource.currentUserId ?: error("No hay sesión activa")
        authSource.reauthenticate(currentPassword)
        userSource.delete(uid)
        authSource.deleteCurrentUser()
    }

    override fun logout() = authSource.logout()

}