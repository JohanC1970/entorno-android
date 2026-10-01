package co.edu.uniquindio.entorno.data.remote


import co.edu.uniquindio.entorno.core.util.asFlow
import co.edu.uniquindio.entorno.data.model.CommentDto
import co.edu.uniquindio.entorno.data.model.NotificationDto
import co.edu.uniquindio.entorno.data.model.ReportDto
import co.edu.uniquindio.entorno.domain.model.NotificationType
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Reportes, votos y comentarios.
 * Como no hay backend propio, las operaciones que deben ser consistentes
 * (comentar + contador + notificación, moderar + notificación) se hacen
 * en un solo batch/transacción.
 */
@Singleton
class ReportRemoteDataSource @Inject constructor(private val db: FirebaseFirestore){

    private val reports = db.collection("reports")
    private val notifications = db.collection("collections")
    private val publicStatuses = listOf(ReportStatus.PENDING.name, ReportStatus.VERIFIED.name)


    // -------  LECTURA  --------------------------------------------

    fun observeFeed(category: ReportCategory?): Flow<List<ReportDto>> {
        var query: Query = reports.whereIn("status", publicStatuses)
        if (category != null) query = query.whereEqualTo("category", category.name)
        return query.orderBy("createdAt",
            Query.Direction.DESCENDING).asFlow<ReportDto>()
    }

    fun observeById(id: String ): Flow<ReportDto?> =
        reports.document(id).asFlow<ReportDto>()

    suspend fun findById(id: String): ReportDto? =
        reports.document(id).get().await().toObject(ReportDto::class.java)

    fun observeByOwner(userId: String): Flow<List<ReportDto>> =
        reports.whereEqualTo("ownerId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .asFlow<ReportDto>()

    fun observeByStatus(status: ReportStatus?): Flow<List<ReportDto>> {
        val base: Query = if (status == null) reports
            else reports.whereEqualTo("status", status.name)

        return base.orderBy("createdAt",
            Query.Direction.DESCENDING).asFlow<ReportDto>()
    }

    /**
     * Usa geohash (GeoFire) para traer solo los reportes dentro del radio
     */
    suspend fun getNearby( lat: Double, lng: Double, radiusMeters: Double,
                           category: ReportCategory?): List<ReportDto> {

        val center = GeoLocation(lat,lng)
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center,radiusMeters)
        val snapshots = coroutineScope {
            bounds.map { b ->
                async {
                    reports.orderBy("geohash").startAt(b.startHash).endAt(b.endHash).get().await()
                }
            }.awaitAll()
        }
        return snapshots
            .flatMap { it.toObjects(ReportDto::class.java) }
            .filter { dto ->
                GeoFireUtils.getDistanceBetween(GeoLocation(dto.latitude, dto.longitude), center) <= radiusMeters &&
                        dto.status in publicStatuses &&
                        (category == null || dto.category == category.name)
            }
    }



    // -------------- ESCRITURA -------------------------

    suspend fun create(dto: ReportDto): String {
        val ref = reports.document()
        ref.set(dto).await()
        return ref.id
    }

    suspend fun update(reportId: String, fields: Map<String, Any?>) {
        reports.document(reportId).update(fields + ("updatedAt" to FieldValue.serverTimestamp())).await()
    }

    suspend fun delete(reportId: String) {
        val ref = reports.document(reportId)
        val comments = ref.collection("comments").get().await()
        val votes = ref.collection("votes").get().await()
        val batch = db.batch()
        (comments.documents + votes.documents).forEach { batch.delete(it.reference) }
        batch.delete(ref)
        batch.commit().await()
    }


    // ---------- VOTOS ----------------

    /** Un voto por usuario: doc reports/{id}/votes/{userId}. Devuelve true si quedó votado. */
    suspend fun toggleImportant(reportId: String, userId: String): Boolean {
        val reportRef = reports.document(reportId)
        val voteRef = reportRef.collection("votes").document(userId)
        return db.runTransaction { tx ->
            val alreadyVoted = tx.get(voteRef).exists()
            if (alreadyVoted) {
                tx.delete(voteRef)
                tx.update(reportRef, "importantCount", FieldValue.increment(-1))
                false
            } else {
                tx.set(voteRef, mapOf("createdAt" to FieldValue.serverTimestamp()))
                tx.update(reportRef, "importantCount", FieldValue.increment(1))
                true
            }
        }.await()
    }

    fun observeHasVoted(reportId: String, userId: String): Flow<Boolean> = callbackFlow {
        val registration = reports.document(reportId).collection("votes").document(userId)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snap?.exists() == true)
            }
        awaitClose { registration.remove() }
    }


    // ------- COMENTARIOS -----------

    fun observeComments(reportId: String): Flow<List<CommentDto>> =
        reports.document(reportId).collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .asFlow<CommentDto>()

    suspend fun addComment(reportId: String, comment: CommentDto) {
        val reportRef = reports.document(reportId)
        val report = reportRef.get().await().toObject(ReportDto::class.java)
            ?: error("El reporte ya no existe")
        val batch = db.batch()
        batch.set(reportRef.collection("comments").document(), comment)
        batch.update(reportRef, "commentCount", FieldValue.increment(1))
        if (report.ownerId != comment.authorId) {
            batch.set(
                notifications.document(),
                NotificationDto(
                    userId = report.ownerId,
                    type = NotificationType.NEW_COMMENT.name,
                    title = "Nuevo comentario",
                    message = "${comment.authorName} comentó en \"${report.title}\"",
                    reportId = reportId
                )
            )
        }
        batch.commit().await()
    }


    // -------- MÉTODOS DEL MODERADOR Y CAMBIOS DE ESTADO ---------------

    suspend fun verify(reportId: String, actorId: String) = updateAndNotify(
        reportId, actorId,
        mapOf("status" to ReportStatus.VERIFIED.name, "rejectionReason" to null),
        NotificationType.REPORT_VERIFIED, "Reporte verificado"
    ) { "Tu reporte \"${it.title}\" fue verificado por un moderador" }

    suspend fun reject(reportId: String, actorId: String, reason: String) = updateAndNotify(
        reportId, actorId,
        mapOf("status" to ReportStatus.REJECTED.name, "rejectionReason" to reason),
        NotificationType.REPORT_REJECTED, "Reporte rechazado"
    ) { "Tu reporte \"${it.title}\" fue rechazado. Motivo: $reason" }

    suspend fun markResolved(reportId: String, actorId: String) = updateAndNotify(
        reportId, actorId,
        mapOf("status" to ReportStatus.RESOLVED.name),
        NotificationType.REPORT_RESOLVED, "Reporte resuelto"
    ) { "Tu reporte \"${it.title}\" fue marcado como resuelto" }

    private suspend fun updateAndNotify(
        reportId: String,
        actorId: String,
        updates: Map<String, Any?>,
        type: NotificationType,
        title: String,
        message: (ReportDto) -> String
    ) {
        val ref = reports.document(reportId)
        val report = ref.get().await().toObject(ReportDto::class.java) ?: error("El reporte no existe")
        val batch = db.batch()
        batch.update(ref, updates + ("updatedAt" to FieldValue.serverTimestamp()))
        if (report.ownerId != actorId) {
            batch.set(
                notifications.document(),
                NotificationDto(
                    userId = report.ownerId, type = type.name,
                    title = title, message = message(report), reportId = reportId
                )
            )
        }
        batch.commit().await()
    }

}