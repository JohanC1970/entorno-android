package co.edu.uniquindio.entorno.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.domain.model.UserSession
import co.edu.uniquindio.entorno.domain.usecase.auth.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Sesión activa para decidir entre el flujo de autenticación y la app principal.
 * Al cerrar sesión (o eliminar la cuenta) la sesión pasa a null y la navegación vuelve sola
 * a la bienvenida.
 */
sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class Active(val session: UserSession) : SessionState
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    observeSession: ObserveSessionUseCase
) : ViewModel() {

    val state: StateFlow<SessionState> = observeSession()
        .map { session -> session?.let { SessionState.Active(it) } ?: SessionState.SignedOut }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)
}