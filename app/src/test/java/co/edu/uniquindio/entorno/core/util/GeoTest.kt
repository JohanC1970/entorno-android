package co.edu.uniquindio.entorno.core.util

import co.edu.uniquindio.entorno.domain.model.Location
import org.junit.Assert.assertEquals
import org.junit.Test

class GeoTest {

    @Test fun `la distancia a si mismo es cero`() {
        assertEquals(0.0, Geo.distanceMeters(Geo.ARMENIA_CENTER, Geo.ARMENIA_CENTER), 0.001)
    }

    @Test fun `un grado de latitud mide unos 111 km`() {
        val a = Location(latitude = 4.0, longitude = -75.0)
        val b = Location(latitude = 5.0, longitude = -75.0)
        assertEquals(111_195.0, Geo.distanceMeters(a, b), 200.0)
    }

    @Test fun `la distancia es simetrica`() {
        val a = Geo.neighborhoods.first().center
        val b = Geo.neighborhoods.last().center
        assertEquals(Geo.distanceMeters(a, b), Geo.distanceMeters(b, a), 0.001)
    }

    @Test fun `el barrio mas cercano a su propio centro es ese barrio`() {
        Geo.neighborhoods.forEach { assertEquals(it.name, Geo.nearestNeighborhood(it.center)) }
    }

    @Test fun `format usa punto decimal y cuatro decimales`() {
        assertEquals("4.5339, -75.6811", Geo.format(Geo.ARMENIA_CENTER))
    }
}
