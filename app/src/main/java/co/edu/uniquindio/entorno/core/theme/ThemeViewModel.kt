package co.edu.uniquindio.entorno.core.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Modo de tema guardado; null mientras se lee del almacenamiento. */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    themePreferences: ThemePreferences
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode?> = themePreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}