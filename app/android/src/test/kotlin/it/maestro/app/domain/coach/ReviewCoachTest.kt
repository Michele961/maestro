package it.maestro.app.domain.coach

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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Task F1R-24g: la spiegazione della revisione dice solo cose vere nella posizione e ammette quando non si vedono. */
class ReviewCoachTest {

    private val settebello = Card(Rank.SETTE, Suit.DENARI)

    /** Un banco che resta sopra dieci comunque si cali: nessun rischio di scopa, la differenza non si vede dalla mano. */
    private val bigTable = listOf(Card(Rank.CAVALLO, Suit.COPPE), Card(Rank.QUATTRO, Suit.COPPE))

    private fun view(
        table: List<Card> = emptyList(),
        hand: List<Card> = emptyList(),
        deckSize: Int = 20,
        totals: Pair<Int, Int> = 0 to 0,
        playedCards: List<Card> = emptyList(),
    ) = ScopaView(
        seat = Seat.P1,
        config = ScopaConfig(),
        dealer = Seat.P2,
        toMove = Seat.P1,
        phase = DealPhase.PLAYING,
        hand = hand,
        table = table,
        capturedBy = mapOf(Seat.P1 to emptyList(), Seat.P2 to emptyList()),
        playedCards = playedCards,
        opponentHandSize = 3,
        deckSize = deckSize,
        sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        totals = mapOf(Seat.P1 to totals.first, Seat.P2 to totals.second),
    )

    @Test
    fun takingTheSettebelloIsExplainedFirst() {
        val table = listOf(settebello, Card(Rank.ASSO, Suit.COPPE), Card(Rank.RE, Suit.BASTONI))
        val played = ScopaMove(Card(Rank.RE, Suit.COPPE), setOf(Card(Rank.RE, Suit.BASTONI)))
        val best = ScopaMove(Card(Rank.FANTE, Suit.SPADE), setOf(settebello, Card(Rank.ASSO, Suit.COPPE)))
        val note = ReviewCoach.explain(played, best, view(table = table))
        assertEquals(ReviewReason.SETTEBELLO_TAKEN, note.reason)
        assertEquals("voice_review_body_settebello_taken", note.body.id)
    }

    @Test
    fun usingTheSettebelloToCaptureIsNotDescribedAsTakingItWithItself() {
        val seven = Card(Rank.SETTE, Suit.SPADE)
        val note = ReviewCoach.explain(ScopaMove(Card(Rank.SETTE, Suit.BASTONI), setOf(seven)), ScopaMove(settebello, setOf(seven)), view(table = listOf(seven)))
        assertEquals(ReviewReason.SETTEBELLO_PLAYED, note.reason)
        assertTrue(note.body.args.isEmpty())
    }

    @Test
    fun aSweepMissedIsReported() {
        val table = listOf(Card(Rank.TRE, Suit.DENARI), Card(Rank.CINQUE, Suit.COPPE))
        val note = ReviewCoach.explain(ScopaMove(Card(Rank.ASSO, Suit.SPADE)), ScopaMove(Card(Rank.FANTE, Suit.SPADE), table.toSet()), view(table = table))
        assertEquals(ReviewReason.SWEEP, note.reason)
    }

    @Test
    fun noSweepIsClaimedOnTheLastPlayOfTheDeal() {
        val table = listOf(Card(Rank.TRE, Suit.DENARI))
        val lastView = view(table = table, hand = listOf(Card(Rank.TRE, Suit.SPADE)), deckSize = 0).copy(opponentHandSize = 0)
        val note = ReviewCoach.explain(ScopaMove(Card(Rank.TRE, Suit.SPADE)), ScopaMove(Card(Rank.TRE, Suit.SPADE), table.toSet()), lastView)
        assertTrue(note.reason != ReviewReason.SWEEP)
    }

    @Test
    fun leavingACombinationUnderElevenWarnsWithTheRealSum() {
        // Banco 3 + 4 = 7: l'Asso porta il banco a 8 (scopa con un 8), il Re a 17.
        val table = listOf(Card(Rank.TRE, Suit.DENARI), Card(Rank.QUATTRO, Suit.COPPE))
        val note = ReviewCoach.explain(ScopaMove(Card(Rank.ASSO, Suit.COPPE)), ScopaMove(Card(Rank.RE, Suit.COPPE)), view(table = table))
        assertEquals(ReviewReason.ALLOWS_SWEEP, note.reason)
        assertEquals(listOf<VoiceArg>(VoiceArg.NumberArg(8)), note.body.args)
    }

    @Test
    fun aSkippedCaptureIsNamedAndSaysWhatItWouldHaveBrought() {
        val six = Card(Rank.SEI, Suit.SPADE)
        val table = listOf(six, Card(Rank.TRE, Suit.COPPE))
        val note = ReviewCoach.explain(ScopaMove(Card(Rank.ASSO, Suit.BASTONI)), ScopaMove(Card(Rank.SEI, Suit.COPPE), setOf(six)), view(table = table))
        assertEquals(ReviewReason.IGNORED_CAPTURE, note.reason)
        assertEquals("voice_review_body_ignored_capture_one", note.body.id)
        assertEquals("voice_review_extra_primiera", note.extra?.id)
    }

    @Test
    fun theSameCaptureWithACheaperCardSaysWhichOneToKeep() {
        val capture = setOf(Card(Rank.TRE, Suit.COPPE), Card(Rank.CINQUE, Suit.SPADE))
        val played = ScopaMove(Card(Rank.FANTE, Suit.DENARI), capture)
        val best = ScopaMove(Card(Rank.FANTE, Suit.COPPE), capture)
        val note = ReviewCoach.explain(played, best, view(table = capture.toList()))
        assertEquals(ReviewReason.SAME_CAPTURE, note.reason)
        assertEquals(listOf<VoiceArg>(VoiceArg.CardArg(best.card), VoiceArg.CardArg(played.card)), note.body.args)
    }

    @Test
    fun whenNothingVisibleExplainsTheMoveItSaysSoAndPointsToWhatIsHidden() {
        val table = bigTable
        val note = ReviewCoach.explain(
            ScopaMove(Card(Rank.DUE, Suit.BASTONI)),
            ScopaMove(Card(Rank.RE, Suit.SPADE)),
            view(table = table, deckSize = 0),
        )
        assertEquals(ReviewReason.CONTEXT, note.reason)
        assertEquals("voice_review_extra_last_deal", note.extra?.id)
    }

    @Test
    fun theContextHintIsOmittedWhenThereIsNothingToPointTo() {
        val note = ReviewCoach.explain(ScopaMove(Card(Rank.DUE, Suit.BASTONI)), ScopaMove(Card(Rank.RE, Suit.SPADE)), view(table = bigTable))
        assertEquals(ReviewReason.CONTEXT, note.reason)
        assertNull(note.extra)
    }

    @Test
    fun aBigScoreGapIsMentionedAsHiddenContext() {
        val note = ReviewCoach.explain(
            ScopaMove(Card(Rank.DUE, Suit.BASTONI)),
            ScopaMove(Card(Rank.RE, Suit.SPADE)),
            view(table = bigTable, totals = 2 to 7),
        )
        assertNotNull(note.extra)
        assertEquals(listOf<VoiceArg>(VoiceArg.NumberArg(2), VoiceArg.NumberArg(7)), note.extra?.args)
    }

    @Test
    fun retryingMakesNoSenseWithOneOrTwoChoices() {
        assertFalse(ReviewCoach.isWorthRetrying(1))
        assertFalse(ReviewCoach.isWorthRetrying(2))
        assertTrue(ReviewCoach.isWorthRetrying(3))
    }
}
