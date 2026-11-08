package co.edu.uniquindio.entorno.features.moderator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ModeratorPanelUiState(
    val reportesPendientes: List<Report> = emptyList(),
    val totalPendientes: Int = 0
)

@HiltViewModel
class ModeratorPanelViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModeratorPanelUiState())
    val uiState: StateFlow<ModeratorPanelUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            reportRepository.observeModerationQueue(ReportStatus.PENDING).collect { pendientes ->
                _uiState.update {
                    it.copy(reportesPendientes = pendientes, totalPendientes = pendientes.size)
                }
            }
        }
    }
}
