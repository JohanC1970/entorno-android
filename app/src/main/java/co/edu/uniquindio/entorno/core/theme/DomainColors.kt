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

private const val DARK_BACKGROUND_ALPHA = 0.2f

@Composable
fun categoryColors(category: ReportCategory): CategoryColors {
    if (!LocalDarkTheme.current) {
        return when (category) {
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
    }
    val accent = when (category) {
        ReportCategory.SECURITY -> IconoCategoriaSeguridadOscuro
        ReportCategory.MEDICAL -> IconoCategoriaEmergenciasOscuro
        ReportCategory.INFRASTRUCTURE -> IconoCategoriaInfraestructuraOscuro
        ReportCategory.PETS -> IconoCategoriaMascotasOscuro
        ReportCategory.COMMUNITY -> IconoCategoriaComunidadOscuro
    }
    return CategoryColors(icon = accent, chipBackground = accent.copy(alpha = DARK_BACKGROUND_ALPHA), chipText = accent)
}

// PENDING usa los colores de "En verificación" (la vista del ciudadano).
@Composable
fun statusColors(status: ReportStatus): StatusColors {
    if (!LocalDarkTheme.current) {
        return when (status) {
            ReportStatus.PENDING -> StatusColors(FondoEstadoEnVerificacion, TextoEstadoEnVerificacion)
            ReportStatus.VERIFIED -> StatusColors(FondoEstadoVerificado, TextoEstadoVerificado)
            ReportStatus.REJECTED -> StatusColors(FondoEstadoRechazado, TextoEstadoRechazado)
            ReportStatus.RESOLVED -> StatusColors(FondoEstadoResuelto, TextoEstadoResuelto)
        }
    }
    val text = when (status) {
        ReportStatus.PENDING -> TextoEstadoEnVerificacionOscuro
        ReportStatus.VERIFIED -> TextoEstadoVerificadoOscuro
        ReportStatus.REJECTED -> TextoEstadoRechazadoOscuro
        ReportStatus.RESOLVED -> TextoEstadoResueltoOscuro
    }
    return StatusColors(background = text.copy(alpha = DARK_BACKGROUND_ALPHA), text = text)
}

@Composable
fun severityColor(severity: Severity): Color {
    val dark = LocalDarkTheme.current
    return when (severity) {
        Severity.LOW -> if (dark) SeveridadBajaOscura else SeveridadBaja
        Severity.MEDIUM -> if (dark) SeveridadMediaOscura else SeveridadMedia
        Severity.HIGH, Severity.CRITICAL -> if (dark) SeveridadAltaOscura else SeveridadAlta
    }
}