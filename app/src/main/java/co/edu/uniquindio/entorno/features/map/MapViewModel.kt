package co.edu.uniquindio.entorno.features.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import co.edu.uniquindio.entorno.core.filter.LocationScope
import co.edu.uniquindio.entorno.core.location.DeviceLocationClient
import co.edu.uniquindio.entorno.core.navigation.MapRoute
import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.core.util.toUserMessage
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.model.AppSettings
import co.edu.uniquindio.entorno.domain.repository.ReportRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import co.edu.uniquindio.entorno.domain.repository.SettingsRepository
import co.edu.uniquindio.entorno.domain.repository.UserRepository
import co.edu.uniquindio.entorno.domain.usecase.auth.observeCurrentUser
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapUiState(
    val reports: List<Report> = emptyList(),
    val selectedCategory: ReportCategory? = null,
    val selectedReport: Report? = null,
    val userLocation: Location = Geo.ARMENIA_CENTER,
    val radiusKm: Int = 2,
    val scope: LocationScope = LocationScope.ALL,
    val showHeat: Boolean = false,
    val allDay: Boolean = true,
    /** Hora de inicio (0 a 23) de la franja de 3 horas cuando no es "todo el día". */
    val startHour: Int = 18,
    val errorMessage: String? = null
) {
    /** Reportes que alimentan el mapa de calor: los del mapa, dentro de la franja elegida. */
    val heatReports: List<Report>
        get() = if (allDay) reports else reports.filter { inWindow(it.hourOfDay(), startHour) }

    val windowLabel: String
        get() = if (allDay) "Todo el día" else "${formatHour(startHour)} – ${formatHour((startHour + WINDOW_HOURS) % 24)}"
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportRepository: ReportRepository,
    sessionRepository: SessionRepository,
    userRepository: UserRepository,
    settingsRepository: SettingsRepository,
    private val deviceLocationClient: DeviceLocationClient
) : ViewModel() {

    private val category = MutableStateFlow<ReportCategory?>(null)
    private val selectedId = MutableStateFlow(savedStateHandle.toRoute<MapRoute>().focus)
    private val scopeState = MutableStateFlow(LocationScope.ALL)
    private val showHeatState = MutableStateFlow(false)
    private val allDayState = MutableStateFlow(true)
    private val startHourState = MutableStateFlow(18)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        val feed = category.flatMapLatest { reportRepository.observeFeed(it) }
        viewModelScope.launch {
            combine(
                feed, category, selectedId,
                sessionRepository.observeCurrentUser(userRepository),
                settingsRepository.observeSettings(),
                scopeState,
                showHeatState,
                allDayState,
                startHourState
            ) { array ->
                @Suppress("UNCHECKED_CAST")
                val reports = array[0] as List<Report>
                @Suppress("UNCHECKED_CAST")
                val selectedCategory = array[1] as ReportCategory?
                val selected = array[2] as String?
                val user = array[3] as User?
                val settings = array[4] as AppSettings
                val currentScope = array[5] as LocationScope
                val heat = array[6] as Boolean
                val allDay = array[7] as Boolean
                val startHour = array[8] as Int

                val userLoc = user?.location ?: Geo.ARMENIA_CENTER
                val filteredReports = if (currentScope == LocationScope.NEARBY) {
                    val radiusMeters = settings.alertRadiusKm * 1000.0
                    reports.filter { Geo.distanceMeters(userLoc, it.location) <= radiusMeters }
                } else {
                    reports
                }
                MapUiState(
                    reports = filteredReports,
                    selectedCategory = selectedCategory,
                    selectedReport = reports.firstOrNull { it.id == selected },
                    userLocation = userLoc,
                    radiusKm = settings.alertRadiusKm,
                    scope = currentScope,
                    showHeat = heat,
                    allDay = allDay,
                    startHour = startHour
                )
            }
                .catch { error -> _uiState.update { it.copy(errorMessage = error.toUserMessage()) } }
                .collect { state -> _uiState.update { state } }
        }
        refreshDeviceLocation()
    }

    fun onCategorySelected(selected: ReportCategory?) {
        category.value = selected
    }

    fun onReportSelected(id: String?) {
        selectedId.value = id
    }

    fun onScopeSelected(scope: LocationScope) {
        scopeState.value = scope
    }

    fun onHeatToggle() {
        showHeatState.value = !showHeatState.value
    }

    fun onAllDayChange(value: Boolean) {
        allDayState.value = value
    }

    fun onStartHourChange(hour: Int) {
        startHourState.value = hour.coerceIn(0, 23)
    }

    fun refreshDeviceLocation() {
        viewModelScope.launch {
            deviceLocationClient.currentLocation()?.let { loc ->
                _uiState.update { it.copy(userLocation = loc) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
