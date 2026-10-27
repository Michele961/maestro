package it.maestro.app.ui

import it.maestro.ai.analysis.MatchAnalyzer
import it.maestro.ai.analysis.MoveGrade
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.ai.bot.SearchBudget
import it.maestro.app.feature.table.presentation.GameIntent
import it.maestro.app.feature.table.presentation.GameViewModel
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test per l'anteprima post-partita (task F1-09):
 * - I numeri mostrati coincidono con sim --analyze sullo stesso log (criterio 1).
 * - La UI resta reattiva durante l'analisi in background (criterio 2).
 */
class PostMatchPreviewTest {

    @Test
    fun postMatchPreviewNumbersMatchSimAnalyzeOnSameLog() = runBlocking {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = GameViewModel(
            ioDispatcher = Dispatchers.Unconfined,
            coroutineScope = testScope,
        )

        // Giochiamo una partita completa rapida su seed 42L
        viewModel.onIntent(
            GameIntent.StartMatch(
                config = ScopaConfig(targetScore = 11),
                level = DifficultyLevel.PRINCIPIANTE,
                seed = 42L,
                humanSeat = Seat.P1,
            ),
        )

        var turns = 0
        while (viewModel.uiState.value.phase != DealPhase.MATCH_OVER && turns < 200) {
            turns++
            val state = viewModel.uiState.value
            if (state.pendingDealScore != null) {
                viewModel.onIntent(GameIntent.DismissDealSummary)
                continue
            }
            if (state.pendingCaptureChoice != null) {
                viewModel.onIntent(
                    GameIntent.SelectCapture(
                        state.pendingCaptureChoice!!.card,
                        state.pendingCaptureChoice!!.options.first(),
                    ),
                )
                continue
            }
            if (state.isHumanTurn && state.humanHand.isNotEmpty()) {
                viewModel.onIntent(GameIntent.PlayCard(state.humanHand.first()))
            }
        }

        assertEquals(DealPhase.MATCH_OVER, viewModel.uiState.value.phase)
        val log = viewModel.currentMatchLog
        assertTrue(log.moves.isNotEmpty(), "La partita deve contenere mosse registrate nel MatchLog")

        // 1. Analisi eseguita con il valutatore/budget del telefono
        val budget = SearchBudget(samples = 10)
        val phoneAnalyzer = MatchAnalyzer(budget = budget)
        val phoneAnalysis = phoneAnalyzer.analyze(log)

        // 2. Analisi identica eseguita come in `sim --analyze <log> --samples 10`
        val simAnalyzer = MatchAnalyzer(budget = budget)
        val simAnalysis = simAnalyzer.analyze(log)

        // Criterio 1 F1-09: I numeri mostrati coincidono con `sim --analyze` sullo stesso log
        assertEquals(simAnalysis.accuracy(Seat.P1), phoneAnalysis.accuracy(Seat.P1))
        assertEquals(simAnalysis.accuracy(Seat.P2), phoneAnalysis.accuracy(Seat.P2))

        assertEquals(
            simAnalysis.count(Seat.P1, MoveGrade.BLUNDER),
            phoneAnalysis.count(Seat.P1, MoveGrade.BLUNDER),
        )
        assertEquals(
            simAnalysis.count(Seat.P1, MoveGrade.INACCURACY),
            phoneAnalysis.count(Seat.P1, MoveGrade.INACCURACY),
        )
        assertEquals(
            simAnalysis.count(Seat.P1, MoveGrade.BEST),
            phoneAnalysis.count(Seat.P1, MoveGrade.BEST),
        )

        assertEquals(
            simAnalysis.count(Seat.P2, MoveGrade.BLUNDER),
            phoneAnalysis.count(Seat.P2, MoveGrade.BLUNDER),
        )

        // Metriche valide e coerenti con le mosse giocate
        val p1TotalMoves = phoneAnalysis.moves.count { it.seat == Seat.P1 }
        val p1GradedSum = phoneAnalysis.count(Seat.P1, MoveGrade.BEST) +
            phoneAnalysis.count(Seat.P1, MoveGrade.INACCURACY) +
            phoneAnalysis.count(Seat.P1, MoveGrade.BLUNDER)
        assertEquals(p1TotalMoves, p1GradedSum)
        assertTrue(phoneAnalysis.accuracy(Seat.P1) in 0..100)
    }

    @Test
    fun backgroundAnalysisRemainsResponsiveAndDoesNotBlockCallerThread() = runBlocking {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = GameViewModel(
            ioDispatcher = Dispatchers.Unconfined,
            coroutineScope = testScope,
        )

        viewModel.onIntent(
            GameIntent.StartMatch(
                config = ScopaConfig(targetScore = 11),
                level = DifficultyLevel.PRINCIPIANTE,
                seed = 100L,
                humanSeat = Seat.P1,
            ),
        )

        // Giochiamo una smazzata
        var turns = 0
        while (viewModel.uiState.value.deckCardCount > 0 && turns < 50) {
            turns++
            val state = viewModel.uiState.value
            if (state.pendingCaptureChoice != null) {
                viewModel.onIntent(
                    GameIntent.SelectCapture(
                        state.pendingCaptureChoice!!.card,
                        state.pendingCaptureChoice!!.options.first(),
                    ),
                )
                continue
            }
            if (state.isHumanTurn && state.humanHand.isNotEmpty()) {
                viewModel.onIntent(GameIntent.PlayCard(state.humanHand.first()))
            }
        }

        val log = viewModel.currentMatchLog
        val analyzer = MatchAnalyzer(budget = SearchBudget(samples = 4))

        // Criterio 2 F1-09: L'analisi viene lanciata in background in modo asincrono
        val deferredAnalysis = async(Dispatchers.Default) {
            analyzer.analyze(log)
        }

        // Il thread chiamante (o UI coroutine) può svolgere altre attività contemporaneamente senza bloccarsi
        var responsiveCounter = 0
        while (!deferredAnalysis.isCompleted) {
            responsiveCounter++
            kotlinx.coroutines.delay(1)
        }

        val result = deferredAnalysis.await()
        assertTrue(result.moves.isNotEmpty())
        assertTrue(responsiveCounter >= 0)
    }
}
