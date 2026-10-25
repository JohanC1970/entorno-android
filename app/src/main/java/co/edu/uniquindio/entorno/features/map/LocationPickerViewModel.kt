package co.edu.uniquindio.entorno.features.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.core.location.AddressResolver
import co.edu.uniquindio.entorno.core.location.DeviceLocationClient
import co.edu.uniquindio.entorno.domain.model.Location
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LocationPickerUiState(
    val picked: Location? = null,
    val city: String = "",
    val address: String = "",
    val isResolving: Boolean = false,
    val isLocating: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class LocationPickerViewModel @Inject constructor(
    private val locationClient: DeviceLocationClient,
    private val addressResolver: AddressResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationPickerUiState())
    val uiState: StateFlow<LocationPickerUiState> = _uiState.asStateFlow()

    /** Eventos de una sola vez: mover la cámara del mapa a un punto. */
    private val _cameraEvents = MutableSharedFlow<Location>(extraBufferCapacity = 1)
    val cameraEvents: SharedFlow<Location> = _cameraEvents.asSharedFlow()

    private var resolveJob: Job? = null

    /** Se llama cada vez que se abre el selector. Sin punto inicial intenta usar el GPS. */
    fun start(initial: Location?) {
        _uiState.value = LocationPickerUiState(picked = initial)
        when {
            initial != null -> resolve(initial)
            locationClient.hasPermission() -> useCurrentLocation()
        }
    }

    fun onMapTap(location: Location) = select(location)

    fun useCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocating = true) }
            val current = locationClient.currentLocation()
            _uiState.update { it.copy(isLocating = false) }
            if (current == null) {
                _uiState.update {
                    it.copy(message = "No pudimos obtener tu ubicación. Activa el GPS o toca el mapa.")
                }
            } else {
                select(current)
                _cameraEvents.emit(current)
            }
        }
    }

    fun onPermissionDenied() = _uiState.update {
        it.copy(message = "Sin permiso de ubicación. Toca el mapa para elegir el punto.")
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    private fun select(location: Location) {
        _uiState.update { it.copy(picked = location) }
        resolve(location)
    }

    private fun resolve(location: Location) {
        resolveJob?.cancel()
        resolveJob = viewModelScope.launch {
            _uiState.update { it.copy(isResolving = true, city = "", address = "") }
            val resolved = addressResolver.resolve(location)
            _uiState.update {
                it.copy(
                    city = resolved?.city.orEmpty(),
                    address = resolved?.address.orEmpty(),
                    isResolving = false,
                    message = if (resolved == null) {
                        "No pudimos obtener la dirección. Podrás escribirla a mano."
                    } else it.message
                )
            }
        }
    }
}