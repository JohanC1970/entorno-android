package co.edu.uniquindio.entorno.data.repository

import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.core.util.safeCall
import co.edu.uniquindio.entorno.data.model.toDomain
import co.edu.uniquindio.entorno.data.model.toNewDto
import co.edu.uniquindio.entorno.data.remote.GamificationRemoteDataSource
import co.edu.uniquindio.entorno.data.remote.ReportRemoteDataSource
import co.edu.uniquindio.entorno.domain.model.Badge
import co.edu.uniquindio.entorno.domain.model.PointsRule
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
import kotlinx.coroutines.flow.catch

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val source: ReportRemoteDataSource,
    private val gamification: GamificationRemoteDataSource
) : ReportRepository {

    private companion object{
        const val VERIFIED_FOR_BADGE = 10;
        const val MIN_VOTES_TOP_REPORT = 10
    }

    override fun observeFeed(category: ReportCategory?): Flow<List<Report>> =
        source.observeFeed(category).map { list -> list.map { it.toDomain() } }.catch { emit(emptyList()) }

    override fun observeReport(id: String): Flow<Report?> =
        source.observeById(id).map { it?.toDomain() }.catch { emit(null) }

    override suspend fun findById(id: String): Result<Report?> =
        safeCall { source.findById(id)?.toDomain() }

    override fun observeUserReports(userId: String): Flow<List<Report>> =
        source.observeByOwner(userId).map { list -> list.map { it.toDomain() } }.catch { emit(emptyList()) }

    override fun observeModerationQueue(status: ReportStatus?): Flow<List<Report>> =
        source.observeByStatus(status).map { list -> list.map { it.toDomain() } }.catch { emit(emptyList()) }

    override fun observeVotesGiven(userId: String): Flow<Int> =
        source.observeVotesGiven(userId).catch { emit(0) }

    override suspend fun getNearbyReports(
        latitude: Double, longitude: Double, radiusMeters: Double, category: ReportCategory?
    ): Result<List<Report>> = safeCall {
        source.getNearby(latitude, longitude, radiusMeters, category).map { it.toDomain() }
    }

    override suspend fun createReport(report: Report): Result<String> = safeCall {
        require(report.title.isNotBlank()) { "El título es obligatorio" }
        require(report.description.isNotBlank()) { "La descripción es obligatoria" }
        require(report.imageUrls.isNotEmpty()) { "Agrega al menos una imagen" }
        val id = source.create(report.toNewDto())
        safeCall { gamification.apply(report.ownerId, PointsRule.CREATE_REPORT, Badge.FIRST_REPORT) }
        id
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
                "neighborhood" to Geo.nearestNeighborhood(report.location),
                "imageUrls" to report.imageUrls
            )
        )
    }

    override suspend fun deleteReport(id: String): Result<Unit> = safeCall { source.delete(id) }

    override suspend fun toggleImportant(reportId: String, userId: String): Result<Boolean> = safeCall {
        val voted = source.toggleImportant(reportId, userId)
        safeCall {
            val report = source.findById(reportId)
            if (report != null && report.ownerId != userId) {
                val points = PointsRule.RECEIVE_IMPORTANT_VOTE
                gamification.apply(report.ownerId, if (voted) points else -points)
            }
            if (voted) awardTopReportOfMonth()
        }
        voted
    }

    override fun observeHasVoted(reportId: String, userId: String): Flow<Boolean> =
        source.observeHasVoted(reportId, userId).catch { emit(false) }

    override suspend fun verifyReport(reportId: String, moderatorId: String): Result<Unit> = safeCall {
        val report = source.findById(reportId) ?: error("El reporte no existe")
        check(report.status == ReportStatus.PENDING.name) { "Este reporte ya fue revisado" }
        source.verify(reportId, moderatorId)
        safeCall {
            val badge = if (gamification.verifiedCount(report.ownerId) >= VERIFIED_FOR_BADGE)
                Badge.TEN_VERIFIED_REPORTS else null
            gamification.apply(report.ownerId, PointsRule.REPORT_VERIFIED, badge)
            awardTopReportOfMonth()
        }
    }

    override suspend fun rejectReport(reportId: String, moderatorId: String, reason: String): Result<Unit> =
        safeCall {
            require(reason.isNotBlank()) { "Debes escribir el motivo del rechazo" }
            source.reject(reportId, moderatorId, reason.trim())
        }

    override suspend fun markResolved(reportId: String, actorId: String): Result<Unit> =
        safeCall { source.markResolved(reportId, actorId) }

    private suspend fun awardTopReportOfMonth() {
        val top = gamification.topReportOfMonth() ?: return
        if (top.importantCount >= MIN_VOTES_TOP_REPORT) {
            gamification.apply(top.ownerId, badge = Badge.TOP_REPORT_OF_MONTH)
        }
    }


}
