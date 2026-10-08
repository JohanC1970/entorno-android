package co.edu.uniquindio.entorno.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.core.util.toUserMessage
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.repository.ReportRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import co.edu.uniquindio.entorno.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "",
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserName()
        observeFeed()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            val name = sessionRepository.session.first()
                ?.let { userRepository.findById(it.userId).getOrNull()?.name }
                .orEmpty()
            _uiState.update { it.copy(userName = name) }
        }
    }

    private fun observeFeed() {
        viewModelScope.launch {
            reportRepository.observeFeed()
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
                .collect { reports ->
                    _uiState.update { it.copy(reports = reports, isLoading = false) }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
