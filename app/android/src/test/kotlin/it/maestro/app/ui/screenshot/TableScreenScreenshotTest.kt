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
import it.maestro.game.scopa.ScopaMove
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        opponentSweeps = 1,
        humanSweepCards = listOf(Card(Rank.SETTE, Suit.DENARI)),
        opponentSweepCards = listOf(Card(Rank.CINQUE, Suit.SPADE)),
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

    @Test
    fun table_with_maestro_hint_screenshot() {
        val hint = ScopaMove(card = midDeal.humanHand[0], capture = setOf(Card(Rank.SETTE, Suit.COPPE)))
        composeRule.setContent { MaestroTheme { TableContent(uiState = midDeal.copy(suggestedMove = hint), onIntent = {}) } }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/table_hint.png")
    }

    @Test
    fun table_with_bot_card_play_screenshot() {
        val botMove = ScopaMove(card = Card(Rank.SETTE, Suit.SPADE), capture = setOf(Card(Rank.SETTE, Suit.COPPE)))
        val botState = midDeal.copy(
            lastMove = botMove,
            lastMover = Seat.P2,
            lastMoveId = 1L,
            isHumanTurn = false,
            isBotThinking = false,
        )
        composeRule.setContent { MaestroTheme { TableContent(uiState = botState, onIntent = {}) } }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/table_bot_play.png")
    }

    @Test
    fun pause_opens_dialog_and_menu_goes_home() {
        var home = 0
        composeRule.setContent { MaestroTheme { TableContent(uiState = midDeal, onIntent = {}, onNavigateHome = { home++ }) } }
        composeRule.onNodeWithTag("pause_button").performClick()
        composeRule.onNodeWithTag("pause_home_button").performClick()
        org.junit.Assert.assertEquals(1, home)
    }

    @Test
    fun playing_card_in_game_does_not_crash() {
        val viewModel = it.maestro.app.game.GameViewModel(
            ioDispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
        )
        viewModel.onIntent(
            it.maestro.app.game.GameIntent.StartMatch(
                config = it.maestro.game.scopa.ScopaConfig(targetScore = 11),
                seed = 42L,
                humanSeat = it.maestro.game.model.Seat.P1,
            ),
        )

        composeRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MaestroTheme {
                TableContent(
                    uiState = uiState,
                    onIntent = viewModel::onIntent,
                )
            }
        }

        // Troviamo la carta nella mano dell'umano e clicchiamola
        val humanCard = viewModel.uiState.value.humanHand.first()
        composeRule.onNodeWithTag("card_${humanCard.suit.name}_${humanCard.rank.name}").performClick()
        composeRule.mainClock.advanceTimeBy(400L)
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(600L)
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(1000L)
        composeRule.waitForIdle()

        org.junit.Assert.assertTrue(viewModel.uiState.value.lastMoveId > 0L)
    }
}
