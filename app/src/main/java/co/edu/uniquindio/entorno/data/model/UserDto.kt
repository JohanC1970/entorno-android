package co.edu.uniquindio.entorno.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserDto(
    @DocumentId val id: String = "",
    val name: String = "",
    val email: String = "",
    val city: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val role: String = "USER",
    val points: Int = 0,
    val badges: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Date? = null
)
