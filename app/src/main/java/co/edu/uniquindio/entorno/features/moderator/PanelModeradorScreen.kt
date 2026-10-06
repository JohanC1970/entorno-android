package co.edu.uniquindio.entorno.features.moderator

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.core.theme.*
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.features.report.EstadoPill
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun PanelModeradorScreen(
    viewModel: PanelModeradorViewModel,
    onReporteClick: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    PanelModeradorContent(
        state = state,
        onReporteClick = onReporteClick
    )
}

@Composable
private fun PanelModeradorContent(
    state: PanelModeradorUiState,
    onReporteClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Título
        item {
            Text(
                text = "Publicaciones pendientes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 12.dp)
            )
        }

        // Badge de pendientes + botón filtro
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(FondoEstadoEnVerificacion)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${state.totalPendientes} pendientes",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextoEstadoEnVerificacion
                    )
                }

                IconButton(onClick = { /* Filtro */ }) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = "Filtrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Lista de reportes pendientes
        items(state.reportesPendientes, key = { it.id }) { reporte ->
            ReportePendienteCard(
                reporte = reporte,
                onClick = { onReporteClick(reporte.id) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun ReportePendienteCard(
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
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail con gradiente de la categoría
            ThumbnailCategoria(reporte.category)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reporte.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${reporte.category.label} · ${formatearTiempoModerador(reporte.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                EstadoPill(ReportStatus.PENDING)
            }
        }
    }
}

@Composable
private fun ThumbnailCategoria(category: ReportCategory) {
    val gradientColors = when (category) {
        ReportCategory.INFRASTRUCTURE -> listOf(
            Color(0xFFD4B8FF), Color(0xFFA8E6CF)
        )
        ReportCategory.PETS -> listOf(
            Color(0xFFFFD4A8), Color(0xFFFFA8C8)
        )
        ReportCategory.COMMUNITY -> listOf(
            Color(0xFFA8E6CF), Color(0xFFD4FFB8)
        )
        ReportCategory.SECURITY -> listOf(
            Color(0xFFFFB8A8), Color(0xFFFFA8D4)
        )
        ReportCategory.MEDICAL -> listOf(
            Color(0xFFFFB8C8), Color(0xFFD4A8FF)
        )
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(gradientColors))
    )
}

private fun formatearTiempoModerador(date: LocalDate): String {
    val dias = ChronoUnit.DAYS.between(date, LocalDate.now())
    val minutos = dias * 24 * 60 // Approximation for display

    return when {
        dias == 0L -> "hace 12 min" // Datos de prueba
        dias == 1L -> "ayer"
        dias < 7 -> "hace $dias días"
        else -> "hace ${dias / 7} semanas"
    }
}

@Preview(showBackground = true)
@Composable
private fun PanelModeradorScreenPreview() {
    EntornoTheme {
        PanelModeradorContent(
            state = PanelModeradorUiState(totalPendientes = 7),
            onReporteClick = {}
        )
    }
}
