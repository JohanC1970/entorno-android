package co.edu.uniquindio.entorno.data.repository

import co.edu.uniquindio.entorno.core.util.safeCall
import co.edu.uniquindio.entorno.data.model.toDomain
import co.edu.uniquindio.entorno.data.model.toNewDto
import co.edu.uniquindio.entorno.data.remote.ReportRemoteDataSource
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.domain.repository.ReportRepository
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val source: ReportRemoteDataSource
) : ReportRepository {

    override fun observeFeed(category: ReportCategory?): Flow<List<Report>> =
        source.observeFeed(category).map { list -> list.map { it.toDomain() } }

    override fun observeReport(id: String): Flow<Report?> =
        source.observeById(id).map { it?.toDomain() }

    override suspend fun findById(id: String): Result<Report?> =
        safeCall { source.findById(id)?.toDomain() }

    override fun observeUserReports(userId: String): Flow<List<Report>> =
        source.observeByOwner(userId).map { list -> list.map { it.toDomain() } }

    override fun observeModerationQueue(status: ReportStatus?): Flow<List<Report>> =
        source.observeByStatus(status).map { list -> list.map { it.toDomain() } }

    override suspend fun getNearbyReports(
        latitude: Double, longitude: Double, radiusMeters: Double, category: ReportCategory?
    ): Result<List<Report>> = safeCall {
        source.getNearby(latitude, longitude, radiusMeters, category).map { it.toDomain() }
    }

    override suspend fun createReport(report: Report): Result<String> = safeCall {
        require(report.title.isNotBlank()) { "El título es obligatorio" }
        require(report.description.isNotBlank()) { "La descripción es obligatoria" }
        require(report.imageUrls.isNotEmpty()) { "Agrega al menos una imagen" }
        source.create(report.toNewDto())
    }

    override suspend fun updateReport(report: Report): Result<Unit> = safeCall {
        require(report.imageUrls.isNotEmpty()) { "Agrega al menos una imagen" }
        source.update(
            report.id,
            mapOf(
                "title" to report.title.trim(),
                "category" to report.category.name,
                "description" to report.description.trim(),
                "latitude" to report.location.latitude,
                "longitude" to report.location.longitude,
                "geohash" to GeoFireUtils.getGeoHashForLocation(
                    GeoLocation(report.location.latitude, report.location.longitude)
                ),
                "imageUrls" to report.imageUrls
            )
        )
    }

    override suspend fun deleteReport(id: String): Result<Unit> = safeCall { source.delete(id) }

    override suspend fun toggleImportant(reportId: String, userId: String): Result<Boolean> =
        safeCall { source.toggleImportant(reportId, userId) }

    override fun observeHasVoted(reportId: String, userId: String): Flow<Boolean> =
        source.observeHasVoted(reportId, userId)

    override suspend fun verifyReport(reportId: String, moderatorId: String): Result<Unit> =
        safeCall { source.verify(reportId, moderatorId) }

    override suspend fun rejectReport(reportId: String, moderatorId: String, reason: String): Result<Unit> =
        safeCall {
            require(reason.isNotBlank()) { "Debes escribir el motivo del rechazo" }
            source.reject(reportId, moderatorId, reason.trim())
        }

    override suspend fun markResolved(reportId: String, actorId: String): Result<Unit> =
        safeCall { source.markResolved(reportId, actorId) }
}
