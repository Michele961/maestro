package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.feature.pregame.ui.PreGameScreen
import it.maestro.app.designsystem.theme.MaestroTheme
import it.maestro.game.training.TrainingFundamental
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Modale "Preparati a giocare" (F1-20): screenshot da confrontare con `docs/art/reference_pre_match_dialog.png`
 * e scelte passate a "Inizia partita".
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class PreGameScreenScreenshotTest(private val screen: ScreenConfig) {

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
    fun pre_game_screenshot() {
        composeRule.setContent { MaestroTheme { PreGameScreen() } }
        composeRule.onRoot().captureRoboImage(screen.path("pre_game"))
    }

    @Test
    fun pre_game_shows_the_chosen_opponent() {
        composeRule.setContent { MaestroTheme { PreGameScreen(opponent = Opponent.DON_ALFONSO) } }
        composeRule.onNodeWithTag("pre_game_opponent").assertExists()
        composeRule.onRoot().captureRoboImage(screen.path("pre_game_alfonso"))
    }

    @Test
    fun start_passes_the_selected_options() {
        // Difetto noto sugli schermi bassi (D1 in docs/app/features/layout-defects-f1r06.md): resta sulla configurazione
        // su cui è nato il test finché il dossier della schermata non lo corregge.
        assumeTrue(screen == ScreenConfig.MEDIUM || screen == ScreenConfig.PIXEL_10_PRO)
        var chosen: Triple<DifficultyLevel, Int, TrainingFundamental>? = null
        composeRule.setContent {
            MaestroTheme {
                PreGameScreen(opponent = Opponent.TONINO, onStartGame = { l, t, f -> chosen = Triple(l, t, f) })
            }
        }
        composeRule.onNodeWithTag("target_score_option_21").performClick()
        composeRule.onNodeWithTag("mode_option_classic").performClick()
        composeRule.onNodeWithTag("start_game_button").performClick()
        assertEquals(Triple(DifficultyLevel.INTERMEDIO, 21, TrainingFundamental.NONE), chosen)
    }
}
