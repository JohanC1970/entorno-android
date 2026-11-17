package co.edu.uniquindio.entorno.core.util

import android.util.Patterns
import androidx.annotation.StringRes
import co.edu.uniquindio.entorno.R

/** Cada función devuelve el mensaje de error, o null si el valor es válido. */
object Validators {
    fun email(value: String): UiText? = when {
        value.isBlank() -> UiText(R.string.error_email_required)
        !Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches() -> UiText(R.string.error_email_invalid)
        else -> null
    }

    fun password(value: String): UiText? = when {
        value.isBlank() -> UiText(R.string.error_password_required)
        value.length < 6 -> UiText(R.string.error_password_min)
        else -> null
    }

    fun confirmPassword(password: String, confirm: String): UiText? =
        if (password != confirm) UiText(R.string.error_passwords_mismatch) else null

    /** [message] es el texto completo del error, p. ej. `R.string.error_name_required`. */
    fun required(value: String, @StringRes message: Int): UiText? =
        if (value.isBlank()) UiText(message) else null
}
