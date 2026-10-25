package co.edu.uniquindio.entorno.features.map

import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import co.edu.uniquindio.entorno.domain.model.Location
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.Style
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val EARTH_RADIUS_METERS = 6_371_000.0

/** Mapbox usa (longitud, latitud), al revés que Google Maps. */
fun Location.toPoint(): Point = Point.fromLngLat(longitude, latitude)

fun Point.toLocation(): Location = Location(latitude = latitude(), longitude = longitude())

/** Calles con nombre en modo claro y estilo oscuro cuando la app está en oscuro. */
@Composable
fun mapStyleUri(): String = if (isSystemInDarkTheme()) Style.DARK else Style.MAPBOX_STREETS

/** Polígono que aproxima un círculo de [radiusMeters] metros alrededor de [center]. */
fun circlePolygon(center: Location, radiusMeters: Double, steps: Int = 64): Polygon {
    val lat1 = Math.toRadians(center.latitude)
    val lon1 = Math.toRadians(center.longitude)
    val angular = radiusMeters / EARTH_RADIUS_METERS
    val ring = (0 until steps).map { i ->
        val bearing = 2 * PI * i / steps
        val lat2 = asin(sin(lat1) * cos(angular) + cos(lat1) * sin(angular) * cos(bearing))
        val lon2 = lon1 + atan2(
            sin(bearing) * sin(angular) * cos(lat1),
            cos(angular) - sin(lat1) * sin(lat2)
        )
        Point.fromLngLat(Math.toDegrees(lon2), Math.toDegrees(lat2))
    }
    return Polygon.fromLngLats(listOf(ring + ring.first()))
}
