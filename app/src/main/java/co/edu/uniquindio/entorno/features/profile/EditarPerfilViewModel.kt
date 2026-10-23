package co.edu.uniquindio.entorno.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.core.util.Validators
import co.edu.uniquindio.entorno.core.util.toUserMessage
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import co.edu.uniquindio.entorno.domain.repository.UserRepository
import co.edu.uniquindio.entorno.domain.usecase.auth.DeleteAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditarPerfilUiState(
    val nombreCompleto: String = "",
    val correo: String = "",
    val telefono: String = "",
    val barrio: String = "",
    val iniciales: String = "",
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val nombreError: String? = null,
    val correoError: String? = null,
    val mensajeError: String? = null,
    val eliminando: Boolean = false
)

private fun initialsOf(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")

@HiltViewModel
class EditarPerfilViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState.asStateFlow()

    private var original: User? = null

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            val userId = sessionRepository.session.first()?.userId ?: return@launch
            userRepository.findById(userId)
                .onSuccess { user ->
                    original = user
                    _uiState.update {
                        it.copy(
                            nombreCompleto = user.name, correo = user.email,
                            telefono = user.phoneNumber, barrio = user.address,
                            iniciales = initialsOf(user.name)
                        )
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(mensajeError = error.toUserMessage()) } }
        }
    }

    fun actualizarNombre(nombre: String) {
        _uiState.update { it.copy(nombreCompleto = nombre, iniciales = initialsOf(nombre), nombreError = null) }
    }

    fun actualizarCorreo(correo: String) {
        _uiState.update { it.copy(correo = correo, correoError = null) }
    }

    fun actualizarTelefono(telefono: String) {
        _uiState.update { it.copy(telefono = telefono) }
    }

    fun actualizarBarrio(barrio: String) {
        _uiState.update { it.copy(barrio = barrio) }
    }

    fun guardarCambios() {
        val state = _uiState.value
        val base = original ?: return
        val nombreError = Validators.required(state.nombreCompleto, "El nombre")
        val correoError = Validators.email(state.correo)
        if (nombreError != null || correoError != null) {
            _uiState.update { it.copy(nombreError = nombreError, correoError = correoError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            userRepository.update(
                base.copy(
                    name = state.nombreCompleto, email = state.correo,
                    phoneNumber = state.telefono, address = state.barrio
                )
            )
                .onSuccess {
                    original = base.copy(
                        name = state.nombreCompleto.trim(), email = state.correo.trim(),
                        phoneNumber = state.telefono.trim(), address = state.barrio.trim()
                    )
                    _uiState.update { it.copy(guardando = false, guardadoExitoso = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(guardando = false, mensajeError = error.toUserMessage()) }
                }
        }
    }

    fun mensajeMostrado() {
        _uiState.update { it.copy(guardadoExitoso = false, mensajeError = null) }
    }

    /** Elimina la cuenta; al quedar sin sesión la app vuelve sola a la pantalla de bienvenida. */
    fun eliminarCuenta(contrasena: String) {
        if (contrasena.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Escribe tu contraseña para confirmar") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(eliminando = true) }
            deleteAccountUseCase(contrasena)
                .onFailure { error ->
                    _uiState.update { it.copy(eliminando = false, mensajeError = error.toUserMessage()) }
                }
        }
    }
}
