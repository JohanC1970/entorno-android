package co.edu.uniquindio.entorno.domain.usecase.auth

import co.edu.uniquindio.entorno.domain.model.UserSession
import co.edu.uniquindio.entorno.domain.repository.AuthRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
){

    /** La sesión guardada solo es válida si Firebase también tiene al mismo usuario autenticado
     * ejemplo: puede que la sesión guardada pertenezca a una cuenta eliminada o con la sesión revocada  */
    operator fun invoke(): Flow<UserSession?> =
        sessionRepository.session.combine(authRepository.observeAuthState()) {
            session, uid -> session?.takeIf { uid != null && it.userId == uid }
        }

}