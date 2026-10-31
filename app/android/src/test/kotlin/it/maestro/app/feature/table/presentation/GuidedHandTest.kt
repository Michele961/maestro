package it.maestro.app.feature.table.presentation

import it.maestro.app.domain.guide.GuideStep
import it.maestro.app.domain.guide.GuideTone
import it.maestro.app.domain.guide.GuidedHand
import it.maestro.app.domain.match.HouseRules
import it.maestro.app.domain.opponents.Opponent
import it.maestro.game.model.Seat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** F1G-02: la mano d'esordio guidata sul tavolo: seme fisso, passi insegnati, reazione del Maestro, fine mano. */
class GuidedHandTest {

    private fun start(guided: Boolean = true, seed: Long = GuidedHand.SEED) = GameReducer.initial(
        GameIntent.StartMatch(
            config = HouseRules().toConfig(GuidedHand.TARGET_SCORE),
            level = Opponent.GIGI.level,
            seed = seed,
            guided = guided,
        ),
    ).state

    @Test
    fun anOrdinaryMatchHasNoGuide() {
        assertNull(start(guided = false).toUiState().guide)
    }

    @Test
    fun theGuidedHandStartsWithTheIntroductionAndNothingTaught() {
        val ui = start().toUiState()
        assertNotNull(ui.guide)
        assertTrue(ui.guide!!.intro)
        assertFalse(ui.guide!!.finished)
    }

    @Test
    fun theIntroductionIsSeenOnce() {
        val state = GameReducer.reduce(start(), GameIntent.DismissGuideIntro).state
        assertFalse(state.toUiState().guide!!.intro)
    }

    @Test
    fun theHandIsTheSameAtEveryStart() {
        val first = start().session.view(Seat.P1)
        val second = start().session.view(Seat.P1)
        assertEquals(first.hand, second.hand)
        assertEquals(first.table, second.table)
        assertEquals(GuidedHand.SEED, start().matchLog.seed)
    }

    @Test
    fun theFirstMoveIsPraisedWhateverCardIsPlayed() {
        val praise = GuidedHandSimulation.play(GuidedHand.SEED).firstFeedback
        assertEquals(GuideStep.PICK, praise?.step)
        assertEquals(GuideTone.OK, praise?.tone)
    }

    @Test
    fun finishingTheGuideEndsTheGuidedHand() {
        val state = GameReducer.reduce(start(), GameIntent.FinishGuide).state
        assertTrue(state.toUiState().guide!!.finished)
        assertNull(state.toUiState().guide!!.line)
    }

    @Test
    fun theFixedSeedTeachesEveryStepInTheOrderOfTheGuide() {
        val result = GuidedHandSimulation.play(GuidedHand.SEED)
        assertTrue(result.finished, "la mano arriva fino al riepilogo")
        val inOrder = listOf(GuideStep.PICK, GuideStep.SINGLE, GuideStep.SUM, GuideStep.LAY, GuideStep.SCOPA, GuideStep.SETTE)
        assertEquals(inOrder, result.taughtInOrder)
    }

    @Test
    fun theFixedSeedGivesTheSameHandAndTheSameMovesEveryTime() {
        val a = GuidedHandSimulation.play(GuidedHand.SEED)
        val b = GuidedHandSimulation.play(GuidedHand.SEED)
        assertEquals(a.firstHand, b.firstHand)
        assertEquals(a.moves, b.moves)
    }
}
