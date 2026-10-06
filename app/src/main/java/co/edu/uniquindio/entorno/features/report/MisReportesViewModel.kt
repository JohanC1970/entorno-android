package co.edu.uniquindio.entorno.features.report

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

class MisReportesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MisReportesUiState())
    val uiState: StateFlow<MisReportesUiState> = _uiState.asStateFlow()

    init {
        cargarReportes()
    }

    private fun cargarReportes() {
        // Datos de prueba que coinciden con el mockup
        val reportes = listOf(
            Report(
                id = "1",
                title = "Hueco profundo en la Av. Centenario",
                description = "Hueco peligroso en la avenida principal",
                category = ReportCategory.INFRASTRUCTURE,
                location = Location(4.5339, -75.6811),
                status = ReportStatus.VERIFIED,
                imageUrls = listOf("https://ejemplo.com/hueco.jpg"),
                ownerId = "user1",
                ownerName = "Juan E. Cayón",
                date = LocalDate.now().minusDays(2),
                importantCount = 47
            ),
            Report(
                id = "2",
                title = "Motos sin placa rondando el conjunto",
                description = "Varias motos sin placa circulan por el conjunto",
                category = ReportCategory.SECURITY,
                location = Location(4.5350, -75.6820),
                status = ReportStatus.PENDING,
                imageUrls = listOf("https://ejemplo.com/motos.jpg"),
                ownerId = "user1",
                ownerName = "Juan E. Cayón",
                date = LocalDate.now().minusDays(4),
                importantCount = 23
            ),
            Report(
                id = "3",
                title = "Basuras acumuladas en la esquina",
                description = "Acumulación de basuras en la esquina del barrio",
                category = ReportCategory.COMMUNITY,
                location = Location(4.5360, -75.6830),
                status = ReportStatus.RESOLVED,
                imageUrls = listOf("https://ejemplo.com/basuras.jpg"),
                ownerId = "user1",
                ownerName = "Juan E. Cayón",
                date = LocalDate.now().minusWeeks(2),
                importantCount = 64
            ),
            Report(
                id = "4",
                title = "Gato encontrado en la 19",
                description = "Gato abandonado encontrado en la calle 19",
                category = ReportCategory.PETS,
                location = Location(4.5370, -75.6840),
                status = ReportStatus.REJECTED,
                imageUrls = listOf("https://ejemplo.com/gato.jpg"),
                ownerId = "user1",
                ownerName = "Juan E. Cayón",
                date = LocalDate.now().minusWeeks(3),
                rejectionReason = "Foto sin relación"
            )
        )

        _uiState.update {
            it.copy(
                reportes = reportes,
                totalReportes = 12 // Total como en el mockup
            )
        }
    }

    fun cambiarFiltro(filtro: ReportFilter) {
        _uiState.update { it.copy(filtroActual = filtro) }
    }
}
