package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.app.designsystem.theme.MaestroTheme
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.season.SeasonFacts
import it.maestro.app.domain.season.SeasonProgress
import it.maestro.app.domain.story.StepAction
import it.maestro.app.domain.story.StoryDirector
import it.maestro.app.feature.season.ui.SeasonScreen
import it.maestro.app.feature.season.ui.StoryScenePlayer
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Le scene della Scalata e il «prossimo passo» (F1G-16a) sulla matrice di schermi. */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class StoryScenesScreenshotTest(private val screen: ScreenConfig) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenConfig.parameters()
    }

    @get:Rule(order = 0)
    val screenRule = ScreenConfigRule(screen)

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val atStart = SeasonProgress.derive(SeasonFacts(), claimed = emptySet())
    private val afterGigi = SeasonProgress.derive(SeasonFacts(defeated = setOf(Opponent.GIGI)), claimed = emptySet())

    private val welcome1 = "Vedi quella targa «Olimpo» sul muro? Segna la nostra sedia, vuota da anni."
    private val welcome2 = "Se nessuno la occupa, il circolo perde il suo posto. E questa sala chiude."

    @Test
    fun story_scene_screenshot() {
        composeRule.setContent {
            MaestroTheme { StoryScenePlayer(StoryDirector.WELCOME, playerName = null, onDone = {}) }
        }
        composeRule.onRoot().captureRoboImage(screen.path("story_scene"))
    }

    @Test
    fun story_scene_with_a_character_screenshot() {
        val scene = requireNotNull(StoryDirector.open(5))
        composeRule.setContent { MaestroTheme { StoryScenePlayer(scene, playerName = null, onDone = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("story_scene_rosa"))
    }

    @Test
    fun season_with_next_step_screenshot() {
        composeRule.setContent { MaestroTheme { SeasonScreen(atStart, onNavigateBack = {}) } }
        composeRule.onNodeWithTag("season_next_button").assertExists()
        composeRule.onRoot().captureRoboImage(screen.path("season_next_step"))
    }

    @Test
    fun season_with_a_scene_on_top_screenshot() {
        composeRule.setContent {
            MaestroTheme { SeasonScreen(afterGigi, onNavigateBack = {}, scene = StoryDirector.close(2), playerName = "Anna") }
        }
        composeRule.onRoot().captureRoboImage(screen.path("season_scene"))
    }

    @Test
    fun a_tap_moves_to_the_next_line_and_the_last_one_ends_the_scene() {
        var done = 0
        composeRule.setContent {
            MaestroTheme { StoryScenePlayer(StoryDirector.WELCOME, playerName = null, onDone = { done++ }) }
        }
        composeRule.onNodeWithText(welcome1).assertExists()
        composeRule.onNodeWithTag("story_scene").performClick()
        composeRule.onNodeWithText(welcome2).assertExists()
        repeat(StoryDirector.WELCOME.beats.size - 2) { composeRule.onNodeWithTag("story_scene").performClick() }
        assertEquals(0, done)
        composeRule.onNodeWithTag("story_scene").performClick()
        assertEquals(1, done)
    }

    @Test
    fun skip_ends_the_scene_at_once() {
        var done = 0
        composeRule.setContent {
            MaestroTheme { StoryScenePlayer(StoryDirector.WELCOME, playerName = null, onDone = { done++ }) }
        }
        composeRule.onNodeWithTag("story_scene_skip").performClick()
        assertEquals(1, done)
    }

    @Test
    fun the_named_line_uses_the_name_only_when_there_is_one() {
        val clickToThird = {
            repeat(2) { composeRule.onNodeWithTag("story_scene").performClick() }
        }
        composeRule.setContent {
            MaestroTheme { StoryScenePlayer(StoryDirector.WELCOME, playerName = "Anna", onDone = {}) }
        }
        clickToThird()
        composeRule.onNodeWithText("Ci stai, Anna?", substring = true).assertExists()
    }

    @Test
    fun a_guest_without_a_name_gets_the_plain_line() {
        composeRule.setContent {
            MaestroTheme { StoryScenePlayer(StoryDirector.WELCOME, playerName = " ", onDone = {}) }
        }
        repeat(2) { composeRule.onNodeWithTag("story_scene").performClick() }
        composeRule.onNodeWithText("Ci stai?", substring = true).assertExists()
    }

    @Test
    fun the_next_step_button_reports_where_to_go() {
        var action: StepAction? = null
        composeRule.setContent { MaestroTheme { SeasonScreen(atStart, onNavigateBack = {}, onStepClicked = { action = it }) } }
        composeRule.onNodeWithTag("season_next_button").performClick()
        assertEquals(StepAction.PlayOpponent(Opponent.GIGI), action)
    }

    @Test
    fun the_remaining_tables_are_shown_from_the_chapters_available() {
        composeRule.setContent { MaestroTheme { SeasonScreen(afterGigi, onNavigateBack = {}) } }
        composeRule.onNodeWithTag("season_remaining").assertTextContains("Ancora 7 tavoli all'Olimpo dei Maestri")
    }
}
