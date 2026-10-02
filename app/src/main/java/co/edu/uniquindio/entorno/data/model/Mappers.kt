package co.edu.uniquindio.entorno.data.model

import co.edu.uniquindio.entorno.core.util.toEnumOrDefault
import co.edu.uniquindio.entorno.domain.model.*
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

private fun Date?.toLocalDate(): LocalDate =
    (this?.toInstant() ?: Instant.now()).atZone(ZoneId.systemDefault()).toLocalDate()

// User
fun UserDto.toDomain() = User(
    id = id, name = name, email = email, city = city, address = address,
    location = if (latitude != null && longitude != null) Location(latitude, longitude) else null,
    phoneNumber = phoneNumber, profilePictureUrl = profilePictureUrl,
    role = role.toEnumOrDefault(UserRole.USER),
    points = points,
    badges = badges.mapNotNull { name -> Badge.entries.firstOrNull { it.name == name } },
    createdAt = createdAt?.time ?: 0L
)

// Report
fun ReportDto.toDomain() = Report(
    id = id, title = title, description = description,
    category = category.toEnumOrDefault(ReportCategory.COMMUNITY),
    location = Location(latitude, longitude),
    status = status.toEnumOrDefault(ReportStatus.PENDING),
    imageUrls = imageUrls, ownerId = ownerId, ownerName = ownerName,
    date = createdAt.toLocalDate(),
    rejectionReason = rejectionReason,
    importantCount = importantCount, commentCount = commentCount
)

/** Solo al crear: el reporte siempre se crea en estado PENDING y sin votos. */
fun Report.toNewDto() = ReportDto(
    ownerId = ownerId, ownerName = ownerName, title = title.trim(),
    category = category.name, description = description.trim(),
    latitude = location.latitude, longitude = location.longitude,
    geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(location.latitude, location.longitude)),
    imageUrls = imageUrls,
    status = ReportStatus.PENDING.name
)

// Comment
fun CommentDto.toDomain() = Comment(
    id = id, reportId = reportId, authorId = authorId, authorName = authorName,
    text = text, createdAt = createdAt?.time ?: 0L
)

fun Comment.toDto() = CommentDto(
    reportId = reportId, authorId = authorId, authorName = authorName, text = text.trim()
)

// Notification
fun NotificationDto.toDomain() = AppNotification(
    id = id, userId = userId,
    type = type.toEnumOrDefault(NotificationType.NEW_COMMENT),
    title = title, message = message, reportId = reportId, read = read,
    createdAt = createdAt?.time ?: 0L
)

fun AppNotification.toDto() = NotificationDto(
    userId = userId, type = type.name, title = title, message = message,
    reportId = reportId, read = read
)