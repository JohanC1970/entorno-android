package co.edu.uniquindio.entorno.domain.usecase.auth

import co.edu.uniquindio.entorno.core.util.safeCall
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.repository.AuthRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import jakarta.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
){
    /** Autentica con Firebase y, si es correcto, guarda la sesión (userId + rol) */
    suspend operator fun invoke(email: String, password: String): Result<User> = safeCall {
        val user = authRepository.login(email,password).getOrThrow()
        sessionRepository.saveSession(user.id,user.role)
        user
    }


}