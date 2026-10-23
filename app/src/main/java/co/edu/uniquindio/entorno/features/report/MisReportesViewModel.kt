package co.edu.uniquindio.entorno.features.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.domain.repository.ReportRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import co.edu.uniquindio.entorno.domain.usecase.auth.withUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ReportFilter(val label: String) {
    TODOS("Todos"),
    ACTIVOS("Activos"),
    RESUELTOS("Resueltos"),
    RECHAZADOS("Rechazados")
}

data class MisReportesUiState(
    val reportes: List<Report> = emptyList(),
    val filtroActual: ReportFilter = ReportFilter.TODOS,
    val totalReportes: Int = 0
) {
    val reportesFiltrados: List<Report>
        get() = when (filtroActual) {
            ReportFilter.TODOS -> reportes
            ReportFilter.ACTIVOS -> reportes.filter {
                it.status == ReportStatus.PENDING || it.status == ReportStatus.VERIFIED
            }
            ReportFilter.RESUELTOS -> reportes.filter { it.status == ReportStatus.RESOLVED }
            ReportFilter.RECHAZADOS -> reportes.filter { it.status == ReportStatus.REJECTED }
        }
}

@HiltViewModel
class MisReportesViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MisReportesUiState())
    val uiState: StateFlow<MisReportesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository
                .withUserId(emptyList<Report>()) { userId -> reportRepository.observeUserReports(userId) }
                .collect { reportes ->
                    _uiState.update { it.copy(reportes = reportes, totalReportes = reportes.size) }
                }
        }
    }

    fun cambiarFiltro(filtro: ReportFilter) {
        _uiState.update { it.copy(filtroActual = filtro) }
    }
}
