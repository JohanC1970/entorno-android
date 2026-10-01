package co.edu.uniquindio.entorno.domain.repository

interface ImageRepository {

    /** Sube una imagen (uri local) al servicio externo y devuelve la URL pública. */
    suspend fun uploadImage(localUri: String): Result<String>
    suspend fun uploadImages(localUris: List<String>): Result<List<String>>

}