package co.edu.uniquindio.entorno.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.domain.model.Severity

data class CategoryColors(
    val icon: Color,
    val chipBackground: Color,
    val chipText: Color
)

data class StatusColors(
    val background: Color,
    val text: Color
)

// Por ahora los colores de dominio son iguales en modo claro y oscuro: en oscuro los
// chips quedan claros sobre el fondo. Si se agregan variantes oscuras en Color.kt,
// se selecciona aquí con isSystemInDarkTheme().
@Composable
fun categoryColors(category: ReportCategory): CategoryColors = when (category) {
    ReportCategory.SECURITY -> CategoryColors(
        IconoCategoriaSeguridad, FondoChipCategoriaSeguridad, TextoCategoriaSeguridad
    )
    ReportCategory.MEDICAL -> CategoryColors(
        IconoCategoriaEmergencias, FondoChipCategoriaEmergencias, TextoCategoriaEmergencias
    )
    ReportCategory.INFRASTRUCTURE -> CategoryColors(
        IconoCategoriaInfraestructura, FondoChipCategoriaInfraestructura, TextoCategoriaInfraestructura
    )
    ReportCategory.PETS -> CategoryColors(
        IconoCategoriaMascotas, FondoChipCategoriaMascotas, TextoCategoriaMascotas
    )
    ReportCategory.COMMUNITY -> CategoryColors(
        IconoCategoriaComunidad, FondoChipCategoriaComunidad, TextoCategoriaComunidad
    )
}

// PENDING usa los colores de "En verificación" (la vista del ciudadano).
@Composable
fun statusColors(status: ReportStatus): StatusColors = when (status) {
    ReportStatus.PENDING -> StatusColors(FondoEstadoEnVerificacion, TextoEstadoEnVerificacion)
    ReportStatus.VERIFIED -> StatusColors(FondoEstadoVerificado, TextoEstadoVerificado)
    ReportStatus.REJECTED -> StatusColors(FondoEstadoRechazado, TextoEstadoRechazado)
    ReportStatus.RESOLVED -> StatusColors(FondoEstadoResuelto, TextoEstadoResuelto)
}

@Composable
fun severityColor(severity: Severity): Color = when (severity) {
    Severity.LOW -> SeveridadBaja
    Severity.MEDIUM -> SeveridadMedia
    Severity.HIGH, Severity.CRITICAL -> SeveridadAlta
}
