package co.edu.uniquindio.entorno.features.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.core.component.icon
import co.edu.uniquindio.entorno.core.theme.MapaAgua
import co.edu.uniquindio.entorno.core.theme.MapaCalor
import co.edu.uniquindio.entorno.core.theme.MapaParque
import co.edu.uniquindio.entorno.core.theme.MapaUsuario
import co.edu.uniquindio.entorno.core.theme.categoryColors
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import kotlin.math.max
import kotlin.math.roundToInt

/** Límites del mapa esquemático de Armenia (Quindío). */
object MapGeo {
    const val MIN_LAT = 4.508
    const val MAX_LAT = 4.556
    const val MIN_LON = -75.704
    const val MAX_LON = -75.656
    const val METERS_PER_DEGREE = 111_320.0
    const val MAX_SCALE = 6f

    val center = Location((MIN_LAT + MAX_LAT) / 2, (MIN_LON + MAX_LON) / 2)
}

/** Estado del mapa: zoom y desplazamiento. Convierte coordenadas a píxeles y viceversa. */
@Stable
class MapViewState {
    var scale by mutableStateOf(1f)
        private set
    var pan by mutableStateOf(Offset.Zero)
        private set
    var size by mutableStateOf(IntSize.Zero)

    val isReady: Boolean get() = size.width > 0 && size.height > 0

    /** Píxeles por grado con el zoom actual ("cover": el mapa siempre llena la pantalla). */
    val pxPerDegree: Double
        get() {
            if (!isReady) return 1.0
            val base = max(
                size.width / (MapGeo.MAX_LON - MapGeo.MIN_LON),
                size.height / (MapGeo.MAX_LAT - MapGeo.MIN_LAT)
            )
            return base * scale
        }

    fun project(location: Location): Offset {
        val cx = size.width / 2f
        val cy = size.height / 2f
        return Offset(
            x = cx + ((location.longitude - MapGeo.center.longitude) * pxPerDegree).toFloat() + pan.x,
            y = cy - ((location.latitude - MapGeo.center.latitude) * pxPerDegree).toFloat() + pan.y
        )
    }

    fun unproject(point: Offset): Location {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val lon = MapGeo.center.longitude + (point.x - cx - pan.x) / pxPerDegree
        val lat = MapGeo.center.latitude - (point.y - cy - pan.y) / pxPerDegree
        return Location(latitude = lat, longitude = lon)
    }

    fun zoomBy(factor: Float, focus: Offset? = null) {
        if (!isReady) return
        val center = Offset(size.width / 2f, size.height / 2f)
        val anchor = focus ?: center
        val newScale = (scale * factor).coerceIn(1f, MapGeo.MAX_SCALE)
        val ratio = newScale / scale
        pan = (anchor - center) - (anchor - center - pan) * ratio
        scale = newScale
        clampPan()
    }

    fun panBy(delta: Offset) {
        pan += delta
        clampPan()
    }

    /** Centra el mapa en [location] (con el zoom actual). */
    fun centerOn(location: Location, zoom: Float? = null) {
        if (!isReady) return
        if (zoom != null) scale = zoom.coerceIn(1f, MapGeo.MAX_SCALE)
        val dx = ((location.longitude - MapGeo.center.longitude) * pxPerDegree).toFloat()
        val dy = ((location.latitude - MapGeo.center.latitude) * pxPerDegree).toFloat()
        pan = Offset(-dx, dy)
        clampPan()
    }

    private fun clampPan() {
        val worldW = ((MapGeo.MAX_LON - MapGeo.MIN_LON) * pxPerDegree).toFloat()
        val worldH = ((MapGeo.MAX_LAT - MapGeo.MIN_LAT) * pxPerDegree).toFloat()
        val maxX = max(0f, (worldW - size.width) / 2f)
        val maxY = max(0f, (worldH - size.height) / 2f)
        pan = Offset(pan.x.coerceIn(-maxX, maxX), pan.y.coerceIn(-maxY, maxY))
    }
}

@Composable
fun rememberMapViewState(): MapViewState = remember { MapViewState() }

// Elementos fijos del plano esquemático (coordenadas aproximadas).
private data class Block(val minLat: Double, val maxLat: Double, val minLon: Double, val maxLon: Double)

private val parks = listOf(
    Block(4.5325, 4.5350, -75.6830, -75.6800),
    Block(4.5430, 4.5455, -75.6755, -75.6720),
    Block(4.5205, 4.5225, -75.6925, -75.6895)
)

private val riverPoints = listOf(
    Location(4.5560, -75.6980), Location(4.5470, -75.6900), Location(4.5390, -75.6930),
    Location(4.5280, -75.6880), Location(4.5190, -75.6960), Location(4.5080, -75.6890)
)

private const val STREET_STEP = 0.0036

/**
 * Mapa esquemático con zoom y desplazamiento, sin SDK externo.
 * Dibuja calles, parques y río; opcionalmente el radio de alertas, la ubicación del usuario
 * y una capa de calor. Lo que va en [content] (marcadores, etiquetas) se ubica con
 * [MapViewState.project].
 */
@Composable
fun MapSurface(
    modifier: Modifier = Modifier,
    state: MapViewState = rememberMapViewState(),
    initialCenter: Location? = null,
    initialZoom: Float = 1.6f,
    userLocation: Location? = null,
    alertRadiusMeters: Double? = null,
    heatLocations: List<Location> = emptyList(),
    onMapTap: (Location) -> Unit = {},
    content: @Composable BoxScope.() -> Unit = {}
) {
    val background = MaterialTheme.colorScheme.surfaceVariant
    val streetColor = MaterialTheme.colorScheme.outlineVariant
    val primary = MaterialTheme.colorScheme.primary
    val currentOnMapTap by rememberUpdatedState(onMapTap)

    LaunchedEffect(state.isReady) {
        if (state.isReady) state.centerOn(initialCenter ?: MapGeo.center, initialZoom)
    }

    Box(
        modifier = modifier
            .background(background)
            .onSizeChanged { state.size = it }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, panChange, zoomChange, _ ->
                    state.zoomBy(zoomChange, centroid)
                    state.panBy(panChange)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset -> currentOnMapTap(state.unproject(offset)) }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Río
            val river = Path()
            riverPoints.forEachIndexed { index, point ->
                val p = state.project(point)
                if (index == 0) river.moveTo(p.x, p.y) else river.lineTo(p.x, p.y)
            }
            drawPath(
                path = river, color = MapaAgua,
                style = Stroke(width = 14.dp.toPx() * state.scale.coerceAtMost(3f))
            )

            // Parques
            parks.forEach { block ->
                val topLeft = state.project(Location(block.maxLat, block.minLon))
                val bottomRight = state.project(Location(block.minLat, block.maxLon))
                drawRect(
                    color = MapaParque.copy(alpha = 0.85f), topLeft = topLeft,
                    size = Size(bottomRight.x - topLeft.x, bottomRight.y - topLeft.y)
                )
            }

            // Calles: una retícula con avenidas principales cada cuatro cuadras
            var i = 0
            var lon = MapGeo.MIN_LON
            while (lon <= MapGeo.MAX_LON) {
                val major = i % 4 == 0
                drawLine(
                    color = streetColor.copy(alpha = if (major) 0.9f else 0.5f),
                    start = state.project(Location(MapGeo.MIN_LAT, lon)),
                    end = state.project(Location(MapGeo.MAX_LAT, lon)),
                    strokeWidth = (if (major) 3.dp else 1.2.dp).toPx()
                )
                lon += STREET_STEP
                i++
            }
            i = 0
            var lat = MapGeo.MIN_LAT
            while (lat <= MapGeo.MAX_LAT) {
                val major = i % 4 == 0
                drawLine(
                    color = streetColor.copy(alpha = if (major) 0.9f else 0.5f),
                    start = state.project(Location(lat, MapGeo.MIN_LON)),
                    end = state.project(Location(lat, MapGeo.MAX_LON)),
                    strokeWidth = (if (major) 3.dp else 1.2.dp).toPx()
                )
                lat += STREET_STEP
                i++
            }

            // Capa de calor: círculos con degradado que se acumulan donde hay más reportes
            if (heatLocations.isNotEmpty()) {
                val radius = (0.0065 * state.pxPerDegree).toFloat()
                heatLocations.forEach { point ->
                    val center = state.project(point)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(MapaCalor.copy(alpha = 0.5f), MapaCalor.copy(alpha = 0.18f), Color.Transparent),
                            center = center, radius = radius
                        ),
                        radius = radius, center = center
                    )
                }
            }

            // Radio de alertas y ubicación del usuario
            if (userLocation != null) {
                val center = state.project(userLocation)
                if (alertRadiusMeters != null) {
                    val radius = (alertRadiusMeters / MapGeo.METERS_PER_DEGREE * state.pxPerDegree).toFloat()
                    drawCircle(color = primary.copy(alpha = 0.07f), radius = radius, center = center)
                    drawCircle(
                        color = primary.copy(alpha = 0.6f), radius = radius, center = center,
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f))
                        )
                    )
                }
                drawCircle(color = Color.White, radius = 9.dp.toPx(), center = center)
                drawCircle(color = MapaUsuario, radius = 6.dp.toPx(), center = center)
            }
        }
        content()
    }
}

/** Marcador circular de un reporte sobre el mapa. */
@Composable
fun BoxScope.MapPin(
    category: ReportCategory,
    location: Location,
    state: MapViewState,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = categoryColors(category)
    val pinSize = if (selected) 46.dp else 36.dp
    val halfPx = with(LocalDensity.current) { (pinSize / 2).roundToPx() }
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .mapOffset(state, location, halfPx)
            .size(pinSize)
            .clip(CircleShape)
            .background(if (selected) colors.icon else colors.chipBackground)
            .border(2.dp, if (selected) colors.chipBackground else colors.icon, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = category.icon(),
            contentDescription = category.label,
            tint = if (selected) colors.chipBackground else colors.icon,
            modifier = Modifier.size(if (selected) 24.dp else 18.dp)
        )
    }
}

/** Ubica un elemento sobre el mapa de modo que su centro caiga en [location]. */
fun Modifier.mapOffset(state: MapViewState, location: Location, halfPx: Int): Modifier =
    this.offset {
        val p = state.project(location)
        IntOffset(p.x.roundToInt() - halfPx, p.y.roundToInt() - halfPx)
    }
