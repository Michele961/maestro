package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import it.maestro.app.game.GameUiState
import it.maestro.app.ui.dialogs.SettingsDialog
import it.maestro.app.ui.screens.TableContent
import it.maestro.app.ui.theme.MaestroTheme
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.DealScore
import it.maestro.game.scopa.ScoreCategory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Dialoghi su pergamena (F1-23): impostazioni, riepilogo smazzata, fine partita. */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "it-w411dp-h891dp-xxhdpi", application = Application::class)
class DialogsScreenshotTest {

    @get:Rule
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
        captureScreenRoboImage("build/outputs/roborazzi/dialog_settings.png")
    }

    @Test
    fun deal_summary_dialog_screenshot() {
        composeRule.setContent { MaestroTheme { TableContent(uiState = base.copy(pendingDealScore = score), onIntent = {}) } }
        captureScreenRoboImage("build/outputs/roborazzi/dialog_deal_summary.png")
    }

    @Test
    fun match_over_dialog_screenshot() {
        composeRule.setContent {
            MaestroTheme { TableContent(uiState = base.copy(phase = DealPhase.MATCH_OVER, humanScore = 11, opponentScore = 8), onIntent = {}) }
        }
        captureScreenRoboImage("build/outputs/roborazzi/dialog_match_over.png")
    }
}
