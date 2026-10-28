package it.maestro.app.domain.coach

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaMove
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Task F1R-27b: gli indizi della Sfida del Giorno nominano le carte della posizione. */
class PuzzleCoachTest {

    private val two = Card(Rank.DUE, Suit.COPPE)
    private val four = Card(Rank.QUATTRO, Suit.SPADE)
    private val six = Card(Rank.SEI, Suit.BASTONI)

    @Test
    fun theFirstHintNamesACardOfTheCaptureAndTheSecondAsksWhy() {
        val move = ScopaMove(six, setOf(two, four))
        val first = PuzzleCoach.hint(move, listOf(two, four), 1)
        assertEquals(listOf(Phrase("puzzle_hint_card", listOf(VoiceArg.CardArg(two)))), first.phrases)
        val second = PuzzleCoach.hint(move, listOf(two, four), 2)
        assertTrue(second.phrases.first().id.startsWith("voice_hint_nudge_"))
    }

    @Test
    fun whenNothingIsCapturedTheFirstHintSaysSoWithoutNamingCards() {
        val hint = PuzzleCoach.hint(ScopaMove(two), listOf(four), 1)
        assertEquals(listOf(Phrase("puzzle_hint_no_capture")), hint.phrases)
    }

    @Test
    fun theSolutionNamesTheCardToPlayAndTheReason() {
        val move = ScopaMove(six, setOf(two, four))
        val line = PuzzleCoach.solution(move, listOf(two, four, Card(Rank.CINQUE, Suit.COPPE)))
        assertTrue(line.phrases.first().args.contains(VoiceArg.CardArg(six)))
        assertEquals(2, line.phrases.size)
    }
}
