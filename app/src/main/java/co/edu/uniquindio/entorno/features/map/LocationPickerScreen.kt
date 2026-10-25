package co.edu.uniquindio.entorno.features.map

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.entorno.core.component.ScreenHeader
import co.edu.uniquindio.entorno.core.location.LOCATION_PERMISSIONS
import co.edu.uniquindio.entorno.core.location.PickedPlace
import co.edu.uniquindio.entorno.core.location.hasLocationPermission
import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.domain.model.Location
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location

private const val ZOOM_PICK = 17.0
private const val ZOOM_DEFAULT = 13.0

private fun cameraAt(location: Location, zoom: Double): CameraOptions =
    CameraOptions.Builder().center(location.toPoint()).zoom(zoom).build()

@OptIn(MapboxExperimental::class)
@Composable
fun LocationPickerScreen(
    initial: Location?,
    onBack: () -> Unit,
    onConfirm: (PickedPlace) -> Unit,
    viewModel: LocationPickerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var permissionGranted by remember { mutableStateOf(context.hasLocationPermission()) }

    val startLoc = initial ?: Geo.ARMENIA_CENTER
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(startLoc.toPoint())
            zoom(if (initial != null) ZOOM_PICK else ZOOM_DEFAULT)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionGranted = result.values.any { it }
        if (permissionGranted) viewModel.useCurrentLocation() else viewModel.onPermissionDenied()
    }

    LaunchedEffect(initial) { viewModel.start(initial) }
    LaunchedEffect(Unit) {
        viewModel.cameraEvents.collect { target ->
            mapViewportState.easeTo(cameraAt(target, ZOOM_PICK))
        }
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val picked = state.picked

    Box(modifier = Modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            style = { MapStyle(style = mapStyleUri()) },
            onMapClickListener = OnMapClickListener { point ->
                viewModel.onMapTap(Location(point.latitude(), point.longitude()))
                true
            }
        ) {
            MapEffect(permissionGranted) { mapView ->
                mapView.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = false)
                    enabled = permissionGranted
                }
            }

            if (picked != null) {
                key(picked) {
                    PointAnnotation(point = picked.toPoint())
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(12.dp)
        ) {
            ScreenHeader(
                title = "Elegir ubicación",
                subtitle = "Usa el GPS o toca el mapa",
                onBack = onBack
            )
        }

        SmallFloatingActionButton(
            onClick = {
                if (permissionGranted) viewModel.useCurrentLocation()
                else permissionLauncher.launch(LOCATION_PERMISSIONS)
            },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp)
        ) {
            if (state.isLocating) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.MyLocation, contentDescription = "Usar mi ubicación actual")
            }
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
                    text = when {
                        picked == null -> "Toca el mapa para elegir un punto"
                        state.isResolving -> "Buscando dirección…"
                        state.address.isNotBlank() -> state.address
                        else -> "Dirección no disponible"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (picked != null) {
                    Text(
                        text = "${state.city.ifBlank { "Ciudad no disponible" }} · ${Geo.format(picked)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = {
                        if (picked != null) onConfirm(PickedPlace(picked, state.city, state.address))
                    },
                    enabled = picked != null && !state.isResolving,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Usar esta ubicación", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 180.dp)
        )
    }
}

/**
 * El mismo selector a pantalla completa como diálogo. Sirve para el registro, que aún no
 * está dentro del NavHost principal.
 */
@Composable
fun LocationPickerDialog(
    initial: Location?,
    onDismiss: () -> Unit,
    onConfirm: (PickedPlace) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            LocationPickerScreen(initial = initial, onBack = onDismiss, onConfirm = onConfirm)
        }
    }
}
