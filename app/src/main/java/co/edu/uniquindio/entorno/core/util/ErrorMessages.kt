package co.edu.uniquindio.entorno.core.util

import co.edu.uniquindio.entorno.domain.model.EmailAlreadyInUseException
import co.edu.uniquindio.entorno.domain.model.InvalidCredentialsException
import co.edu.uniquindio.entorno.domain.model.WeakPasswordException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

/** Mensajes para el usuario. Más adelante se pueden mover a strings.xml. */
fun Throwable.toUserMessage(): String = when (this) {
    is InvalidCredentialsException,
    is EmailAlreadyInUseException,
    is WeakPasswordException -> message ?: "Ocurrió un error"
    is FirebaseNetworkException -> "Sin conexión a internet. Inténtalo de nuevo"
    is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo"
    is IllegalArgumentException, is IllegalStateException -> message ?: "Ocurrió un error"
    else -> "Ocurrió un error inesperado. Inténtalo de nuevo"
}