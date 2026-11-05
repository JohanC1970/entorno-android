package co.edu.uniquindio.entorno.core.filter

import co.edu.uniquindio.entorno.core.util.Geo
import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.Report

enum class LocationScope(val label: String) {
    ALL("Toda la ciudad"),
    NEARBY("Cercanos"),
    NEIGHBORHOOD("Mi barrio")
}

/** Aplica el alcance elegido tomando como centro la ubicación del usuario. */
fun LocationScope.filterBy(reports: List<Report>, center: Location, radiusKm: Int): List<Report> =
    when (this) {
        LocationScope.ALL -> reports
        LocationScope.NEARBY ->
            reports.filter { Geo.distanceMeters(center, it.location) <= radiusKm * 1000.0 }
        LocationScope.NEIGHBORHOOD -> {
            val mine = Geo.nearestNeighborhood(center)
            reports.filter { it.neighborhood == mine }
        }
    }