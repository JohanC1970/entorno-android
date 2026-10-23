package co.edu.uniquindio.entorno.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.core.util.toRelativeTime
import co.edu.uniquindio.entorno.domain.model.AppNotification
import co.edu.uniquindio.entorno.domain.model.Badge
import co.edu.uniquindio.entorno.domain.model.NotificationType
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.model.UserLevel
import co.edu.uniquindio.entorno.domain.model.UserRole
import co.edu.uniquindio.entorno.domain.repository.NotificationRepository
import co.edu.uniquindio.entorno.domain.repository.ReportRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import co.edu.uniquindio.entorno.domain.repository.UserRepository
import co.edu.uniquindio.entorno.domain.usecase.auth.observeCurrentUser
import co.edu.uniquindio.entorno.domain.usecase.auth.withUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Representa una actividad reciente en el perfil del usuario */
data class ActividadReciente(
    val id: String,
    val icono: ActividadTipo,
    val texto: String,
    val tiempo: String
)

enum class ActividadTipo {
    REPORTE_VERIFICADO,
    VOTOS_DADOS,
    COMENTARIO,
    REPORTE_RECHAZADO,
    REPORTE_RESUELTO,
    INSIGNIA,
    NIVEL
}

/** Insignias visibles en el perfil (incluye las bloqueadas) */
data class InsigniaPerfil(
    val nombre: String,
    val badge: Badge? = null,
    val desbloqueada: Boolean = true
)

data class PerfilUiState(
    val usuario: User? = null,
    val reportesTotal: Int = 0,
    val verificados: Int = 0,
    val votosDados: Int = 0,
    val insignias: List<InsigniaPerfil> = emptyList(),
    val actividadReciente: List<ActividadReciente> = emptyList()
) {
    val esModerador: Boolean get() = usuario?.role == UserRole.MODERATOR

    val nivel: UserLevel get() = usuario?.level ?: UserLevel.NOVICE

    val puntosParaSiguienteNivel: Int
        get() {
            val actual = nivel
            val siguiente = UserLevel.entries.getOrNull(actual.ordinal + 1)
            return if (siguiente != null) {
                siguiente.minPoints - (usuario?.points ?: 0)
            } else 0
        }

    val siguienteNivel: UserLevel?
        get() = UserLevel.entries.getOrNull(nivel.ordinal + 1)

    val progresoNivel: Float
        get() {
            val actual = nivel
            val siguiente = UserLevel.entries.getOrNull(actual.ordinal + 1) ?: return 1f
            val puntosEnNivel = (usuario?.points ?: 0) - actual.minPoints
            val rangoNivel = siguiente.minPoints - actual.minPoints
            return (puntosEnNivel.toFloat() / rangoNivel).coerceIn(0f, 1f)
        }
}

private const val MAX_ACTIVIDAD = 4

private fun AppNotification.toActividad() = ActividadReciente(
    id = id,
    icono = when (type) {
        NotificationType.REPORT_VERIFIED -> ActividadTipo.REPORTE_VERIFICADO
        NotificationType.REPORT_REJECTED -> ActividadTipo.REPORTE_RECHAZADO
        NotificationType.REPORT_RESOLVED -> ActividadTipo.REPORTE_RESUELTO
        NotificationType.NEW_COMMENT -> ActividadTipo.COMENTARIO
        NotificationType.BADGE_EARNED -> ActividadTipo.INSIGNIA
        NotificationType.LEVEL_UP -> ActividadTipo.NIVEL
    },
    texto = title.trimEnd('.'),
    tiempo = createdAt.toRelativeTime()
)

fun buildBadgeList(user: User): List<InsigniaPerfil> =
    Badge.entries.map { badge ->
        InsigniaPerfil(nombre = badge.label, badge = badge, desbloqueada = badge in user.badges)
    }

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val reportRepository: ReportRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                sessionRepository.observeCurrentUser(userRepository),
                sessionRepository.withUserId(emptyList<Report>()) { reportRepository.observeUserReports(it) },
                sessionRepository.withUserId(0) { reportRepository.observeVotesGiven(it) },
                sessionRepository.withUserId(emptyList<AppNotification>()) { notificationRepository.observeNotifications(it) }
            ) { user, reportes, votos, notificaciones ->
                PerfilUiState(
                    usuario = user,
                    reportesTotal = reportes.size,
                    verificados = reportes.count {
                        it.status == ReportStatus.VERIFIED || it.status == ReportStatus.RESOLVED
                    },
                    votosDados = votos,
                    insignias = user?.let { buildBadgeList(it) }.orEmpty(),
                    actividadReciente = notificaciones.take(MAX_ACTIVIDAD).map { it.toActividad() }
                )
            }.collect { state -> _uiState.update { state } }
        }
    }
}
