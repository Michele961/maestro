package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.app.game.GameUiState
import it.maestro.app.ui.screens.TableContent
import it.maestro.app.ui.theme.MaestroTheme
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Tavolo da gioco (F1-21): screenshot da confrontare con `docs/art/reference_table_screen.jpg`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "it-w411dp-h891dp-xxhdpi", application = Application::class)
class TableScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val midDeal = GameUiState(
        phase = DealPhase.PLAYING,
        humanSeat = Seat.P1,
        humanHand = listOf(Card(Rank.SETTE, Suit.DENARI), Card(Rank.RE, Suit.BASTONI), Card(Rank.TRE, Suit.SPADE)),
        tableCards = listOf(
            Card(Rank.ASSO, Suit.BASTONI),
            Card(Rank.SETTE, Suit.COPPE),
            Card(Rank.DUE, Suit.COPPE),
            Card(Rank.QUATTRO, Suit.SPADE),
        ),
        opponentCardCount = 3,
        deckCardCount = 24,
        humanCapturedCount = 6,
        opponentCapturedCount = 2,
        humanSweeps = 1,
        opponentSweeps = 0,
        humanScore = 4,
        opponentScore = 2,
        isHumanTurn = true,
        isBotThinking = false,
        pendingCaptureChoice = null,
        lastEvent = null,
        errorMessage = null,
    )

    @Test
    fun table_mid_deal_screenshot() {
        composeRule.setContent { MaestroTheme { TableContent(uiState = midDeal, onIntent = {}) } }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/table_mid_deal.png")
    }
}
