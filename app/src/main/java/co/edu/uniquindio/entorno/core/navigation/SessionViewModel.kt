package co.edu.uniquindio.entorno.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.domain.model.UserSession
import co.edu.uniquindio.entorno.domain.usecase.auth.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Sesión activa para decidir entre el flujo de autenticación y la app principal.
 * Al cerrar sesión (o eliminar la cuenta) la sesión pasa a null y la navegación vuelve sola
 * a la bienvenida.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    observeSession: ObserveSessionUseCase
) : ViewModel() {

    val session: StateFlow<UserSession?> =
        observeSession().stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
