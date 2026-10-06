package co.edu.uniquindio.entorno.features.notifications

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.core.theme.*
import co.edu.uniquindio.entorno.domain.model.AppNotification
import co.edu.uniquindio.entorno.domain.model.NotificationType

@Composable
fun NotificacionesScreen(
    viewModel: NotificacionesViewModel,
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    NotificacionesContent(
        state = state,
        onFiltroClick = viewModel::cambiarFiltro,
        onMarcarLeidas = viewModel::marcarTodasComoLeidas,
        onBackClick = onBackClick
    )
}

@Composable
private fun NotificacionesContent(
    state: NotificacionesUiState,
    onFiltroClick: (NotificationFilter) -> Unit,
    onMarcarLeidas: () -> Unit,
    onBackClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Encabezado con flecha atrás, título y "Marcar leídas"
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 20.dp, top = 40.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Notificaciones",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Marcar leídas",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onMarcarLeidas)
                )
            }
        }

        // Chips de filtro: Todas / Sin leer (con badge de count)
        item {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationFilter.entries.forEach { filtro ->
                    FilterChip(
                        selected = state.filtroActual == filtro,
                        onClick = { onFiltroClick(filtro) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = filtro.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (state.filtroActual == filtro)
                                        FontWeight.SemiBold else FontWeight.Normal
                                )
                                if (filtro == NotificationFilter.SIN_LEER && state.sinLeerCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${state.sinLeerCount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            enabled = true,
                            selected = state.filtroActual == filtro
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // Lista de notificaciones
        items(state.notificacionesFiltradas, key = { it.id }) { notificacion ->
            NotificacionCard(
                notificacion = notificacion,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun NotificacionCard(
    notificacion: AppNotification,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            // Icono del tipo de notificación
            NotificacionIcono(notificacion.type)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val annotatedText = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(notificacion.title)
                    }
                    append(" ")
                    append(notificacion.message)
                }

                Text(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatearTiempoNotificacion(notificacion.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Indicador de no leída
            if (!notificacion.read) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun NotificacionIcono(type: NotificationType) {
    val (icon: ImageVector, bgColor: Color, iconColor: Color) = when (type) {
        NotificationType.REPORT_VERIFIED -> Triple(
            Icons.Outlined.Check,
            FondoEstadoVerificado,
            TextoEstadoVerificado
        )
        NotificationType.REPORT_REJECTED -> Triple(
            Icons.Outlined.Close,
            FondoEstadoRechazado,
            TextoEstadoRechazado
        )
        NotificationType.REPORT_RESOLVED -> Triple(
            Icons.Outlined.Check,
            FondoEstadoResuelto,
            TextoEstadoResuelto
        )
        NotificationType.NEW_COMMENT -> Triple(
            Icons.Outlined.Comment,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        NotificationType.BADGE_EARNED -> Triple(
            Icons.Outlined.Flag,
            ContenedorPrimarioClaro,
            PrimarioClaro
        )
        NotificationType.LEVEL_UP -> Triple(
            Icons.Outlined.StarOutline,
            FondoEstadoEnVerificacion,
            TextoEstadoEnVerificacion
        )
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type.name,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

private fun formatearTiempoNotificacion(createdAt: Long): String {
    val diff = System.currentTimeMillis() - createdAt
    val minutos = diff / (60 * 1000)
    val horas = diff / (60 * 60 * 1000)
    val dias = diff / (24 * 60 * 60 * 1000)
    val semanas = dias / 7

    return when {
        minutos < 60 -> "hace $minutos min"
        horas < 24 -> "hace $horas h"
        dias == 1L -> "ayer"
        dias < 7 -> "hace $dias días"
        semanas < 4 -> "hace $semanas semanas"
        else -> "hace ${dias / 30} meses"
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificacionesScreenPreview() {
    EntornoTheme {
        NotificacionesContent(
            state = NotificacionesUiState(),
            onFiltroClick = {},
            onMarcarLeidas = {},
            onBackClick = {}
        )
    }
}
