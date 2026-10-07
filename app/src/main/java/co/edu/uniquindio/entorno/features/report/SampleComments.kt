package co.edu.uniquindio.entorno.features.report

import co.edu.uniquindio.entorno.domain.model.Comment

private const val MINUTE_MS = 60_000L
private const val HOUR_MS = 60 * MINUTE_MS
private const val DAY_MS = 24 * HOUR_MS

/** Datos de ejemplo para previews: 5 comentarios del reporte "1" de SampleReports. */
object SampleComments {
    const val REPORT_ID = "1"
    const val CURRENT_USER_ID = "user-me"
    const val MODERATOR_ID = "mod-1"

    val moderatorIds: Set<String> = setOf(MODERATOR_ID)

    private val now = System.currentTimeMillis()

    val comments: List<Comment> = listOf(
        Comment(
            id = "c1",
            reportId = REPORT_ID,
            authorId = "user-laura",
            authorName = "Laura Martínez",
            text = "Ayer casi se cae una moto ahí. Ojalá lo arreglen pronto.",
            createdAt = now - 2 * DAY_MS
        ),
        Comment(
            id = "c2",
            reportId = REPORT_ID,
            authorId = MODERATOR_ID,
            authorName = "Equipo de moderación",
            text = "Reporte verificado. Lo enviamos a la secretaría de infraestructura.",
            createdAt = now - 3 * HOUR_MS
        ),
        Comment(
            id = "c3",
            reportId = REPORT_ID,
            authorId = CURRENT_USER_ID,
            authorName = "Juan Cayón",
            text = "Adjunté otra foto desde el otro lado de la vía.",
            createdAt = now - 45 * MINUTE_MS
        ),
        Comment(
            id = "c4",
            reportId = REPORT_ID,
            authorId = "user-camilo",
            authorName = "Camilo Ríos",
            text = "Totalmente de acuerdo, es peligroso a la hora de salida del colegio.",
            createdAt = now - 5 * MINUTE_MS,
            parentCommentId = "c1"
        ),
        Comment(
            id = "c5",
            reportId = REPORT_ID,
            authorId = "user-sofia",
            authorName = "Sofía Gómez",
            text = "Ya van tres días y siguen sin señalizarlo.",
            createdAt = now - 20_000L
        )
    )
}
