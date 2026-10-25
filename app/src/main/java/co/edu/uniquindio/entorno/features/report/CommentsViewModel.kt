package co.edu.uniquindio.entorno.features.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import co.edu.uniquindio.entorno.core.navigation.Comments
import co.edu.uniquindio.entorno.core.util.toUserMessage
import co.edu.uniquindio.entorno.domain.model.Comment
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.UserRole
import co.edu.uniquindio.entorno.domain.repository.CommentRepository
import co.edu.uniquindio.entorno.domain.repository.ReportRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommentsUiState(
    val report: Report? = null,
    val comments: List<Comment> = emptyList(),
    val currentUserId: String = "",
    /** Comentario al que se está respondiendo (null = comentario nuevo). */
    val replyingTo: Comment? = null,
    /** Comentarios marcados como "Útil" por el usuario en esta sesión. */
    val usefulIds: Set<String> = emptySet(),
    val isSending: Boolean = false,
    val errorMessage: String? = null
) {
    val moderatorIds: Set<String>
        get() = comments.filter { it.authorRole == UserRole.MODERATOR }.map { it.authorId }.toSet()
}

@HiltViewModel
class CommentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportRepository: ReportRepository,
    private val commentRepository: CommentRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val reportId: String = savedStateHandle.toRoute<Comments>().reportId

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = sessionRepository.session.first()?.userId.orEmpty()
            _uiState.update { it.copy(currentUserId = userId) }
        }
        viewModelScope.launch {
            reportRepository.observeReport(reportId)
                .catch { error -> _uiState.update { it.copy(errorMessage = error.toUserMessage()) } }
                .collect { report -> _uiState.update { it.copy(report = report) } }
        }
        viewModelScope.launch {
            commentRepository.observeComments(reportId)
                .catch { error -> _uiState.update { it.copy(errorMessage = error.toUserMessage()) } }
                .collect { comments -> _uiState.update { it.copy(comments = comments) } }
        }
    }

    fun startReply(comment: Comment) = _uiState.update { it.copy(replyingTo = comment) }

    fun cancelReply() = _uiState.update { it.copy(replyingTo = null) }

    fun toggleUseful(commentId: String) = _uiState.update {
        it.copy(usefulIds = if (commentId in it.usefulIds) it.usefulIds - commentId else it.usefulIds + commentId)
    }

    fun send(text: String) {
        if (text.isBlank() || _uiState.value.isSending) return
        viewModelScope.launch {
            val session = sessionRepository.session.first()
            if (session == null) {
                _uiState.update { it.copy(errorMessage = "Debes iniciar sesión para comentar") }
                return@launch
            }
            _uiState.update { it.copy(isSending = true) }
            val parentId = _uiState.value.replyingTo?.id
            commentRepository.addComment(
                Comment(
                    reportId = reportId, authorId = session.userId, authorName = "",
                    text = text, parentCommentId = parentId
                )
            )
                .onSuccess { _uiState.update { it.copy(isSending = false, replyingTo = null) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isSending = false, errorMessage = error.toUserMessage()) }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
