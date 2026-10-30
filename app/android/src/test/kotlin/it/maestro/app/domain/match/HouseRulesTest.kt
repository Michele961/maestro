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

    @Test
    fun `napola e asso che taglia entrano nella configurazione, spente di default`() {
        assertEquals(ScopaConfig(napola = true, assoPigliatutto = true), HouseRules(napola = true, assoPigliatutto = true).toConfig(11))
        assertEquals(false, HouseRules().toConfig(11).napola)
    }
}
