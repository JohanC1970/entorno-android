package co.edu.uniquindio.entorno.data.repository

import co.edu.uniquindio.entorno.core.util.safeCall
import co.edu.uniquindio.entorno.data.remote.CloudinaryDataSource
import co.edu.uniquindio.entorno.domain.repository.ImageRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    private val cloudinary: CloudinaryDataSource
) : ImageRepository {

    override suspend fun uploadImage(localUri: String): Result<String> =
        safeCall { cloudinary.upload(localUri) }

    override suspend fun uploadImages(localUris: List<String>): Result<List<String>> = safeCall {
        coroutineScope { localUris.map { async { cloudinary.upload(it) } }.awaitAll() }
    }
}