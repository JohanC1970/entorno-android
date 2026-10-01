package co.edu.uniquindio.entorno.domain.model

/** Umbrales de ejemplo: ajustarlos entre el grupo. */
enum class UserLevel(val label: String, val minPoints: Int) {
    NOVICE("Novato", 0),
    COLLABORATOR("Colaborador", 100),
    GUARDIAN("Guardián", 300),
    COMMUNITY_HERO("Héroe Comunitario", 700);

    companion object {
        fun fromPoints(points: Int): UserLevel =
            entries.last { points >= it.minPoints }
    }
}

enum class Badge(val label: String, val description: String) {
    FIRST_REPORT("Primer reporte", "Creaste tu primer reporte"),
    FIRST_COMMENT("Primer aporte", "Hiciste tu primer comentario"),
    TEN_VERIFIED_REPORTS("Ciudadano confiable", "10 reportes verificados"),
    TOP_REPORT_OF_MONTH("Destacado del mes", "Tu reporte fue el más votado del mes")
}

/** Puntos por acción (se usan en la Fase 6). */
object PointsRule {
    const val CREATE_REPORT = 10
    const val COMMENT = 2
    const val RECEIVE_IMPORTANT_VOTE = 1
    const val REPORT_VERIFIED = 15
}