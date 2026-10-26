package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.app.ui.screens.HomeScreen
import it.maestro.app.ui.theme.MaestroTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Menu iniziale (F1-18): screenshot da confrontare con `docs/art/reference_menu_screen.jpg`
 * e comportamento dei pulsanti attivi e decorativi.
 * Gli screenshot si scrivono con `./gradlew :app:android:testDebugUnitTest -Proborazzi.record=true`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// Application vuota: MaestroApp avvia Koin, che non serve per disegnare la Home.
@Config(sdk = [35], qualifiers = "it-w411dp-h891dp-xxhdpi", application = Application::class)
class HomeScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun home_screen_screenshot() {
        composeRule.setContent { MaestroTheme { HomeScreen() } }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/home_screen.png")
    }

    @Test
    fun home_screen_with_active_match_screenshot() {
        composeRule.setContent { MaestroTheme { HomeScreen(hasActiveMatch = true) } }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/home_screen_active_match.png")
    }

    @Test
    fun home_screen_with_streak_screenshot() {
        composeRule.setContent { MaestroTheme { HomeScreen(streakCount = 5) } }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/home_screen_with_streak.png")
    }

    @Test
    fun play_now_and_scopa_start_a_new_game() {
        var starts = 0
        composeRule.setContent { MaestroTheme { HomeScreen(onStartGameClicked = { starts++ }) } }
        composeRule.onNodeWithTag("play_now_button").performClick()
        composeRule.onNodeWithTag("scopa_tile_button").performClick()
        assertEquals(2, starts)
    }

    @Test
    fun active_match_hero_button_resumes_the_match() {
        var resumes = 0
        composeRule.setContent { MaestroTheme { HomeScreen(hasActiveMatch = true, onResumeGameClicked = { resumes++ }) } }
        composeRule.onNodeWithTag("resume_game_button").performClick()
        assertEquals(1, resumes)
    }

    @Test
    fun briscola_is_coming_soon_and_does_not_start_a_game() {
        var starts = 0
        composeRule.setContent { MaestroTheme { HomeScreen(onStartGameClicked = { starts++ }) } }
        composeRule.onNodeWithText("Presto disponibile").assertExists()
        composeRule.onNodeWithTag("briscola_tile_button").performClick()
        assertEquals(0, starts)
    }

    @Test
    fun gear_opens_settings() {
        var opened = 0
        composeRule.setContent { MaestroTheme { HomeScreen(onSettingsClicked = { opened++ }) } }
        composeRule.onNodeWithTag("home_settings_button").performClick()
        assertEquals(1, opened)
    }
}
