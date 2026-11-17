package co.edu.uniquindio.entorno.core.util

import co.edu.uniquindio.entorno.R
import co.edu.uniquindio.entorno.domain.model.EmailAlreadyInUseException
import co.edu.uniquindio.entorno.domain.model.InvalidCredentialsException
import co.edu.uniquindio.entorno.domain.model.WeakPasswordException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

fun Throwable.toUserMessage(): UiText = when (this) {
    is InvalidCredentialsException -> UiText(R.string.error_invalid_credentials)
    is EmailAlreadyInUseException -> UiText(R.string.error_email_in_use)
    is WeakPasswordException -> UiText(R.string.error_weak_password)
    is FirebaseNetworkException -> UiText(R.string.error_no_connection)
    is FirebaseAuthInvalidUserException -> UiText(R.string.error_no_account)
    // Mensajes de validación que llegan desde la capa de datos
    is IllegalArgumentException, is IllegalStateException ->
        message?.let(UiText::Dynamic) ?: UiText(R.string.error_generic)
    else -> UiText(R.string.error_unexpected)
}
