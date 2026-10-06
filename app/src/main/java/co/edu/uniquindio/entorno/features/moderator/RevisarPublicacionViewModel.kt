package co.edu.uniquindio.entorno.features.moderator

import androidx.lifecycle.ViewModel
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class RevisarPublicacionUiState(
    val reporte: Report? = null,
    val motivoRechazo: String = "",
    val verificando: Boolean = false,
    val rechazando: Boolean = false,
    val accionCompletada: Boolean = false
)

class RevisarPublicacionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RevisarPublicacionUiState())
    val uiState: StateFlow<RevisarPublicacionUiState> = _uiState.asStateFlow()

    init {
        cargarReporte()
    }

    private fun cargarReporte() {
        // Datos de prueba que coinciden con el mockup
        val reporte = Report(
            id = "mod1",
            title = "Poste de luz averiado",
            description = "El poste frente al número 45 lleva 3 días sin encender. La calle queda muy oscura de noche y ya hubo un intento de robo.",
            category = ReportCategory.INFRASTRUCTURE,
            location = Location(4.5339, -75.6811),
            status = ReportStatus.PENDING,
            imageUrls = listOf("https://ejemplo.com/poste.jpg"),
            ownerId = "user2",
            ownerName = "Carlos López",
            date = LocalDate.now()
        )

        _uiState.update { it.copy(reporte = reporte) }
    }

    fun actualizarMotivo(motivo: String) {
        _uiState.update { it.copy(motivoRechazo = motivo) }
    }

    fun verificar() {
        _uiState.update { it.copy(verificando = true) }
        // Simulación: cambiar estado a verificado
        _uiState.update { state ->
            state.copy(
                reporte = state.reporte?.copy(status = ReportStatus.VERIFIED),
                verificando = false,
                accionCompletada = true
            )
        }
    }

    fun rechazar() {
        val motivo = _uiState.value.motivoRechazo
        if (motivo.isBlank()) return

        _uiState.update { it.copy(rechazando = true) }
        // Simulación: cambiar estado a rechazado
        _uiState.update { state ->
            state.copy(
                reporte = state.reporte?.copy(
                    status = ReportStatus.REJECTED,
                    rejectionReason = motivo
                ),
                rechazando = false,
                accionCompletada = true
            )
        }
    }
}
