package it.maestro.game.training

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrainingChecksTest {

    private fun card(rank: Rank, suit: Suit) = Card(rank, suit)

    private fun createView(
        playedCards: List<Card> = emptyList(),
        table: List<Card> = emptyList(),
        myCaptures: List<Card> = emptyList(),
        oppCaptures: List<Card> = emptyList(),
        myHand: List<Card> = listOf(card(Rank.SETTE, Suit.COPPE)),
    ): ScopaView {
        return ScopaView(
            seat = Seat.P1,
            config = ScopaConfig(),
            dealer = Seat.P2,
            toMove = Seat.P1,
            phase = DealPhase.PLAYING,
            hand = myHand,
            table = table,
            capturedBy = mapOf(
                Seat.P1 to myCaptures,
                Seat.P2 to oppCaptures,
            ),
            playedCards = playedCards,
            opponentHandSize = 3,
            deckSize = 10,
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
            totals = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        )
    }

    @Test
    fun passedCardsExcludesHandAndIncludesPlayedTableAndCaptures() {
        val played = listOf(card(Rank.ASSO, Suit.DENARI), card(Rank.DUE, Suit.DENARI))
        val table = listOf(card(Rank.TRE, Suit.DENARI))
        val myCaptures = listOf(card(Rank.QUATTRO, Suit.DENARI), card(Rank.CINQUE, Suit.DENARI))
        val oppCaptures = listOf(card(Rank.SEI, Suit.DENARI))
        val myHand = listOf(card(Rank.SETTE, Suit.DENARI))

        val view = createView(
            playedCards = played,
            table = table,
            myCaptures = myCaptures,
            oppCaptures = oppCaptures,
            myHand = myHand,
        )

        val passed = view.passedCards()
        assertEquals(6, passed.size)
        assertTrue(card(Rank.ASSO, Suit.DENARI) in passed)
        assertTrue(card(Rank.TRE, Suit.DENARI) in passed)
        assertTrue(card(Rank.QUATTRO, Suit.DENARI) in passed)
        assertTrue(card(Rank.SEI, Suit.DENARI) in passed)
        // La carta ancora in mano non è considerata passata!
        assertFalse(card(Rank.SETTE, Suit.DENARI) in passed)
    }

    @Test
    fun countPassedSevensCalculatesCorrectly() {
        val view0 = createView()
        assertEquals(0, countPassedSevens(view0))

        val view2 = createView(
            playedCards = listOf(card(Rank.SETTE, Suit.DENARI), card(Rank.ASSO, Suit.COPPE)),
            table = listOf(card(Rank.SETTE, Suit.SPADE)),
        )
        assertEquals(2, countPassedSevens(view2))

        val view4 = createView(
            playedCards = listOf(card(Rank.SETTE, Suit.DENARI), card(Rank.SETTE, Suit.COPPE)),
            myCaptures = listOf(card(Rank.SETTE, Suit.BASTONI)),
            oppCaptures = listOf(card(Rank.SETTE, Suit.SPADE)),
        )
        assertEquals(4, countPassedSevens(view4))
    }

    @Test
    fun countPassedDenariCalculatesCorrectly() {
        val view = createView(
            playedCards = listOf(card(Rank.ASSO, Suit.DENARI), card(Rank.SETTE, Suit.DENARI)),
            table = listOf(card(Rank.RE, Suit.DENARI)),
            myCaptures = listOf(card(Rank.DUE, Suit.DENARI), card(Rank.TRE, Suit.DENARI)),
            oppCaptures = listOf(card(Rank.QUATTRO, Suit.COPPE)),
        )
        assertEquals(5, countPassedDenari(view))
    }

    @Test
    fun calculatePartialPrimieraSumsMaxPerSuit() {
        // D: 7 (21), C: 6 (18), S: A (16), B: manca (0) -> totale 55
        val cards = listOf(
            card(Rank.SETTE, Suit.DENARI),
            card(Rank.DUE, Suit.DENARI),
            card(Rank.SEI, Suit.COPPE),
            card(Rank.ASSO, Suit.SPADE),
        )
        assertEquals(55, calculatePartialPrimiera(cards))
    }

    @Test
    fun determinePrimieraLeadEvaluatesCorrectLeader() {
        // Human: 7D (21) + 7C (21) = 42
        // Opponent: 7S (21) + 6B (18) = 39 -> Human leads
        val viewHumanLead = createView(
            myCaptures = listOf(card(Rank.SETTE, Suit.DENARI), card(Rank.SETTE, Suit.COPPE)),
            oppCaptures = listOf(card(Rank.SETTE, Suit.SPADE), card(Rank.SEI, Suit.BASTONI)),
        )
        assertEquals(SeatLead.HUMAN, determinePrimieraLead(viewHumanLead))

        // Inversione: Opponent leads
        val viewOppLead = createView(
            myCaptures = listOf(card(Rank.SETTE, Suit.SPADE), card(Rank.SEI, Suit.BASTONI)),
            oppCaptures = listOf(card(Rank.SETTE, Suit.DENARI), card(Rank.SETTE, Suit.COPPE)),
        )
        assertEquals(SeatLead.OPPONENT, determinePrimieraLead(viewOppLead))

        // Parità:
        val viewTie = createView(
            myCaptures = listOf(card(Rank.SETTE, Suit.DENARI)),
            oppCaptures = listOf(card(Rank.SETTE, Suit.COPPE)),
        )
        assertEquals(SeatLead.TIE, determinePrimieraLead(viewTie))
    }

    @Test
    fun determineSettebelloLocationIdentifiesLocation() {
        val settebello = card(Rank.SETTE, Suit.DENARI)

        val inPlayView = createView(table = listOf(settebello))
        assertEquals(SettebelloLocation.IN_PLAY_OR_UNKNOWN, determineSettebelloLocation(inPlayView))

        val myView = createView(myCaptures = listOf(settebello))
        assertEquals(SettebelloLocation.CAPTURED_BY_ME, determineSettebelloLocation(myView))

        val oppView = createView(oppCaptures = listOf(settebello))
        assertEquals(SettebelloLocation.CAPTURED_BY_OPPONENT, determineSettebelloLocation(oppView))
    }

    @Test
    fun determineCardsLeadIdentifiesLeader() {
        val humanLead = createView(
            myCaptures = listOf(card(Rank.ASSO, Suit.DENARI), card(Rank.DUE, Suit.DENARI)),
            oppCaptures = listOf(card(Rank.TRE, Suit.DENARI)),
        )
        assertEquals(SeatLead.HUMAN, determineCardsLead(humanLead))

        val oppLead = createView(
            myCaptures = listOf(card(Rank.ASSO, Suit.DENARI)),
            oppCaptures = listOf(card(Rank.DUE, Suit.DENARI), card(Rank.TRE, Suit.DENARI)),
        )
        assertEquals(SeatLead.OPPONENT, determineCardsLead(oppLead))

        val tie = createView(
            myCaptures = listOf(card(Rank.ASSO, Suit.DENARI)),
            oppCaptures = listOf(card(Rank.DUE, Suit.DENARI)),
        )
        assertEquals(SeatLead.TIE, determineCardsLead(tie))
    }

    @Test
    fun isCriticalTrainingMomentBehavesAsExpected() {
        // Non scatta prima di 8 carte giocate
        val earlyView = createView(playedCards = List(7) { card(Rank.DUE, Suit.COPPE) })
        assertFalse(isCriticalTrainingMoment(TrainingFundamental.SEVENS_COUNT, earlyView))

        // Scatta sempre al checkpoint di metà smazzata (>= 18 carte giocate)
        val midDealView = createView(playedCards = List(18) { card(Rank.DUE, Suit.COPPE) })
        assertTrue(isCriticalTrainingMoment(TrainingFundamental.SEVENS_COUNT, midDealView))

        // Scatta nei momenti critici (es. Sette appena giocato alla 10ª mossa)
        val settePlayedView = createView(
            playedCards = List(9) { card(Rank.DUE, Suit.COPPE) } + listOf(card(Rank.SETTE, Suit.DENARI)),
        )
        assertTrue(isCriticalTrainingMoment(TrainingFundamental.SEVENS_COUNT, settePlayedView))
    }

    @Test
    fun buildTrainingQuestionProducesConsistentQuestions() {
        val view = createView(
            playedCards = listOf(card(Rank.SETTE, Suit.DENARI), card(Rank.SETTE, Suit.COPPE)),
            myCaptures = listOf(card(Rank.SETTE, Suit.DENARI), card(Rank.ASSO, Suit.DENARI)),
            oppCaptures = listOf(card(Rank.SETTE, Suit.SPADE)),
        )

        // Sevens question
        val qSevens = buildTrainingQuestion(TrainingFundamental.SEVENS_COUNT, view)
        val correctSevens = (qSevens.options[qSevens.correctIndex] as TrainingOption.IntCount).count
        assertEquals(countPassedSevens(view), correctSevens)

        // Denari question
        val qDenari = buildTrainingQuestion(TrainingFundamental.DENARI_COUNT, view)
        val correctDenari = (qDenari.options[qDenari.correctIndex] as TrainingOption.IntCount).count
        assertEquals(countPassedDenari(view), correctDenari)

        // Primiera question
        val qPrimiera = buildTrainingQuestion(TrainingFundamental.PRIMIERA_LEAD, view)
        val correctPrimiera = (qPrimiera.options[qPrimiera.correctIndex] as TrainingOption.Lead).lead
        assertEquals(determinePrimieraLead(view), correctPrimiera)

        // Settebello question
        val qSettebello = buildTrainingQuestion(TrainingFundamental.SETTEBELLO_LOCATION, view)
        val correctSettebello = (qSettebello.options[qSettebello.correctIndex] as TrainingOption.Location).location
        assertEquals(determineSettebelloLocation(view), correctSettebello)

        // Cards lead question
        val qCards = buildTrainingQuestion(TrainingFundamental.CARDS_LEAD, view)
        val correctCards = (qCards.options[qCards.correctIndex] as TrainingOption.Lead).lead
        assertEquals(determineCardsLead(view), correctCards)
    }
}
