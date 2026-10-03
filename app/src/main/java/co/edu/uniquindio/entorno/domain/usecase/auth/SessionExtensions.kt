package co.edu.uniquindio.entorno.domain.usecase.auth

import co.edu.uniquindio.entorno.domain.model.UserRole
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/** Id del usuario con sesión activa; falla si no hay sesión */
suspend fun SessionRepository.requireUserId(): String =
    session.first()?.userId ?: error("No hay una sesión activa")

/** Id del moderador con sesión activa; falla si el rol no es MODERATOR */
suspend fun SessionRepository.requireModeratorId(): String {
    val current = session.first() ?: error("No hay una sesión activa")
    check(current.role == UserRole.MODERATOR) { "Acción reservada a moderadores" }
    return current.userId
}

/** Observa un flujo que depende del usuario actual; sin sesión emite [empty]. */
fun <T> SessionRepository.withUserId(empty: T, block: (String) -> Flow<T>): Flow<T> =
    session.flatMapLatest { s -> if (s == null) flowOf(empty) else block(s.userId) }

