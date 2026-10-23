package co.edu.uniquindio.entorno.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.domain.model.AppNotification
import co.edu.uniquindio.entorno.domain.repository.NotificationRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import co.edu.uniquindio.entorno.domain.usecase.auth.withUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

@HiltViewModel
class NotificacionesViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacionesUiState())
    val uiState: StateFlow<NotificacionesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository
                .withUserId(emptyList<AppNotification>()) { userId ->
                    notificationRepository.observeNotifications(userId)
                }
                .collect { lista -> _uiState.update { it.copy(notificaciones = lista) } }
        }
    }

    fun cambiarFiltro(filtro: NotificationFilter) {
        _uiState.update { it.copy(filtroActual = filtro) }
    }

    fun marcarTodasComoLeidas() {
        viewModelScope.launch {
            val userId = sessionRepository.session.first()?.userId ?: return@launch
            notificationRepository.markAllAsRead(userId)
        }
    }

    fun marcarComoLeida(notificationId: String) {
        viewModelScope.launch { notificationRepository.setRead(notificationId, true) }
    }
}
