package co.edu.uniquindio.entorno.features.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.core.util.toUserMessage
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
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
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportRepository: ReportRepository,
    sessionRepository: SessionRepository,
    userRepository: UserRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val category = MutableStateFlow<ReportCategory?>(null)
    private val selectedId = MutableStateFlow(savedStateHandle.get<String>("focus"))

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        val feed = category.flatMapLatest { reportRepository.observeFeed(it) }
        viewModelScope.launch {
            combine(
                feed, category, selectedId,
                sessionRepository.observeCurrentUser(userRepository),
                settingsRepository.observeSettings()
            ) { reports, selectedCategory, selected, user, settings ->
                MapUiState(
                    reports = reports,
                    selectedCategory = selectedCategory,
                    selectedReport = reports.firstOrNull { it.id == selected },
                    userLocation = user?.location ?: Geo.ARMENIA_CENTER,
                    radiusKm = settings.alertRadiusKm
                )
            }
                .catch { error -> _uiState.update { it.copy(errorMessage = error.toUserMessage()) } }
                .collect { state -> _uiState.update { state } }
        }
    }

    fun onCategorySelected(selected: ReportCategory?) {
        category.value = selected
    }

    fun onReportSelected(id: String?) {
        selectedId.value = id
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
