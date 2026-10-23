package co.edu.uniquindio.entorno.features.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.uniquindio.entorno.core.component.ScreenHeader
import co.edu.uniquindio.entorno.core.theme.MapaCalor
import kotlin.math.roundToInt

@Composable
fun HeatMapScreen(
    onBack: () -> Unit,
    viewModel: HeatMapViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val mapState = rememberMapViewState()

    Box(modifier = Modifier.fillMaxSize()) {
        MapSurface(
            modifier = Modifier.fillMaxSize(),
            state = mapState,
            initialZoom = 1.3f,
            heatLocations = state.locations
        ) {
            state.hotspots.forEach { hotspot ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 3.dp,
                    modifier = Modifier.align(Alignment.TopStart).mapOffset(mapState, hotspot.location, 0)
                ) {
                    Text(
                        text = "${hotspot.name} · ${hotspot.count}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp
            ) {
                ScreenHeader(
                    title = "Mapa de calor",
                    subtitle = "Armenia · últimos 30 días · ${state.total} reportes",
                    onBack = onBack
                )
            }
            CategoryFilterRow(selected = state.category, onSelected = viewModel::onCategorySelected)
        }

        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Franja horaria",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = state.windowLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = state.allDay,
                        onClick = { viewModel.onAllDayChange(!state.allDay) },
                        label = { Text("Todo el día") }
                    )
                    Slider(
                        value = state.startHour.toFloat(),
                        onValueChange = { viewModel.onStartHourChange(it.roundToInt()) },
                        valueRange = 0f..23f,
                        steps = 22,
                        enabled = !state.allDay,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = "Concentración de reportes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(MapaCalor.copy(alpha = 0.15f), MapaCalor)))
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Baja", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Alta", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (state.total == 0) {
                    Text(
                        text = "No hay reportes en esta franja. Prueba con otra hora o categoría.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
