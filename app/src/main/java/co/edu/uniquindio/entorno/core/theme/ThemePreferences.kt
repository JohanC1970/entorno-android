package co.edu.uniquindio.entorno.core.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import co.edu.uniquindio.entorno.core.util.toEnumOrDefault
import co.edu.uniquindio.entorno.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme")

/** Modo de tema elegido por el usuario; sobrevive al cierre de la app. */
@Singleton
class ThemePreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val key = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> = context.themeDataStore.data
        .map { prefs -> prefs[key].toEnumOrDefault(DEFAULT_THEME_MODE) }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { it[key] = mode.name }
    }

    companion object {
        /** Cambien a ThemeMode.LIGHT si quieren que la app arranque siempre en claro. */
        val DEFAULT_THEME_MODE = ThemeMode.SYSTEM
    }
}