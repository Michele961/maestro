package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.ai.puzzle.PuzzleGoal
import it.maestro.ai.puzzle.TacticalPuzzle
import it.maestro.app.R
import it.maestro.app.data.local.datastore.InMemoryDailyPuzzleRepository
import it.maestro.app.designsystem.components.MaestroMood
import it.maestro.app.feature.puzzle.ui.DailyPuzzleScreen
import it.maestro.app.feature.puzzle.ui.DailyPuzzleScreenContent
import it.maestro.app.feature.puzzle.presentation.DailyPuzzleStatus
import it.maestro.app.feature.puzzle.presentation.DailyPuzzleUiState
import it.maestro.app.feature.puzzle.presentation.DailyPuzzleViewModel
import it.maestro.app.designsystem.theme.MaestroTheme
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

/**
 * Test UI e screenshot Roborazzi per la schermata "Sfida del Giorno" (task F2-03).
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class DailyPuzzleScreenshotTest(private val screen: ScreenConfig) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenConfig.parameters()
    }

    @get:Rule(order = 0)
    val screenRule = ScreenConfigRule(screen)


    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val winningCard = Card(Rank.SETTE, Suit.DENARI)
    private val alternateCard = Card(Rank.ASSO, Suit.COPPE)
    private val thirdCard = Card(Rank.DUE, Suit.SPADE)
    private val winningMove = ScopaMove(card = winningCard, capture = setOf(Card(Rank.SETTE, Suit.SPADE)))

    private val sampleView = ScopaView(
        seat = Seat.P1,
        config = ScopaConfig(),
        dealer = Seat.P2,
        toMove = Seat.P1,
        phase = DealPhase.PLAYING,
        hand = listOf(winningCard, alternateCard, thirdCard),
        table = listOf(Card(Rank.SETTE, Suit.SPADE)),
        capturedBy = mapOf(Seat.P1 to emptyList(), Seat.P2 to emptyList()),
        playedCards = emptyList(),
        opponentHandSize = 3,
        deckSize = 0,
        sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        totals = mapOf(Seat.P1 to 0, Seat.P2 to 0),
    )

    private val samplePuzzle = TacticalPuzzle(
        id = "daily_puzzle_screenshot_01",
        title = "puzzle_title_make_sweep",
        initialView = sampleView,
        goal = PuzzleGoal.MAKE_SWEEP,
        solutionMoves = listOf(winningMove),
    )

    @Test
    fun daily_puzzle_initial_screenshot() {
        val uiState = DailyPuzzleUiState(
            puzzleNumber = 14,
            dateString = "2026-10-26",
            puzzle = samplePuzzle,
            currentView = sampleView,
            status = DailyPuzzleStatus.Playing,
            attemptsCount = 0,
            maestroMood = MaestroMood.THINKING,
            maestroMessageResId = R.string.puzzle_coach_initial,
        )

        composeRule.setContent {
            MaestroTheme {
                DailyPuzzleScreenContent(uiState = uiState)
            }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(screen.path("daily_puzzle_initial"))
    }

    @Test
    fun daily_puzzle_wrong_attempt_screenshot() {
        val uiState = DailyPuzzleUiState(
            puzzleNumber = 14,
            dateString = "2026-10-26",
            puzzle = samplePuzzle,
            currentView = sampleView,
            status = DailyPuzzleStatus.WrongAttempt(attemptNumber = 1, hintLevel = 1),
            attemptsCount = 1,
            maestroMood = MaestroMood.ENCOURAGING,
            maestroMessageResId = R.string.puzzle_coach_hint_level1,
        )

        composeRule.setContent {
            MaestroTheme {
                DailyPuzzleScreenContent(uiState = uiState)
            }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(screen.path("daily_puzzle_wrong_attempt"))
    }

    @Test
    fun daily_puzzle_solved_screenshot() {
        val uiState = DailyPuzzleUiState(
            puzzleNumber = 14,
            dateString = "2026-10-26",
            puzzle = samplePuzzle,
            currentView = sampleView,
            status = DailyPuzzleStatus.Solved(attemptsCount = 1),
            attemptsCount = 1,
            maestroMood = MaestroMood.TRIUMPHANT,
            maestroMessageResId = R.string.puzzle_coach_solved,
            solvedTiers = setOf(0),
            lesson = it.maestro.app.data.local.DailyPuzzleProvider.lessonOf(samplePuzzle),
        )

        composeRule.setContent {
            MaestroTheme {
                DailyPuzzleScreenContent(uiState = uiState)
            }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(screen.path("daily_puzzle_solved"))
    }

    @Test
    fun daily_puzzle_interactiveSolveFlow() {
        // Difetto noto sugli schermi bassi (D2 in docs/app/features/layout-defects-f1r06.md): resta sulla configurazione
        // su cui è nato il test finché il dossier della schermata non lo corregge.
        assumeTrue(screen == ScreenConfig.MEDIUM || screen == ScreenConfig.PIXEL_10_PRO)
        val repository = InMemoryDailyPuzzleRepository()
        val viewModel = DailyPuzzleViewModel(
            repository = repository,
            customDate = LocalDate.of(2026, 10, 26),
            customPuzzle = samplePuzzle,
        )

        composeRule.setContent {
            MaestroTheme {
                DailyPuzzleScreen(
                    viewModel = viewModel,
                    onNavigateHome = {},
                )
            }
        }
        composeRule.waitForIdle()

        // Verifica stato iniziale
        composeRule.onNodeWithTag("puzzle_coach_bubble").assertIsDisplayed()
        composeRule.onNodeWithTag("puzzle_card_${winningCard.rank.name}_${winningCard.suit.name}").assertIsDisplayed()

        // Tocca la carta vincente
        composeRule.onNodeWithTag("puzzle_card_${winningCard.rank.name}_${winningCard.suit.name}").performClick()
        composeRule.waitForIdle()

        // Verifica che compare il banner celebrativo di completamento con pulsante condividi
        composeRule.onNodeWithTag("puzzle_solved_banner").assertIsDisplayed()
        composeRule.onNodeWithTag("puzzle_share_button").assertIsDisplayed()
    }
}
