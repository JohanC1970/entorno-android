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

data class PanelModeradorUiState(
    val reportesPendientes: List<Report> = emptyList(),
    val totalPendientes: Int = 0
)

class PanelModeradorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PanelModeradorUiState())
    val uiState: StateFlow<PanelModeradorUiState> = _uiState.asStateFlow()

    init {
        cargarReportesPendientes()
    }

    private fun cargarReportesPendientes() {
        val pendientes = listOf(
            Report(
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
            ),
            Report(
                id = "mod2",
                title = "Perro perdido cerca al parque",
                description = "Se encontró un perro mestizo sin collar cerca del parque principal",
                category = ReportCategory.PETS,
                location = Location(4.5340, -75.6815),
                status = ReportStatus.PENDING,
                imageUrls = listOf("https://ejemplo.com/perro.jpg"),
                ownerId = "user3",
                ownerName = "María García",
                date = LocalDate.now()
            ),
            Report(
                id = "mod3",
                title = "Acumulación de basuras",
                description = "Basuras acumuladas en la esquina de la cra 14 con calle 12",
                category = ReportCategory.COMMUNITY,
                location = Location(4.5350, -75.6820),
                status = ReportStatus.PENDING,
                imageUrls = listOf("https://ejemplo.com/basura.jpg"),
                ownerId = "user4",
                ownerName = "Pedro Martínez",
                date = LocalDate.now()
            ),
            Report(
                id = "mod4",
                title = "Intento de robo en la 19",
                description = "Reportan intento de robo en la calle 19 con carrera 8",
                category = ReportCategory.SECURITY,
                location = Location(4.5360, -75.6830),
                status = ReportStatus.PENDING,
                imageUrls = listOf("https://ejemplo.com/robo.jpg"),
                ownerId = "user5",
                ownerName = "Ana Ruiz",
                date = LocalDate.now()
            )
        )

        _uiState.update {
            it.copy(
                reportesPendientes = pendientes,
                totalPendientes = 7 // Como en el mockup
            )
        }
    }
}
