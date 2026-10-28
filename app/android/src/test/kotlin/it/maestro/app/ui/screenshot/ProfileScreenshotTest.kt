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
import it.maestro.app.feature.profile.presentation.ProfileTab
import it.maestro.app.domain.retention.StreakState
import it.maestro.app.domain.mastery.MatchHistoryEntry
import it.maestro.app.feature.profile.presentation.RadarAxis
import it.maestro.game.training.TrainingFundamental
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.opponents.OpponentProgress
import it.maestro.app.feature.pregame.presentation.LadderState
import it.maestro.app.feature.profile.ui.ProfileContent
import it.maestro.app.feature.profile.presentation.ProfileUiState
import it.maestro.app.designsystem.theme.MaestroTheme
import org.junit.Assert.assertEquals
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

    @Test
    fun captureProfileScreen_ladderCompleteWithOpenDetail() {
        val state = ProfileUiState(
            profile = MasteryProfile.INITIAL,
            earnedTitles = Opponent.entries,
            currentTitle = Opponent.MAESTRO,
            ladder = LadderState.of(OpponentProgress(Opponent.entries.toSet())),
        )

        composeRule.setContent { MaestroTheme { ProfileContent(uiState = state, onBackClicked = {}) } }

        composeRule.onNodeWithTag("profile_ladder_TONINO").performScrollTo().performClick()
        composeRule.onNodeWithTag("profile_ladder_detail").performScrollTo().assertIsDisplayed()
        composeRule.onRoot().captureRoboImage(screen.path("profile_screen_ladder_complete"))
    }

    @Test
    fun captureProfileScreen_newPlayerLadderShowsLocks() {
        val state = ProfileUiState(ladder = LadderState.of(OpponentProgress()))

        composeRule.setContent { MaestroTheme { ProfileContent(uiState = state, onBackClicked = {}) } }

        composeRule.onNodeWithTag("profile_ladder_GIGI").performScrollTo().performClick()
        composeRule.onNodeWithTag("profile_ladder_detail").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("profile_ladder_MAESTRO").performScrollTo().performClick()
        composeRule.onRoot().captureRoboImage(screen.path("profile_screen_ladder_new_player"))
    }

    @Test
    fun captureProfileScreen_weakAxisWithTrainButtonAndExplanation() {
        val state = ProfileUiState(
            profile = MasteryProfile(
                matchesPlayed = 6,
                accuracyEma = 70.0,
                cognitiveElo = 1300,
                radar = AbilityRadar(sevens = 80, denari = 35, primiera = 70, board = 60, endgame = 75),
            ),
            maestroAdviceRes = R.string.profile_advice_denari,
            weakestAxis = RadarAxis.DENARI,
            trainObjective = TrainingFundamental.DENARI_COUNT,
        )
        var trained: TrainingFundamental? = null

        composeRule.setContent {
            MaestroTheme { ProfileContent(uiState = state, onBackClicked = {}, onTrain = { trained = it }) }
        }

        composeRule.onNodeWithTag("radar_axis_endgame").performScrollTo().performClick()
        composeRule.onNodeWithTag("profile_radar_explanation").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("profile_train_button").performScrollTo().assertIsDisplayed()
        composeRule.onRoot().captureRoboImage(screen.path("profile_screen_weak_axis_train"))
        composeRule.onNodeWithTag("profile_train_button").performClick()
        assertEquals(TrainingFundamental.DENARI_COUNT, trained)
    }

    @Test
    fun captureProfileScreen_weakBoardAxisHasNoTrainButton() {
        val state = ProfileUiState(
            profile = MasteryProfile(
                matchesPlayed = 6,
                radar = AbilityRadar(sevens = 80, denari = 75, primiera = 70, board = 30, endgame = 75),
            ),
            maestroAdviceRes = R.string.profile_advice_board,
            weakestAxis = RadarAxis.BOARD,
        )

        composeRule.setContent { MaestroTheme { ProfileContent(uiState = state, onBackClicked = {}) } }

        composeRule.onNodeWithTag("profile_maestro_advice_panel").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("profile_train_button").assertDoesNotExist()
        composeRule.onRoot().captureRoboImage(screen.path("profile_screen_weak_board_no_train"))
    }

    @Test
    fun captureProfileScreen_historyShowsOpponentAndMode() {
        fun entry(i: Int, opponent: String?, objective: String?, win: Boolean) = MatchHistoryEntry(
            id = "m$i", timestampMillis = 0L, accuracy = 80 - i, cognitiveElo = 1200, eloDelta = if (win) 18 else -12,
            humanScore = if (win) 11 else 7, botScore = if (win) 6 else 11, isWin = win, opponent = opponent, sparringObjective = objective,
        )
        val state = ProfileUiState(
            selectedTab = ProfileTab.HISTORY,
            matchHistory = listOf(
                entry(0, Opponent.TONINO.name, null, win = true),
                entry(1, Opponent.GIGI.name, TrainingFundamental.DENARI_COUNT.name, win = false),
                entry(2, null, null, win = true),
            ),
        )

        composeRule.setContent { MaestroTheme { ProfileContent(uiState = state, onBackClicked = {}) } }

        composeRule.onNodeWithTag("profile_history_card_0").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage(screen.path("profile_screen_history_modes"))
    }

    @Test
    fun captureProfileScreen_statsShowRecentRecordAndStreak() {
        val state = ProfileUiState(
            profile = MasteryProfile(matchesPlayed = 9, accuracyEma = 74.0, cognitiveElo = 1400),
            streak = StreakState(currentStreak = 4, bestStreak = 9, lastPlayedDate = "2026-10-28", freezeTokens = 1),
            matchHistory = List(3) { MatchHistoryEntry("m$it", 0L, 80, 1400, 5, 11, 4, it != 1) },
        )

        composeRule.setContent { MaestroTheme { ProfileContent(uiState = state, onBackClicked = {}) } }

        composeRule.onNodeWithTag("profile_stats_card").performScrollTo().assertIsDisplayed()
        composeRule.onRoot().captureRoboImage(screen.path("profile_screen_stats_streak"))
    }
}
