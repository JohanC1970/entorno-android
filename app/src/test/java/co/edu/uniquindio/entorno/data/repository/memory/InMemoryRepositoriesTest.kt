package co.edu.uniquindio.entorno.data.repository.memory

import co.edu.uniquindio.entorno.domain.model.Comment
import co.edu.uniquindio.entorno.domain.model.EmailAlreadyInUseException
import co.edu.uniquindio.entorno.domain.model.InvalidCredentialsException
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.NotificationType
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.model.UserRole
import co.edu.uniquindio.entorno.domain.model.WeakPasswordException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de la capa de datos en memoria de la Fase 2 (autenticación, reportes,
 * moderación, comentarios, notificaciones y perfil). Cada prueba parte de una base nueva
 * con los datos de demostración de [MemorySeed].
 */
class InMemoryRepositoriesTest {

    private val db = MemoryDatabase()
    private val auth = InMemoryAuthRepository(db)
    private val users = InMemoryUserRepository(db)
    private val reports = InMemoryReportRepository(db)
    private val comments = InMemoryCommentRepository(db)
    private val notifications = InMemoryNotificationRepository(db)
    private val settings = InMemorySettingsRepository(db)

    private fun newReport(owner: String = "u-juan") = Report(
        title = "Prueba", description = "Descripción", category = ReportCategory.SECURITY,
        location = Location(4.5340, -75.6812), imageUrls = listOf("content://foto"), ownerId = owner,
        // Valores que el repositorio debe ignorar al crear:
        status = ReportStatus.VERIFIED, importantCount = 99
    )

    // ------------------------------------------------------------ autenticación

    @Test
    fun login_conCredencialesDeDemo_funciona() = runBlocking {
        assertTrue(auth.login("juan.cayon@uqvirtual.edu.co", "123456").isSuccess)
        assertEquals("u-juan", auth.currentUserId)
    }

    @Test
    fun login_conClaveIncorrectaOCorreoInexistente_falla() = runBlocking {
        assertTrue(auth.login("juan.cayon@uqvirtual.edu.co", "x").exceptionOrNull() is InvalidCredentialsException)
        assertTrue(auth.login("no@existe.co", "123456").isFailure)
    }

    @Test
    fun registro_rechazaCorreoDuplicadoYClaveDebil() = runBlocking {
        val duplicado = auth.register(User(name = "A", email = "JUAN.cayon@uqvirtual.edu.co"), "123456")
        assertTrue(duplicado.exceptionOrNull() is EmailAlreadyInUseException)
        val debil = auth.register(User(name = "A", email = "a@a.co"), "123")
        assertTrue(debil.exceptionOrNull() is WeakPasswordException)
    }

    @Test
    fun registro_fuerzaRolUsuarioYPuntosEnCero() = runBlocking {
        val nuevo = auth.register(
            User(name = "Nuevo", email = "nuevo@correo.co", role = UserRole.MODERATOR, points = 500), "abcdef"
        ).getOrThrow()
        assertEquals(UserRole.USER, nuevo.role)
        assertEquals(0, nuevo.points)
        assertTrue(auth.login("nuevo@correo.co", "abcdef").isSuccess)
    }

    @Test
    fun cambiarContrasena_verificaLaActual() = runBlocking {
        auth.login("juan.cayon@uqvirtual.edu.co", "123456")
        assertTrue(auth.changePassword("incorrecta", "Nueva123").isFailure)
        assertTrue(auth.changePassword("123456", "Nueva123").isSuccess)
        assertTrue(auth.login("juan.cayon@uqvirtual.edu.co", "Nueva123").isSuccess)
    }

    @Test
    fun recuperarContrasena_soloParaCuentasExistentes() = runBlocking {
        assertTrue(auth.sendPasswordReset("no@existe.co").isFailure)
        assertTrue(auth.sendPasswordReset("juan.cayon@uqvirtual.edu.co").isSuccess)
    }

    @Test
    fun eliminarCuenta_exigeLaClaveYCierraLaSesion() = runBlocking {
        auth.login("juan.cayon@uqvirtual.edu.co", "123456")
        assertTrue(auth.deleteAccount("mal").isFailure)
        assertTrue(auth.deleteAccount("123456").isSuccess)
        assertTrue(users.findById("u-juan").isFailure)
        assertEquals(null, auth.currentUserId)
    }

    // ------------------------------------------------------- reportes y moderación

    @Test
    fun crearReporte_nacePendienteSinVotosYSumaPuntos() = runBlocking {
        val antes = users.findById("u-juan").getOrThrow().points
        val id = reports.createReport(newReport()).getOrThrow()
        val creado = reports.observeReport(id).first()!!
        assertEquals(ReportStatus.PENDING, creado.status)
        assertEquals(0, creado.importantCount)
        assertTrue(creado.neighborhood.isNotBlank())
        assertEquals(antes + 10, users.findById("u-juan").getOrThrow().points)
        assertTrue(reports.observeFeed().first().any { it.id == id })
        assertTrue(reports.observeUserReports("u-juan").first().any { it.id == id })
    }

    @Test
    fun soloUnModeradorVerificaYSoloUnaVez() = runBlocking {
        val id = reports.createReport(newReport()).getOrThrow()
        assertTrue(reports.verifyReport(id, "u-juan").isFailure)
        val puntos = users.findById("u-juan").getOrThrow().points
        assertTrue(reports.verifyReport(id, "m-carlos").isSuccess)
        assertEquals(puntos + 15, users.findById("u-juan").getOrThrow().points)
        assertTrue(reports.verifyReport(id, "m-carlos").isFailure)
        val avisos = notifications.observeNotifications("u-juan").first()
        assertTrue(avisos.any { it.reportId == id && it.type == NotificationType.REPORT_VERIFIED })
    }

    @Test
    fun rechazar_exigeMotivoYSacaElReporteDelFeed() = runBlocking {
        assertTrue(reports.rejectReport("r-poste", "m-carlos", "  ").isFailure)
        assertTrue(reports.rejectReport("r-poste", "m-carlos", "Foto borrosa").isSuccess)
        assertTrue(reports.observeFeed().first().none { it.id == "r-poste" })
        assertEquals("Foto borrosa", reports.observeReport("r-poste").first()?.rejectionReason)
    }

    @Test
    fun colaDeModeracion_soloTienePendientes() = runBlocking {
        val cola = reports.observeModerationQueue(ReportStatus.PENDING).first()
        assertTrue(cola.isNotEmpty())
        assertTrue(cola.all { it.status == ReportStatus.PENDING })
    }

    @Test
    fun esImportante_sumaYSeRevierteAlVolverATocar() = runBlocking {
        val inicial = reports.observeReport("r-hurto").first()!!.importantCount
        assertTrue(reports.toggleImportant("r-hurto", "u-johan").getOrThrow())
        assertEquals(inicial + 1, reports.observeReport("r-hurto").first()!!.importantCount)
        assertTrue(reports.observeHasVoted("r-hurto", "u-johan").first())
        assertFalse(reports.toggleImportant("r-hurto", "u-johan").getOrThrow())
        assertEquals(inicial, reports.observeReport("r-hurto").first()!!.importantCount)
    }

    @Test
    fun resolver_soloAutorOModeradorYSoloSiEstaVerificado() = runBlocking {
        assertTrue(reports.markResolved("r-hurto", "u-johan").isFailure)
        assertTrue(reports.markResolved("r-motos", "u-juan").isFailure)
        assertTrue(reports.markResolved("r-hurto", "m-carlos").isSuccess)
        assertEquals(ReportStatus.RESOLVED, reports.observeReport("r-hurto").first()!!.status)
    }

    @Test
    fun reportesCercanos_respetanElRadio() = runBlocking {
        val cerca = reports.getNearbyReports(4.5339, -75.6811, 1_000.0).getOrThrow()
        assertTrue(cerca.isNotEmpty())
        assertTrue(cerca.size < reports.observeFeed().first().size)
    }

    // --------------------------------------------------------------- comentarios

    @Test
    fun comentar_sumaContadorYNotificaAlAutor() = runBlocking {
        val antes = reports.observeReport("r-hueco").first()!!.commentCount
        assertTrue(comments.addComment(Comment(reportId = "r-hueco", authorId = "u-johan", authorName = "", text = " Hola ")).isSuccess)
        assertEquals(antes + 1, reports.observeReport("r-hueco").first()!!.commentCount)
        val avisos = notifications.observeNotifications("u-juan").first()
        assertTrue(avisos.any { it.type == NotificationType.NEW_COMMENT && it.title.contains("Johan") })
        assertTrue(comments.addComment(Comment(reportId = "r-hueco", authorId = "u-johan", authorName = "", text = "  ")).isFailure)
    }

    @Test
    fun respuestasSonDeUnSoloNivel_yElModeradorQuedaMarcado() = runBlocking {
        comments.addComment(Comment(reportId = "r-hueco", authorId = "u-johan", authorName = "", text = "raiz"))
        val raiz = comments.observeComments("r-hueco").first().first { it.text == "raiz" }
        comments.addComment(Comment(reportId = "r-hueco", authorId = "u-diego", authorName = "", text = "r1", parentCommentId = raiz.id))
        val r1 = comments.observeComments("r-hueco").first().first { it.text == "r1" }
        comments.addComment(Comment(reportId = "r-hueco", authorId = "m-carlos", authorName = "", text = "r2", parentCommentId = r1.id))
        val r2 = comments.observeComments("r-hueco").first().first { it.text == "r2" }
        assertEquals(raiz.id, r1.parentCommentId)
        assertEquals(raiz.id, r2.parentCommentId)
        assertEquals(UserRole.MODERATOR, r2.authorRole)
    }

    // ------------------------------------------------- notificaciones y ajustes

    @Test
    fun marcarTodasLeidas_dejaElContadorEnCero() = runBlocking {
        assertTrue(notifications.observeUnreadCount("u-juan").first() > 0)
        notifications.markAllAsRead("u-juan")
        assertEquals(0, notifications.observeUnreadCount("u-juan").first())
    }

    @Test
    fun losAjustesSilencianTiposDeNotificacion() = runBlocking {
        comments.addComment(Comment(reportId = "r-hueco", authorId = "u-johan", authorName = "", text = "aviso"))
        assertTrue(notifications.observeNotifications("u-juan").first().any { it.type == NotificationType.NEW_COMMENT })
        settings.update { it.copy(notifyCommentsOnMyReports = false) }
        assertTrue(notifications.observeNotifications("u-juan").first().none { it.type == NotificationType.NEW_COMMENT })
    }

    // --------------------------------------------------------- perfil y ranking

    @Test
    fun editarPerfil_noPermiteCambiarRolNiPuntos_yActualizaElNombreEnSusReportes() = runBlocking {
        val resultado = users.update(
            User(id = "u-johan", name = "Johan G.", email = "johanc.garciag@uqvirtual.edu.co",
                role = UserRole.MODERATOR, points = 99_999)
        )
        assertTrue(resultado.isSuccess)
        val johan = users.findById("u-johan").getOrThrow()
        assertEquals(UserRole.USER, johan.role)
        assertTrue(johan.points < 99_999)
        assertEquals("Johan G.", reports.observeReport("r-ambulancia").first()!!.ownerName)
        val duplicado = users.update(User(id = "u-johan", name = "J", email = "juan.cayon@uqvirtual.edu.co"))
        assertTrue(duplicado.exceptionOrNull() is EmailAlreadyInUseException)
    }

    @Test
    fun ranking_vieneOrdenadoPorPuntos() = runBlocking {
        val ranking = users.observeRanking().first()
        assertTrue(ranking.zipWithNext().all { (a, b) -> a.points >= b.points })
        assertNotNull(ranking.firstOrNull())
        assertEquals("u-diego", ranking.first().id)
    }

    @Test
    fun votosDados_cuentanLosVotosDelUsuario() = runBlocking {
        assertEquals(3, reports.observeVotesGiven("u-juan").first())
    }
}
