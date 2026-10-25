package co.edu.uniquindio.entorno.features.moderator

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import co.edu.uniquindio.entorno.core.navigation.ReviewReport
import co.edu.uniquindio.entorno.core.util.toUserMessage
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.repository.ReportRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import co.edu.uniquindio.entorno.domain.usecase.auth.requireModeratorId
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RevisarPublicacionUiState(
    val reporte: Report? = null,
    val motivoRechazo: String = "",
    val verificando: Boolean = false,
    val rechazando: Boolean = false,
    val accionCompletada: Boolean = false,
    val mensajeError: String? = null,
    /** Texto de confirmación que se muestra antes de volver al panel. */
    val mensajeExito: String? = null
)

@HiltViewModel
class RevisarPublicacionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportRepository: ReportRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val reportId: String = savedStateHandle.toRoute<ReviewReport>().reportId
    private val _uiState = MutableStateFlow(RevisarPublicacionUiState())
    val uiState: StateFlow<RevisarPublicacionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Se toma una sola vez: tras verificar o rechazar el reporte sale de la cola.
            val reporte = reportRepository.findById(reportId).getOrNull()
            _uiState.update { it.copy(reporte = reporte) }
        }
    }

    fun actualizarMotivo(motivo: String) {
        _uiState.update { it.copy(motivoRechazo = motivo, mensajeError = null) }
    }

    fun verificar() {
        if (_uiState.value.verificando || _uiState.value.rechazando) return
        viewModelScope.launch {
            _uiState.update { it.copy(verificando = true) }
            runCatching { sessionRepository.requireModeratorId() }
                .mapCatching { moderatorId -> reportRepository.verifyReport(reportId, moderatorId).getOrThrow() }
                .onSuccess {
                    _uiState.update {
                        it.copy(verificando = false, accionCompletada = true, mensajeExito = "Reporte verificado")
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(verificando = false, mensajeError = error.toUserMessage()) }
                }
        }
    }

    fun rechazar() {
        val motivo = _uiState.value.motivoRechazo
        if (motivo.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Escribe el motivo del rechazo") }
            return
        }
        if (_uiState.value.verificando || _uiState.value.rechazando) return
        viewModelScope.launch {
            _uiState.update { it.copy(rechazando = true) }
            runCatching { sessionRepository.requireModeratorId() }
                .mapCatching { moderatorId ->
                    reportRepository.rejectReport(reportId, moderatorId, motivo).getOrThrow()
                }
                .onSuccess {
                    _uiState.update {
                        it.copy(rechazando = false, accionCompletada = true, mensajeExito = "Reporte rechazado")
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(rechazando = false, mensajeError = error.toUserMessage()) }
                }
        }
    }

    fun mensajeMostrado() {
        _uiState.update { it.copy(mensajeError = null) }
    }
}
