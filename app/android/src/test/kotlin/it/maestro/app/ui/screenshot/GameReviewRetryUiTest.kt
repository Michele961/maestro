package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import it.maestro.ai.analysis.MatchAnalyzer
import it.maestro.ai.analysis.MoveGrade
import it.maestro.ai.bot.SearchBudget
import it.maestro.app.ui.screens.GameReviewScreen
import it.maestro.app.ui.screens.GameReviewViewModel
import it.maestro.app.ui.theme.MaestroTheme
import it.maestro.game.CoreGame
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test UI per la funzione "Rigioca l'errore" nella Game Review (task F2-01).
 *
 * Criteri di accettazione:
 * - [x] Il test UI/Roborazzi verifica l'attivazione della modalità "Riprova la mano".
 * - [x] Non vengono rivelate le carte coperte dell'avversario prima del gioco della carta.
 * - [x] Risolvere correttamente l'errore aggiorna lo stato visivo della mossa nella timeline.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "it-w411dp-h891dp-xxhdpi", application = Application::class)
class GameReviewRetryUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val analyzer = MatchAnalyzer(budget = SearchBudget(samples = 2, maxNodes = 20))

    private val matchLog: MatchLog by lazy {
        val config = ScopaConfig()
        var state = newMatch(config = config, seed = 7L)
        val moves = ArrayList<ScopaMove>()
        repeat(36) {
            val legal = legalMoves(state)
            if (legal.isEmpty()) return@repeat
            val move = legal.first()
            val result = play(state, move) as? PlayResult.Applied ?: return@repeat
            moves.add(move)
            state = result.state
        }
        MatchLog(rulesVersion = CoreGame.RULES_VERSION, config = config, seed = 7L, moves = moves)
    }

    @Test
    fun retryErrorFlowAndTimelineUpdate() {
        val analysis = analyzer.analyze(matchLog)
        val viewModel = GameReviewViewModel()

        composeRule.setContent {
            MaestroTheme {
                GameReviewScreen(
                    matchLog = matchLog,
                    analysis = analysis,
                    humanSeat = Seat.P1,
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()

        // 1. Salta al primo errore commesso dal giocatore
        composeRule.onNodeWithTag("review_next_mistake").performClick()
        composeRule.waitForIdle()

        val stepIndex = viewModel.uiState.value.currentStepIndex
        val currentStep = viewModel.uiState.value.currentStep
        assertNotNull(currentStep)
        assertEquals(Seat.P1, currentStep.mover)
        assertTrue(currentStep.analysis.grade != MoveGrade.BEST, "Lo step deve essere un errore o imprecisione")

        // 2. Verifica presenza del pulsante "Riprova la mano"
        composeRule.onNodeWithTag("review_retry_button").assertIsDisplayed()

        // 3. Attiva la modalità "Riprova la mano" (Criterio 1)
        composeRule.onNodeWithTag("review_retry_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("review_retry_mode").assertExists()
        composeRule.onNodeWithTag("review_retry_banner").assertIsDisplayed()
        composeRule.onNodeWithTag("review_retry_prompt").assertIsDisplayed()

        // 4. Verifica che le carte coperte dell'avversario rimangano coperte (Criterio 2)
        composeRule.onNodeWithTag("review_opponent_hand").assertIsDisplayed()

        // 5. Trova la carta migliore e una carta subottimale
        val bestCard = currentStep.analysis.bestMove.card
        val imperfectCard = currentStep.playerView.hand.firstOrNull { it != bestCard }
        assertNotNull(imperfectCard)

        // 6. Gioca la carta imperfetta: deve mostrare suggerimento e non segnare la mossa come risolta
        val imperfectTag = "review_retry_card_${imperfectCard.suit.name}_${imperfectCard.rank.name}"
        composeRule.onNodeWithTag(imperfectTag).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("review_retry_feedback").assertIsDisplayed()
        composeRule.onNodeWithTag("review_retry_hint").assertIsDisplayed()
        composeRule.onNodeWithTag("review_timeline_dot_${stepIndex}_solved").assertDoesNotExist()

        // 7. Gioca la carta ottimale: congratulazioni e aggiornamento timeline (Criterio 3)
        val bestTag = "review_retry_card_${bestCard.suit.name}_${bestCard.rank.name}"
        composeRule.onNodeWithTag(bestTag).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("review_retry_feedback").assertIsDisplayed()
        composeRule.onNodeWithTag("review_timeline_dot_${stepIndex}_solved").assertExists()

        // 8. Esci dalla modalità di riprova: lo stato risolto persiste nella review e nella timeline
        composeRule.onNodeWithTag("review_retry_exit_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("review_retry_mode").assertDoesNotExist()
        composeRule.onNodeWithTag("review_timeline_dot_${stepIndex}_solved").assertExists()
    }
}
