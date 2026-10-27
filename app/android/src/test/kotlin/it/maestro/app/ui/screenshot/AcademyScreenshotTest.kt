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
import it.maestro.app.data.local.academy.AcademyChapterId
import it.maestro.app.data.local.academy.AcademyContent
import it.maestro.app.data.local.academy.AcademyLessonProgress
import it.maestro.app.ui.screens.academy.AcademyChapterCardState
import it.maestro.app.ui.screens.academy.AcademyChapterMapContent
import it.maestro.app.ui.screens.academy.AcademyLessonCardState
import it.maestro.app.ui.screens.academy.AcademyLessonContent
import it.maestro.app.ui.screens.academy.AcademyViewMode
import it.maestro.app.ui.theme.MaestroTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot Roborazzi e test UI per l'Accademia arricchita a Capitoli e Sotto-Moduli (task F2-06).
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class AcademyScreenshotTest(private val screen: ScreenConfig) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenConfig.parameters()
    }

    @get:Rule(order = 0)
    val screenRule = ScreenConfigRule(screen)


    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val sampleChapters = AcademyContent.CHAPTERS

    @Test
    fun academy_map_initial_screenshot() {
        val chapterStates = sampleChapters.map { chapter ->
            val lessonStates = chapter.lessons.map { lesson ->
                AcademyLessonCardState(
                    lesson = lesson,
                    progress = AcademyLessonProgress(
                        lessonId = lesson.id,
                        isUnlocked = chapter.number == 1 && lesson.lessonNumber == 1,
                        isCompleted = false,
                    ),
                )
            }
            AcademyChapterCardState(
                chapter = chapter,
                lessons = lessonStates,
                completedCount = 0,
                totalCount = lessonStates.size,
                isCompleted = false,
                isUnlocked = chapter.number == 1,
            )
        }

        composeRule.setContent {
            MaestroTheme {
                AcademyChapterMapContent(
                    chapters = chapterStates,
                    expandedChapterId = AcademyChapterId.CHAPTER_1_FONDAMENTI,
                    onToggleChapter = {},
                    onOpenLesson = {},
                    onNavigateHome = {},
                )
            }
        }

        composeRule.onRoot().captureRoboImage(screen.path("academy_map_initial"))
        composeRule.onNodeWithTag("academy_chapter_card_1").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_lesson_tile_1_1").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_lesson_action_1_1").assertIsEnabled()
        composeRule.onNodeWithTag("academy_lesson_action_1_2").assertIsNotEnabled()
    }

    @Test
    fun academy_map_all_completed_screenshot() {
        val chapterStates = sampleChapters.map { chapter ->
            val lessonStates = chapter.lessons.map { lesson ->
                AcademyLessonCardState(
                    lesson = lesson,
                    progress = AcademyLessonProgress(
                        lessonId = lesson.id,
                        isUnlocked = true,
                        isCompleted = true,
                    ),
                )
            }
            AcademyChapterCardState(
                chapter = chapter,
                lessons = lessonStates,
                completedCount = lessonStates.size,
                totalCount = lessonStates.size,
                isCompleted = true,
                isUnlocked = true,
            )
        }

        composeRule.setContent {
            MaestroTheme {
                AcademyChapterMapContent(
                    chapters = chapterStates,
                    expandedChapterId = AcademyChapterId.CHAPTER_1_FONDAMENTI,
                    onToggleChapter = {},
                    onOpenLesson = {},
                    onNavigateHome = {},
                )
            }
        }

        composeRule.onRoot().captureRoboImage(screen.path("academy_map_completed"))
        composeRule.onNodeWithTag("academy_chapter_completed_tag_1", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("academy_lesson_completed_icon_1_1").assertIsDisplayed()
    }

    @Test
    fun academy_lesson_step1_question_screenshot() {
        val lesson = AcademyViewMode.Lesson(
            lesson = sampleChapters[0].lessons[0],
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

        composeRule.onRoot().captureRoboImage(screen.path("academy_lesson_step1_question"))
        composeRule.onNodeWithTag("academy_table_area").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_question_section").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_next_step_button").assertIsNotEnabled()
    }

    @Test
    fun academy_lesson_step2_card_action_screenshot() {
        // Lezione 1.3: Il Re dei Denari (Card Action)
        val lesson = AcademyViewMode.Lesson(
            lesson = sampleChapters[0].lessons[2],
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

        composeRule.onRoot().captureRoboImage(screen.path("academy_lesson_step2_card_action"))
        composeRule.onNodeWithTag("academy_table_area").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_card_action_section").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_hand_card_SETTE_BASTONI").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_next_step_button").assertIsNotEnabled()
    }

    @Test
    fun academy_lesson_step4_summary_screenshot() {
        // Step finale di riepilogo e proclamazione regola ferrea
        val lesson = AcademyViewMode.Lesson(
            lesson = sampleChapters[0].lessons[0],
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

        composeRule.onRoot().captureRoboImage(screen.path("academy_lesson_step4_summary"))
        composeRule.onNodeWithTag("academy_summary_section").assertIsDisplayed()
        composeRule.onNodeWithTag("academy_complete_module_button").assertIsEnabled()
    }

    @Test
    fun backButtonNavigatesHome() {
        var navigatedHome = false
        val chapterStates = sampleChapters.map { chapter ->
            AcademyChapterCardState(
                chapter = chapter,
                lessons = emptyList(),
                completedCount = 0,
                totalCount = 0,
                isCompleted = false,
                isUnlocked = true,
            )
        }

        composeRule.setContent {
            MaestroTheme {
                AcademyChapterMapContent(
                    chapters = chapterStates,
                    expandedChapterId = null,
                    onToggleChapter = {},
                    onOpenLesson = {},
                    onNavigateHome = { navigatedHome = true },
                )
            }
        }

        composeRule.onNodeWithTag("academy_back_button").performClick()
        assertTrue(navigatedHome)
    }
}
