package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.app.feature.home.ui.HomeScreen
import it.maestro.app.designsystem.theme.MaestroTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Menu iniziale (F1-18): screenshot da confrontare con `docs/art/reference_menu_screen.jpg`
 * e comportamento dei pulsanti attivi e decorativi.
 * Gli screenshot si scrivono con `./gradlew :app:android:testDebugUnitTest -Proborazzi.record=true`.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// Application vuota: MaestroApp avvia Koin, che non serve per disegnare la Home.
@Config(sdk = [35], application = Application::class)
class HomeScreenScreenshotTest(private val screen: ScreenConfig) {

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
    fun home_screen_screenshot() {
        composeRule.setContent { MaestroTheme { HomeScreen() } }
        composeRule.onRoot().captureRoboImage(screen.path("home_screen"))
    }

    @Test
    fun home_screen_with_active_match_screenshot() {
        composeRule.setContent { MaestroTheme { HomeScreen(hasActiveMatch = true) } }
        composeRule.onRoot().captureRoboImage(screen.path("home_screen_active_match"))
    }

    @Test
    fun home_screen_with_streak_screenshot() {
        composeRule.setContent { MaestroTheme { HomeScreen(streakCount = 5) } }
        composeRule.onRoot().captureRoboImage(screen.path("home_screen_with_streak"))
    }

    @Test
    fun play_now_starts_a_new_game_and_there_is_only_one_play_button() {
        var starts = 0
        composeRule.setContent { MaestroTheme { HomeScreen(onStartGameClicked = { starts++ }) } }
        composeRule.onNodeWithTag("play_now_button").performClick()
        assertEquals(1, starts)
        composeRule.onNodeWithTag("scopa_tile_button").assertDoesNotExist()
    }

    @Test
    fun active_match_hero_button_resumes_the_match() {
        var resumes = 0
        composeRule.setContent { MaestroTheme { HomeScreen(hasActiveMatch = true, onResumeGameClicked = { resumes++ }) } }
        composeRule.onNodeWithTag("resume_game_button").performClick()
        assertEquals(1, resumes)
    }

    @Test
    fun the_home_has_no_fake_entries() {
        composeRule.setContent { MaestroTheme { HomeScreen() } }
        // Gemme, messaggi, Amici, Briscola e Classifica non esistono ancora: non si mostrano (F1R-19).
        listOf("home_gems_pill", "briscola_tile", "home_nav_profile_button", "home_nav_shop_button", "home_nav_academy_button").forEach {
            composeRule.onNodeWithTag(it).assertDoesNotExist()
        }
        composeRule.onNodeWithText("Amici").assertDoesNotExist()
        composeRule.onNodeWithText("Presto disponibile").assertDoesNotExist()
        composeRule.onNodeWithText("Classifica").assertDoesNotExist()
    }

    @Test
    fun the_player_is_shown_with_the_name_and_the_real_mastery_not_a_fake_level() {
        composeRule.setContent { MaestroTheme { HomeScreen(playerName = "Michele", mastery = 1218) } }
        composeRule.onNodeWithTag("home_player_name").assertTextContains("Michele")
        composeRule.onNodeWithTag("home_mastery").assertTextContains("1218", substring = true)
        composeRule.onNodeWithText("Liv. 1", substring = true).assertDoesNotExist()
    }

    @Test
    fun without_a_name_the_player_is_simply_called_giocatore() {
        composeRule.setContent { MaestroTheme { HomeScreen() } }
        composeRule.onNodeWithTag("home_player_name").assertTextContains("Giocatore")
    }

    @Test
    fun gear_opens_settings() {
        var opened = 0
        composeRule.setContent { MaestroTheme { HomeScreen(onSettingsClicked = { opened++ }) } }
        composeRule.onNodeWithTag("home_settings_button").performClick()
        assertEquals(1, opened)
    }

    @Test
    fun home_screen_displays_dynamic_credits() {
        composeRule.setContent { MaestroTheme { HomeScreen(credits = 125) } }
        composeRule.onNodeWithTag("home_credits_pill").assertExists()
        composeRule.onNodeWithText("125").assertExists()
    }

    @Test
    fun every_tappable_element_goes_to_an_existing_function() {
        var dailyPuzzle = 0
        var profile = 0
        var missions = 0
        var academy = 0
        var shop = 0

        composeRule.setContent {
            MaestroTheme {
                HomeScreen(
                    onDailyPuzzleClicked = { dailyPuzzle++ },
                    onProfileClicked = { profile++ },
                    onMissionsClicked = { missions++ },
                    onAcademyClicked = { academy++ },
                    onShopClicked = { shop++ },
                )
            }
        }

        composeRule.onNodeWithTag("home_daily_puzzle_tile").performClick()
        composeRule.onNodeWithTag("home_missions_tile").performClick()
        composeRule.onNodeWithTag("home_academy_tile").assertIsDisplayed().performClick()
        // L'avatar apre il Profilo, il portafoglio apre la Bottega.
        composeRule.onNodeWithTag("home_player_profile").performClick()
        composeRule.onNodeWithTag("home_credits_pill").performClick()
        assertEquals(listOf(1, 1, 1, 1, 1), listOf(dailyPuzzle, missions, academy, profile, shop))
    }

    @Test
    @Config(qualifiers = "it-w390dp-h780dp-xxhdpi")
    fun the_academy_tile_is_displayed_on_a_standard_phone() {
        composeRule.setContent { MaestroTheme { HomeScreen() } }
        composeRule.onNodeWithTag("home_academy_tile").assertIsDisplayed()
    }
}
