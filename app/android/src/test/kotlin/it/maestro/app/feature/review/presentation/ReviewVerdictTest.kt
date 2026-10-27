package it.maestro.app.feature.review.presentation

import it.maestro.ai.analysis.MatchAnalyzer
import it.maestro.ai.analysis.MoveAnalysis
import it.maestro.ai.analysis.MoveGrade
import it.maestro.ai.bot.MoveValue
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaMove
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Una differenza trascurabile dalla mossa migliore non è un errore e non è nemmeno «la migliore» (Michele, 2026-10-27). */
class ReviewVerdictTest {

    private val move = ScopaMove(Card(Rank.DUE, Suit.COPPE))

    private fun analysis(loss: Double) = MoveAnalysis(
        moveIndex = 0,
        seat = Seat.P1,
        playedMove = move,
        playedValue = 1.0 - loss,
        bestMove = move,
        bestValue = 1.0,
        loss = loss,
        grade = MatchAnalyzer.classifyLoss(loss),
        alternatives = listOf(MoveValue(move, 1.0)),
    )

    @Test
    fun theBestMoveStaysTheBestMove() {
        assertEquals(ReviewVerdict.BEST, analysis(0.0).verdict())
        assertEquals(MoveGrade.BEST, analysis(0.05).grade)
        assertEquals(ReviewVerdict.BEST, analysis(0.05).verdict())
    }

    @Test
    fun aNegligibleLossIsAGoodMoveNotAMistakeAndNotTheBest() {
        val good = analysis(0.08)
        assertEquals(ReviewVerdict.GOOD, good.verdict())
        assertFalse(good.isMistake())
        assertEquals(ReviewVerdict.GOOD, analysis(NEGLIGIBLE_LOSS).verdict())
    }

    @Test
    fun aRealLossIsStillAMistake() {
        assertEquals(ReviewVerdict.INACCURACY, analysis(0.16).verdict())
        assertTrue(analysis(0.16).isMistake())
        assertEquals(ReviewVerdict.BLUNDER, analysis(0.5).verdict())
        assertTrue(analysis(0.5).isMistake())
    }
}
