package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.ai.analysis.MatchAnalyzer
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.ai.bot.SearchBudget
import it.maestro.app.ui.screens.GameReviewScreen
import it.maestro.app.ui.screens.PostMatchPreviewScreen
import it.maestro.app.ui.theme.MaestroTheme
import it.maestro.game.CoreGame
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Fine partita e Game Review (F1-22): screenshot da confrontare con `docs/art/reference_review_screen.png`.
 * La partita è una smazzata giocata in modo deterministico (seed fisso, prima mossa legale).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "it-w411dp-h891dp-xxhdpi", application = Application::class)
class ReviewScreensScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val analyzer = MatchAnalyzer(budget = SearchBudget(samples = 2, maxNodes = 40))

    private val log: MatchLog by lazy {
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
    fun post_match_preview_screenshot() {
        composeRule.setContent {
            MaestroTheme {
                PostMatchPreviewScreen(
                    matchLog = log,
                    level = DifficultyLevel.INTERMEDIO,
                    analyzer = analyzer,
                    ioDispatcher = Dispatchers.Unconfined,
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/post_match.png")
    }

    @Test
    fun game_review_screenshot() {
        val analysis = analyzer.analyze(log)
        composeRule.setContent { MaestroTheme { GameReviewScreen(matchLog = log, analysis = analysis) } }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/game_review.png")
    }
}
