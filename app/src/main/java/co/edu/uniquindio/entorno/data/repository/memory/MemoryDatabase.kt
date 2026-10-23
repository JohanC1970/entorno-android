package co.edu.uniquindio.entorno.data.repository.memory

import co.edu.uniquindio.entorno.domain.model.AppNotification
import co.edu.uniquindio.entorno.domain.model.AppSettings
import co.edu.uniquindio.entorno.domain.model.Comment
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.model.UserSession
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * "Base de datos" en memoria de la Fase 2: todo vive mientras la app esté abierta y se
 * reinicia con datos de demostración al abrirla de nuevo. Los repositorios InMemory*
 * comparten esta única instancia; en la Fase 3 se reemplazan por los de Firebase.
 */
@Singleton
class MemoryDatabase @Inject constructor() {

    val users = MutableStateFlow<List<User>>(emptyList())
    /** userId -> contraseña (solo para la demo en memoria). */
    val passwords = MutableStateFlow<Map<String, String>>(emptyMap())
    val reports = MutableStateFlow<List<Report>>(emptyList())
    val comments = MutableStateFlow<List<Comment>>(emptyList())
    val notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    /** Votos "Es importante" como pares (reportId, userId). */
    val votes = MutableStateFlow<Set<Pair<String, String>>>(emptySet())
    val authUserId = MutableStateFlow<String?>(null)
    val session = MutableStateFlow<UserSession?>(null)
    val settings = MutableStateFlow(AppSettings())

    private val counter = AtomicLong(1_000L)

    fun newId(prefix: String): String = "$prefix-${counter.incrementAndGet()}"

    init {
        MemorySeed.load(this)
    }
}
