package co.edu.uniquindio.entorno.features.auth

import co.edu.uniquindio.entorno.R
import co.edu.uniquindio.entorno.core.util.UiText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.core.util.RequestResult
import co.edu.uniquindio.entorno.core.util.Validators
import co.edu.uniquindio.entorno.core.util.toUserMessage
import co.edu.uniquindio.entorno.domain.usecase.auth.SendPasswordResetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import jakarta.inject.Inject

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: UiText? = null,
    val result: RequestResult? = null
) {
    val isFormValid: Boolean get() = email.isNotBlank() && emailError == null
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val sendPasswordResetUseCase: SendPasswordResetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(v: String) =
        _uiState.update { it.copy(email = v, emailError = Validators.email(v)) }

    fun sendResetLink() {
        val s = _uiState.value
        if (!s.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(result = RequestResult.Loading) }
            val result = sendPasswordResetUseCase(s.email).fold(
                // Mensaje neutro: no revela si el correo tiene cuenta o no
                onSuccess = { RequestResult.Success(UiText(R.string.forgot_success)) },
                onFailure = { RequestResult.Failure(it.toUserMessage()) }
            )
            _uiState.update { it.copy(result = result) }
        }
    }

    fun clearResult() = _uiState.update { it.copy(result = null) }
}
