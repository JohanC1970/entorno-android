package co.edu.uniquindio.entorno.features.report

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import co.edu.uniquindio.entorno.core.navigation.CreateReport
import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.core.util.RequestResult
import co.edu.uniquindio.entorno.core.util.toUserMessage
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.domain.repository.DuplicateDetector
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

/** Clave con la que el selector de ubicación devuelve el punto elegido ("lat,lon"). */
const val KEY_PICKED_LOCATION = "picked_location"

private const val DUPLICATE_RADIUS_METERS = 500.0
private const val MAX_CANDIDATES = 15
private val EDITABLE_STATUSES = setOf(ReportStatus.PENDING, ReportStatus.VERIFIED)

data class CreateReportUiState(
    /** null = reporte nuevo; con valor = se está editando ese reporte. */
    val editingId: String? = null,
    val category: ReportCategory? = null,
    val title: String = "",
    val description: String = "",
    /** Fotos que el reporte ya tiene subidas (solo al editar). */
    val existingImageUrls: List<String> = emptyList(),
    /** Fotos nuevas elegidas de la galería, aún sin subir. */
    val imageUris: List<Uri> = emptyList(),
    val location: Location? = null,
    val result: RequestResult? = null,
    val isLoadingReport: Boolean = false,
    /** Reportes cercanos que la IA considera el mismo evento; no vacío = mostrar advertencia. */
    val duplicates: List<Report> = emptyList()
) {
    val isEditing: Boolean get() = editingId != null
    val photoCount: Int get() = existingImageUrls.size + imageUris.size
    val isLoading: Boolean get() = result is RequestResult.Loading
    val canPublish: Boolean
        get() = category != null && title.isNotBlank() && description.isNotBlank() &&
                photoCount > 0 && location != null && !isLoading && !isLoadingReport
}

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val reportRepository: ReportRepository,
    private val imageRepository: ImageRepository,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val duplicateDetector: DuplicateDetector
) : ViewModel() {

    private val editingId: String? = savedStateHandle.toRoute<CreateReport>().reportId
    private var original: Report? = null

    private val _uiState = MutableStateFlow(CreateReportUiState(editingId = editingId))
    val uiState: StateFlow<CreateReportUiState> = _uiState.asStateFlow()

    init {
        // El selector de ubicación devuelve el punto en el SavedStateHandle de esta pantalla.
        viewModelScope.launch {
            savedStateHandle.getStateFlow<String?>(KEY_PICKED_LOCATION, null).collect { raw ->
                val parts = raw?.split(",")
                val lat = parts?.getOrNull(0)?.toDoubleOrNull()
                val lon = parts?.getOrNull(1)?.toDoubleOrNull()
                if (lat != null && lon != null) {
                    _uiState.update { it.copy(location = Location(lat, lon)) }
                }
            }
        }
        editingId?.let(::loadReport)
    }

    private fun loadReport(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingReport = true) }
            val report = reportRepository.findById(id).getOrNull()
            val session = sessionRepository.session.first()
            if (report == null || session?.userId != report.ownerId || report.status !in EDITABLE_STATUSES) {
                _uiState.update {
                    it.copy(
                        isLoadingReport = false,
                        result = RequestResult.Failure("No puedes editar este reporte")
                    )
                }
                return@launch
            }
            original = report
            // El selector de ubicación arranca desde este punto.
            savedStateHandle[KEY_PICKED_LOCATION] = "${report.location.latitude},${report.location.longitude}"
            _uiState.update {
                it.copy(
                    isLoadingReport = false,
                    category = report.category,
                    title = report.title,
                    description = report.description,
                    existingImageUrls = report.imageUrls
                )
            }
        }
    }

    fun onCategoryChange(category: ReportCategory) = _uiState.update { it.copy(category = category) }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value.take(MAX_TITLE)) }

    fun onDescriptionChange(value: String) =
        _uiState.update { it.copy(description = value.take(MAX_DESCRIPTION)) }

    fun onPhotosPicked(uris: List<Uri>) = _uiState.update {
        it.copy(imageUris = (it.imageUris + uris).distinct().take(MAX_IMAGES - it.existingImageUrls.size))
    }

    fun onPhotoRemoved(uri: Uri) = _uiState.update { it.copy(imageUris = it.imageUris - uri) }

    fun onExistingPhotoRemoved(url: String) =
        _uiState.update { it.copy(existingImageUrls = it.existingImageUrls - url) }

    fun clearResult() = _uiState.update { it.copy(result = null) }

    fun dismissDuplicates() = _uiState.update { it.copy(duplicates = emptyList()) }

    fun publish() = submit(checkDuplicates = true)

    /** El usuario vio los posibles duplicados y decidió publicar igual. */
    fun publishAnyway() = submit(checkDuplicates = false)

    private fun submit(checkDuplicates: Boolean) {
        val state = _uiState.value
        val category = state.category
        val location = state.location
        if (!state.canPublish || category == null || location == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(result = RequestResult.Loading, duplicates = emptyList()) }

            val session = sessionRepository.session.first()
            if (session == null) {
                _uiState.update { it.copy(result = RequestResult.Failure("Debes iniciar sesión para publicar")) }
                return@launch
            }

            val base = original
            // Solo los reportes nuevos se revisan; se hace antes de subir fotos para no gastar subidas.
            if (base == null && checkDuplicates) {
                val duplicates = findDuplicates(state, location)
                if (duplicates.isNotEmpty()) {
                    _uiState.update { it.copy(result = null, duplicates = duplicates) }
                    return@launch
                }
            }

            val saved: Result<Any> = resolveImageUrls(state).fold(
                onSuccess = { urls ->
                    if (base == null) {
                        val ownerName = userRepository.findById(session.userId).getOrNull()?.name.orEmpty()
                        reportRepository.createReport(
                            Report(
                                title = state.title.trim(),
                                description = state.description.trim(),
                                category = category,
                                location = location,
                                neighborhood = Geo.nearestNeighborhood(location),
                                imageUrls = urls,
                                ownerId = session.userId,
                                ownerName = ownerName
                            )
                        )
                    } else {
                        reportRepository.updateReport(
                            base.copy(
                                title = state.title.trim(),
                                description = state.description.trim(),
                                category = category,
                                location = location,
                                neighborhood = Geo.nearestNeighborhood(location),
                                imageUrls = urls
                            )
                        )
                    }
                },
                onFailure = { Result.failure(it) }
            )

            val message = if (base == null) "Reporte enviado a verificación" else "Cambios guardados"
            _uiState.update {
                it.copy(
                    result = saved.fold(
                        onSuccess = { RequestResult.Success(message) },
                        onFailure = { error -> RequestResult.Failure(error.toUserMessage()) }
                    )
                )
            }
        }
    }

    /** Fotos que ya estaban subidas + las nuevas, que se suben ahora. */
    private suspend fun resolveImageUrls(state: CreateReportUiState): Result<List<String>> =
        if (state.imageUris.isEmpty()) Result.success(state.existingImageUrls)
        else imageRepository.uploadImages(state.imageUris.map { it.toString() })
            .map { uploaded -> state.existingImageUrls + uploaded }

    /**
     * Capa geoespacial + capa de inferencia. Si algo falla devuelve vacío: la IA nunca debe
     * impedir que el usuario publique.
     */
    private suspend fun findDuplicates(state: CreateReportUiState, location: Location): List<Report> {
        val nearby = reportRepository
            .getNearbyReports(location.latitude, location.longitude, DUPLICATE_RADIUS_METERS)
            .getOrNull().orEmpty()
            .sortedBy { Geo.distanceMeters(location, it.location) }
            .take(MAX_CANDIDATES)
        if (nearby.isEmpty()) return emptyList()
        return duplicateDetector
            .findDuplicates(state.title.trim(), state.description.trim(), nearby)
            .getOrDefault(emptyList())
    }
}