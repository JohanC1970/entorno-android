package co.edu.uniquindio.entorno.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.core.util.RequestResult
import co.edu.uniquindio.entorno.core.util.Validators
import co.edu.uniquindio.entorno.core.util.toUserMessage
import co.edu.uniquindio.entorno.domain.usecase.auth.LoginUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import jakarta.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginResult: RequestResult? = null
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && emailError == null && passwordError == null
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = Validators.email(value)) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = Validators.password(value)) }

    fun login() {
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(loginResult = RequestResult.Loading) }

            val result = loginUserUseCase(state.email, state.password).fold(
                onSuccess = { RequestResult.Success("Bienvenido, ${it.name}") },
                onFailure = { RequestResult.Failure(it.toUserMessage()) }
            )
            // Si fue exitoso, la sesión queda guardada y AppNavigation cambia sola a MainScreen.
            _uiState.update { it.copy(loginResult = result) }
        }
    }

    fun clearResult() = _uiState.update { it.copy(loginResult = null) }
}
