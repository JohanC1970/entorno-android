package co.edu.uniquindio.entorno.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.entorno.core.theme.*

@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    onEditarClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    PerfilContent(
        state = state,
        onEditarClick = onEditarClick
    )
}

@Composable
private fun PerfilContent(
    state: PerfilUiState,
    onEditarClick: () -> Unit
) {
    val usuario = state.usuario ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Encabezado: avatar, nombre, ubicación, botón editar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar circular con iniciales
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(ContenedorPrimarioClaro),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = usuario.name.split(" ")
                            .take(2)
                            .map { it.first().uppercaseChar() }
                            .joinToString(""),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimarioClaro
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = usuario.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${usuario.address}  ·  ${usuario.city}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onEditarClick) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar perfil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Tarjeta de nivel actual + progreso
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ContenedorPrimarioClaro),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.StarOutline,
                                    contentDescription = "Nivel",
                                    tint = PrimarioClaro,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "NIVEL ACTUAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = state.nivel.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${usuario.points}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimarioClaro
                            )
                            Text(
                                text = "puntos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { state.progresoNivel },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PrimarioClaro,
                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Faltan ${state.puntosParaSiguienteNivel} puntos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val siguienteLabel = state.siguienteNivel?.label ?: "Máximo"
                        Text(
                            text = "Siguiente: $siguienteLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Estadísticas (3 cards en fila)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EstadisticaCard(
                    valor = "${state.reportesTotal}",
                    etiqueta = "Reportes",
                    modifier = Modifier.weight(1f)
                )
                EstadisticaCard(
                    valor = "${state.verificados}",
                    etiqueta = "Verificados",
                    modifier = Modifier.weight(1f)
                )
                EstadisticaCard(
                    valor = "${state.votosDados}",
                    etiqueta = "Votos dados",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Insignias
        item {
            Text(
                text = "Insignias",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, bottom = 12.dp)
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.insignias) { insignia ->
                    InsigniaItem(insignia)
                }
            }
        }

        // Actividad reciente
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Actividad reciente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, bottom = 12.dp)
            )
        }

        items(state.actividadReciente, key = { it.id }) { actividad ->
            ActividadCard(
                actividad = actividad,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun EstadisticaCard(
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InsigniaItem(insignia: InsigniaPerfil) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        val bgColor = if (insignia.desbloqueada) {
            when (insignia.badge) {
                co.edu.uniquindio.entorno.domain.model.Badge.FIRST_REPORT ->
                    FondoEstadoEnVerificacion
                co.edu.uniquindio.entorno.domain.model.Badge.FIRST_COMMENT ->
                    FondoEstadoVerificado
                co.edu.uniquindio.entorno.domain.model.Badge.TEN_VERIFIED_REPORTS ->
                    ContenedorPrimarioClaro
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        } else {
            MaterialTheme.colorScheme.surface
        }

        val iconColor = if (insignia.desbloqueada) {
            when (insignia.badge) {
                co.edu.uniquindio.entorno.domain.model.Badge.FIRST_REPORT ->
                    TextoEstadoEnVerificacion
                co.edu.uniquindio.entorno.domain.model.Badge.FIRST_COMMENT ->
                    TextoEstadoVerificado
                co.edu.uniquindio.entorno.domain.model.Badge.TEN_VERIFIED_REPORTS ->
                    PrimarioClaro
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        }

        val icon: ImageVector = if (insignia.desbloqueada) {
            when (insignia.badge) {
                co.edu.uniquindio.entorno.domain.model.Badge.FIRST_REPORT ->
                    Icons.Outlined.Warning
                co.edu.uniquindio.entorno.domain.model.Badge.FIRST_COMMENT ->
                    Icons.Outlined.Check
                co.edu.uniquindio.entorno.domain.model.Badge.TEN_VERIFIED_REPORTS ->
                    Icons.Outlined.Flag
                else -> Icons.Outlined.StarOutline
            }
        } else {
            Icons.Outlined.Lock
        }

        val borderModifier = if (!insignia.desbloqueada) {
            Modifier.border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            )
        } else Modifier

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .then(borderModifier)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = insignia.nombre,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = insignia.nombre,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun ActividadCard(
    actividad: ActividadReciente,
    modifier: Modifier = Modifier
) {
    val (icon, bgColor, iconColor) = when (actividad.icono) {
        ActividadTipo.REPORTE_VERIFICADO -> Triple(
            Icons.Outlined.Check,
            FondoEstadoVerificado,
            TextoEstadoVerificado
        )
        ActividadTipo.VOTOS_DADOS -> Triple(
            Icons.Outlined.Flag,
            ContenedorPrimarioClaro,
            PrimarioClaro
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = actividad.texto,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = actividad.texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = actividad.tiempo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PerfilScreenPreview() {
    EntornoTheme {
        PerfilContent(
            state = PerfilUiState(),
            onEditarClick = {}
        )
    }
}
