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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.core.theme.*
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.core.component.StatusChip
import co.edu.uniquindio.entorno.core.component.labelForModerator
import co.edu.uniquindio.entorno.core.util.toRelativeTime
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.uniquindio.entorno.core.component.icon

@Composable
fun PanelModeradorScreen(
    onReporteClick: (String) -> Unit = {},
    viewModel: PanelModeradorViewModel = hiltViewModel()
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
                val pendingColors = statusColors(ReportStatus.PENDING)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(pendingColors.background)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${state.totalPendientes} pendientes",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = pendingColors.text
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
                    text = "${reporte.category.label} · ${reporte.createdAt.toRelativeTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                StatusChip(ReportStatus.PENDING, label = ReportStatus.PENDING.labelForModerator())
            }
        }
    }
}

@Composable
private fun ThumbnailCategoria(category: ReportCategory) {
    val colors = categoryColors(category)
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.chipBackground),
        contentAlignment = Alignment.Center
    ) {
        Icon(category.icon(), contentDescription = category.label, tint = colors.icon)
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
