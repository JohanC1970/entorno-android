package co.edu.uniquindio.entorno.domain.repository

import co.edu.uniquindio.entorno.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    fun update(transform: (AppSettings) -> AppSettings)
}
