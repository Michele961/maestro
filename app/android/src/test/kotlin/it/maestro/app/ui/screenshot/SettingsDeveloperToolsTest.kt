package it.maestro.app.ui.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import it.maestro.app.ui.dialogs.SettingsDialog
import it.maestro.app.ui.theme.MaestroTheme
import org.junit.Rule
import org.junit.runner.RunWith
import android.app.Application
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test

/** Task F1R-05: gli strumenti di sviluppo non compaiono nella build di rilascio. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class SettingsDeveloperToolsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsInReleaseHideDeveloperTools() {
        composeRule.setContent {
            MaestroTheme { SettingsDialog(true, {}, {}, {}, showDeveloperTools = false) }
        }
        composeRule.onNodeWithTag("sound_enabled_switch").assertExists()
        composeRule.onNodeWithTag("trigger_test_crash_button").assertDoesNotExist()
        composeRule.onNodeWithTag("start_test_match_button").assertDoesNotExist()
    }

    @Test
    fun settingsInDebugShowDeveloperTools() {
        composeRule.setContent {
            MaestroTheme { SettingsDialog(true, {}, {}, {}, showDeveloperTools = true) }
        }
        composeRule.onNodeWithTag("trigger_test_crash_button").assertExists()
        composeRule.onNodeWithTag("start_test_match_button").assertExists()
    }
}
