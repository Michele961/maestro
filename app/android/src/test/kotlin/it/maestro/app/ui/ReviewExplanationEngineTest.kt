package it.maestro.app.ui

import it.maestro.app.ui.screens.ReviewExplanationEngine
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReviewExplanationEngineTest {

    private fun createView(
        table: List<Card> = emptyList(),
        hand: List<Card> = emptyList(),
        capturedBy: Map<Seat, List<Card>> = mapOf(Seat.P1 to emptyList(), Seat.P2 to emptyList()),
    ): ScopaView = ScopaView(
        seat = Seat.P1,
        config = ScopaConfig(),
        dealer = Seat.P2,
        toMove = Seat.P1,
        phase = DealPhase.PLAYING,
        hand = hand,
        table = table,
        capturedBy = capturedBy,
        playedCards = emptyList(),
        opponentHandSize = 3,
        deckSize = 20,
        sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        totals = mapOf(Seat.P1 to 0, Seat.P2 to 0),
    )

    @Test
    fun explain_settebelloCapturePrioritized() {
        val settebello = Card(Rank.SETTE, Suit.DENARI)
        val cardFante = Card(Rank.FANTE, Suit.SPADE)
        val cardRe = Card(Rank.RE, Suit.COPPE)

        val played = ScopaMove(card = cardRe, capture = setOf(Card(Rank.RE, Suit.BASTONI)))
        val best = ScopaMove(card = cardFante, capture = setOf(settebello, Card(Rank.ASSO, Suit.COPPE)))

        val view = createView(table = listOf(settebello, Card(Rank.ASSO, Suit.COPPE), Card(Rank.RE, Suit.BASTONI)))
        val explanation = ReviewExplanationEngine.explain(played, best, view)

        assertEquals("Il Settebello", explanation.title)
        assertTrue(explanation.explanation.contains("7 di Denari"))
        assertTrue(explanation.keyConcept.contains("Settebello"))
    }

    @Test
    fun explain_sweepOpportunityHighlighted() {
        val fanteSpade = Card(Rank.FANTE, Suit.SPADE)
        val assoSpade = Card(Rank.ASSO, Suit.SPADE)

        val table = listOf(Card(Rank.TRE, Suit.DENARI), Card(Rank.CINQUE, Suit.COPPE))
        val played = ScopaMove(card = assoSpade, capture = emptySet())
        val best = ScopaMove(card = fanteSpade, capture = table.toSet())

        val view = createView(table = table)
        val explanation = ReviewExplanationEngine.explain(played, best, view)

        assertEquals("Occasione di Scopa", explanation.title)
        assertTrue(explanation.explanation.contains("ripulisce completamente il tavolo"))
    }

    @Test
    fun explain_sweepDangerWarned() {
        val assoCoppe = Card(Rank.ASSO, Suit.COPPE)
        val reCoppe = Card(Rank.RE, Suit.COPPE)

        // Tavolo ha 3 + 4 = 7. Calare Asso porta il tavolo a 8 (scopa immediata con 8).
        // Calare Re porta il tavolo a 17 (fuori portata di scopa a carta singola).
        val table = listOf(Card(Rank.TRE, Suit.DENARI), Card(Rank.QUATTRO, Suit.COPPE))
        val played = ScopaMove(card = assoCoppe, capture = emptySet())
        val best = ScopaMove(card = reCoppe, capture = emptySet())

        val view = createView(table = table)
        val explanation = ReviewExplanationEngine.explain(played, best, view)

        assertEquals("Rischio di Scopa Avversaria", explanation.title)
        assertTrue(explanation.explanation.contains("8"))
    }

    @Test
    fun explain_sameCaptureDifferentHandCardExplainsSuitPreservation() {
        // Fante di Spade vs Fante di Coppe to take 3 di Denari + 5 di Spade
        val fanteSpade = Card(Rank.FANTE, Suit.SPADE)
        val fanteCoppe = Card(Rank.FANTE, Suit.COPPE)
        val captureCards = setOf(Card(Rank.TRE, Suit.DENARI), Card(Rank.CINQUE, Suit.SPADE))

        val played = ScopaMove(card = fanteSpade, capture = captureCards)
        val best = ScopaMove(card = fanteCoppe, capture = captureCards)

        val view = createView(
            table = captureCards.toList(),
            capturedBy = mapOf(
                Seat.P1 to listOf(Card(Rank.SETTE, Suit.COPPE)),
                Seat.P2 to emptyList(),
            ),
        )

        val explanation = ReviewExplanationEngine.explain(played, best, view)
        assertEquals("Gestione dei Semi e Primiera", explanation.title)
        assertTrue(explanation.explanation.contains("Primiera"))
        assertTrue(explanation.keyConcept.contains("Primiera"))
    }

    @Test
    fun explain_primieraSevensAndSixesPrioritized() {
        val seiBastoni = Card(Rank.SEI, Suit.BASTONI)
        val treSpade = Card(Rank.TRE, Suit.SPADE)

        val played = ScopaMove(card = treSpade, capture = setOf(Card(Rank.TRE, Suit.COPPE)))
        val best = ScopaMove(card = seiBastoni, capture = setOf(Card(Rank.SEI, Suit.SPADE)))

        val view = createView(table = listOf(Card(Rank.TRE, Suit.COPPE), Card(Rank.SEI, Suit.SPADE)))
        val explanation = ReviewExplanationEngine.explain(played, best, view)

        assertEquals("Valore di Primiera", explanation.title)
        assertTrue(explanation.explanation.contains("21"))
    }
}
