package co.edu.uniquindio.entorno.features.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val WINDOW_HOURS = 3
private const val PERIOD_DAYS = 30L
private const val DAY_MS = 24 * 60 * 60 * 1000L

data class Hotspot(val name: String, val location: Location, val count: Int)

data class HeatMapUiState(
    val locations: List<Location> = emptyList(),
    val hotspots: List<Hotspot> = emptyList(),
    val total: Int = 0,
    val category: ReportCategory? = null,
    val allDay: Boolean = true,
    /** Hora de inicio (0 a 23) de la franja de 3 horas cuando no es "todo el día". */
    val startHour: Int = 18
) {
    val windowLabel: String
        get() = if (allDay) "Todo el día" else "${formatHour(startHour)} – ${formatHour((startHour + WINDOW_HOURS) % 24)}"
}

fun formatHour(hour: Int): String {
    val suffix = if (hour < 12) "a. m." else "p. m."
    val h12 = when (val h = hour % 12) { 0 -> 12 else -> h }
    return "$h12 $suffix"
}

private fun Report.hourOfDay(): Int =
    Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()).hour

private fun inWindow(hour: Int, start: Int): Boolean {
    val offset = (hour - start + 24) % 24
    return offset < WINDOW_HOURS
}

@HiltViewModel
class HeatMapViewModel @Inject constructor(
    reportRepository: ReportRepository
) : ViewModel() {

    private val category = MutableStateFlow<ReportCategory?>(null)
    private val allDay = MutableStateFlow(true)
    private val startHour = MutableStateFlow(18)

    private val _uiState = MutableStateFlow(HeatMapUiState())
    val uiState: StateFlow<HeatMapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(reportRepository.observeAllReports(), category, allDay, startHour) { reports, cat, day, start ->
                val since = System.currentTimeMillis() - PERIOD_DAYS * DAY_MS
                val selected = reports
                    .filter { it.status != ReportStatus.REJECTED && it.createdAt >= since }
                    .filter { cat == null || it.category == cat }
                    .filter { day || inWindow(it.hourOfDay(), start) }
                val hotspots = selected
                    .groupBy { it.neighborhood.ifBlank { Geo.nearestNeighborhood(it.location) } }
                    .map { (name, list) ->
                        val center = Geo.neighborhoods.firstOrNull { it.name == name }?.center ?: list.first().location
                        Hotspot(name, center, list.size)
                    }
                    .sortedByDescending { it.count }
                    .take(3)
                HeatMapUiState(
                    locations = selected.map { it.location }, hotspots = hotspots, total = selected.size,
                    category = cat, allDay = day, startHour = start
                )
            }.collect { state -> _uiState.update { state } }
        }
    }

    fun onCategorySelected(selected: ReportCategory?) {
        category.value = selected
    }

    fun onAllDayChange(value: Boolean) {
        allDay.value = value
    }

    fun onStartHourChange(hour: Int) {
        startHour.value = hour.coerceIn(0, 23)
    }
}
