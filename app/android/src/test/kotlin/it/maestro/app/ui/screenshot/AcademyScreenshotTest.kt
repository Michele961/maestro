package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.app.data.local.academy.AcademyContent
import it.maestro.app.data.local.academy.AcademyProgress
import it.maestro.app.ui.screens.academy.AcademyLessonContent
import it.maestro.app.ui.screens.academy.AcademyModuleCardState
import it.maestro.app.ui.screens.academy.AcademyModuleMapContent
import it.maestro.app.ui.screens.academy.AcademyViewMode
import it.maestro.app.ui.theme.MaestroTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot Roborazzi e test UI per le schermate dell'Accademia del Maestro (task F2-06).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "it-w411dp-h891dp-xxhdpi", application = Application::class)
class AcademyScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sampleModules = AcademyContent.MODULES

    @Test
    fun academy_map_initial_screenshot() {
        val cardStates = sampleModules.map { module ->
            AcademyModuleCardState(
                module = module,
                progress = AcademyProgress(
                    moduleId = module.id,
                    isUnlocked = module.number == 1,
                    isCompleted = false,
                ),
            )
        }

        composeRule.setContent {
            MaestroTheme {
                AcademyModuleMapContent(
                    modules = cardStates,
                    onOpenModule = {},
                    onNavigateHome = {},
                )
            }
        }

        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/academy_map_initial.png")
        composeRule.onNodeWithTag("academy_module_card_1").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_module_card_2").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_module_card_3").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_module_action_1").assertIsEnabled()
        composeRule.onNodeWithTag("academy_module_action_2").assertIsNotEnabled()
    }

    @Test
    fun academy_map_all_completed_screenshot() {
        val cardStates = sampleModules.map { module ->
            AcademyModuleCardState(
                module = module,
                progress = AcademyProgress(
                    moduleId = module.id,
                    isUnlocked = true,
                    isCompleted = true,
                ),
            )
        }

        composeRule.setContent {
            MaestroTheme {
                AcademyModuleMapContent(
                    modules = cardStates,
                    onOpenModule = {},
                    onNavigateHome = {},
                )
            }
        }

        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/academy_map_completed.png")
        composeRule.onNodeWithTag("academy_module_completed_tag_1").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_module_completed_tag_2").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_module_completed_tag_3").assertIsDisplayed()
    }

    @Test
    fun academy_lesson_step1_question_screenshot() {
        val lesson = AcademyViewMode.Lesson(
            module = sampleModules[0],
            currentStepIndex = 0,
        )

        composeRule.setContent {
            MaestroTheme {
                AcademyLessonContent(
                    lesson = lesson,
                    onSelectOption = {},
                    onPlayCard = {},
                    onNextStep = {},
                    onBackToMap = {},
                )
            }
        }

        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/academy_lesson_step1_question.png")
        composeRule.onNodeWithTag("academy_table_area").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_question_section").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_next_step_button").assertIsNotEnabled()
    }

    @Test
    fun academy_lesson_step2_card_action_screenshot() {
        val lesson = AcademyViewMode.Lesson(
            module = sampleModules[0],
            currentStepIndex = 1,
        )

        composeRule.setContent {
            MaestroTheme {
                AcademyLessonContent(
                    lesson = lesson,
                    onSelectOption = {},
                    onPlayCard = {},
                    onNextStep = {},
                    onBackToMap = {},
                )
            }
        }

        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/academy_lesson_step2_card_action.png")
        composeRule.onNodeWithTag("academy_table_area").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_card_action_section").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_hand_card_SETTE_BASTONI").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_next_step_button").assertIsNotEnabled()
    }

    @Test
    fun academy_lesson_step4_summary_screenshot() {
        val lesson = AcademyViewMode.Lesson(
            module = sampleModules[0],
            currentStepIndex = 3,
        )

        composeRule.setContent {
            MaestroTheme {
                AcademyLessonContent(
                    lesson = lesson,
                    onSelectOption = {},
                    onPlayCard = {},
                    onNextStep = {},
                    onBackToMap = {},
                )
            }
        }

        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/academy_lesson_step4_summary.png")
        composeRule.onNodeWithTag("academy_summary_section").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_complete_module_button").assertIsEnabled()
    }

    @Test
    fun backButtonNavigatesHome() {
        var navigatedHome = false
        val cardStates = sampleModules.map { module ->
            AcademyModuleCardState(
                module = module,
                progress = AcademyProgress(module.id, isUnlocked = true),
            )
        }

        composeRule.setContent {
            MaestroTheme {
                AcademyModuleMapContent(
                    modules = cardStates,
                    onOpenModule = {},
                    onNavigateHome = { navigatedHome = true },
                )
            }
        }

        composeRule.onNodeWithTag("academy_back_button").performClick()
        assertTrue(navigatedHome)
    }
}
