package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.app.designsystem.theme.MaestroTheme
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.opponents.OpponentProgress
import it.maestro.app.feature.pregame.presentation.LadderState
import it.maestro.app.feature.pregame.ui.LadderScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** La scala degli avversari (F1R-18b) sulla matrice di schermi: all'inizio, a metà e completata. */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class LadderScreenScreenshotTest(private val screen: ScreenConfig) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenConfig.parameters()
    }

    @get:Rule(order = 0)
    val screenRule = ScreenConfigRule(screen)

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @Test
    fun ladder_at_the_start_screenshot() {
        composeRule.setContent {
            MaestroTheme { LadderScreen(LadderState.of(OpponentProgress()), onOpponentClick = {}, onNavigateBack = {}) }
        }
        composeRule.onRoot().captureRoboImage(screen.path("ladder_start"))
    }

    @Test
    fun ladder_midway_screenshot() {
        val progress = OpponentProgress(setOf(Opponent.GIGI, Opponent.NONNA_ROSA))
        composeRule.setContent {
            MaestroTheme { LadderScreen(LadderState.of(progress), onOpponentClick = {}, onNavigateBack = {}) }
        }
        composeRule.onRoot().captureRoboImage(screen.path("ladder_midway"))
    }

    @Test
    fun ladder_complete_screenshot() {
        composeRule.setContent {
            MaestroTheme {
                LadderScreen(LadderState.of(OpponentProgress(Opponent.entries.toSet())), onOpponentClick = {}, onNavigateBack = {})
            }
        }
        composeRule.onRoot().captureRoboImage(screen.path("ladder_complete"))
    }

    @Test
    fun a_locked_opponent_cannot_be_chosen_and_names_who_to_beat() {
        val chosen = mutableListOf<Opponent>()
        composeRule.setContent {
            MaestroTheme {
                LadderScreen(LadderState.of(OpponentProgress()), onOpponentClick = { chosen += it }, onNavigateBack = {})
            }
        }
        composeRule.onNodeWithTag("ladder_row_TONINO").assertIsNotEnabled()
        composeRule.onNodeWithTag("ladder_locked_hint_TONINO", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("ladder_row_TONINO").performClick()
        composeRule.onNodeWithTag("ladder_row_GIGI").performClick()
        assertEquals(listOf(Opponent.GIGI), chosen)
    }
}
