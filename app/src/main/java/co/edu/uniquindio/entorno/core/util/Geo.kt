package co.edu.uniquindio.entorno.core.util

import co.edu.uniquindio.entorno.domain.model.Location
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Utilidades geográficas simples (sin SDK de mapas): distancias y barrios de Armenia. */
object Geo {

    /** Centro de Armenia, Quindío: ubicación por defecto del usuario. */
    val ARMENIA_CENTER = Location(latitude = 4.5339, longitude = -75.6811)

    data class Neighborhood(val name: String, val center: Location)

    /** Barrios de referencia (coordenadas aproximadas) para agrupar reportes por zona. */
    val neighborhoods: List<Neighborhood> = listOf(
        Neighborhood("Centro", Location(4.5339, -75.6811)),
        Neighborhood("La Castilla", Location(4.5445, -75.6735)),
        Neighborhood("Puerto Espejo", Location(4.5236, -75.6912)),
        Neighborhood("Fundadores", Location(4.5290, -75.6700)),
        Neighborhood("La Fachada", Location(4.5420, -75.6890)),
        Neighborhood("Los Naranjos", Location(4.5180, -75.6760))
    )

    /** Distancia en metros entre dos puntos (fórmula de Haversine). */
    fun distanceMeters(a: Location, b: Location): Double {
        val earthRadius = 6_371_000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLon / 2).pow(2)
        return 2 * earthRadius * asin(sqrt(h))
    }

    /** Nombre del barrio de referencia más cercano al punto. */
    fun nearestNeighborhood(location: Location): String =
        neighborhoods.minByOrNull { distanceMeters(it.center, location) }?.name ?: "Centro"

    fun format(location: Location): String =
        "%.4f, %.4f".format(java.util.Locale.US, location.latitude, location.longitude)
}
