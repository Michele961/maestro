package it.maestro.app.domain.coach

import it.maestro.app.domain.match.Beat
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.GameEvent
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView
import it.maestro.game.training.SeatLead
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Task F1R-24a (`docs/app/features/maestro-voice.md` §3.1): ogni fatto ha il suo test, dalla sola vista. */
class MatchFactsTest {

    private val settebello = Card(Rank.SETTE, Suit.DENARI)
    private fun c(rank: Rank, suit: Suit = Suit.COPPE) = Card(rank, suit)

    private fun view(
        hand: List<Card> = listOf(c(Rank.DUE)),
        table: List<Card> = emptyList(),
        mine: List<Card> = emptyList(),
        theirs: List<Card> = emptyList(),
        deck: Int = 20,
        opponentHand: Int = 3,
        totals: Pair<Int, Int> = 0 to 0,
        target: Int = 11,
    ) = ScopaView(
        seat = Seat.P1,
        config = ScopaConfig(targetScore = target),
        dealer = Seat.P2,
        toMove = Seat.P1,
        phase = DealPhase.PLAYING,
        hand = hand,
        table = table,
        capturedBy = mapOf(Seat.P1 to mine, Seat.P2 to theirs),
        playedCards = emptyList(),
        opponentHandSize = opponentHand,
        deckSize = deck,
        sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        totals = mapOf(Seat.P1 to totals.first, Seat.P2 to totals.second),
    )

    private inline fun <reified F : Fact> List<Fact>.one(): F? = filterIsInstance<F>().firstOrNull()

    @Test
    fun sweepAvailableWhenAMoveTakesAllTheTable() {
        val take = ScopaMove(c(Rank.CINQUE), setOf(c(Rank.DUE), c(Rank.TRE)))
        val facts = MatchFacts.of(view(hand = listOf(c(Rank.CINQUE)), table = listOf(c(Rank.DUE), c(Rank.TRE)), opponentHand = 3), listOf(take))
        assertEquals(take, facts.one<Fact.SweepAvailable>()?.move)
    }

    @Test
    fun noSweepOnTheLastPlayOfTheDeal() {
        val take = ScopaMove(c(Rank.CINQUE), setOf(c(Rank.DUE), c(Rank.TRE)))
        val facts = MatchFacts.of(
            view(hand = listOf(c(Rank.CINQUE)), table = listOf(c(Rank.DUE), c(Rank.TRE)), deck = 0, opponentHand = 0),
            listOf(take),
        )
        assertNull(facts.one<Fact.SweepAvailable>(), "Spec 5.2: l'ultima giocata non vale scopa")
        assertTrue(Fact.LastPlay in facts)
    }

    @Test
    fun settebelloInHandOnTableAndTaken() {
        assertTrue(Fact.SettebelloInHand in MatchFacts.of(view(hand = listOf(settebello)), emptyList()))

        val capture = ScopaMove(c(Rank.SETTE, Suit.SPADE), setOf(settebello))
        val onTable = MatchFacts.of(view(hand = listOf(c(Rank.SETTE, Suit.SPADE)), table = listOf(settebello)), listOf(capture))
        assertEquals(capture, onTable.one<Fact.SettebelloOnTable>()?.capture)
        val unreachable = MatchFacts.of(view(hand = listOf(c(Rank.DUE)), table = listOf(settebello)), listOf(ScopaMove(c(Rank.DUE))))
        assertNull(unreachable.one<Fact.SettebelloOnTable>()?.capture)

        assertEquals(SeatLead.HUMAN, MatchFacts.of(view(mine = listOf(settebello)), emptyList()).one<Fact.SettebelloTaken>()?.owner)
        assertEquals(SeatLead.OPPONENT, MatchFacts.of(view(theirs = listOf(settebello)), emptyList()).one<Fact.SettebelloTaken>()?.owner)
    }

    @Test
    fun lowTableSumFlagsTheRiskOfASweep() {
        assertEquals(6, MatchFacts.of(view(table = listOf(c(Rank.DUE), c(Rank.QUATTRO))), emptyList()).one<Fact.TableSumLow>()?.sum)
        assertNull(MatchFacts.of(view(table = listOf(c(Rank.SETTE), c(Rank.SEI))), emptyList()).one<Fact.TableSumLow>())
        assertNull(MatchFacts.of(view(table = emptyList()), emptyList()).one<Fact.TableSumLow>())
    }

    @Test
    fun mustDiscardAndSafeDiscard() {
        val table = listOf(c(Rank.SETTE), c(Rank.SEI))
        val discards = listOf(ScopaMove(c(Rank.ASSO)), ScopaMove(c(Rank.FANTE)))
        val facts = MatchFacts.of(view(hand = listOf(c(Rank.ASSO), c(Rank.FANTE)), table = table), discards)
        assertTrue(Fact.MustDiscard in facts)
        assertEquals(discards.first(), facts.one<Fact.SafeDiscardExists>()?.move)
        assertFalse(
            Fact.MustDiscard in MatchFacts.of(view(hand = listOf(c(Rank.ASSO)), table = listOf(c(Rank.ASSO, Suit.SPADE))), listOf(ScopaMove(c(Rank.ASSO), setOf(c(Rank.ASSO, Suit.SPADE))))),
        )
    }

    @Test
    fun manyChoicesNeedsSixLegalMoves() {
        val moves = List(6) { ScopaMove(c(Rank.entries[it])) }
        assertEquals(6, MatchFacts.of(view(), moves).one<Fact.ManyChoices>()?.legalCount)
        assertNull(MatchFacts.of(view(), moves.take(5)).one<Fact.ManyChoices>())
    }

    @Test
    fun cardsAndDenariSecuredOnlyBeyondTheThreshold() {
        val twentyOne = Suit.entries.flatMap { s -> Rank.entries.map { Card(it, s) } }.take(21)
        assertEquals(SeatLead.HUMAN, MatchFacts.of(view(mine = twentyOne), emptyList()).one<Fact.CardsSecured>()?.owner)
        assertNull(MatchFacts.of(view(mine = twentyOne.take(20)), emptyList()).one<Fact.CardsSecured>())
        val sixDenari = Rank.entries.take(6).map { Card(it, Suit.DENARI) }
        assertEquals(SeatLead.OPPONENT, MatchFacts.of(view(theirs = sixDenari), emptyList()).one<Fact.DenariSecured>()?.owner)
        assertNull(MatchFacts.of(view(theirs = sixDenari.take(5)), emptyList()).one<Fact.DenariSecured>())
    }

    @Test
    fun lastDealWhenTheDeckIsEmpty() {
        assertTrue(Fact.LastDeal in MatchFacts.of(view(deck = 0, hand = listOf(c(Rank.DUE), c(Rank.TRE))), emptyList()))
        assertFalse(Fact.LastDeal in MatchFacts.of(view(deck = 4), emptyList()))
    }

    @Test
    fun scoreGapAndMatchPoint() {
        val facts = MatchFacts.of(view(totals = 10 to 6), emptyList())
        assertEquals(4, facts.one<Fact.ScoreGap>()?.ahead)
        assertTrue(Fact.MatchPoint(SeatLead.HUMAN) in facts)
        assertEquals(-3, MatchFacts.of(view(totals = 4 to 7), emptyList()).one<Fact.ScoreGap>()?.ahead)
        assertNull(MatchFacts.of(view(totals = 5 to 5), emptyList()).one<Fact.ScoreGap>())
        assertTrue(Fact.MatchPoint(SeatLead.OPPONENT) in MatchFacts.of(view(totals = 3 to 10), emptyList()))
    }

    @Test
    fun lastMoveFactsSweepSettebelloAndGift() {
        val before = view(table = listOf(c(Rank.DUE)))
        val after = view(hand = listOf(c(Rank.DUE, Suit.SPADE)), table = listOf(c(Rank.DUE)))
        val sweepBeat = Beat(1, Seat.P2, ScopaMove(c(Rank.DUE, Suit.BASTONI), setOf(c(Rank.DUE))), listOf(GameEvent.Sweep(Seat.P2)), before, after)
        assertTrue(Fact.SweepMade(Seat.P2) in MatchFacts.of(after, emptyList(), sweepBeat))

        val settebelloBeat = Beat(
            2, Seat.P1, ScopaMove(c(Rank.SETTE, Suit.SPADE), setOf(settebello)),
            listOf(GameEvent.Captured(Seat.P1, setOf(settebello))), before, after,
        )
        assertTrue(Fact.SettebelloCaptured(Seat.P1) in MatchFacts.of(after, emptyList(), settebelloBeat))

        val giftMove = ScopaMove(c(Rank.DUE, Suit.SPADE), setOf(c(Rank.DUE)))
        val giftView = view(hand = listOf(c(Rank.DUE, Suit.SPADE)), table = listOf(c(Rank.DUE)))
        val dropBeat = Beat(3, Seat.P2, ScopaMove(c(Rank.DUE)), emptyList(), before, giftView)
        assertTrue(Fact.OpponentGift in MatchFacts.of(giftView, listOf(giftMove), dropBeat))
        assertFalse(Fact.OpponentGift in MatchFacts.of(giftView, emptyList(), dropBeat))
    }
}
