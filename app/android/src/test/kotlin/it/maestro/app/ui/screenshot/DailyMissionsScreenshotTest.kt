package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import it.maestro.app.domain.retention.DailyMission
import it.maestro.app.domain.retention.DailyMissionType
import it.maestro.app.domain.retention.DailyMissionsState
import it.maestro.app.domain.retention.StreakState
import it.maestro.app.feature.missions.ui.DailyMissionsDialog
import it.maestro.app.feature.missions.presentation.DailyMissionsUiState
import it.maestro.app.designsystem.theme.MaestroTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot test per il Dialog delle Missioni Didattiche Quotidiane e Streak ("Fiamma") (task F2-05).
 */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class DailyMissionsScreenshotTest(private val screen: ScreenConfig) {

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
    fun daily_missions_dialog_active_streak_screenshot() {
        val uiState = DailyMissionsUiState(
            streak = StreakState(
                currentStreak = 4,
                bestStreak = 7,
                lastPlayedDate = "2026-10-25",
                freezeTokens = 1,
            ),
            missionsState = DailyMissionsState(
                date = "2026-10-26",
                missions = listOf(
                    DailyMission(
                        id = "puzzle_1",
                        type = DailyMissionType.SOLVE_DAILY_PUZZLE,
                        progress = 1,
                        target = 1,
                        isCompleted = true,
                        isClaimed = false,
                    ),
                    DailyMission(
                        id = "sevens_1",
                        type = DailyMissionType.ANSWER_SEVENS_CHECKS,
                        progress = 1,
                        target = 2,
                        isCompleted = false,
                    ),
                    DailyMission(
                        id = "denari_1",
                        type = DailyMissionType.CAPTURE_DENARI,
                        progress = 3,
                        target = 10,
                        isCompleted = false,
                    ),
                ),
            ),
        )

        composeRule.setContent {
            MaestroTheme {
                DailyMissionsDialog(
                    uiState = uiState,
                    onDismissRequest = {},
                )
            }
        }
        captureScreenRoboImage(screen.path("daily_missions_dialog_active_streak"))
    }

    @Test
    fun daily_missions_dialog_all_completed_screenshot() {
        val uiState = DailyMissionsUiState(
            streak = StreakState(
                currentStreak = 5,
                bestStreak = 7,
                lastPlayedDate = "2026-10-26",
                freezeTokens = 1,
            ),
            missionsState = DailyMissionsState(
                date = "2026-10-26",
                missions = listOf(
                    DailyMission(
                        id = "puzzle_1",
                        type = DailyMissionType.SOLVE_DAILY_PUZZLE,
                        progress = 1,
                        target = 1,
                        isCompleted = true,
                        isClaimed = false,
                    ),
                    DailyMission(
                        id = "sevens_1",
                        type = DailyMissionType.ANSWER_SEVENS_CHECKS,
                        progress = 2,
                        target = 2,
                        isCompleted = true,
                    ),
                    DailyMission(
                        id = "denari_1",
                        type = DailyMissionType.CAPTURE_DENARI,
                        progress = 10,
                        target = 10,
                        isCompleted = true,
                    ),
                ),
            ),
        )

        composeRule.setContent {
            MaestroTheme {
                DailyMissionsDialog(
                    uiState = uiState,
                    onDismissRequest = {},
                )
            }
        }
        captureScreenRoboImage(screen.path("daily_missions_dialog_all_completed"))
    }
}
