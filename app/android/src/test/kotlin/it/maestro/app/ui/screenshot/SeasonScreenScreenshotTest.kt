package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.app.designsystem.theme.MaestroTheme
import it.maestro.app.domain.academy.AcademyChapterId
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.season.SeasonFacts
import it.maestro.app.domain.season.SeasonProgress
import it.maestro.app.feature.season.ui.SeasonScreen
import it.maestro.game.training.TrainingFundamental
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** La Scalata dei Circoli (F1G-07) sulla matrice di schermi: all'inizio, a metà e finita. */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class SeasonScreenScreenshotTest(private val screen: ScreenConfig) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenConfig.parameters()
    }

    @get:Rule(order = 0)
    val screenRule = ScreenConfigRule(screen)

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val midway = SeasonFacts(
        defeated = setOf(Opponent.GIGI, Opponent.NONNA_ROSA),
        sparringChecksPlayed = setOf(TrainingFundamental.SEVENS_COUNT),
        dailyPuzzleSolvedToday = true,
    )

    private fun progress(facts: SeasonFacts) = SeasonProgress.derive(facts, claimed = emptySet())

    @Test
    fun season_at_the_start_screenshot() {
        composeRule.setContent { MaestroTheme { SeasonScreen(progress(SeasonFacts()), onNavigateBack = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("season_start"))
    }

    @Test
    fun season_midway_screenshot() {
        composeRule.setContent { MaestroTheme { SeasonScreen(progress(midway), onNavigateBack = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("season_midway"))
    }

    @Test
    fun season_complete_screenshot() {
        val all = SeasonFacts(
            defeated = Opponent.entries.toSet(),
            dailyPuzzleSolvedToday = true,
            completedAcademyChapters = AcademyChapterId.entries.toSet(),
            sparringChecksPlayed = TrainingFundamental.entries.toSet(),
        )
        composeRule.setContent { MaestroTheme { SeasonScreen(progress(all), onNavigateBack = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("season_complete"))
    }

    @Test
    fun the_current_chapter_and_the_done_ones_are_shown_and_back_works() {
        var back = 0
        composeRule.setContent { MaestroTheme { SeasonScreen(progress(midway), onNavigateBack = { back++ }) } }
        composeRule.onNodeWithTag("season_current_card").assertExists()
        composeRule.onNodeWithTag("season_done_2").assertExists()
        composeRule.onNodeWithTag("season_circle_banner").assertExists()
        composeRule.onNodeWithTag("season_done_thumb_2").assertExists()
        composeRule.onNodeWithTag("season_back_button").performClick()
        assertEquals(1, back)
    }
}
