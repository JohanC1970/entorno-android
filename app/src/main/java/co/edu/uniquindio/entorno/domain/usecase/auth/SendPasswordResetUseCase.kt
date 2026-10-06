package co.edu.uniquindio.entorno.domain.usecase.auth

import co.edu.uniquindio.entorno.domain.repository.AuthRepository
import jakarta.inject.Inject

class SendPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
){

    /** Firebase envía al correo un enlace para restablecer la contraseña  */
    suspend operator fun invoke(email: String): Result<Unit> = authRepository.sendPasswordReset(email)

}