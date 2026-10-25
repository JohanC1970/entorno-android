package co.edu.uniquindio.entorno.core.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import co.edu.uniquindio.entorno.domain.model.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

data class ResolvedAddress(val city: String, val address: String)

/** Geocodificación inversa: de coordenadas a ciudad y dirección. */
@Singleton
class AddressResolver @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    suspend fun resolve(location: Location): ResolvedAddress? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.forLanguageTag("es-CO"))
        return try {
            lookup(geocoder, location)?.toResolvedAddress()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun lookup(geocoder: Geocoder, location: Location): Address? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(
                    location.latitude, location.longitude, 1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            cont.resume(addresses.firstOrNull())
                        }

                        override fun onError(errorMessage: String?) {
                            cont.resume(null)
                        }
                    }
                )
            }
        } else {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
            }
        }

    private fun Address.toResolvedAddress(): ResolvedAddress {
        val street = listOfNotNull(thoroughfare, subThoroughfare?.let { "# $it" }).joinToString(" ")
        return ResolvedAddress(
            city = locality ?: subAdminArea.orEmpty(),
            address = street.ifBlank { getAddressLine(0).orEmpty().substringBefore(",") }
        )
    }
}