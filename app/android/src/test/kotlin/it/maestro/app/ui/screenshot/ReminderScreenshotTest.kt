package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import it.maestro.app.designsystem.theme.MaestroTheme
import it.maestro.app.feature.home.ui.HomeScreen
import it.maestro.app.feature.settings.presentation.SettingsUiState
import it.maestro.app.feature.settings.ui.SettingsScreenContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** F1G-13: richiesta di consenso al promemoria (Home) e interruttore (Impostazioni), sulla matrice a 4 configurazioni. */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class ReminderScreenshotTest(private val screen: ScreenConfig) {

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
    fun reminder_consent_dialog_screenshot() {
        composeRule.setContent { MaestroTheme { HomeScreen(askReminder = true) } }
        composeRule.onNodeWithTag("reminder_consent_dialog").assertIsDisplayed()
        captureScreenRoboImage(screen.path("reminder_consent_dialog"))
    }

    @Test
    fun consent_dialog_reports_each_answer() {
        var accepted = 0
        var declined = 0
        composeRule.setContent {
            MaestroTheme {
                HomeScreen(askReminder = true, onReminderAccepted = { accepted++ }, onReminderDeclined = { declined++ })
            }
        }
        composeRule.onNodeWithTag("reminder_accept").performClick()
        composeRule.onNodeWithTag("reminder_decline").performClick()
        assertEquals(1 to 1, accepted to declined)
    }

    @Test
    fun consent_dialog_waits_for_the_player_name_dialog() {
        composeRule.setContent { MaestroTheme { HomeScreen(askPlayerName = true, askReminder = true) } }
        composeRule.onNodeWithTag("player_name_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("reminder_consent_dialog").assertDoesNotExist()
    }

    @Test
    fun settings_with_the_reminder_switch_screenshot() {
        composeRule.setContent {
            MaestroTheme {
                SettingsScreenContent(
                    state = SettingsUiState(),
                    onSoundToggled = {},
                    onGameSpeedChanged = {},
                    onMaestroChattinessChanged = {},
                    onShowPointsRaceToggled = {},
                    onTriggerTestCrash = {},
                    onStartTestEndgameMatch = {},
                    onNavigateHome = {},
                    reminderAvailable = true,
                    reminderEnabled = true,
                )
            }
        }
        composeRule.onNodeWithTag("reminder_row").performScrollTo().assertIsDisplayed()
        captureScreenRoboImage(screen.path("settings_reminder"))
    }

    @Test
    fun settings_hide_the_reminder_switch_before_the_first_win_and_report_the_toggle() {
        var toggled: Boolean? = null
        var available by mutableStateOf(false)
        composeRule.setContent {
            MaestroTheme {
                SettingsScreenContent(
                    state = SettingsUiState(),
                    onSoundToggled = {},
                    onGameSpeedChanged = {},
                    onMaestroChattinessChanged = {},
                    onShowPointsRaceToggled = {},
                    onTriggerTestCrash = {},
                    onStartTestEndgameMatch = {},
                    onNavigateHome = {},
                    reminderAvailable = available,
                    onReminderToggled = { toggled = it },
                )
            }
        }
        composeRule.onNodeWithTag("reminder_row").assertDoesNotExist()
        available = true
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reminder_switch").performScrollTo().performClick()
        assertEquals(true, toggled)
    }
}
