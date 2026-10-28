package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import it.maestro.app.designsystem.theme.MaestroTheme
import it.maestro.app.domain.coach.Chattiness
import it.maestro.app.domain.settings.GameSpeed
import it.maestro.app.feature.settings.presentation.SettingsUiState
import it.maestro.app.feature.settings.ui.SettingsScreenContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Schermata Impostazioni (F1R-31a): stati principali nella matrice a 4 configurazioni, font al 130% compreso. */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class SettingsScreenshotTest(private val screen: ScreenConfig) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenConfig.parameters()
    }

    @get:Rule(order = 0)
    val screenRule = ScreenConfigRule(screen)

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @Composable
    private fun Content(
        state: SettingsUiState = SettingsUiState(),
        showDeveloperTools: Boolean = false,
        onSoundToggled: (Boolean) -> Unit = {},
        onGameSpeedChanged: (GameSpeed) -> Unit = {},
        onMaestroChattinessChanged: (Chattiness) -> Unit = {},
        onShowPointsRaceToggled: (Boolean) -> Unit = {},
    ) = MaestroTheme {
        SettingsScreenContent(
            state = state,
            onSoundToggled = onSoundToggled,
            onGameSpeedChanged = onGameSpeedChanged,
            onMaestroChattinessChanged = onMaestroChattinessChanged,
            onShowPointsRaceToggled = onShowPointsRaceToggled,
            onTriggerTestCrash = {},
            onStartTestEndgameMatch = {},
            onNavigateHome = {},
            showDeveloperTools = showDeveloperTools,
        )
    }

    @Test
    fun settings_default_screenshot() {
        composeRule.setContent { Content() }
        captureScreenRoboImage(screen.path("settings_default"))
    }

    @Test
    fun settings_sound_off_other_choices_screenshot() {
        composeRule.setContent {
            Content(
                SettingsUiState(
                    soundEnabled = false,
                    gameSpeed = GameSpeed.FAST,
                    maestroChattiness = Chattiness.QUIET,
                    showPointsRace = true,
                ),
            )
        }
        captureScreenRoboImage(screen.path("settings_choices"))
    }

    @Test
    fun settings_developer_section_screenshot() {
        composeRule.setContent { Content(showDeveloperTools = true) }
        captureScreenRoboImage(screen.path("settings_developer"))
    }

    @Test
    fun choices_report_what_was_tapped() {
        var sound: Boolean? = null
        var speed: GameSpeed? = null
        var chat: Chattiness? = null
        var race: Boolean? = null
        composeRule.setContent {
            Content(
                onSoundToggled = { sound = it },
                onGameSpeedChanged = { speed = it },
                onMaestroChattinessChanged = { chat = it },
                onShowPointsRaceToggled = { race = it },
            )
        }
        composeRule.onNodeWithTag("sound_enabled_switch").performScrollTo().performClick()
        composeRule.onNodeWithTag("game_speed_fast").performScrollTo().performClick()
        composeRule.onNodeWithTag("maestro_chat_off").performScrollTo().performClick()
        composeRule.onNodeWithTag("points_race_switch").performScrollTo().performClick()

        assertEquals(false, sound)
        assertEquals(GameSpeed.FAST, speed)
        assertEquals(Chattiness.OFF, chat)
        assertTrue(race == true)
    }
}
