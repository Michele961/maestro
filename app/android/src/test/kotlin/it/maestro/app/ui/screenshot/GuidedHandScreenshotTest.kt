package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.app.designsystem.theme.MaestroTheme
import it.maestro.app.domain.guide.GuideLine
import it.maestro.app.domain.guide.GuideStep
import it.maestro.app.domain.guide.GuideTone
import it.maestro.app.domain.guide.GuidedHandStatus
import it.maestro.app.feature.firstlaunch.ui.FirstLaunchHost
import it.maestro.app.feature.firstlaunch.ui.FirstLaunchScreen
import it.maestro.app.feature.table.presentation.GameUiState
import it.maestro.app.feature.table.presentation.GuideUi
import it.maestro.app.feature.table.ui.TableContent
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Primo avvio e mano d'esordio guidata (F1G-01, F1G-02) sulla matrice di schermi. */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class GuidedHandScreenshotTest(private val screen: ScreenConfig) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenConfig.parameters()
    }

    @get:Rule(order = 0)
    val screenRule = ScreenConfigRule(screen)

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val table = GameUiState(
        phase = DealPhase.PLAYING,
        humanSeat = Seat.P1,
        humanHand = listOf(Card(Rank.TRE, Suit.DENARI), Card(Rank.RE, Suit.BASTONI), Card(Rank.CINQUE, Suit.SPADE)),
        tableCards = listOf(Card(Rank.TRE, Suit.COPPE), Card(Rank.ASSO, Suit.BASTONI), Card(Rank.QUATTRO, Suit.SPADE)),
        opponentCardCount = 3,
        deckCardCount = 24,
        humanCapturedCount = 0,
        opponentCapturedCount = 0,
        humanSweeps = 0,
        opponentSweeps = 0,
        humanScore = 0,
        opponentScore = 0,
        isHumanTurn = true,
        isBotThinking = false,
        pendingCaptureChoice = null,
        lastEvent = null,
        errorMessage = null,
    )

    private fun guided(line: GuideLine?, intro: Boolean = false, finished: Boolean = false) =
        table.copy(guide = GuideUi(line, intro, finished))

    @Test
    fun first_launch_asks_the_name_screenshot() {
        composeRule.setContent {
            MaestroTheme {
                FirstLaunchScreen(null, false, 20, onNameConfirmed = {}, onNameSkipped = {}, onStartHand = {}, onAlreadyKnow = {})
            }
        }
        composeRule.onNodeWithTag("player_name_dialog").assertExists()
        composeRule.onRoot().captureRoboImage(screen.path("first_launch_name"))
    }

    @Test
    fun first_launch_with_the_two_actions_screenshot() {
        composeRule.setContent {
            MaestroTheme {
                FirstLaunchScreen("Anna", true, 20, onNameConfirmed = {}, onNameSkipped = {}, onStartHand = {}, onAlreadyKnow = {})
            }
        }
        composeRule.onNodeWithText("Piacere, Anna. Cominciamo con una mano facile.", substring = true).assertExists()
        composeRule.onRoot().captureRoboImage(screen.path("first_launch"))
    }

    @Test
    fun a_guest_hears_the_guest_line() {
        composeRule.setContent {
            MaestroTheme {
                FirstLaunchScreen(null, true, 20, onNameConfirmed = {}, onNameSkipped = {}, onStartHand = {}, onAlreadyKnow = {})
            }
        }
        composeRule.onNodeWithText("Nessun problema: giochi da ospite.", substring = true).assertExists()
    }

    @Test
    fun the_first_hand_button_starts_the_hand_and_i_already_know_asks_for_the_skip_scene() {
        var started = 0
        var toGigi = 0
        composeRule.setContent {
            MaestroTheme {
                FirstLaunchHost(true, "Anna", true, 20, {}, {}, onStartHand = { started++ }, onGoToGigi = { toGigi++ })
            }
        }
        composeRule.onNodeWithTag("first_launch_start_button").performClick()
        assertEquals(1, started)
        composeRule.onNodeWithTag("first_launch_skip_button").performClick()
        composeRule.onNodeWithTag("story_scene").assertExists()
        composeRule.onRoot().captureRoboImage(screen.path("first_launch_skip_scene"))
        repeat(2) { composeRule.onNodeWithTag("story_scene").performClick() }
        assertEquals(0, toGigi)
        composeRule.onNodeWithTag("story_scene_end_button").performClick()
        assertEquals(1, toGigi)
    }

    @Test
    fun guided_table_with_the_instruction_screenshot() {
        val state = guided(GuideLine(GuideStep.SINGLE, GuideTone.INSTRUCTION, Card(Rank.TRE, Suit.COPPE)))
        composeRule.setContent { MaestroTheme { TableContent(uiState = state, scene = sceneOf(state), onIntent = {}) } }
        composeRule.onNodeWithTag("guide_bubble").assertExists()
        composeRule.onRoot().captureRoboImage(screen.path("guided_table_instruction"))
    }

    @Test
    fun guided_table_with_the_praise_screenshot() {
        val state = guided(GuideLine(GuideStep.SINGLE, GuideTone.OK)).copy(isHumanTurn = false)
        composeRule.setContent { MaestroTheme { TableContent(uiState = state, scene = sceneOf(state), onIntent = {}) } }
        composeRule.onNodeWithText("Presa pulita!", substring = true).assertExists()
        composeRule.onRoot().captureRoboImage(screen.path("guided_table_praise"))
    }

    @Test
    fun an_ordinary_table_has_no_guide_bubble() {
        composeRule.setContent { MaestroTheme { TableContent(uiState = table, scene = sceneOf(table), onIntent = {}) } }
        composeRule.onNodeWithTag("guide_bubble").assertDoesNotExist()
    }

    @Test
    fun guided_table_introduction_can_be_skipped_screenshot() {
        val state = guided(null, intro = true)
        var done: GuidedHandStatus? = null
        composeRule.setContent {
            MaestroTheme { TableContent(uiState = state, scene = sceneOf(state), onIntent = {}, onGuideDone = { done = it }) }
        }
        composeRule.onNodeWithText("Salta la guida").assertExists()
        composeRule.onRoot().captureRoboImage(screen.path("guided_table_intro"))
        composeRule.onNodeWithTag("story_scene_skip").performClick()
        assertEquals(null, done)
        // «Salta la guida» porta alla scena di «So già giocare» e da lì il capitolo 1 è fatto.
        repeat(2) { composeRule.onNodeWithTag("story_scene").performClick() }
        composeRule.onNodeWithTag("story_scene_end_button").performClick()
        assertEquals(GuidedHandStatus.SKIPPED, done)
    }

    @Test
    fun the_end_of_the_hand_says_goodbye_and_sends_to_gigi_screenshot() {
        val state = guided(null, finished = true)
        var done: GuidedHandStatus? = null
        composeRule.setContent {
            MaestroTheme { TableContent(uiState = state, scene = sceneOf(state), onIntent = {}, onGuideDone = { done = it }) }
        }
        composeRule.onNodeWithTag("story_scene").performClick()
        composeRule.onNodeWithTag("story_scene_end_button").assertExists()
        composeRule.onRoot().captureRoboImage(screen.path("guided_table_end"))
        composeRule.onNodeWithTag("story_scene_end_button").performClick()
        assertEquals(GuidedHandStatus.DONE, done)
    }
}
