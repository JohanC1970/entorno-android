package co.edu.uniquindio.entorno.core.util

import android.util.Patterns

/** Cada función devuelve el mensaje de error, o null si el valor es válido. */
object Validators {
    fun email(value: String): String? = when {
        value.isBlank() -> "El correo es obligatorio"
        !Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches() -> "Correo no válido"
        else -> null
    }

    fun password(value: String): String? = when {
        value.isBlank() -> "La contraseña es obligatoria"
        value.length < 6 -> "Mínimo 6 caracteres"
        else -> null
    }

    fun confirmPassword(password: String, confirm: String): String? =
        if (password != confirm) "Las contraseñas no coinciden" else null

    fun required(value: String, field: String): String? =
        if (value.isBlank()) "$field es obligatorio" else null
}