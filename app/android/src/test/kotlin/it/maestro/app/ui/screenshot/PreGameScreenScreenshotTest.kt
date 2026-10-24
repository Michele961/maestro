package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.ui.screens.PreGameScreen
import it.maestro.app.ui.theme.MaestroTheme
import it.maestro.game.training.TrainingFundamental
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Modale "Preparati a giocare" (F1-20): screenshot da confrontare con `docs/art/reference_pre_match_dialog.png`
 * e scelte passate a "Inizia partita".
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "it-w411dp-h891dp-xxhdpi", application = Application::class)
class PreGameScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pre_game_screenshot() {
        composeRule.setContent { MaestroTheme { PreGameScreen() } }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/pre_game.png")
    }

    @Test
    fun start_passes_the_selected_options() {
        var chosen: Triple<DifficultyLevel, Int, TrainingFundamental>? = null
        composeRule.setContent {
            MaestroTheme { PreGameScreen(onStartGame = { l, t, f -> chosen = Triple(l, t, f) }) }
        }
        composeRule.onNodeWithTag("target_score_option_21").performClick()
        composeRule.onNodeWithTag("level_option_MAESTRO").performClick()
        composeRule.onNodeWithTag("mode_option_classic").performClick()
        composeRule.onNodeWithTag("start_game_button").performClick()
        assertEquals(Triple(DifficultyLevel.MAESTRO, 21, TrainingFundamental.NONE), chosen)
    }
}
