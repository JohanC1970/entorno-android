package co.edu.uniquindio.entorno.features.report

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.entorno.core.util.RequestResult
import co.edu.uniquindio.entorno.core.util.toUserMessage
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.repository.ImageRepository
import co.edu.uniquindio.entorno.domain.repository.ReportRepository
import co.edu.uniquindio.entorno.domain.repository.SessionRepository
import co.edu.uniquindio.entorno.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MAX_TITLE = 80
const val MAX_DESCRIPTION = 500
const val MAX_IMAGES = 3

data class CreateReportUiState(
    val category: ReportCategory? = null,
    val title: String = "",
    val description: String = "",
    val imageUris: List<Uri> = emptyList(),
    val result: RequestResult? = null
) {
    val isLoading: Boolean get() = result is RequestResult.Loading
    val canPublish: Boolean
        get() = category != null && title.isNotBlank() && imageUris.isNotEmpty() && !isLoading
}

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val imageRepository: ImageRepository,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateReportUiState())
    val uiState: StateFlow<CreateReportUiState> = _uiState.asStateFlow()

    // TODO: reemplazar por la ubicación real cuando se integre el mapa
    private val currentLocation = Location(latitude = 4.5339, longitude = -75.6811)

    fun onCategoryChange(category: ReportCategory) =
        _uiState.update { it.copy(category = category) }

    fun onTitleChange(value: String) =
        _uiState.update { it.copy(title = value.take(MAX_TITLE)) }

    fun onDescriptionChange(value: String) =
        _uiState.update { it.copy(description = value.take(MAX_DESCRIPTION)) }

    fun onPhotosPicked(uris: List<Uri>) =
        _uiState.update { it.copy(imageUris = (it.imageUris + uris).distinct().take(MAX_IMAGES)) }

    fun onPhotoRemoved(uri: Uri) =
        _uiState.update { it.copy(imageUris = it.imageUris - uri) }

    fun clearResult() = _uiState.update { it.copy(result = null) }

    fun publish() {
        val state = _uiState.value
        val category = state.category
        if (!state.canPublish || category == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(result = RequestResult.Loading) }

            val session = sessionRepository.session.first()
            val result = if (session == null) {
                RequestResult.Failure("Debes iniciar sesión para publicar")
            } else {
                val ownerName = userRepository.findById(session.userId).getOrNull()?.name.orEmpty()
                imageRepository.uploadImages(state.imageUris.map { it.toString() })
                    .fold(
                        onSuccess = { urls ->
                            reportRepository.createReport(
                                Report(
                                    title = state.title.trim(),
                                    description = state.description.trim(),
                                    category = category,
                                    location = currentLocation,
                                    imageUrls = urls,
                                    ownerId = session.userId,
                                    ownerName = ownerName
                                )
                            )
                        },
                        onFailure = { Result.failure(it) }
                    )
                    .fold(
                        onSuccess = { RequestResult.Success("Reporte enviado a verificación") },
                        onFailure = { RequestResult.Failure(it.toUserMessage()) }
                    )
            }
            _uiState.update { it.copy(result = result) }
        }
    }
}
