package co.edu.uniquindio.entorno.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.core.util.RequestResult
import co.edu.uniquindio.entorno.core.util.Validators
import co.edu.uniquindio.entorno.core.util.toUserMessage
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.usecase.auth.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import jakarta.inject.Inject

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val city: String = "",
    val address: String = "",
    val location: Location? = null,      // dirección elegida en el mapa (lat/lng)
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val cityError: String? = null,
    val addressError: String? = null,
    val registerResult: RequestResult? = null
) {
    val isFormValid: Boolean
        get() = listOf(name, email, password, confirmPassword, city, address).all { it.isNotBlank() } &&
            location != null &&
            listOf(nameError, emailError, passwordError, confirmPasswordError, cityError, addressError)
                .all { it == null }
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(v: String) =
        _uiState.update { it.copy(name = v, nameError = Validators.required(v, "El nombre")) }

    fun onEmailChange(v: String) =
        _uiState.update { it.copy(email = v, emailError = Validators.email(v)) }

    fun onPasswordChange(v: String) = _uiState.update {
        it.copy(
            password = v,
            passwordError = Validators.password(v),
            // Si ya escribió la confirmación, se revalida contra la nueva contraseña
            confirmPasswordError = if (it.confirmPassword.isEmpty()) null
            else Validators.confirmPassword(v, it.confirmPassword)
        )
    }

    fun onConfirmPasswordChange(v: String) = _uiState.update {
        it.copy(confirmPassword = v, confirmPasswordError = Validators.confirmPassword(it.password, v))
    }

    fun onCityChange(v: String) =
        _uiState.update { it.copy(city = v, cityError = Validators.required(v, "La ciudad")) }

    fun onAddressChange(v: String) =
        _uiState.update { it.copy(address = v, addressError = Validators.required(v, "La dirección")) }

    /** Se llama desde el mapa (onMapClickListener) con el punto que eligió el usuario. */
    fun onLocationSelected(latitude: Double, longitude: Double) =
        _uiState.update { it.copy(location = Location(latitude, longitude)) }

    fun register() {
        val s = _uiState.value
        if (!s.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(registerResult = RequestResult.Loading) }

            val user = User(
                name = s.name, email = s.email, city = s.city,
                address = s.address, location = s.location
            )
            val result = registerUserUseCase(user, s.password).fold(
                onSuccess = { RequestResult.Success("Registro exitoso. Ya puedes iniciar sesión") },
                onFailure = { RequestResult.Failure(it.toUserMessage()) }
            )
            _uiState.update { it.copy(registerResult = result) }
        }
    }

    fun clearResult() = _uiState.update { it.copy(registerResult = null) }
}
