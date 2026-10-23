package co.edu.uniquindio.entorno.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.core.util.toUserMessage
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.User
import co.edu.uniquindio.entorno.domain.repository.NotificationRepository
import co.edu.uniquindio.entorno.domain.repository.ReportRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import co.edu.uniquindio.entorno.domain.repository.SettingsRepository
import co.edu.uniquindio.entorno.domain.repository.UserRepository
import co.edu.uniquindio.entorno.domain.usecase.auth.observeCurrentUser
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "",
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val query: String = "",
    val selectedCategory: ReportCategory? = null,
    val unreadCount: Int = 0,
    /** Radio de alertas configurado (km); 0 cuando aún no se carga. */
    val radiusKm: Int = 0,
    /** Reportes del feed que quedan fuera del radio de alertas. */
    val hiddenByRadius: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<ReportCategory?>(null)

    private val currentUser: Flow<User?> = sessionRepository.observeCurrentUser(userRepository)

    init {
        observeFeed()
    }

    private fun observeFeed() {
        val feed = category.flatMapLatest { reportRepository.observeFeed(it) }
        val unread = currentUser.flatMapLatest { user ->
            if (user == null) flowOf(0) else notificationRepository.observeUnreadCount(user.id)
        }
        viewModelScope.launch {
            combine(feed, query, settingsRepository.observeSettings(), currentUser, unread) {
                reports, text, settings, user, unreadCount ->
                val center = user?.location ?: Geo.ARMENIA_CENTER
                val inRadius = reports.filter {
                    Geo.distanceMeters(center, it.location) <= settings.alertRadiusKm * 1000.0
                }
                val matching = inRadius.filter { it.matches(text) }
                HomeUiState(
                    userName = user?.name.orEmpty(), reports = matching, isLoading = false,
                    query = text, selectedCategory = category.value, unreadCount = unreadCount,
                    radiusKm = settings.alertRadiusKm, hiddenByRadius = reports.size - inRadius.size
                )
            }
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
                .collect { state -> _uiState.update { state.copy(errorMessage = it.errorMessage) } }
        }
    }

    private fun Report.matches(text: String): Boolean {
        val term = text.trim()
        if (term.isEmpty()) return true
        return title.contains(term, ignoreCase = true) ||
            description.contains(term, ignoreCase = true) ||
            neighborhood.contains(term, ignoreCase = true) ||
            category.label.contains(term, ignoreCase = true)
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onCategorySelected(selected: ReportCategory?) {
        category.value = selected
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
