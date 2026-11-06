package co.edu.uniquindio.entorno.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import co.edu.uniquindio.entorno.domain.model.AppSettings
import co.edu.uniquindio.entorno.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val COMMENTS = booleanPreferencesKey("notify_comments")
        val LEVEL_BADGES = booleanPreferencesKey("notify_level_badges")
        val RADIUS = intPreferencesKey("alert_radius_km")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val defaults = AppSettings()

    private fun Preferences.toSettings() = AppSettings(
        notifyCommentsOnMyReports = this[Keys.COMMENTS] ?: defaults.notifyCommentsOnMyReports,
        notifyLevelAndBadges = this[Keys.LEVEL_BADGES] ?: defaults.notifyLevelAndBadges,
        alertRadiusKm = this[Keys.RADIUS] ?: defaults.alertRadiusKm
    )

    override fun observeSettings(): Flow<AppSettings> =
        context.settingsDataStore.data.map { it.toSettings() }

    override fun update(transform: (AppSettings) -> AppSettings) {
        scope.launch {
            // La lectura y la escritura ocurren dentro del mismo edit, así que no hay carreras.
            context.settingsDataStore.edit { prefs ->
                val new = transform(prefs.toSettings())
                prefs[Keys.COMMENTS] = new.notifyCommentsOnMyReports
                prefs[Keys.LEVEL_BADGES] = new.notifyLevelAndBadges
                prefs[Keys.RADIUS] = new.alertRadiusKm
            }
        }
    }
}
