package co.edu.uniquindio.entorno.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ReportDto(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val title: String = "",
    val category: String = "COMMUNITY",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geohash: String = "",
    val imageUrls: List<String> = emptyList(),
    val status: String = "PENDING",
    val rejectionReason: String? = null,
    val importantCount: Int = 0,
    val commentCount: Int = 0,
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
)