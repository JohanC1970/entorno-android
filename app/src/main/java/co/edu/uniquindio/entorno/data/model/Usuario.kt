package co.edu.uniquindio.entorno.data.model

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: String = "usuario", // usuario, moderador
    val nivel: String = "Novato",
    val puntos: Int = 0
)
