package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.ai.mastery.AbilityRadar
import it.maestro.ai.mastery.MasteryProfile
import it.maestro.app.R
import it.maestro.app.feature.profile.ui.ProfileContent
import it.maestro.app.feature.profile.presentation.ProfileUiState
import it.maestro.app.designsystem.theme.MaestroTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Test UI e screenshot Roborazzi per la schermata Profilo & Radar delle Abilità (task F2-04).
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class ProfileScreenshotTest(private val screen: ScreenConfig) {

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
    fun captureProfileScreen_defaultState() {
        var backClicked = false

        val state = ProfileUiState(
            profile = MasteryProfile.INITIAL,
            rankTitleRes = R.string.mastery_rank_dilettante,
            maestroAdviceRes = R.string.profile_advice_default,
            isLoading = false,
        )

        composeRule.setContent {
            MaestroTheme {
                ProfileContent(
                    uiState = state,
                    onBackClicked = { backClicked = true },
                )
            }
        }

        composeRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_identity_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("profile_elo_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("profile_elo_text").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("profile_radar_panel").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("ability_radar_chart").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("radar_axis_sevens").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("radar_axis_denari").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("radar_axis_primiera").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("radar_axis_board").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("radar_axis_endgame").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("profile_maestro_advice_panel").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("profile_stats_card").performScrollTo().assertIsDisplayed()

        // Torna in cima: lo screenshot mostra la schermata come appare all'apertura.
        composeRule.onNodeWithTag("profile_identity_card").performScrollTo()
        composeRule.onRoot().captureRoboImage(screen.path("profile_screen_default"))

        composeRule.onNodeWithTag("profile_back_button").performClick()
        assertTrue(backClicked)
    }

    @Test
    fun captureProfileScreen_advancedCalibratedState() {
        val advancedProfile = MasteryProfile(
            matchesPlayed = 14,
            accuracyEma = 78.0,
            cognitiveElo = 1736,
            radar = AbilityRadar(
                sevens = 88,
                denari = 92,
                primiera = 74,
                board = 62,
                endgame = 85,
            ),
        )

        val state = ProfileUiState(
            profile = advancedProfile,
            rankTitleRes = R.string.mastery_rank_avanzato,
            maestroAdviceRes = R.string.profile_advice_board,
            isLoading = false,
        )

        composeRule.setContent {
            MaestroTheme {
                ProfileContent(
                    uiState = state,
                    onBackClicked = {},
                )
            }
        }

        composeRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_elo_text").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("ability_radar_chart").performScrollTo().assertIsDisplayed()

        // Torna in cima: lo screenshot mostra la schermata come appare all'apertura.
        composeRule.onNodeWithTag("profile_identity_card").performScrollTo()
        composeRule.onRoot().captureRoboImage(screen.path("profile_screen_advanced"))
    }
}
