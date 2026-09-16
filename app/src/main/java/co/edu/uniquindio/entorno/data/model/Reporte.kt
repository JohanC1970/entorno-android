package co.edu.uniquindio.entorno.data.model

data class Reporte(
    val id: String = "",
    val titulo: String = "",
    val categoria: String = "",
    val descripcion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val imagenes: List<String> = emptyList(),
    val estado: String = "pendiente", // pendiente, verificado, rechazado, resuelto
    val autorId: String = "",
    val votosImportante: Int = 0,
    val fechaCreacion: Long = System.currentTimeMillis()
)


