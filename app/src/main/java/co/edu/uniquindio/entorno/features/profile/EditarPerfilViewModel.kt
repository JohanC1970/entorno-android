package co.edu.uniquindio.entorno.features.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EditarPerfilUiState(
    val nombreCompleto: String = "",
    val correo: String = "",
    val telefono: String = "",
    val barrio: String = "",
    val iniciales: String = "",
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false
)

class EditarPerfilViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        _uiState.update {
            it.copy(
                nombreCompleto = "Johan García",
                correo = "johanc.garciag@uqvirtual.edu.co",
                telefono = "+57 300 000 0000",
                barrio = "Ej. La Fachada, Armenia",
                iniciales = "JG"
            )
        }
    }

    fun actualizarNombre(nombre: String) {
        _uiState.update {
            it.copy(
                nombreCompleto = nombre,
                iniciales = nombre.split(" ")
                    .take(2)
                    .filter { p -> p.isNotBlank() }
                    .map { p -> p.first().uppercaseChar() }
                    .joinToString("")
            )
        }
    }

    fun actualizarCorreo(correo: String) {
        _uiState.update { it.copy(correo = correo) }
    }

    fun actualizarTelefono(telefono: String) {
        _uiState.update { it.copy(telefono = telefono) }
    }

    fun actualizarBarrio(barrio: String) {
        _uiState.update { it.copy(barrio = barrio) }
    }

    fun guardarCambios() {
        _uiState.update { it.copy(guardando = true) }
        // Simulación de guardado en memoria
        _uiState.update { it.copy(guardando = false, guardadoExitoso = true) }
    }

    fun eliminarCuenta() {
        // En Fase 2 no se elimina realmente
    }
}
