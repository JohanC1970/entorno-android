package co.edu.uniquindio.entorno.domain.model

data class UserSession(
    val userId: String,
    val role: UserRole
)