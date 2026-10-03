package co.edu.uniquindio.entorno.domain.usecase.auth

import co.edu.uniquindio.entorno.core.util.safeCall
import co.edu.uniquindio.entorno.domain.repository.AuthRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import jakarta.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
){

    suspend operator fun invoke(): Result<Unit> = safeCall {
        sessionRepository.clearSession()
        authRepository.logout()
    }

}