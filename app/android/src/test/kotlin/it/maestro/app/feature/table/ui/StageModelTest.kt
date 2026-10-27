package it.maestro.app.feature.table.ui

import it.maestro.app.domain.match.Beat
import it.maestro.app.domain.match.MatchSession
import it.maestro.app.domain.match.SessionPlay
import it.maestro.app.feature.table.presentation.director.BeatScript
import it.maestro.app.feature.table.presentation.director.SceneStep
import it.maestro.app.feature.table.presentation.director.TableScene
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.GameEvent
import it.maestro.game.scopa.ScopaConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Task F1R-12a/b (`docs/app/features/table.md` §3): cosa mostra il tavolo in ogni passo della regia. */
class StageModelTest {

    private val human = Seat.P1

    /** Battute di partite a seed fissi, giocando sempre la prima mossa legale. */
    private fun beats(): Sequence<Beat> = sequence {
        for (seed in 1L..120L) {
            var session = MatchSession.start(ScopaConfig(), seed)
            while (session.phase == DealPhase.PLAYING) {
                val move = session.legalMovesFor(session.toMove).firstOrNull() ?: break
                val applied = session.play(move, human) as SessionPlay.Applied
                yield(applied.beat)
                session = applied.session
            }
        }
    }

    private fun findBeat(predicate: (Beat) -> Boolean): Beat = beats().first(predicate)

    private fun sceneAt(beat: Beat, step: SceneStep?): TableScene = TableScene(
        view = beat.viewBefore,
        humanSeat = human,
        step = step,
        beatId = beat.id,
        busy = step != null,
        after = beat.viewAfter,
        actor = beat.actor,
        played = beat.move.card,
        deal = BeatScript.dealPlan(beat),
        stepMillis = 100,
    )

    private fun stepsOf(beat: Beat) = BeatScript.steps(beat, human)

    @Test
    fun atRestTheStageShowsTheViewAsIs() {
        val beat = findBeat { it.actor == human }
        val model = StageModel.from(TableScene(beat.viewAfter, human))
        assertEquals(beat.viewAfter.hand, model.hand)
        assertEquals(beat.viewAfter.table, model.table.map { it.card })
        assertEquals(beat.viewAfter.deckSize, model.deck)
    }

    @Test
    fun humanCardInFlightLeavesTheHandAndEntersFromTheHand() {
        val beat = findBeat { it.actor == human && BeatScript.dealPlan(it) == null }
        val step = stepsOf(beat).first() as SceneStep.CardToPlayZone
        val model = StageModel.from(sceneAt(beat, step))
        assertEquals(beat.viewBefore.hand - step.card, model.hand)
        val played = model.table.last()
        assertEquals(step.card, played.card)
        assertEquals(EntryKind.PLAYED, played.kind)
        assertEquals(Anchor.HAND, played.origin)
        assertEquals(beat.viewBefore.table.size + 1, model.layoutCount)
    }

    @Test
    fun opponentRevealEntersFromTheOpponentHandAndTakesOneCoveredCard() {
        val beat = findBeat { it.actor != human && BeatScript.dealPlan(it) == null }
        val step = stepsOf(beat).first() as SceneStep.OpponentReveal
        val model = StageModel.from(sceneAt(beat, step))
        assertEquals(beat.viewBefore.opponentHandSize - 1, model.opponentHand)
        assertEquals(Anchor.OPPONENT_HAND, model.table.last().origin)
    }

    @Test
    fun captureHighlightLightsUpTheCardsAboutToBeTaken() {
        val beat = findBeat { b -> b.events.any { it is GameEvent.Captured } }
        val step = stepsOf(beat).filterIsInstance<SceneStep.CaptureHighlight>().first()
        val model = StageModel.from(sceneAt(beat, step))
        val lit = model.table.filter { it.kind == EntryKind.HIGHLIGHTED }.map { it.card }.toSet()
        assertEquals(step.captured, lit)
    }

    @Test
    fun captureFlightSendsPlayedAndTakenCardsToTheTakersPile() {
        val beat = findBeat { b -> b.actor == human && b.events.any { it is GameEvent.Captured } }
        val step = stepsOf(beat).filterIsInstance<SceneStep.CaptureFlight>().first()
        val model = StageModel.from(sceneAt(beat, step))
        val flying = model.table.filter { it.flyTo != null }
        assertEquals(step.cards.toSet(), flying.map { it.card }.toSet())
        assertTrue(flying.all { it.flyTo == Anchor.HUMAN_PILE && it.slot == null })
        // La pila cresce solo a volo finito.
        assertEquals(beat.viewBefore.capturedBy[human]?.size, model.humanCaptured)
    }

    @Test
    fun afterTheFlightThePileHasGrown() {
        val beat = findBeat { b -> b.events.any { it is GameEvent.Sweep } && b.actor == human }
        val step = stepsOf(beat).filterIsInstance<SceneStep.Sweep>().first()
        val model = StageModel.from(sceneAt(beat, step))
        assertEquals(beat.viewAfter.capturedBy[human]?.size, model.humanCaptured)
        assertEquals(beat.viewAfter.sweeps[human], model.humanSweeps)
    }

    @Test
    fun handDealGivesOneCardAtATimeWithTheDeckGoingDown() {
        val beat = findBeat { b -> BeatScript.dealPlan(b)?.tableCards == 0 }
        val deals = stepsOf(beat).filterIsInstance<SceneStep.DealCard>()
        var previousDeck = Int.MAX_VALUE
        var humanSeen = 0
        deals.forEach { step ->
            val model = StageModel.from(sceneAt(beat, step))
            assertEquals(humanSeen, model.hand.size, "Le carte già arrivate in mano prima di ${step.index}")
            val flight = model.flights.single()
            assertEquals(Anchor.DECK, flight.from)
            if (step.toSeat == human) humanSeen++
            assertTrue(model.deck < previousDeck, "Il mazzo scende a ogni carta")
            previousDeck = model.deck
        }
        assertEquals(beat.viewAfter.hand.size, humanSeen)
        assertEquals(beat.viewAfter.deckSize, previousDeck)
    }

    @Test
    fun beforeTheDealTheHandsAreEmpty() {
        val beat = findBeat { b -> BeatScript.dealPlan(b) != null }
        val step = stepsOf(beat).first { it is SceneStep.CaptureFlight || it is SceneStep.DropToTable }
        val model = StageModel.from(sceneAt(beat, step))
        assertTrue(model.hand.isEmpty())
        assertEquals(0, model.opponentHand)
        assertNull(model.flights.firstOrNull())
    }
}
