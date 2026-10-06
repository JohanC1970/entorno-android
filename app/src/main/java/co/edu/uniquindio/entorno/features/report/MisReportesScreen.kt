package co.edu.uniquindio.entorno.features.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.core.theme.*
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun MisReportesScreen(
    viewModel: MisReportesViewModel,
    onReporteClick: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    MisReportesContent(
        state = state,
        onFiltroClick = viewModel::cambiarFiltro,
        onReporteClick = onReporteClick
    )
}

@Composable
private fun MisReportesContent(
    state: MisReportesUiState,
    onFiltroClick: (ReportFilter) -> Unit,
    onReporteClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Encabezado: título y total
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Mis reportes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${state.totalReportes} en total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Chips de filtro
        item {
            LazyRow(
                modifier = Modifier.padding(bottom = 12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ReportFilter.entries) { filtro ->
                    FilterChip(
                        selected = state.filtroActual == filtro,
                        onClick = { onFiltroClick(filtro) },
                        label = {
                            Text(
                                text = filtro.label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (state.filtroActual == filtro) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            enabled = true,
                            selected = state.filtroActual == filtro
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // Lista de reportes
        items(state.reportesFiltrados, key = { it.id }) { reporte ->
            ReporteCard(
                reporte = reporte,
                onClick = { onReporteClick(reporte.id) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun ReporteCard(
    reporte: Report,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icono de categoría con fondo
            CategoriaIcono(reporte.category)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reporte.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${reporte.category.label} · ${formatearTiempo(reporte.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    EstadoPill(reporte.status)

                    if (reporte.status == ReportStatus.REJECTED && reporte.rejectionReason != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reporte.rejectionReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (reporte.importantCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = "Votos importantes",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${reporte.importantCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriaIcono(category: ReportCategory) {
    val (icon, bgColor, iconColor) = when (category) {
        ReportCategory.INFRASTRUCTURE -> Triple(
            Icons.Outlined.Construction,
            FondoChipCategoriaInfraestructura,
            IconoCategoriaInfraestructura
        )
        ReportCategory.SECURITY -> Triple(
            Icons.Outlined.Shield,
            FondoChipCategoriaSeguridad,
            IconoCategoriaSeguridad
        )
        ReportCategory.COMMUNITY -> Triple(
            Icons.Outlined.Groups,
            FondoChipCategoriaComunidad,
            IconoCategoriaComunidad
        )
        ReportCategory.PETS -> Triple(
            Icons.Outlined.Pets,
            FondoChipCategoriaMascotas,
            IconoCategoriaMascotas
        )
        ReportCategory.MEDICAL -> Triple(
            Icons.Outlined.HealthAndSafety,
            FondoChipCategoriaEmergencias,
            IconoCategoriaEmergencias
        )
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.label,
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun EstadoPill(status: ReportStatus) {
    val (text, bgColor, textColor) = when (status) {
        ReportStatus.PENDING -> Triple("En verificación", FondoEstadoEnVerificacion, TextoEstadoEnVerificacion)
        ReportStatus.VERIFIED -> Triple("Verificado", FondoEstadoVerificado, TextoEstadoVerificado)
        ReportStatus.REJECTED -> Triple("Rechazado", FondoEstadoRechazado, TextoEstadoRechazado)
        ReportStatus.RESOLVED -> Triple("Resuelto", FondoEstadoResuelto, TextoEstadoResuelto)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

private fun formatearTiempo(date: LocalDate): String {
    val dias = ChronoUnit.DAYS.between(date, LocalDate.now())
    return when {
        dias == 0L -> "hoy"
        dias == 1L -> "ayer"
        dias < 7 -> "hace $dias días"
        dias < 30 -> "hace ${dias / 7} semanas"
        else -> "hace ${dias / 30} meses"
    }
}

@Preview(showBackground = true)
@Composable
private fun MisReportesScreenPreview() {
    EntornoTheme {
        MisReportesContent(
            state = MisReportesUiState(
                totalReportes = 12,
                filtroActual = ReportFilter.TODOS,
                reportes = emptyList()
            ),
            onFiltroClick = {},
            onReporteClick = {}
        )
    }
}
