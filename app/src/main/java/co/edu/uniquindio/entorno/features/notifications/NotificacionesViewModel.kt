package co.edu.uniquindio.entorno.features.notifications

import androidx.lifecycle.ViewModel
import co.edu.uniquindio.entorno.domain.model.AppNotification
import co.edu.uniquindio.entorno.domain.model.NotificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class NotificationFilter(val label: String) {
    TODAS("Todas"),
    SIN_LEER("Sin leer")
}

data class NotificacionesUiState(
    val notificaciones: List<AppNotification> = emptyList(),
    val filtroActual: NotificationFilter = NotificationFilter.TODAS
) {
    val notificacionesFiltradas: List<AppNotification>
        get() = when (filtroActual) {
            NotificationFilter.TODAS -> notificaciones
            NotificationFilter.SIN_LEER -> notificaciones.filter { !it.read }
        }

    val sinLeerCount: Int
        get() = notificaciones.count { !it.read }
}

class NotificacionesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacionesUiState())
    val uiState: StateFlow<NotificacionesUiState> = _uiState.asStateFlow()

    init {
        cargarNotificaciones()
    }

    private fun cargarNotificaciones() {
        // Datos de prueba que coinciden con el mockup
        val notificaciones = listOf(
            AppNotification(
                id = "1",
                userId = "user1",
                type = NotificationType.REPORT_VERIFIED,
                title = "Tu reporte fue verificado.",
                message = "«Hueco profundo en la Av. Centenario» ya es visible para todos.",
                reportId = "1",
                read = false,
                createdAt = System.currentTimeMillis() - 20 * 60 * 1000 // hace 20 min
            ),
            AppNotification(
                id = "2",
                userId = "user1",
                type = NotificationType.BADGE_EARNED,
                title = "12 vecinos",
                message = "marcaron tu reporte como importante.",
                reportId = "1",
                read = false,
                createdAt = System.currentTimeMillis() - 60 * 60 * 1000 // hace 1 h
            ),
            AppNotification(
                id = "3",
                userId = "user1",
                type = NotificationType.REPORT_VERIFIED,
                title = "Reporte de seguridad a 300 m.",
                message = "Intento de hurto en la Carrera 14.",
                reportId = "2",
                read = false,
                createdAt = System.currentTimeMillis() - 2 * 60 * 60 * 1000 // hace 2 h
            ),
            AppNotification(
                id = "4",
                userId = "user1",
                type = NotificationType.LEVEL_UP,
                title = "Nuevo nivel: Colaborador.",
                message = "Llevas 8 reportes verificados en tu barrio.",
                read = true,
                createdAt = System.currentTimeMillis() - 24 * 60 * 60 * 1000 // ayer
            ),
            AppNotification(
                id = "5",
                userId = "user1",
                type = NotificationType.NEW_COMMENT,
                title = "Marcela R. comentó",
                message = "en tu reporte de basuras acumuladas.",
                reportId = "3",
                read = true,
                createdAt = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000 // hace 2 días
            ),
            AppNotification(
                id = "6",
                userId = "user1",
                type = NotificationType.REPORT_REJECTED,
                title = "Reporte no publicado.",
                message = "La foto no corresponde a la categoría. Puedes editarlo.",
                reportId = "4",
                read = true,
                createdAt = System.currentTimeMillis() - 21L * 24 * 60 * 60 * 1000 // hace 3 semanas
            )
        )

        _uiState.update { it.copy(notificaciones = notificaciones) }
    }

    fun cambiarFiltro(filtro: NotificationFilter) {
        _uiState.update { it.copy(filtroActual = filtro) }
    }

    fun marcarTodasComoLeidas() {
        _uiState.update { state ->
            state.copy(
                notificaciones = state.notificaciones.map { it.copy(read = true) }
            )
        }
    }
}
