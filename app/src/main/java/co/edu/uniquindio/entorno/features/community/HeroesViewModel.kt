package co.edu.uniquindio.entorno.features.community

import co.edu.uniquindio.entorno.R
import co.edu.uniquindio.entorno.core.component.labelRes
import co.edu.uniquindio.entorno.core.util.UiText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.model.UserLevel
import co.edu.uniquindio.entorno.domain.model.UserRole
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import co.edu.uniquindio.entorno.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TOP_N = 5

data class HeroEntry(val rank: Int, val user: User)

data class HeroesUiState(
    val ranking: List<HeroEntry> = emptyList(),
    val currentUserId: String = "",
    val tip: UiText? = null
) {
    val podium: List<HeroEntry> get() = ranking.take(3)
    val rest: List<HeroEntry> get() = ranking.drop(3)
}

@HiltViewModel
class HeroesViewModel @Inject constructor(
    userRepository: UserRepository,
    sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HeroesUiState())
    val uiState: StateFlow<HeroesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(userRepository.observeRanking(), sessionRepository.session) { users, session ->
                // Los moderadores no compiten: el ranking es entre ciudadanos.
                val ranking = users.filter { it.role == UserRole.USER }
                    .mapIndexed { index, user -> HeroEntry(index + 1, user) }
                val currentId = session?.userId.orEmpty()
                HeroesUiState(ranking = ranking, currentUserId = currentId, tip = buildTip(ranking, currentId))
            }.collect { state -> _uiState.update { state } }
        }
    }

    private fun buildTip(ranking: List<HeroEntry>, currentId: String): UiText? {
        val me = ranking.firstOrNull { it.user.id == currentId } ?: return null
        val nextLevel = UserLevel.entries.getOrNull(me.user.level.ordinal + 1)
        val toNext = nextLevel?.let { it.minPoints - me.user.points }
        val fifth = ranking.getOrNull(TOP_N - 1)
        return when {
            me.rank <= TOP_N && toNext != null ->
                UiText(R.string.heroes_tip_top_next, TOP_N, toNext, UiText(nextLevel.labelRes()))
            me.rank <= TOP_N -> UiText(R.string.heroes_tip_top_max, TOP_N)
            toNext != null && fifth != null ->
                UiText(
                    R.string.heroes_tip_next_and_top,
                    toNext, UiText(nextLevel.labelRes()), fifth.user.points - me.user.points + 1, TOP_N
                )
            fifth != null ->
                UiText(R.string.heroes_tip_top, fifth.user.points - me.user.points + 1, TOP_N)
            else -> null
        }
    }
}
