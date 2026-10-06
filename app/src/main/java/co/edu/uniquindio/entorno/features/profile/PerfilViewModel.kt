package co.edu.uniquindio.entorno.features.profile

import androidx.lifecycle.ViewModel
import co.edu.uniquindio.entorno.domain.model.Badge
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.model.UserLevel
import co.edu.uniquindio.entorno.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Representa una actividad reciente en el perfil del usuario */
data class ActividadReciente(
    val id: String,
    val icono: ActividadTipo,
    val texto: String,
    val tiempo: String
)

enum class ActividadTipo {
    REPORTE_VERIFICADO,
    VOTOS_DADOS
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

class PerfilViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        val usuario = User(
            id = "user1",
            name = "Juan E. Cayón",
            email = "juanc.cayon@uqvirtual.edu.co",
            city = "Armenia",
            address = "Barrio Fundadores",
            phoneNumber = "+57 300 000 0000",
            role = UserRole.USER,
            points = 340,
            badges = listOf(Badge.FIRST_REPORT, Badge.FIRST_COMMENT)
        )

        val insignias = listOf(
            InsigniaPerfil(nombre = "Ojo de\nhalcón", badge = Badge.FIRST_REPORT, desbloqueada = true),
            InsigniaPerfil(nombre = "Primer\nverificado", badge = Badge.FIRST_COMMENT, desbloqueada = true),
            InsigniaPerfil(nombre = "100\nvotos", badge = Badge.TEN_VERIFIED_REPORTS, desbloqueada = true),
            InsigniaPerfil(nombre = "Guardián\ndel barrio", badge = null, desbloqueada = false)
        )

        val actividad = listOf(
            ActividadReciente(
                id = "1",
                icono = ActividadTipo.REPORTE_VERIFICADO,
                texto = "Tu reporte del hueco fue verificado",
                tiempo = "2 d"
            ),
            ActividadReciente(
                id = "2",
                icono = ActividadTipo.VOTOS_DADOS,
                texto = "Marcaste 3 reportes como importantes",
                tiempo = "4 d"
            )
        )

        _uiState.update {
            it.copy(
                usuario = usuario,
                reportesTotal = 12,
                verificados = 8,
                votosDados = 203,
                insignias = insignias,
                actividadReciente = actividad
            )
        }
    }
}
