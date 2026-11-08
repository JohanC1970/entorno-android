package co.edu.uniquindio.entorno.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SeverityTest {

    @Test fun `sin votos la severidad es la mas baja`() {
        assertEquals(Severity.entries.first(), Severity.fromVotes(0))
    }

    @Test fun `cada umbral activa su severidad`() {
        Severity.entries.forEach { assertEquals(it, Severity.fromVotes(it.minVotes)) }
    }

    @Test fun `un voto menos que el umbral queda en el nivel anterior`() {
        Severity.entries.zipWithNext().forEach { (lower, upper) ->
            assertEquals(lower, Severity.fromVotes(upper.minVotes - 1))
        }
    }

    @Test fun `muchos votos dan la severidad maxima`() {
        assertEquals(Severity.CRITICAL, Severity.fromVotes(10_000))
    }
}
