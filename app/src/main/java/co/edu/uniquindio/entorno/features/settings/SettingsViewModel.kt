package co.edu.uniquindio.entorno.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.domain.model.AppSettings
import co.edu.uniquindio.entorno.domain.model.ThemeMode
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import co.edu.uniquindio.entorno.domain.repository.SettingsRepository
import co.edu.uniquindio.entorno.domain.repository.UserRepository
import co.edu.uniquindio.entorno.domain.usecase.auth.LogoutUseCase
import co.edu.uniquindio.entorno.domain.usecase.auth.observeCurrentUser
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import co.edu.uniquindio.entorno.core.theme.ThemePreferences

data class SettingsUiState(
    val user: User? = null,
    val settings: AppSettings = AppSettings(),
    val themeMode: ThemeMode = ThemePreferences.DEFAULT_THEME_MODE
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val themePreferences: ThemePreferences,
    private val logoutUseCase: LogoutUseCase,
    sessionRepository: SessionRepository,
    userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                sessionRepository.observeCurrentUser(userRepository),
                settingsRepository.observeSettings(),
                themePreferences.themeMode
            ) { user, settings, theme -> SettingsUiState(user, settings, theme) }
                .collect { state -> _uiState.update { state } }
        }
    }

    fun onNotifyNearbyChange(value: Boolean) = settingsRepository.update { it.copy(notifyNearbyReports = value) }

    fun onNotifyCommentsChange(value: Boolean) =
        settingsRepository.update { it.copy(notifyCommentsOnMyReports = value) }

    fun onNotifyLevelChange(value: Boolean) = settingsRepository.update { it.copy(notifyLevelAndBadges = value) }

    fun onRadiusChange(km: Int) = settingsRepository.update { it.copy(alertRadiusKm = km.coerceIn(1, 10)) }

    fun onThemeChange(mode: ThemeMode) { viewModelScope.launch { themePreferences.setThemeMode(mode) } }

    /** Al limpiar la sesión la navegación vuelve sola a la bienvenida. */
    fun logout() {
        viewModelScope.launch { logoutUseCase() }
    }
}
