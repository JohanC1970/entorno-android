package co.edu.uniquindio.entorno.features.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.core.component.ScreenHeader
import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.domain.model.Location

/** Selector de ubicación para un reporte: se toca el mapa para mover el pin. */
@Composable
fun LocationPickerScreen(
    initial: Location,
    onBack: () -> Unit,
    onConfirm: (Location) -> Unit
) {
    var picked by remember { mutableStateOf(initial) }
    val mapState = rememberMapViewState()
    val pinHeight = 44.dp
    val halfPinPx = with(LocalDensity.current) { (pinHeight / 2).roundToPx() }

    Box(modifier = Modifier.fillMaxSize()) {
        MapSurface(
            modifier = Modifier.fillMaxSize(),
            state = mapState,
            initialCenter = initial,
            initialZoom = 2.5f,
            onMapTap = { picked = it }
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Ubicación elegida",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .mapOffset(mapState, picked, halfPinPx)
                    .offset(y = -(pinHeight / 2))
                    .size(pinHeight)
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(12.dp)
        ) {
            ScreenHeader(title = "Elegir ubicación", subtitle = "Toca el mapa para mover el pin", onBack = onBack)
        }

        SmallFloatingActionButton(
            onClick = {
                picked = initial
                mapState.centerOn(initial, 2.5f)
            },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp)
        ) {
            Icon(Icons.Filled.MyLocation, contentDescription = "Volver a mi ubicación")
        }

        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.navigationBarsPadding().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = Geo.nearestNeighborhood(picked),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = Geo.format(picked),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { onConfirm(picked) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Usar esta ubicación", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
