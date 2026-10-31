package it.maestro.app.domain.match

import it.maestro.app.domain.opponents.Opponent
import it.maestro.game.scopa.ScopaConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** F1G-09: le regole fisse della Scalata seguono l'abbinamento deciso da Michele (2026-10-31). */
class StoryHouseRulesTest {
    @Test
    fun `contro Gigi nessuna regola in piu e restano i default della specifica`() {
        assertEquals(emptyList(), StoryHouseRules.inForce(Opponent.GIGI))
        assertEquals(HouseRules(), StoryHouseRules.forOpponent(Opponent.GIGI))
        assertNull(StoryHouseRules.enteringWith(Opponent.GIGI))
    }

    @Test
    fun `contro Nonna Rosa vale il Re bello, sbloccato da Gigi`() {
        assertEquals(listOf(HouseRule.RE_BELLO), StoryHouseRules.inForce(Opponent.NONNA_ROSA))
        assertEquals(HouseRules(reBello = true), StoryHouseRules.forOpponent(Opponent.NONNA_ROSA))
        assertEquals(HouseRule.RE_BELLO, StoryHouseRules.enteringWith(Opponent.NONNA_ROSA))
    }

    @Test
    fun `contro Tonino valgono Re bello e tre Re, sbloccati da Gigi e Nonna Rosa`() {
        assertEquals(listOf(HouseRule.RE_BELLO, HouseRule.REDEAL_ON_THREE_KINGS), StoryHouseRules.inForce(Opponent.TONINO))
        assertEquals(HouseRules(reBello = true, redealOnThreeKings = true), StoryHouseRules.forOpponent(Opponent.TONINO))
        assertEquals(HouseRule.REDEAL_ON_THREE_KINGS, StoryHouseRules.enteringWith(Opponent.TONINO))
    }

    @Test
    fun `contro Don Alfonso si aggiunge la napola, sbloccata da Tonino`() {
        assertEquals(
            listOf(HouseRule.RE_BELLO, HouseRule.REDEAL_ON_THREE_KINGS, HouseRule.NAPOLA),
            StoryHouseRules.inForce(Opponent.DON_ALFONSO),
        )
        assertEquals(HouseRules(reBello = true, napola = true), StoryHouseRules.forOpponent(Opponent.DON_ALFONSO))
        assertEquals(HouseRule.NAPOLA, StoryHouseRules.enteringWith(Opponent.DON_ALFONSO))
    }

    @Test
    fun `contro il Maestro si aggiunge l'asso che taglia, sbloccato da Don Alfonso`() {
        assertEquals(HouseRule.entries, StoryHouseRules.inForce(Opponent.MAESTRO))
        assertEquals(
            HouseRules(reBello = true, napola = true, assoPigliatutto = true),
            StoryHouseRules.forOpponent(Opponent.MAESTRO),
        )
        assertEquals(HouseRule.ASSO_PIGLIATUTTO, StoryHouseRules.enteringWith(Opponent.MAESTRO))
    }

    @Test
    fun `le regole fisse arrivano alla configurazione di partita`() {
        assertEquals(
            ScopaConfig(targetScore = 16, redealOnThreeKings = true, reBello = true, napola = true, assoPigliatutto = true),
            StoryHouseRules.resolve(Opponent.MAESTRO, sparring = false, chosen = HouseRules()).toConfig(16),
        )
        assertEquals(
            ScopaConfig(targetScore = 11, reBello = true),
            StoryHouseRules.resolve(Opponent.NONNA_ROSA, sparring = false, chosen = HouseRules()).toConfig(11),
        )
    }

    @Test
    fun `in Classica la scelta del giocatore non puo disattivare le regole fisse`() {
        val allOff = HouseRules(reBello = false, redealOnThreeKings = false, napola = false, assoPigliatutto = false)

        assertEquals(StoryHouseRules.forOpponent(Opponent.DON_ALFONSO), StoryHouseRules.resolve(Opponent.DON_ALFONSO, false, allOff))
        assertEquals(StoryHouseRules.forOpponent(Opponent.GIGI), StoryHouseRules.resolve(Opponent.GIGI, false, allOff))
    }

    @Test
    fun `in Sparring la scelta del giocatore resta libera come prima`() {
        val chosen = HouseRules(reBello = true, redealOnThreeKings = false, napola = true, assoPigliatutto = true)

        assertEquals(chosen, StoryHouseRules.resolve(Opponent.GIGI, sparring = true, chosen = chosen))
    }
}
