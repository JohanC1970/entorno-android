package co.edu.uniquindio.entorno.domain.usecase.auth

import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.repository.AuthRepository
import jakarta.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
){

    /**
     * Crea la cuenta. Firebase deja la sesión abierta al registrar, pero se debe cerrar la sesión
     * para que el usuario inicie sesión.
     */
    suspend operator fun invoke(user: User, password: String) : Result<User> =
        authRepository.register(user,password).also { result ->
            if (result.isSuccess) authRepository.logout()
        }

}