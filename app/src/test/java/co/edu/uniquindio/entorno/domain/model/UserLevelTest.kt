package co.edu.uniquindio.entorno.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserLevelTest {

    @Test fun `cero puntos es novato`() {
        assertEquals(UserLevel.NOVICE, UserLevel.fromPoints(0))
    }

    @Test fun `cada umbral de puntos activa su nivel`() {
        UserLevel.entries.forEach { assertEquals(it, UserLevel.fromPoints(it.minPoints)) }
    }

    @Test fun `un punto menos que el umbral queda en el nivel anterior`() {
        UserLevel.entries.zipWithNext().forEach { (lower, upper) ->
            assertEquals(lower, UserLevel.fromPoints(upper.minPoints - 1))
        }
    }

    @Test fun `muchos puntos dan heroe comunitario`() {
        assertEquals(UserLevel.COMMUNITY_HERO, UserLevel.fromPoints(1_000_000))
    }
}
