package co.edu.uniquindio.entorno.core.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import java.util.Locale

/** Idiomas de la interfaz. [SYSTEM] sigue el idioma del teléfono (español si no está soportado). */
enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    SPANISH("es"),
    ENGLISH("en");

    companion object {
        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: SYSTEM
    }
}

/**
 * Guarda el idioma elegido en Configuración y lo aplica al contexto de la Activity.
 *
 * Se usa SharedPreferences (y no el SettingsRepository en memoria) porque el idioma se necesita
 * en `attachBaseContext`, antes de que Hilt y los ViewModels existan, y debe sobrevivir al cierre
 * de la app.
 */
object LocaleHelper {

    private const val PREFS = "entorno_locale"
    private const val KEY_LANGUAGE = "language"

    fun current(context: Context): AppLanguage =
        AppLanguage.fromTag(prefs(context).getString(KEY_LANGUAGE, null))

    fun save(context: Context, language: AppLanguage) {
        prefs(context).edit().putString(KEY_LANGUAGE, language.tag).apply()
    }

    /** Devuelve un contexto cuyos recursos usan el idioma guardado. */
    fun wrap(base: Context): Context {
        val language = current(base)
        val locale = language.tag?.let(Locale::forLanguageTag)
            ?: Resources.getSystem().configuration.locales[0]
        Locale.setDefault(locale)
        if (language == AppLanguage.SYSTEM) return base
        val config = Configuration(base.resources.configuration).apply { setLocales(LocaleList(locale)) }
        return base.createConfigurationContext(config)
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
