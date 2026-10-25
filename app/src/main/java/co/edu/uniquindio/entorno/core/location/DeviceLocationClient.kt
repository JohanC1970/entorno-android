package co.edu.uniquindio.entorno.core.location

import android.content.Context
import co.edu.uniquindio.entorno.domain.model.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/** Lee la ubicación actual del dispositivo (GPS / red) con FusedLocationProvider. */
@Singleton
class DeviceLocationClient @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    fun hasPermission(): Boolean = context.hasLocationPermission()

    /** Devuelve null si no hay permiso, el GPS está apagado o la lectura falla. */
    suspend fun currentLocation(): Location? {
        if (!hasPermission()) return null
        return try {
            val token = CancellationTokenSource()
            @Suppress("MissingPermission")
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token)
                .await()
                ?.let { Location(it.latitude, it.longitude) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }


}
