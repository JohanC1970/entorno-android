package co.edu.uniquindio.entorno.core.util

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Texto que el ViewModel entrega a la pantalla sin resolverlo todavía.
 *
 * Los ViewModels no tienen acceso al idioma de la Activity, así que en vez de un `String`
 * guardan el id del recurso y sus argumentos; la pantalla lo resuelve con [asString].
 * Los argumentos pueden ser a su vez [UiText] (por ejemplo, el nombre de un nivel).
 */
sealed interface UiText {

    /** Texto que viene de los datos (nombre de un usuario, mensaje del servidor...). */
    data class Dynamic(val value: String) : UiText

    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText

    data class Plural(@PluralsRes val id: Int, val count: Int, val args: List<Any> = emptyList()) : UiText

    fun asString(context: Context): String = when (this) {
        is Dynamic -> value
        is Res -> context.getString(id, *args.resolve(context))
        is Plural -> context.resources.getQuantityString(id, count, *args.resolve(context))
    }

    @Composable
    fun asString(): String = asString(LocalContext.current)

    companion object {
        operator fun invoke(@StringRes id: Int, vararg args: Any): UiText = Res(id, args.toList())

        fun plural(@PluralsRes id: Int, count: Int, vararg args: Any): UiText = Plural(id, count, args.toList())
    }
}

private fun List<Any>.resolve(context: Context): Array<Any> =
    map { arg -> if (arg is UiText) arg.asString(context) else arg }.toTypedArray()
