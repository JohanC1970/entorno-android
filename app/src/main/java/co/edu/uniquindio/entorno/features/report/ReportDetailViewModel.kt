package co.edu.uniquindio.entorno.features.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.core.util.toUserMessage
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.repository.ReportRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportDetailUiState(
    val report: Report? = null,
    val isImportant: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportRepository: ReportRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val reportId: String = savedStateHandle.get<String>("reportId").orEmpty()

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    init {
        observeReport()
        observeVote()
    }

    private fun observeReport() {
        viewModelScope.launch {
            reportRepository.observeReport(reportId)
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
                .collect { report ->
                    _uiState.update { it.copy(report = report, isLoading = false) }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeVote() {
        viewModelScope.launch {
            sessionRepository.session
                .flatMapLatest { session ->
                    session?.let { reportRepository.observeHasVoted(reportId, it.userId) }
                        ?: flowOf(false)
                }
                .catch { emit(false) }
                .collect { voted -> _uiState.update { it.copy(isImportant = voted) } }
        }
    }

    fun toggleImportant() {
        viewModelScope.launch {
            val session = sessionRepository.session.first()
            if (session == null) {
                _uiState.update { it.copy(errorMessage = "Debes iniciar sesión para marcar un reporte") }
                return@launch
            }
            reportRepository.toggleImportant(reportId, session.userId).onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.toUserMessage()) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
