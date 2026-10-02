package co.edu.uniquindio.entorno.domain.model

import java.time.LocalDate

/** Severidad determinada por los votos "Es importante". */
enum class Severity(val minVotes: Int) {
    LOW(0), MEDIUM(5), HIGH(15), CRITICAL(30);

    companion object {
        fun fromVotes(votes: Int): Severity = entries.last { votes >= it.minVotes }
    }
}

data class Report(
    val id: String = "",
    val title: String,
    val description: String,
    val category: ReportCategory,
    val location: Location,
    val status: ReportStatus = ReportStatus.PENDING,
    val imageUrls: List<String>,          // al menos una imagen
    val ownerId: String,
    val ownerName: String = "",
    val date: LocalDate = LocalDate.now(),
    val rejectionReason: String? = null,
    val importantCount: Int = 0,
    val commentCount: Int = 0
){
    val severity: Severity get() = Severity.fromVotes(importantCount)
}
