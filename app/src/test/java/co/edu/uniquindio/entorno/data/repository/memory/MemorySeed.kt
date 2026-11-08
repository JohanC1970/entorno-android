package co.edu.uniquindio.entorno.data.repository.memory

import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.domain.model.AppNotification
import co.edu.uniquindio.entorno.domain.model.Badge
import co.edu.uniquindio.entorno.domain.model.Comment
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.NotificationType
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.model.UserRole
import java.time.Instant
import java.time.ZoneId

/**
 * Datos de demostración. Todas las cuentas usan la contraseña [DEMO_PASSWORD].
 *
 *  - juan.cayon@uqvirtual.edu.co      (ciudadano)
 *  - johanc.garciag@uqvirtual.edu.co  (ciudadano)
 *  - moderador@entorno.co             (moderador)
 */
internal object MemorySeed {

    const val DEMO_PASSWORD = "123456"

    private const val MIN = 60_000L
    private const val HOUR = 60 * MIN
    private const val DAY = 24 * HOUR

    fun load(db: MemoryDatabase) {
        val now = System.currentTimeMillis()

        val users = seedUsers()
        db.users.value = users
        db.passwords.value = users.associate { it.id to DEMO_PASSWORD }

        val comments = seedComments(now)
        val reports = seedReports(now).map { report ->
            report.copy(commentCount = comments.count { it.reportId == report.id })
        }
        db.comments.value = comments
        db.reports.value = reports
        db.votes.value = setOf(
            "r-hurto" to "u-juan",
            "r-perro" to "u-juan",
            "r-fuga" to "u-juan",
            "r-semaforo" to "u-johan"
        )
        db.notifications.value = seedNotifications(now)
    }

    // ---------------------------------------------------------------- usuarios

    private fun seedUsers(): List<User> = listOf(
        User(
            id = "u-juan", name = "Juan Esteban Cayón", email = "juan.cayon@uqvirtual.edu.co",
            city = "Armenia", address = "Barrio Fundadores", phoneNumber = "+57 300 111 2233",
            points = 340, badges = listOf(Badge.FIRST_REPORT, Badge.FIRST_COMMENT)
        ),
        User(
            id = "u-johan", name = "Johan García", email = "johanc.garciag@uqvirtual.edu.co",
            city = "Armenia", address = "Barrio La Fachada", phoneNumber = "+57 300 000 0000",
            points = 120, badges = listOf(Badge.FIRST_REPORT)
        ),
        User(
            id = "u-andres", name = "Juan Andrés Pérez", email = "juan.perez@uqvirtual.edu.co",
            city = "Armenia", address = "Barrio Centro", points = 185,
            badges = listOf(Badge.FIRST_REPORT)
        ),
        User(
            id = "u-diego", name = "Diego Alvarado", email = "diego.alvarado@correo.co",
            city = "Armenia", address = "Barrio Centro", points = 1240,
            badges = listOf(
                Badge.FIRST_REPORT, Badge.FIRST_COMMENT,
                Badge.TEN_VERIFIED_REPORTS, Badge.TOP_REPORT_OF_MONTH
            )
        ),
        User(
            id = "u-marcela", name = "Marcela Rojas", email = "marcela.rojas@correo.co",
            city = "Armenia", address = "Barrio Puerto Espejo", points = 980,
            badges = listOf(Badge.FIRST_REPORT, Badge.FIRST_COMMENT, Badge.TEN_VERIFIED_REPORTS)
        ),
        User(
            id = "u-laura", name = "Laura Vargas", email = "laura.vargas@correo.co",
            city = "Armenia", address = "Barrio La Castilla", points = 870,
            badges = listOf(Badge.FIRST_REPORT, Badge.FIRST_COMMENT)
        ),
        User(
            id = "u-camilo", name = "Camilo Ríos", email = "camilo.rios@correo.co",
            city = "Armenia", address = "Barrio Los Naranjos", points = 690,
            badges = listOf(Badge.FIRST_REPORT, Badge.FIRST_COMMENT)
        ),
        User(
            id = "u-sofia", name = "Sofía Gómez", email = "sofia.gomez@correo.co",
            city = "Armenia", address = "Barrio La Castilla", points = 515,
            badges = listOf(Badge.FIRST_REPORT)
        ),
        User(
            id = "m-carlos", name = "Carlos Fernández", email = "moderador@entorno.co",
            city = "Armenia", address = "Barrio Centro", role = UserRole.MODERATOR,
            points = 400, badges = listOf(Badge.FIRST_COMMENT)
        )
    ).map { it.copy(createdAt = System.currentTimeMillis() - 60 * DAY) }

    // ---------------------------------------------------------------- reportes

    private data class R(
        val id: String, val title: String, val description: String, val category: ReportCategory,
        val lat: Double, val lon: Double, val status: ReportStatus, val owner: String,
        val minutesAgo: Long, val votes: Int, val rejection: String? = null
    )

    private fun seedReports(now: Long): List<Report> {
        val seeds = listOf(
            R("r-hueco", "Hueco profundo en la Av. Centenario",
                "El hueco lleva más de tres semanas y tiene cerca de 40 cm de profundidad. Dos motos se han caído esta semana. Está justo en el carril derecho, antes del semáforo.",
                ReportCategory.INFRASTRUCTURE, 4.5342, -75.6816, ReportStatus.VERIFIED, "u-juan", 52 * 60L, 47),
            R("r-motos", "Motos sin placa rondando el conjunto",
                "Desde hace varios días pasan motos sin placa a toda velocidad por el conjunto, sobre todo en la noche.",
                ReportCategory.SECURITY, 4.5295, -75.6704, ReportStatus.PENDING, "u-juan", 96 * 60L, 23),
            R("r-basuras", "Basuras acumuladas en la esquina",
                "Hay bolsas de basura acumuladas en la esquina desde hace una semana y ya hay malos olores.",
                ReportCategory.COMMUNITY, 4.5287, -75.6695, ReportStatus.RESOLVED, "u-juan", 14 * 24 * 60L, 64),
            R("r-gato", "Gato encontrado en la 19",
                "Encontré un gato en la calle 19 con carrera 8. Parece perdido.",
                ReportCategory.PETS, 4.5311, -75.6728, ReportStatus.REJECTED, "u-juan", 21 * 24 * 60L, 0,
                rejection = "La foto no corresponde a la categoría. Puedes volver a reportarlo con una imagen clara."),
            R("r-hurto", "Intento de hurto a las 8 p. m.",
                "Dos personas en moto intentaron arrebatar un celular en la carrera 14 con calle 20. No se llevaron nada.",
                ReportCategory.SECURITY, 4.5352, -75.6798, ReportStatus.VERIFIED, "u-marcela", 3 * 60L, 31),
            R("r-perro", "Perro criollo perdido cerca del parque",
                "Perro mediano, color café, con collar azul. Está asustado y se esconde debajo de una banca.",
                ReportCategory.PETS, 4.5448, -75.6738, ReportStatus.VERIFIED, "u-laura", 5 * 60L, 18),
            R("r-poste", "Poste de luz averiado",
                "El poste frente al número 45 lleva 3 días sin encender. La calle queda muy oscura de noche y ya hubo un intento de robo.",
                ReportCategory.INFRASTRUCTURE, 4.5239, -75.6908, ReportStatus.PENDING, "u-camilo", 12L, 4),
            R("r-accidente", "Accidente de moto en la glorieta",
                "Una moto chocó contra el separador de la glorieta. El conductor necesitó ambulancia.",
                ReportCategory.MEDICAL, 4.5331, -75.6823, ReportStatus.VERIFIED, "u-diego", 30 * 60L, 22),
            R("r-jornada", "Jornada de limpieza comunitaria del sábado",
                "Invitamos a los vecinos a limpiar el parque este sábado a las 8 a. m. Llevar guantes y bolsas.",
                ReportCategory.COMMUNITY, 4.5441, -75.6730, ReportStatus.VERIFIED, "u-sofia", 40 * 60L, 9),
            R("r-fuga", "Fuga de agua en la calle 12",
                "Sale agua limpia por una grieta del andén desde ayer. Se está desperdiciando mucha agua.",
                ReportCategory.INFRASTRUCTURE, 4.5424, -75.6886, ReportStatus.VERIFIED, "u-marcela", 20 * 60L, 12),
            R("r-semaforo", "Semáforo dañado en la carrera 14",
                "El semáforo está en rojo intermitente y se forman trancones a la hora pico.",
                ReportCategory.INFRASTRUCTURE, 4.5336, -75.6808, ReportStatus.RESOLVED, "u-diego", 9 * 24 * 60L, 40),
            R("r-robo19", "Intento de robo en la 19",
                "Reportan intento de robo en la calle 19 con carrera 8, hacia las 6 de la tarde.",
                ReportCategory.SECURITY, 4.5176, -75.6762, ReportStatus.PENDING, "u-sofia", 150L, 2),
            R("r-perro2", "Perro perdido cerca al parque",
                "Se encontró un perro mestizo sin collar cerca del parque principal.",
                ReportCategory.PETS, 4.5436, -75.6741, ReportStatus.PENDING, "u-laura", 40L, 1),
            R("r-basura2", "Acumulación de basuras",
                "Basuras acumuladas en la esquina de la cra 14 con calle 12. Pasó el carro recolector y no las recogió.",
                ReportCategory.COMMUNITY, 4.5233, -75.6917, ReportStatus.PENDING, "u-camilo", 60L, 3),
            R("r-ambulancia", "Adulto mayor desmayado en el parque",
                "Un adulto mayor se desmayó en el parque. Los vecinos llamaron a la línea de emergencias.",
                ReportCategory.MEDICAL, 4.5345, -75.6805, ReportStatus.RESOLVED, "u-johan", 5 * 24 * 60L, 15),
            R("r-rinas", "Riñas frecuentes afuera del bar",
                "Todos los fines de semana hay peleas afuera del bar, a veces con botellas.",
                ReportCategory.SECURITY, 4.5417, -75.6893, ReportStatus.VERIFIED, "u-andres", 70 * 60L, 27),
            R("r-ciclovia", "Hueco en la ciclovía",
                "Un hueco en medio de la ciclovía puede causar caídas a quienes van en bicicleta.",
                ReportCategory.INFRASTRUCTURE, 4.5184, -75.6757, ReportStatus.VERIFIED, "u-diego", 30 * 60L + 17, 8),
            R("r-gatos", "Colonia de gatos sin alimento",
                "Hay unos diez gatos en el lote vecino y los vecinos no tienen cómo alimentarlos.",
                ReportCategory.PETS, 4.5240, -75.6905, ReportStatus.VERIFIED, "u-laura", 100 * 60L + 41, 6),
            R("r-ruido", "Obra con ruido a altas horas de la noche",
                "Una obra trabaja con maquinaria hasta pasadas las 11 de la noche y no deja dormir.",
                ReportCategory.COMMUNITY, 4.5298, -75.6711, ReportStatus.VERIFIED, "u-camilo", 55 * 60L + 9, 11)
        )
        val owners = seedUsers().associateBy { it.id }
        return seeds.map { s ->
            val created = now - s.minutesAgo * MIN
            val location = Location(s.lat, s.lon)
            Report(
                id = s.id, title = s.title, description = s.description, category = s.category,
                location = location, status = s.status, imageUrls = emptyList(),
                ownerId = s.owner, ownerName = owners[s.owner]?.name.orEmpty(),
                date = Instant.ofEpochMilli(created).atZone(ZoneId.systemDefault()).toLocalDate(),
                rejectionReason = s.rejection, importantCount = s.votes,
                neighborhood = Geo.nearestNeighborhood(location), createdAt = created
            )
        }
    }

    // ------------------------------------------------------------- comentarios

    private fun seedComments(now: Long): List<Comment> = listOf(
        Comment("c1", "r-hueco", "u-laura", "Laura Vargas",
            "Ayer casi se cae una moto ahí. Ojalá lo arreglen pronto.", now - 2 * DAY),
        Comment("c2", "r-hueco", "m-carlos", "Carlos Fernández",
            "Reporte verificado. Lo enviamos a la secretaría de infraestructura.",
            now - 3 * HOUR, authorRole = UserRole.MODERATOR),
        Comment("c3", "r-hueco", "u-juan", "Juan Esteban Cayón",
            "Adjunté otra foto desde el otro lado de la vía.", now - 45 * MIN),
        Comment("c4", "r-hueco", "u-camilo", "Camilo Ríos",
            "Totalmente de acuerdo, es peligroso a la hora de salida del colegio.",
            now - 5 * MIN, parentCommentId = "c1"),
        Comment("c5", "r-hueco", "u-sofia", "Sofía Gómez",
            "Ya van tres días y siguen sin señalizarlo.", now - 20_000L),
        Comment("c6", "r-hurto", "u-diego", "Diego Alvarado",
            "Yo pasé por ahí a esa hora, tengan cuidado con el celular.", now - 2 * HOUR),
        Comment("c7", "r-hurto", "m-carlos", "Carlos Fernández",
            "Avisamos a la policía del cuadrante. Gracias por reportar.", now - 90 * MIN,
            authorRole = UserRole.MODERATOR),
        Comment("c8", "r-perro", "u-marcela", "Marcela Rojas",
            "Creo que es el perro de la vecina de la 14, voy a preguntarle.", now - 4 * HOUR),
        Comment("c9", "r-basuras", "u-marcela", "Marcela Rojas",
            "Ya pasó el camión, quedó limpio. Gracias a todos.", now - 2 * DAY)
    )

    // ----------------------------------------------------------- notificaciones

    private fun seedNotifications(now: Long): List<AppNotification> = listOf(
        AppNotification("n1", "u-juan", NotificationType.REPORT_VERIFIED, "Tu reporte fue verificado.",
            "«Hueco profundo en la Av. Centenario» ya es visible para todos.", "r-hueco", false, now - 20 * MIN),
        AppNotification("n2", "u-juan", NotificationType.NEW_COMMENT, "Laura Vargas comentó",
            "en tu reporte «Hueco profundo en la Av. Centenario».", "r-hueco", false, now - 60 * MIN),
        AppNotification("n3", "u-juan", NotificationType.REPORT_RESOLVED, "Tu reporte fue resuelto.",
            "«Basuras acumuladas en la esquina» se marcó como resuelto.", "r-basuras", false, now - 2 * HOUR),
        AppNotification("n4", "u-juan", NotificationType.LEVEL_UP, "Nuevo nivel: Guardián.",
            "Llegaste a 340 puntos. ¡Sigue reportando!", null, true, now - DAY),
        AppNotification("n5", "u-juan", NotificationType.BADGE_EARNED, "Nueva insignia: Primer aporte",
            "Hiciste tu primer comentario.", null, true, now - 2 * DAY),
        AppNotification("n6", "u-juan", NotificationType.REPORT_REJECTED, "Reporte no publicado.",
            "La foto no corresponde a la categoría. Puedes volver a reportarlo con una imagen clara.",
            "r-gato", true, now - 21 * DAY),
        AppNotification("n7", "u-johan", NotificationType.REPORT_RESOLVED, "Tu reporte fue resuelto.",
            "«Adulto mayor desmayado en el parque» se marcó como resuelto.", "r-ambulancia", false, now - 3 * HOUR)
    )
}
