package it.maestro.app.domain.match

import it.maestro.game.scopa.ScopaConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class HouseRulesTest {
    @Test
    fun `le regole di default sono quelle della specifica`() {
        assertEquals(ScopaConfig(targetScore = 11), HouseRules().toConfig(11))
    }

    @Test
    fun `re bello, ridistribuzione e punteggio entrano nella configurazione della partita`() {
        val config = HouseRules(reBello = true, redealOnThreeKings = false).toConfig(21)

        assertEquals(ScopaConfig(targetScore = 21, redealOnThreeKings = false, reBello = true), config)
    }
}
