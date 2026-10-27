package it.maestro.app.domain.match

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaView
import it.maestro.game.scopa.DealPhase
import it.maestro.game.training.SeatLead
import it.maestro.game.training.SettebelloLocation
import kotlin.test.Test
import kotlin.test.assertEquals

/** Task F1R-13 (`docs/app/game-feel.md` P4): la corsa ai punti si calcola dalla sola vista. */
class PointsRaceTest {

    private fun view(mine: List<Card>, theirs: List<Card>) = ScopaView(
        seat = Seat.P1,
        config = ScopaConfig(),
        dealer = Seat.P2,
        toMove = Seat.P1,
        phase = DealPhase.PLAYING,
        hand = emptyList(),
        table = emptyList(),
        capturedBy = mapOf(Seat.P1 to mine, Seat.P2 to theirs),
        playedCards = emptyList(),
        opponentHandSize = 0,
        deckSize = 0,
        sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        totals = mapOf(Seat.P1 to 0, Seat.P2 to 0),
    )

    @Test
    fun noCaptureNobodyLeads() {
        val race = pointsRaceOf(view(emptyList(), emptyList()))
        assertEquals(SeatLead.TIE, race.cards.lead)
        assertEquals(SeatLead.TIE, race.denari.lead)
        assertEquals(SeatLead.TIE, race.primiera.lead)
        assertEquals(SettebelloLocation.IN_PLAY_OR_UNKNOWN, race.settebello)
    }

    @Test
    fun cardsAndDenariCountCapturedCards() {
        val mine = listOf(Card(Rank.DUE, Suit.DENARI), Card(Rank.TRE, Suit.DENARI), Card(Rank.TRE, Suit.COPPE))
        val theirs = listOf(Card(Rank.RE, Suit.SPADE), Card(Rank.ASSO, Suit.DENARI))
        val race = pointsRaceOf(view(mine, theirs))
        assertEquals(RaceLine(3, 2), race.cards)
        assertEquals(SeatLead.HUMAN, race.cards.lead)
        assertEquals(RaceLine(2, 1), race.denari)
    }

    @Test
    fun settebelloFollowsWhoCapturedIt() {
        val seven = Card(Rank.SETTE, Suit.DENARI)
        assertEquals(SettebelloLocation.CAPTURED_BY_ME, pointsRaceOf(view(listOf(seven), emptyList())).settebello)
        assertEquals(SettebelloLocation.CAPTURED_BY_OPPONENT, pointsRaceOf(view(emptyList(), listOf(seven))).settebello)
    }

    @Test
    fun primieraUsesTheBestCardOfEachSuit() {
        // 7 di Denari (21) + 6 di Coppe (18) contro Re di Spade (10): spec 6.3.
        val mine = listOf(Card(Rank.SETTE, Suit.DENARI), Card(Rank.SEI, Suit.COPPE), Card(Rank.DUE, Suit.COPPE))
        val theirs = listOf(Card(Rank.RE, Suit.SPADE))
        val race = pointsRaceOf(view(mine, theirs))
        assertEquals(RaceLine(39, 10), race.primiera)
        assertEquals(SeatLead.HUMAN, race.primiera.lead)
    }

    @Test
    fun tiesAssignNoLead() {
        assertEquals(SeatLead.TIE, RaceLine(5, 5).lead)
        assertEquals(SeatLead.OPPONENT, RaceLine(4, 5).lead)
    }
}
