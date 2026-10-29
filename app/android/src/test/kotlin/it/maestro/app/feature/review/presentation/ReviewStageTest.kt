package it.maestro.app.feature.review.presentation

import it.maestro.app.feature.review.ui.buildReviewSteps
import it.maestro.game.model.Seat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Task F1R-17a: la revisione mette in scena la mossa giocata e la migliore con la regia del tavolo. */
@OptIn(ExperimentalCoroutinesApi::class)
class ReviewStageTest {

    private val steps = buildReviewSteps(PostMatchFixtures.log, PostMatchFixtures.analysis, Seat.P1)

    @Test
    fun everyStepCarriesTheBeatOfTheMoveThatWasPlayed() {
        assertTrue(steps.isNotEmpty())
        steps.forEach { step ->
            assertEquals(step.analysis.playedMove, step.beat.move)
            assertEquals(step.playerView, step.beat.viewBefore)
            assertEquals(step.mover, step.beat.actor)
        }
    }

    @Test
    fun onlyAMistakeOfThePlayerHasABestBeatAndItPlaysTheBestMove() {
        steps.forEach { step ->
            val expected = step.mover == Seat.P1 && step.analysis.isMistake()
            assertEquals(expected, step.bestBeat != null, "passo ${step.moveIndex}")
            step.bestBeat?.let { assertEquals(step.analysis.bestMove, it.move) }
        }
    }

    @Test
    fun thePlayedMoveAnimatesThenTheTableRests() = runTest(StandardTestDispatcher()) {
        val step = steps.first()
        val stage = ReviewStage(TestScope(testScheduler), Seat.P1, step.playerView)
        stage.showPlayed(step)
        testScheduler.runCurrent()
        assertTrue(stage.scene.value.busy)
        advanceUntilIdle()
        assertFalse(stage.scene.value.busy)
        assertEquals(step.beat.viewAfter, stage.scene.value.view)
    }

    @Test
    fun restShowsThePositionBeforeTheMoveWithoutAnimation() = runTest(StandardTestDispatcher()) {
        val step = steps.first()
        val stage = ReviewStage(TestScope(testScheduler), Seat.P1, step.playerView)
        stage.rest(step)
        assertFalse(stage.scene.value.busy)
        assertEquals(step.playerView, stage.scene.value.view)
        assertNull(stage.scene.value.step)
    }

    @Test
    fun theBestMoveIsShownInsteadOfThePlayedOneWhenThereIsAMistake() = runTest(StandardTestDispatcher()) {
        val step = steps.firstOrNull { it.bestBeat != null } ?: return@runTest
        val stage = ReviewStage(TestScope(testScheduler), Seat.P1, step.playerView)
        stage.showBest(step)
        advanceUntilIdle()
        assertNotNull(step.bestBeat)
        assertEquals(step.bestBeat!!.viewAfter, stage.scene.value.view)
    }
}
