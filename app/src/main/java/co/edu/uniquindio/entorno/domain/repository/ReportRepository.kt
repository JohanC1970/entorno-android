package co.edu.uniquindio.entorno.domain.repository

import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ReportRepository {
    /** Feed público: reportes PENDING o VERIFIED (no rechazados ni resueltos). */
    fun observeFeed(category: ReportCategory? = null): Flow<List<Report>>
    fun observeReport(id: String): Flow<Report?>
    suspend fun findById(id: String): Result<Report?>
    fun observeUserReports(userId: String): Flow<List<Report>>

    /** Para moderadores. status = null devuelve todos. */
    fun observeModerationQueue(status: ReportStatus? = ReportStatus.PENDING): Flow<List<Report>>

    /** Consulta geoespacial: filtro "cercanos", mapa de calor (Fase 7) e IA de duplicados. */
    suspend fun getNearbyReports(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double = 2000.0,
        category: ReportCategory? = null
    ): Result<List<Report>>

    suspend fun createReport(report: Report): Result<String>
    suspend fun updateReport(report: Report): Result<Unit>
    suspend fun deleteReport(id: String): Result<Unit>

    /** Alterna el voto "Es importante". Devuelve true si quedó votado. */
    suspend fun toggleImportant(reportId: String, userId: String): Result<Boolean>
    fun observeHasVoted(reportId: String, userId: String): Flow<Boolean>

    suspend fun verifyReport(reportId: String, moderatorId: String): Result<Unit>
    suspend fun rejectReport(reportId: String, moderatorId: String, reason: String): Result<Unit>
    suspend fun markResolved(reportId: String, actorId: String): Result<Unit>

    /** Todos los reportes sin importar el estado: estadísticas y mapa de calor. */
    fun observeAllReports(): Flow<List<Report>> = observeModerationQueue(null)

    /** Cantidad de votos "Es importante" que ha dado el usuario. */
    fun observeVotesGiven(userId: String): Flow<Int> = flowOf(0)
}