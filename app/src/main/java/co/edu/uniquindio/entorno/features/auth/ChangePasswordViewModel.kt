package co.edu.uniquindio.entorno.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.core.util.RequestResult
import co.edu.uniquindio.entorno.core.util.toUserMessage
import co.edu.uniquindio.entorno.domain.usecase.auth.ChangePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MIN_LENGTH = 8

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentError: String? = null,
    val confirmError: String? = null,
    val result: RequestResult? = null
) {
    val hasMinLength: Boolean get() = newPassword.length >= MIN_LENGTH
    val hasUppercase: Boolean get() = newPassword.any { it.isUpperCase() }
    val hasDigit: Boolean get() = newPassword.any { it.isDigit() }
    val isNewPasswordValid: Boolean get() = hasMinLength && hasUppercase && hasDigit
    val isLoading: Boolean get() = result is RequestResult.Loading
    val canSubmit: Boolean
        get() = currentPassword.isNotBlank() && isNewPasswordValid && confirmPassword.isNotBlank() && !isLoading
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun onCurrentChange(value: String) =
        _uiState.update { it.copy(currentPassword = value, currentError = null) }

    fun onNewChange(value: String) =
        _uiState.update { it.copy(newPassword = value, confirmError = null) }

    fun onConfirmChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, confirmError = null) }

    fun clearResult() = _uiState.update { it.copy(result = null) }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(confirmError = "Las contraseñas no coinciden") }
            return
        }
        if (state.newPassword == state.currentPassword) {
            _uiState.update { it.copy(confirmError = "La nueva contraseña debe ser distinta a la actual") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(result = RequestResult.Loading) }
            changePasswordUseCase(state.currentPassword, state.newPassword)
                .onSuccess {
                    _uiState.update {
                        it.copy(result = RequestResult.Success("Contraseña actualizada"))
                    }
                }
                .onFailure { error ->
                    val message = error.toUserMessage()
                    _uiState.update {
                        it.copy(
                            currentError = if (error is IllegalArgumentException) message else null,
                            result = RequestResult.Failure(message)
                        )
                    }
                }
        }
    }
}
