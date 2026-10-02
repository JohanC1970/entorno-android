package co.edu.uniquindio.entorno.domain.model

data class User(
    val id: String = "",
    val name: String,
    val email: String,
    val city: String = "",
    val address: String = "",
    val location: Location? = null,        // dirección elegida en el mapa
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val role: UserRole = UserRole.USER,
    val points: Int = 0,
    val badges: List<Badge> = emptyList(),
    val createdAt: Long = 0L
){
    val level: UserLevel get() = UserLevel.fromPoints(points)

}
