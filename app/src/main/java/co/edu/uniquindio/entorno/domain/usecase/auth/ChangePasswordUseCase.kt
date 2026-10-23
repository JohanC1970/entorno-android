package co.edu.uniquindio.entorno.domain.usecase.auth

import co.edu.uniquindio.entorno.domain.repository.AuthRepository
import jakarta.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /** Verifica la contraseña actual y la reemplaza por la nueva. */
    suspend operator fun invoke(currentPassword: String, newPassword: String): Result<Unit> =
        authRepository.changePassword(currentPassword, newPassword)
}
