package co.edu.uniquindio.entorno.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.domain.model.Badge
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.model.UserLevel
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import co.edu.uniquindio.entorno.domain.repository.UserRepository
import co.edu.uniquindio.entorno.domain.usecase.auth.observeCurrentUser
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BadgeDetail(val badge: Badge, val unlocked: Boolean)

data class InsigniasUiState(
    val user: User? = null,
    val badges: List<BadgeDetail> = emptyList()
) {
    val level: UserLevel get() = user?.level ?: UserLevel.NOVICE
    val nextLevel: UserLevel? get() = UserLevel.entries.getOrNull(level.ordinal + 1)
    val points: Int get() = user?.points ?: 0

    /** Progreso (0 a 1) dentro del nivel actual hacia el siguiente. */
    val progress: Float
        get() {
            val next = nextLevel ?: return 1f
            val range = next.minPoints - level.minPoints
            return ((points - level.minPoints).toFloat() / range).coerceIn(0f, 1f)
        }
}

@HiltViewModel
class InsigniasViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsigniasUiState())
    val uiState: StateFlow<InsigniasUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository.observeCurrentUser(userRepository).collect { user ->
                _uiState.update {
                    InsigniasUiState(
                        user = user,
                        badges = Badge.entries.map { BadgeDetail(it, user != null && it in user.badges) }
                    )
                }
            }
        }
    }
}
