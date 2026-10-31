package it.maestro.app.domain.guide

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaMove
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** F1G-02: quale passo insegna il Maestro a ogni turno e quando la mossa fatta era quella giusta. */
class GuidedHandTest {

    private fun card(rank: Rank, suit: Suit = Suit.COPPE) = Card(rank, suit)
    private fun lay(card: Card) = ScopaMove(card, emptySet())
    private fun take(card: Card, vararg captured: Card) = ScopaMove(card, captured.toSet())

    private val sevenOfCoins = card(Rank.SETTE, Suit.DENARI)

    @Test
    fun theFirstTurnAlwaysTeachesPickingACardEvenWhenAnythingCanBeTaken() {
        val two = card(Rank.DUE)
        val line = GuidedHand.step(emptySet(), 0, listOf(take(two, card(Rank.DUE, Suit.SPADE))), listOf(card(Rank.DUE, Suit.SPADE)))
        assertEquals(GuideLine(GuideStep.PICK, GuideTone.INSTRUCTION), line)
    }

    @Test
    fun aMatchingCardTeachesTheSingleCaptureAndNamesTheCardOnTheTable() {
        val onTable = card(Rank.TRE, Suit.SPADE)
        val table = listOf(onTable, card(Rank.RE, Suit.SPADE))
        val line = GuidedHand.step(setOf(GuideStep.PICK), 1, listOf(take(card(Rank.TRE), onTable), lay(card(Rank.CINQUE))), table)
        assertEquals(GuideStep.SINGLE, line?.step)
        assertEquals(onTable, line?.card)
    }

    @Test
    fun aSumCaptureWithoutAMatchTeachesTheSum() {
        val table = listOf(card(Rank.DUE, Suit.SPADE), card(Rank.TRE, Suit.SPADE), card(Rank.RE, Suit.BASTONI))
        val moves = listOf(take(card(Rank.CINQUE), table[0], table[1]), lay(card(Rank.ASSO)))
        assertEquals(GuideStep.SUM, GuidedHand.step(setOf(GuideStep.PICK), 1, moves, table)?.step)
    }

    @Test
    fun whenNothingCanBeTakenTheMaestroTeachesLayingACard() {
        val moves = listOf(lay(card(Rank.RE)), lay(card(Rank.ASSO)))
        assertEquals(GuideStep.LAY, GuidedHand.step(setOf(GuideStep.PICK), 1, moves, listOf(card(Rank.DUE, Suit.SPADE)))?.step)
    }

    @Test
    fun clearingTheTableTeachesTheScopa() {
        val onTable = card(Rank.QUATTRO, Suit.SPADE)
        val moves = listOf(take(card(Rank.QUATTRO), onTable))
        val taught = setOf(GuideStep.PICK, GuideStep.SINGLE)
        assertEquals(GuideStep.SCOPA, GuidedHand.step(taught, 1, moves, listOf(onTable))?.step)
    }

    @Test
    fun theSettebelloComesBeforeTheOtherLessonsBecauseItIsRare() {
        val moves = listOf(take(card(Rank.SETTE), sevenOfCoins), lay(card(Rank.RE)))
        val table = listOf(sevenOfCoins, card(Rank.RE, Suit.SPADE))
        assertEquals(GuideStep.SETTE, GuidedHand.step(setOf(GuideStep.PICK), 1, moves, table)?.step)
    }

    @Test
    fun aStepAlreadyTaughtIsNotRepeatedAndWhenAllAreTaughtTheMaestroIsQuiet() {
        val moves = listOf(lay(card(Rank.RE)))
        val table = listOf(card(Rank.DUE, Suit.SPADE))
        assertNull(GuidedHand.step(GuideStep.entries.toSet(), 5, moves, table))
    }

    @Test
    fun withoutLegalMovesThereIsNothingToTeach() {
        assertNull(GuidedHand.step(emptySet(), 0, emptyList(), emptyList()))
    }

    @Test
    fun theMoveJudgedRightIsTheOneTaught() {
        val table = listOf(card(Rank.TRE, Suit.SPADE), card(Rank.DUE, Suit.SPADE))
        assertTrue(GuidedHand.isRight(GuideStep.SINGLE, take(card(Rank.TRE), table[0]), table))
        assertFalse(GuidedHand.isRight(GuideStep.SINGLE, lay(card(Rank.RE)), table))
        assertTrue(GuidedHand.isRight(GuideStep.SUM, take(card(Rank.CINQUE), table[0], table[1]), table))
        assertFalse(GuidedHand.isRight(GuideStep.SUM, take(card(Rank.TRE), table[0]), table))
        assertTrue(GuidedHand.isRight(GuideStep.SCOPA, take(card(Rank.CINQUE), table[0], table[1]), table))
        assertFalse(GuidedHand.isRight(GuideStep.SCOPA, take(card(Rank.TRE), table[0]), table))
        assertTrue(GuidedHand.isRight(GuideStep.SETTE, take(card(Rank.SETTE), sevenOfCoins), listOf(sevenOfCoins)))
        assertFalse(GuidedHand.isRight(GuideStep.SETTE, lay(card(Rank.RE)), listOf(sevenOfCoins)))
    }

    @Test
    fun pickingAndLayingHaveNoWrongMove() {
        val table = listOf(card(Rank.DUE, Suit.SPADE))
        assertTrue(GuidedHand.isRight(GuideStep.PICK, lay(card(Rank.RE)), table))
        assertTrue(GuidedHand.isRight(GuideStep.LAY, lay(card(Rank.RE)), table))
        assertNotNull(GuideTexts.res(GuideLine(GuideStep.PICK, GuideTone.OK)))
        assertNull(GuideTexts.res(GuideLine(GuideStep.PICK, GuideTone.WRONG)))
        assertNull(GuideTexts.res(GuideLine(GuideStep.LAY, GuideTone.WRONG)))
    }
}
