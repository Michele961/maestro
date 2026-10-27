package it.maestro.app.domain.coach

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaMove
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Task F1R-24e: il consiglio su richiesta nomina le carte e dice perché. */
class HintComposerTest {

    private val settebello = Card(Rank.SETTE, Suit.DENARI)
    private val two = Card(Rank.DUE, Suit.COPPE)
    private val four = Card(Rank.QUATTRO, Suit.SPADE)
    private val six = Card(Rank.SEI, Suit.BASTONI)
    private val five = Card(Rank.CINQUE, Suit.COPPE)

    @Test
    fun clearingTheTableIsASweep() {
        assertEquals(HintComposer.Reason.SWEEP, HintComposer.reason(ScopaMove(five, setOf(two, four)), listOf(two, four)))
    }

    @Test
    fun takingTheSettebelloBeatsOtherReasons() {
        assertEquals(HintComposer.Reason.SETTEBELLO, HintComposer.reason(ScopaMove(Card(Rank.FANTE, Suit.SPADE), setOf(settebello)), listOf(settebello, two)))
    }

    @Test
    fun aCoinCapturedIsADenariReasonAndAGoodSixIsPrimiera() {
        assertEquals(HintComposer.Reason.DENARI, HintComposer.reason(ScopaMove(four, setOf(Card(Rank.QUATTRO, Suit.DENARI))), listOf(Card(Rank.QUATTRO, Suit.DENARI), two)))
        assertEquals(HintComposer.Reason.PRIMIERA, HintComposer.reason(ScopaMove(six, setOf(Card(Rank.SEI, Suit.COPPE))), listOf(Card(Rank.SEI, Suit.COPPE), two)))
        assertEquals(HintComposer.Reason.CAPTURE, HintComposer.reason(ScopaMove(four, setOf(Card(Rank.QUATTRO, Suit.COPPE))), listOf(Card(Rank.QUATTRO, Suit.COPPE), two)))
    }

    @Test
    fun aDiscardIsSafeOnlyIfTheTableStaysAboveTen() {
        assertEquals(HintComposer.Reason.SAFE_DISCARD, HintComposer.reason(ScopaMove(Card(Rank.RE, Suit.SPADE)), listOf(four)))
        assertEquals(HintComposer.Reason.LEAST_BAD_DISCARD, HintComposer.reason(ScopaMove(two), listOf(four)))
    }

    @Test
    fun theHintNamesTheCardsAndTheReason() {
        val line = HintComposer.compose(ScopaMove(four, setOf(Card(Rank.QUATTRO, Suit.COPPE))), listOf(Card(Rank.QUATTRO, Suit.COPPE), two))
        assertEquals(2, line.phrases.size)
        val lead = line.phrases.first()
        assertTrue(lead.id.startsWith("voice_hint_take_one"))
        assertEquals(listOf<VoiceArg>(VoiceArg.CardArg(four), VoiceArg.CardArg(Card(Rank.QUATTRO, Suit.COPPE))), lead.args)
        assertEquals("voice_hint_why_capture_1", line.phrases.last().id)
    }

    @Test
    fun takingSeveralCardsGivesTheirCountAndADiscardNamesOnlyTheCard() {
        val many = HintComposer.compose(ScopaMove(six, setOf(two, four)), listOf(two, four, five))
        assertTrue(many.phrases.first().id.startsWith("voice_hint_take_many"))
        assertEquals(VoiceArg.NumberArg(2), many.phrases.first().args[1])
        val discard = HintComposer.compose(ScopaMove(two), listOf(four))
        assertTrue(discard.phrases.first().id.startsWith("voice_hint_discard"))
        assertEquals(1, discard.phrases.first().args.size)
        assertEquals(Pose.THINKING, discard.pose)
    }

    @Test
    fun theSameHandGetsTheSameAdvice() {
        val move = ScopaMove(four, setOf(Card(Rank.QUATTRO, Suit.COPPE)))
        val table = listOf(Card(Rank.QUATTRO, Suit.COPPE), two)
        assertEquals(HintComposer.compose(move, table), HintComposer.compose(move, table))
    }

    @Test
    fun theNudgeAsksAQuestionWithoutNamingTheMoveAndInvitesToTapAgain() {
        val move = ScopaMove(four, setOf(Card(Rank.QUATTRO, Suit.COPPE)))
        val nudge = HintComposer.nudge(move, listOf(Card(Rank.QUATTRO, Suit.COPPE), two))
        assertEquals(listOf("voice_hint_nudge_capture", "voice_hint_nudge_more"), nudge.phrases.map { it.id })
        assertTrue(nudge.phrases.all { it.args.isEmpty() }, "Lo stimolo non nomina carte")
        assertEquals(Pose.THINKING, nudge.pose)
    }

    @Test
    fun bothDiscardReasonsShareTheSameKindOfNudgeAsTheirOwnQuestion() {
        assertEquals("safe", HintComposer.nudgeKey(HintComposer.Reason.SAFE_DISCARD))
        assertEquals("least_bad", HintComposer.nudgeKey(HintComposer.Reason.LEAST_BAD_DISCARD))
    }
}
