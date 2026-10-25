package co.edu.uniquindio.entorno.features.map

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.entorno.core.component.CategoryChip
import co.edu.uniquindio.entorno.core.component.ScopeFilterRow
import co.edu.uniquindio.entorno.core.component.StatusChip
import co.edu.uniquindio.entorno.core.filter.LocationScope
import co.edu.uniquindio.entorno.core.location.LOCATION_PERMISSIONS
import co.edu.uniquindio.entorno.core.location.hasLocationPermission
import co.edu.uniquindio.entorno.core.theme.categoryColors
import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.style.layers.generated.FillLayer
import com.mapbox.maps.extension.compose.style.layers.generated.HeatmapLayer
import com.mapbox.maps.extension.compose.style.layers.generated.LineLayer
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.launch

// Zooms de Mapbox (1 menos que los de Google Maps).
private const val ZOOM_USER = 13.0
private const val ZOOM_NEIGHBORHOOD = 14.0
private const val ZOOM_REPORT = 15.0

/** Tras tocar un pin se ignora el toque al mapa, para que no deseleccione de inmediato. */
private const val PIN_TAP_GUARD_MS = 300L

private const val HEAT_RADIUS_MIN_PX = 18.0
private const val HEAT_RADIUS_MAX_PX = 48.0

/** Un solo tono: el de MapaCalor (0xFFD32F2F = 211, 47, 47), de transparente a intenso. */
private val heatColor: Expression = interpolate {
    linear()
    heatmapDensity()
    stop { literal(0.0); rgba(211.0, 47.0, 47.0, 0.0) }
    stop { literal(0.25); rgba(211.0, 47.0, 47.0, 0.35) }
    stop { literal(0.6); rgba(211.0, 47.0, 47.0, 0.7) }
    stop { literal(1.0); rgba(183.0, 28.0, 28.0, 0.95) }
}

/** El radio de cada mancha crece con el peso (votos) del reporte. */
private val heatRadius: Expression = interpolate {
    linear()
    get { literal("weight") }
    stop { literal(0.0); literal(HEAT_RADIUS_MIN_PX) }
    stop { literal(1.0); literal(HEAT_RADIUS_MAX_PX) }
}

private fun cameraAt(location: Location, zoom: Double): CameraOptions =
    CameraOptions.Builder().center(location.toPoint()).zoom(zoom).build()

@OptIn(MapboxExperimental::class)
@Composable
fun MapScreen(
    onReportClick: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var lastPinTap by remember { mutableLongStateOf(0L) }

    var locationGranted by remember { mutableStateOf(context.hasLocationPermission()) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        locationGranted = result.values.any { it }
        if (locationGranted) viewModel.refreshDeviceLocation()
    }
    LaunchedEffect(Unit) {
        if (!locationGranted) permissionLauncher.launch(LOCATION_PERMISSIONS)
    }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(state.userLocation.toPoint())
            zoom(ZOOM_USER)
        }
    }
    val selected = state.selectedReport

    LaunchedEffect(state.userLocation) {
        if (selected == null) mapViewportState.easeTo(cameraAt(state.userLocation, ZOOM_USER))
    }
    LaunchedEffect(selected?.id) {
        selected?.let { mapViewportState.easeTo(cameraAt(it.location, ZOOM_REPORT)) }
    }

    fun search() {
        val text = query.trim()
        val neighborhood = Geo.neighborhoods.firstOrNull { it.name.contains(text, ignoreCase = true) }
        when {
            text.isEmpty() -> Unit
            neighborhood != null ->
                mapViewportState.easeTo(cameraAt(neighborhood.center, ZOOM_NEIGHBORHOOD))
            else -> scope.launch { snackbarHostState.showSnackbar("No encontramos ese barrio") }
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    // El reporte enfocado se dibuja aunque quede fuera del alcance elegido.
    val markers = remember(state.reports, selected) {
        (state.reports + listOfNotNull(selected)).distinctBy { it.id }
    }
    val heatJson = remember(state.reports) { state.reports.toHeatFeatures().toJson() }
    val radiusJson = remember(state.userLocation, state.radiusKm) {
        circlePolygon(state.userLocation, state.radiusKm * 1000.0).toJson()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            style = { MapStyle(style = mapStyleUri()) },
            onMapClickListener = OnMapClickListener {
                if (System.currentTimeMillis() - lastPinTap > PIN_TAP_GUARD_MS) {
                    viewModel.onReportSelected(null)
                }
                true
            }
        ) {
            MapEffect(locationGranted) { mapView ->
                mapView.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = false)
                    enabled = locationGranted
                }
            }

            // Fuentes de datos; se actualizan cuando cambian los reportes o el radio.
            val heatSource = rememberGeoJsonSourceState { data = GeoJSONData(heatJson) }
            LaunchedEffect(heatJson) { heatSource.data = GeoJSONData(heatJson) }
            val radiusSource = rememberGeoJsonSourceState { data = GeoJSONData(radiusJson) }
            LaunchedEffect(radiusJson) { radiusSource.data = GeoJSONData(radiusJson) }

            if (state.scope == LocationScope.NEARBY) {
                FillLayer(sourceState = radiusSource) {
                    fillColor = ColorValue(primary)
                    fillOpacity = DoubleValue(0.08)
                }
                LineLayer(sourceState = radiusSource) {
                    lineColor = ColorValue(primary)
                    lineWidth = DoubleValue(2.0)
                }
            }

            if (state.showHeat) {
                HeatmapLayer(sourceState = heatSource) {
                    heatmapWeight = DoubleValue(Expression.get("weight"))
                    heatmapRadius = DoubleValue(heatRadius)
                    heatmapColor = ColorValue(heatColor)
                    heatmapOpacity = DoubleValue(0.85)
                }
            } else {
                markers.forEach { report ->
                    val colors = categoryColors(report.category)
                    val isSelected = report.id == selected?.id
                    key(report.id) {
                        CircleAnnotation(point = report.location.toPoint()) {
                            circleRadius = if (isSelected) 13.0 else 9.0
                            circleColor = colors.icon
                            circleStrokeColor = Color.White
                            circleStrokeWidth = if (isSelected) 3.0 else 2.0
                            interactionsState.onClicked {
                                lastPinTap = System.currentTimeMillis()
                                viewModel.onReportSelected(report.id)
                                true
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar barrio") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { search() }),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )
            CategoryFilterRow(selected = state.selectedCategory, onSelected = viewModel::onCategorySelected)
            ScopeFilterRow(selected = state.scope, onSelected = viewModel::onScopeSelected)
        }

        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(
                onClick = viewModel::onHeatToggle,
                containerColor = if (state.showHeat) primary else MaterialTheme.colorScheme.surface,
                contentColor = if (state.showHeat) MaterialTheme.colorScheme.onPrimary else primary
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = if (state.showHeat) "Ocultar mapa de calor" else "Mostrar mapa de calor"
                )
            }
            // Mapbox no trae botón de "mi ubicación": se centra en la ubicación del usuario.
            SmallFloatingActionButton(
                onClick = { mapViewportState.easeTo(cameraAt(state.userLocation, ZOOM_USER)) }
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = "Mi ubicación")
            }
        }

        // El padding inferior deja libres el logo y la atribución de Mapbox.
        when {
            selected != null && !state.showHeat -> SelectedReportCard(
                report = selected,
                onClick = { onReportClick(selected.id) },
                modifier = Modifier.align(Alignment.BottomCenter).padding(start = 12.dp, end = 12.dp, bottom = 44.dp)
            )
            state.showHeat -> HeatLegend(
                total = state.reports.size,
                modifier = Modifier.align(Alignment.BottomCenter).padding(start = 12.dp, end = 12.dp, bottom = 44.dp)
            )
            else -> Text(
                text = "${state.reports.size} reportes en el mapa",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 44.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp)
        )
    }
}

/** Chips "Todos" + una por categoría; compartido por el mapa y el mapa de calor. */
@Composable
fun CategoryFilterRow(
    selected: ReportCategory?,
    onSelected: (ReportCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelected(null) },
                label = { Text("Todos") },
                colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
        items(ReportCategory.entries.toList()) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelected(if (selected == category) null else category) },
                label = { Text(category.label) },
                colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    }
}

@Composable
private fun SelectedReportCard(report: Report, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChip(report.category)
                StatusChip(report.status)
            }
            Text(
                text = report.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${report.neighborhood} · Es importante: ${report.importantCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Ver detalle",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HeatLegend(
    total: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Mapa de calor · $total reportes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0x00D32F2F),
                                Color(0x59D32F2F),
                                Color(0xB3D32F2F),
                                Color(0xF3B71C1C)
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Baja densidad",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Alta densidad",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
