package it.maestro.app.ui

import it.maestro.ai.analysis.MatchAnalyzer
import it.maestro.ai.analysis.MoveGrade
import it.maestro.ai.bot.SearchBudget
import it.maestro.app.R
import it.maestro.app.ui.screens.GameReviewViewModel
import it.maestro.app.ui.screens.RetryFeedback
import it.maestro.game.CoreGame
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test di unità per [GameReviewViewModel] e la modalità didattica "Rigioca l'errore" (F2-01).
 *
 * - Criterio 1: Attivazione e gestione della modalità "Riprova la mano".
 * - Criterio 2: Nessuna esposizione delle carte coperte dell'avversario.
 * - Criterio 3: Risoluzione dell'errore con aggiornamento della timeline e feedback differenziato.
 */
class GameReviewViewModelTest {

    private val analyzer = MatchAnalyzer(budget = SearchBudget(samples = 2, maxNodes = 20))

    private fun generateTestMatch(seed: Long = 7L): Pair<MatchLog, it.maestro.ai.analysis.MatchAnalysis> {
        val config = ScopaConfig()
        var state = newMatch(config = config, seed = seed)
        val moves = ArrayList<ScopaMove>()
        repeat(36) {
            val legal = legalMoves(state)
            if (legal.isEmpty()) return@repeat
            val move = legal.first()
            val result = play(state, move) as? PlayResult.Applied ?: return@repeat
            moves.add(move)
            state = result.state
        }
        val log = MatchLog(rulesVersion = CoreGame.RULES_VERSION, config = config, seed = seed, moves = moves)
        val analysis = analyzer.analyze(log)
        return Pair(log, analysis)
    }

    @Test
    fun retryModeActivationAndOpponentHandPrivacy() {
        val (log, analysis) = generateTestMatch(seed = 7L)
        val viewModel = GameReviewViewModel()
        viewModel.initialize(log, analysis, Seat.P1)

        val initialState = viewModel.uiState.value
        assertEquals(log.moves.size, initialState.steps.size)
        assertFalse(initialState.isRetryMode)

        // Trova il primo errore del giocatore umano
        val firstMistakeIndex = initialState.steps.indexOfFirst {
            it.mover == Seat.P1 && it.analysis.grade != MoveGrade.BEST
        }
        assertTrue(firstMistakeIndex >= 0, "Deve essere presente almeno un errore umano nel match di test")

        viewModel.selectStep(firstMistakeIndex)
        assertEquals(firstMistakeIndex, viewModel.uiState.value.currentStepIndex)

        // Criterio 2: La vista della mossa non espone alcuna carta segreta avversaria
        val currentStep = viewModel.uiState.value.currentStep!!
        val playerView = currentStep.playerView
        assertTrue(playerView.opponentHandSize > 0, "L'avversario deve avere carte in mano")
        // La vista non ha alcuna lista di carte avversarie, solo opponentHandSize
        assertEquals(playerView.hand.toSet().intersect(playerView.table.toSet()), emptySet())

        // Criterio 1: Attivazione modalità "Riprova la mano"
        viewModel.startRetry()
        assertTrue(viewModel.uiState.value.isRetryMode, "La modalità 'Riprova la mano' deve essere attiva")
        assertEquals(null, viewModel.uiState.value.retryFeedback)
    }

    @Test
    fun imperfectMoveProvidesHintAndDoesNotSolveStep() {
        val (log, analysis) = generateTestMatch(seed = 7L)
        val viewModel = GameReviewViewModel()
        viewModel.initialize(log, analysis, Seat.P1)

        val firstMistakeIndex = viewModel.uiState.value.steps.indexOfFirst {
            it.mover == Seat.P1 && it.analysis.grade != MoveGrade.BEST
        }
        assertTrue(firstMistakeIndex >= 0)
        viewModel.selectStep(firstMistakeIndex)
        viewModel.startRetry()

        val step = viewModel.uiState.value.currentStep!!
        val bestCard = step.analysis.bestMove.card
        // Cerca una carta in mano che NON sia la migliore
        val imperfectCard = step.playerView.hand.firstOrNull { it != bestCard }
        assertNotNull(imperfectCard, "Deve esserci una carta imperfetta nella mano con errore")

        viewModel.playRetryCard(imperfectCard)

        val feedback = viewModel.uiState.value.retryFeedback
        assertIs<RetryFeedback.Imperfect>(feedback, "Giocare una carta subottimale deve dare feedback Imperfect")
        assertEquals(R.string.review_retry_imperfect, feedback.messageRes)
        assertTrue(feedback.hintRes != 0, "Deve essere fornito un suggerimento didattico valido")

        // Criterio 3: L'errore non deve essere segnato come risolto
        assertFalse(viewModel.uiState.value.isCurrentStepSolved)
        assertFalse(viewModel.uiState.value.solvedStepIndices.contains(firstMistakeIndex))
    }

    @Test
    fun optimalMoveCongratulatesAndUpdatesTimelineSolvedState() {
        val (log, analysis) = generateTestMatch(seed = 7L)
        val viewModel = GameReviewViewModel()
        viewModel.initialize(log, analysis, Seat.P1)

        val firstMistakeIndex = viewModel.uiState.value.steps.indexOfFirst {
            it.mover == Seat.P1 && it.analysis.grade != MoveGrade.BEST
        }
        assertTrue(firstMistakeIndex >= 0)
        viewModel.selectStep(firstMistakeIndex)
        viewModel.startRetry()

        val step = viewModel.uiState.value.currentStep!!
        val bestCard = step.analysis.bestMove.card

        // Gioca la carta migliore
        viewModel.playRetryCard(bestCard)

        val feedback = viewModel.uiState.value.retryFeedback
        assertIs<RetryFeedback.Correct>(feedback, "Giocare la mossa ottimale deve dare feedback Correct")
        assertEquals(R.string.review_retry_correct, feedback.messageRes)
        assertEquals(step.analysis.bestMove, feedback.bestMove)

        // Criterio 3: Risolvere correttamente aggiorna lo stato visivo nella timeline
        assertTrue(viewModel.uiState.value.isCurrentStepSolved)
        assertTrue(viewModel.uiState.value.solvedStepIndices.contains(firstMistakeIndex))

        // Uscendo dalla retry mode, l'indice risolto persiste
        viewModel.exitRetry()
        assertFalse(viewModel.uiState.value.isRetryMode)
        assertTrue(viewModel.uiState.value.solvedStepIndices.contains(firstMistakeIndex))

        // Navigando a un altro step e tornando indietro, lo stato risolto è conservato
        viewModel.nextStep()
        viewModel.selectStep(firstMistakeIndex)
        assertTrue(viewModel.uiState.value.isCurrentStepSolved)
    }
}
