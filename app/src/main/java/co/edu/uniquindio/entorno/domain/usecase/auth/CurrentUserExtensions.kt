package co.edu.uniquindio.entorno.domain.usecase.auth

import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import co.edu.uniquindio.entorno.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

/** Usuario con sesión activa, reactivo a cambios del perfil; sin sesión emite null. */
fun SessionRepository.observeCurrentUser(users: UserRepository): Flow<User?> =
    withUserId<User?>(null) { userId -> users.observeUser(userId) }
