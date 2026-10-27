package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import it.maestro.app.game.GameUiState
import it.maestro.app.ui.dialogs.SettingsDialog
import it.maestro.app.ui.screens.TableContent
import it.maestro.app.ui.theme.MaestroTheme
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.DealScore
import it.maestro.game.scopa.ScoreCategory
import it.maestro.game.training.TrainingFundamental
import it.maestro.game.training.TrainingOption
import it.maestro.game.training.TrainingQuestion
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Dialoghi su pergamena (F1-23): impostazioni, riepilogo smazzata, fine partita. */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class DialogsScreenshotTest(private val screen: ScreenConfig) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenConfig.parameters()
    }

    @get:Rule(order = 0)
    val screenRule = ScreenConfigRule(screen)


    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val base = GameUiState(
        phase = DealPhase.DEAL_OVER,
        humanSeat = Seat.P1,
        humanHand = emptyList(),
        tableCards = emptyList(),
        opponentCardCount = 0,
        deckCardCount = 0,
        humanCapturedCount = 22,
        opponentCapturedCount = 18,
        humanSweeps = 2,
        opponentSweeps = 0,
        humanScore = 7,
        opponentScore = 3,
        isHumanTurn = false,
        isBotThinking = false,
        pendingCaptureChoice = null,
        lastEvent = null,
        errorMessage = null,
    )

    private val score = DealScore(
        pointsBySeatAndCategory = mapOf(
            Seat.P1 to ScoreCategory.entries.associateWith { 0 } + mapOf(ScoreCategory.CARTE to 1, ScoreCategory.SETTEBELLO to 1, ScoreCategory.SCOPE to 2),
            Seat.P2 to ScoreCategory.entries.associateWith { 0 } + mapOf(ScoreCategory.DENARI to 1, ScoreCategory.PRIMIERA to 1),
        ),
        primieraSums = mapOf(Seat.P1 to 70, Seat.P2 to 78),
    )

    @Test
    fun settings_dialog_screenshot() {
        composeRule.setContent { MaestroTheme { SettingsDialog(true, {}, {}, {}) } }
        captureScreenRoboImage(screen.path("dialog_settings"))
    }

    @Test
    fun deal_summary_dialog_screenshot() {
        composeRule.setContent { MaestroTheme { TableContent(uiState = base.copy(pendingDealScore = score), onIntent = {}) } }
        captureScreenRoboImage(screen.path("dialog_deal_summary"))
    }

    @Test
    fun training_question_dialog_screenshot() {
        val question = TrainingQuestion(
            fundamental = TrainingFundamental.SEVENS_COUNT,
            options = listOf(0, 1, 2, 3).map { TrainingOption.IntCount(it) },
            correctIndex = 2,
        )
        val opponentMove = it.maestro.app.game.OpponentMoveSummary(
            card = Card(Rank.SETTE, Suit.DENARI),
            capture = setOf(Card(Rank.SETTE, Suit.SPADE)),
            isSweep = false,
        )
        composeRule.setContent {
            MaestroTheme {
                TableContent(
                    uiState = base.copy(
                        phase = DealPhase.PLAYING,
                        activeTrainingQuestion = question,
                        opponentLastMove = opponentMove,
                    ),
                    onIntent = {},
                )
            }
        }
        captureScreenRoboImage(screen.path("dialog_training_question"))
    }
}
