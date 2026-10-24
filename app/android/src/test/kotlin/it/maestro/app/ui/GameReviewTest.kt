package it.maestro.app.ui

import it.maestro.ai.analysis.MatchAnalyzer
import it.maestro.ai.analysis.MoveGrade
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.ai.bot.SearchBudget
import it.maestro.app.game.GameIntent
import it.maestro.app.game.GameViewModel
import it.maestro.app.ui.screens.buildReviewSteps
import it.maestro.app.ui.screens.findNextMistake
import it.maestro.app.ui.screens.findPreviousMistake
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test per la Game Review mossa per mossa (task F1-10, `docs/ai/maestro.md`, `docs/vision.md` §5.B):
 * - Criterio 1: La review mostra solo ciò che il giocatore sapeva in quel momento più le carte
 *   uscite dopo; mai la mano avversaria del momento.
 * - Criterio 2: Navigazione mossa per mossa, salto ai momenti chiave (errori), evidenziazione della mossa migliore.
 * - Criterio 3: Gratuita e completamente locale (ADR-0005).
 */
class GameReviewTest {

    @Test
    fun reviewNeverExposesOpponentSecretHandAtAnyStep() = runBlocking {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = GameViewModel(
            ioDispatcher = Dispatchers.Unconfined,
            coroutineScope = testScope,
        )

        // Partita deterministica completa su seed 42L
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

        val log = viewModel.currentMatchLog
        assertNotNull(log, "Il MatchLog della partita non deve essere null")
        assertTrue(log.moves.isNotEmpty(), "La partita deve contenere mosse registrate")

        // Analisi locale con SearchBudget rapido deterministico
        val analyzer = MatchAnalyzer(budget = SearchBudget(samples = 2, maxNodes = 5))
        val analysis = analyzer.analyze(log)
        val steps = buildReviewSteps(log, analysis, Seat.P1)

        assertEquals(log.moves.size, steps.size, "Ogni mossa del log deve corrispondere a uno step di review")

        // Rigioca lo stato completo e verifica che la vista del giocatore in ogni step
        // NON contenga MAI le carte segrete che l'avversario aveva in mano in quel momento
        var fullState = newMatch(config = log.config, seed = log.seed)

        for ((index, move) in log.moves.withIndex()) {
            val step = steps[index]
            val opponentSecretHand = fullState.hand(Seat.P2)
            val playerHand = fullState.hand(Seat.P1)

            // La vista per Seat.P1 deve esporre solo il conteggio della mano avversaria, mai le carte
            assertEquals(
                opponentSecretHand.size,
                step.playerView.opponentHandSize,
                "L'opponentHandSize dello step $index deve coincidere con la dimensione reale della mano avversaria",
            )

            // Nessuna delle carte attualmente in mano all'avversario deve essere presente nella mano del giocatore
            val opponentHandInPlayerHand = opponentSecretHand.intersect(step.playerView.hand.toSet())
            assertTrue(
                opponentHandInPlayerHand.isEmpty(),
                "La mano del giocatore nello step $index contiene carte dell'avversario: $opponentHandInPlayerHand",
            )

            // Nessuna delle carte attualmente in mano all'avversario deve essere presente sul tavolo
            val opponentHandOnTable = opponentSecretHand.intersect(step.playerView.table.toSet())
            assertTrue(
                opponentHandOnTable.isEmpty(),
                "Il tavolo nello step $index contiene carte ancora in mano all'avversario: $opponentHandOnTable",
            )

            // Le carte che l'avversario ha in mano in questo turno NON possono essere già uscite tra le carte giocate
            val opponentHandInPlayedCards = opponentSecretHand.intersect(step.playerView.playedCards.toSet())
            assertTrue(
                opponentHandInPlayedCards.isEmpty(),
                "Le carte giocate dello step $index contengono carte non ancora giocate: $opponentHandInPlayedCards",
            )

            // La mano del giocatore corrisponde a ciò che il giocatore sapeva in quel momento
            assertEquals(
                playerHand.toSet(),
                step.playerView.hand.toSet(),
                "La mano esposta nello step $index deve corrispondere esattamente alla mano del giocatore",
            )

            // Avanza lo stato completo con la mossa giocata
            val playResult = play(fullState, move)
            assertTrue(playResult is PlayResult.Applied)
            fullState = playResult.state
        }
    }

    @Test
    fun keyMomentsNavigationJumpsToMistakesCorrectly() = runBlocking {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = GameViewModel(
            ioDispatcher = Dispatchers.Unconfined,
            coroutineScope = testScope,
        )

        // Partita deterministica rapida
        viewModel.onIntent(
            GameIntent.StartMatch(
                config = ScopaConfig(targetScore = 11),
                level = DifficultyLevel.PRINCIPIANTE,
                seed = 123L,
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

        val log = viewModel.currentMatchLog!!
        val analyzer = MatchAnalyzer(budget = SearchBudget(samples = 2, maxNodes = 5))
        val analysis = analyzer.analyze(log)
        val steps = buildReviewSteps(log, analysis, Seat.P1)

        val mistakeIndices = steps.indices.filter {
            steps[it].mover == Seat.P1 && steps[it].analysis.grade != MoveGrade.BEST
        }

        if (mistakeIndices.size >= 2) {
            val firstMistake = mistakeIndices[0]
            val secondMistake = mistakeIndices[1]

            // Dal primo errore, il prossimo errore deve saltare al secondo
            val next = findNextMistake(firstMistake, steps, Seat.P1)
            assertEquals(secondMistake, next)

            // Dal secondo errore, l'errore precedente deve saltare al primo
            val prev = findPreviousMistake(secondMistake, steps, Seat.P1)
            assertEquals(firstMistake, prev)
        }
    }

    @Test
    fun mistakesHighlightBestAlternativeAndExpectedValue() = runBlocking {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = GameViewModel(
            ioDispatcher = Dispatchers.Unconfined,
            coroutineScope = testScope,
        )

        viewModel.onIntent(
            GameIntent.StartMatch(
                config = ScopaConfig(targetScore = 11),
                level = DifficultyLevel.PRINCIPIANTE,
                seed = 99L,
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

        val log = viewModel.currentMatchLog!!
        val analyzer = MatchAnalyzer(budget = SearchBudget(samples = 2, maxNodes = 5))
        val analysis = analyzer.analyze(log)
        val steps = buildReviewSteps(log, analysis, Seat.P1)

        for (step in steps) {
            val moveAnalysis = step.analysis
            if (moveAnalysis.grade != MoveGrade.BEST) {
                // Per errori o imprecisioni, la perdita deve essere > 0 e la mossa migliore deve essere valida
                assertTrue(
                    moveAnalysis.loss > 0.0,
                    "La perdita di una mossa non BEST deve essere > 0.0, trovata: ${moveAnalysis.loss}",
                )
                assertTrue(
                    moveAnalysis.bestValue >= moveAnalysis.playedValue,
                    "Il valore della mossa migliore (${moveAnalysis.bestValue}) deve essere >= di quella giocata (${moveAnalysis.playedValue})",
                )
            } else {
                // Per BEST, la perdita deve essere <= 0.05
                assertTrue(moveAnalysis.loss <= MatchAnalyzer.BEST_MAX_LOSS)
            }
        }
    }
}
