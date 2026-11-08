package co.edu.uniquindio.entorno.features.map

import co.edu.uniquindio.entorno.domain.model.Location
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeatWeightTest {

    private fun report(votes: Int) = Report(
        title = "t", description = "d", category = ReportCategory.SECURITY,
        location = Location(4.5339, -75.6811), imageUrls = emptyList(), ownerId = "u",
        importantCount = votes
    )

    @Test fun `un reporte sin votos pesa 1`() {
        assertEquals(1.0, report(0).heatWeight(), 0.0001)
    }

    @Test fun `el peso crece con los votos`() {
        assertTrue(report(9).heatWeight() > report(1).heatWeight())
    }

    @Test fun `el peso usa raiz de los votos`() {
        assertEquals(4.0, report(9).heatWeight(), 0.0001)
    }

    @Test fun `la franja de tres horas incluye su inicio y excluye su fin`() {
        assertTrue(inWindow(18, 18))
        assertTrue(inWindow(20, 18))
        assertTrue(!inWindow(21, 18))
    }

    @Test fun `la franja cruza la medianoche`() {
        assertTrue(inWindow(0, 22))
        assertTrue(!inWindow(2, 22))
    }

    @Test fun `formatHour usa formato de 12 horas`() {
        assertEquals("12 a. m.", formatHour(0))
        assertEquals("12 p. m.", formatHour(12))
        assertEquals("6 p. m.", formatHour(18))
    }
}
