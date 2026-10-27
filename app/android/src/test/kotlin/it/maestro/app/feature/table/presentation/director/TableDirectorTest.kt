package it.maestro.app.feature.table.presentation.director

import it.maestro.app.designsystem.motion.MotionSpec
import it.maestro.app.domain.match.Beat
import it.maestro.app.domain.settings.GameSpeed
import it.maestro.game.model.Seat
import it.maestro.game.scopa.GameEvent
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaState
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import it.maestro.game.scopa.viewFor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Task F1R-10 (ADR-0015, `docs/app/table-director.md` §3–6): copione e regia con tempo virtuale. */
@OptIn(ExperimentalCoroutinesApi::class)
class TableDirectorTest {

    private val human = Seat.P1

    private class Played(val beat: Beat, val stateAfter: ScopaState)

    private fun beatOf(state: ScopaState, move: ScopaMove, id: Long): Played {
        val applied = play(state, move) as PlayResult.Applied
        return Played(
            Beat(id, state.toMove, move, applied.events, viewFor(state, human), viewFor(applied.state, human)),
            applied.state,
        )
    }

    /** Gioca partite da seed fissi finché trova una battuta che soddisfa [predicate]. */
    private fun findBeat(predicate: (Beat) -> Boolean): Beat {
        for (seed in 1L..400L) {
            var state = newMatch(ScopaConfig(), seed)
            var id = 0L
            while (true) {
                val legal = legalMoves(state)
                if (legal.isEmpty()) break
                val played = beatOf(state, legal.first(), ++id)
                if (predicate(played.beat)) return played.beat
                state = played.stateAfter
            }
        }
        error("Nessuna battuta trovata")
    }

    private fun Beat.has(predicate: (GameEvent) -> Boolean) = events.any(predicate)

    @Test
    fun humanCaptureIsPlayZoneHighlightFlight() {
        val beat = findBeat { it.actor == human && it.has { e -> e is GameEvent.Captured } && it.events.size == 2 }
        val steps = BeatScript.steps(beat, human)
        assertEquals(
            listOf(SceneStep.CardToPlayZone::class, SceneStep.CaptureHighlight::class, SceneStep.CaptureFlight::class),
            steps.map { it::class },
        )
    }

    @Test
    fun opponentDropIsRevealReadDrop() {
        val beat = findBeat { it.actor != human && it.events.size == 1 }
        assertEquals(
            listOf(SceneStep.OpponentReveal::class, SceneStep.OpponentReadPause::class, SceneStep.DropToTable::class),
            BeatScript.steps(beat, human).map { it::class },
        )
    }

    @Test
    fun sweepAddsTheCelebration() {
        val beat = findBeat { it.has { e -> e is GameEvent.Sweep } }
        assertTrue(BeatScript.steps(beat, human).any { it is SceneStep.Sweep })
    }

    @Test
    fun newHandsAreDealtOneAtATimeStartingFromTheNonDealer() {
        val beat = findBeat { it.has { e -> e is GameEvent.CardsDealt } && !it.has { e -> e is GameEvent.DealEnded } }
        val deal = BeatScript.steps(beat, human).filterIsInstance<SceneStep.DealCard>()
        val first = beat.viewAfter.dealer.other()
        assertEquals(listOf(first, first.other(), first, first.other(), first, first.other()), deal.map { it.toSeat })
    }

    @Test
    fun dealEndShowsSummaryThenANewDeal() {
        val beat = findBeat { it.has { e -> e is GameEvent.DealEnded } && !it.has { e -> e is GameEvent.MatchEnded } }
        val steps = BeatScript.steps(beat, human)
        val shuffleAt = steps.indexOf(SceneStep.Shuffle)
        assertTrue(steps.take(shuffleAt).any { it is SceneStep.DealSummaryRow })
        assertEquals(4, steps.drop(shuffleAt).count { it is SceneStep.DealCard && it.toSeat == null })
        assertEquals(6, steps.drop(shuffleAt).count { it is SceneStep.DealCard && it.toSeat != null })
    }

    private fun expectedMillis(beat: Beat, speed: GameSpeed) =
        BeatScript.steps(beat, human).sumOf { MotionSpec.millis(it.token, speed) }

    @Test
    fun beatLastsExactlyTheSumOfItsSteps() = runTest {
        val beat = findBeat { it.actor == human && it.events.size == 1 }
        val director = TableDirector(TestScope(StandardTestDispatcher(testScheduler)), human, beat.viewBefore)
        director.enqueue(beat)
        val start = currentTime
        director.awaitPresented(beat.id, 0L)
        assertEquals(expectedMillis(beat, GameSpeed.NORMAL), currentTime - start)
        assertEquals(beat.viewAfter, director.scene.value.view)
        assertFalse(director.scene.value.busy)
    }

    @Test
    fun fasterSpeedShortensTheScene() = runTest {
        val beat = findBeat { it.actor == human && it.events.size == 1 }
        val director = TableDirector(
            TestScope(StandardTestDispatcher(testScheduler)), human, beat.viewBefore, speed = { GameSpeed.FASTER },
        )
        director.enqueue(beat)
        val start = currentTime
        director.awaitPresented(beat.id, 0L)
        assertEquals(expectedMillis(beat, GameSpeed.FASTER), currentTime - start)
    }

    @Test
    fun twoQuickBeatsAreBothShownInOrder() = runTest {
        val first = findBeat { it.actor == human && it.events.size == 1 }
        val second = findBeat { it.actor != human && it.events.size == 1 }.copy(id = first.id + 1)
        val director = TableDirector(TestScope(StandardTestDispatcher(testScheduler)), human, first.viewBefore)
        val shown = mutableListOf<SceneStep>()
        backgroundScope.launch(StandardTestDispatcher(testScheduler)) {
            director.scene.collect { scene -> scene.step?.let { if (shown.lastOrNull() != it) shown += it } }
        }
        director.enqueue(first)
        director.enqueue(second)
        advanceUntilIdle()
        assertEquals(BeatScript.steps(first, human) + BeatScript.steps(second, human), shown)
        assertEquals(second.id, director.scene.value.beatId)
    }

    @Test
    fun skipJumpsToTheEndOfTheBeat() = runTest {
        val beat = findBeat { it.actor != human && it.events.size == 1 }
        val director = TableDirector(TestScope(StandardTestDispatcher(testScheduler)), human, beat.viewBefore)
        director.enqueue(beat)
        advanceTimeBy(10)
        runCurrent()
        director.skip()
        val start = currentTime
        director.awaitPresented(beat.id, 0L)
        assertTrue(currentTime - start < expectedMillis(beat, GameSpeed.NORMAL))
        assertEquals(beat.viewAfter, director.scene.value.view)
    }

    @Test
    fun inputIsDisabledWhileTheSceneIsBusy() = runTest {
        val beat = findBeat { it.actor != human && it.events.size == 1 && it.viewAfter.toMove == human }
        val director = TableDirector(TestScope(StandardTestDispatcher(testScheduler)), human, beat.viewBefore)
        director.enqueue(beat)
        runCurrent()
        assertFalse(director.scene.value.inputEnabled)
        advanceUntilIdle()
        assertTrue(director.scene.value.inputEnabled)
    }

    @Test
    fun restShowsTheViewWithoutAnimations() = runTest {
        val beat = findBeat { it.actor == human }
        val director = TableDirector(TestScope(StandardTestDispatcher(testScheduler)), human, beat.viewBefore)
        director.rest(beat.viewAfter, beatId = 7L)
        assertEquals(beat.viewAfter, director.scene.value.view)
        val start = currentTime
        director.awaitPresented(7L, 0L)
        assertEquals(start, currentTime)
    }
}
