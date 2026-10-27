package it.maestro.app.feature.table.ui

import it.maestro.app.domain.match.Beat
import it.maestro.app.domain.match.MatchSession
import it.maestro.app.domain.match.SessionPlay
import it.maestro.app.feature.table.presentation.director.BeatScript
import it.maestro.app.feature.table.presentation.director.SceneStep
import it.maestro.app.feature.table.presentation.director.TableScene
import it.maestro.game.model.Seat
import it.maestro.game.scopa.ScopaConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Task F1R-12a (`docs/app/features/table.md` §3): cosa mostra il tavolo in ogni passo della regia. */
class StageModelTest {

    private val human = Seat.P1

    /** Prima battuta dell'umano (o del bot) con presa, cercata sui seed fissi. */
    private fun firstBeat(actor: Seat, captures: Boolean): Beat {
        for (seed in 1L..300L) {
            var session = MatchSession.start(ScopaConfig(), seed)
            repeat(40) {
                val moves = session.legalMovesFor(session.toMove)
                if (moves.isEmpty()) return@repeat
                val move = moves.firstOrNull { (it.capture.isNotEmpty()) == captures } ?: moves.first()
                val applied = session.play(move, human) as SessionPlay.Applied
                if (applied.beat.actor == actor && (move.capture.isNotEmpty()) == captures) return applied.beat
                session = applied.session
            }
        }
        error("Nessuna battuta")
    }

    private fun sceneAt(beat: Beat, step: SceneStep?): TableScene =
        TableScene(beat.viewBefore, human, step = step, beatId = beat.id, busy = step != null, after = beat.viewAfter)

    @Test
    fun atRestTheStageShowsTheViewAsIs() {
        val beat = firstBeat(human, captures = false)
        val model = StageModel.from(TableScene(beat.viewAfter, human))
        assertEquals(beat.viewAfter.hand, model.hand)
        assertEquals(beat.viewAfter.table, model.table)
        assertEquals(beat.viewAfter.deckSize, model.deck)
        assertEquals(null, model.played)
    }

    @Test
    fun humanCardInFlightLeavesTheHandAndStaysVisibleInThePlayZone() {
        val beat = firstBeat(human, captures = false)
        val step = BeatScript.steps(beat, human).first() as SceneStep.CardToPlayZone
        val model = StageModel.from(sceneAt(beat, step))
        assertEquals(beat.viewBefore.hand - step.card, model.hand)
        assertEquals(step.card, model.played)
        assertEquals(beat.viewBefore.table, model.table)
    }

    @Test
    fun opponentRevealTurnsOneCoveredCardIntoTheVisiblePlayedCard() {
        val beat = firstBeat(human.other(), captures = false)
        val step = BeatScript.steps(beat, human).first() as SceneStep.OpponentReveal
        val model = StageModel.from(sceneAt(beat, step))
        assertEquals(beat.viewBefore.opponentHandSize - 1, model.opponentHand)
        assertEquals(step.card, model.played)
    }

    @Test
    fun captureHighlightLightsUpTheCardsAboutToBeTaken() {
        val beat = firstBeat(human, captures = true)
        val step = BeatScript.steps(beat, human).filterIsInstance<SceneStep.CaptureHighlight>().single()
        val model = StageModel.from(sceneAt(beat, step))
        assertTrue(step.captured.isNotEmpty())
        assertEquals(step.captured, model.highlighted)
        assertTrue(model.table.containsAll(step.captured), "Le carte prese sono ancora a terra")
    }

    @Test
    fun afterTheCaptureFlightTheBoardIsTheOneAfterTheMove() {
        val beat = firstBeat(human, captures = true)
        val step = BeatScript.steps(beat, human).filterIsInstance<SceneStep.CaptureFlight>().single()
        val model = StageModel.from(sceneAt(beat, step))
        assertEquals(beat.viewAfter.table, model.table)
        assertEquals(beat.viewAfter.capturedBy[human]?.size, model.humanCaptured)
        assertFalse(model.highlighted.isNotEmpty())
        assertNotNull(model)
    }
}
