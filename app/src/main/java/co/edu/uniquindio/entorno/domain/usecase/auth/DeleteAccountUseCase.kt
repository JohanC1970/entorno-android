package co.edu.uniquindio.entorno.domain.usecase.auth

import co.edu.uniquindio.entorno.core.util.safeCall
import co.edu.uniquindio.entorno.domain.repository.AuthRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import jakarta.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
){

    /** Firebase pide la contraseña actual para eliminar la cuenta*/
    suspend operator fun invoke(currentPassword: String): Result<Unit> = safeCall {
        authRepository.deleteAccount(currentPassword).getOrThrow()
        sessionRepository.clearSession()
    }

}