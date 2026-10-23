package co.edu.uniquindio.entorno.features.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PERIOD_DAYS = 30L
private const val DAY_MS = 24 * 60 * 60 * 1000L

data class CategoryStat(val category: ReportCategory, val count: Int)
data class ZoneStat(val name: String, val count: Int)

data class StatsUiState(
    val activos: Int = 0,
    val resueltos: Int = 0,
    val ciudadanosActivos: Int = 0,
    /** Porcentaje de reportes resueltos sobre los que ya pasaron por verificación. */
    val tasaResolucion: Int = 0,
    val porCategoria: List<CategoryStat> = emptyList(),
    val zonas: List<ZoneStat> = emptyList()
) {
    val totalCategorias: Int get() = porCategoria.sumOf { it.count }
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            reportRepository.observeAllReports().collect { all ->
                val since = System.currentTimeMillis() - PERIOD_DAYS * DAY_MS
                val period = all.filter { it.createdAt >= since && it.status != ReportStatus.REJECTED }
                val activos = period.count { it.status == ReportStatus.PENDING || it.status == ReportStatus.VERIFIED }
                val resueltos = period.count { it.status == ReportStatus.RESOLVED }
                val reviewed = period.count { it.status == ReportStatus.VERIFIED || it.status == ReportStatus.RESOLVED }
                val state = StatsUiState(
                    activos = activos,
                    resueltos = resueltos,
                    ciudadanosActivos = period.map { it.ownerId }.distinct().size,
                    tasaResolucion = if (reviewed == 0) 0 else resueltos * 100 / reviewed,
                    porCategoria = ReportCategory.entries
                        .map { c -> CategoryStat(c, period.count { it.category == c }) }
                        .sortedByDescending { it.count },
                    zonas = period
                        .groupBy { it.neighborhood.ifBlank { Geo.nearestNeighborhood(it.location) } }
                        .map { (name, list) -> ZoneStat(name, list.size) }
                        .sortedByDescending { it.count }
                        .take(3)
                )
                _uiState.update { state }
            }
        }
    }
}
