package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import it.maestro.app.game.GameIntent
import it.maestro.app.game.GameReducer
import it.maestro.app.game.OpponentMoveSummary
import it.maestro.app.ui.screens.TableContent
import it.maestro.app.ui.theme.MaestroTheme
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaMove
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "it-w360dp-h800dp-xxhdpi", application = Application::class)
class TableGesturesTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tappingCardSelectsItAndShowsConfirmPlayButton() {
        val initial = GameReducer.initial(GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1))
        var uiState = initial.state.toUiState()
        // Assicuriamoci che sia il turno dell'umano
        if (!uiState.isHumanTurn) {
            val botMove = it.maestro.game.scopa.legalMoves(initial.state.scopaState).first()
            val afterBot = GameReducer.reduce(initial.state, GameIntent.ApplyBotMove(botMove))
            uiState = afterBot.state.toUiState()
        }

        var playedCard: Card? = null
        composeRule.setContent {
            MaestroTheme {
                TableContent(
                    uiState = uiState,
                    onIntent = { intent ->
                        if (intent is GameIntent.PlayCard) {
                            playedCard = intent.card
                        }
                    },
                )
            }
        }

        // Attendiamo che qualsiasi animazione iniziale sia conclusa
        composeRule.mainClock.advanceTimeBy(3000L)
        composeRule.waitForIdle()

        val cardToSelect = uiState.humanHand.first()
        val cardTag = "card_${cardToSelect.suit.name}_${cardToSelect.rank.name}"

        // Prima del tap: nessun pulsante di conferma
        composeRule.onNodeWithTag("confirm_play_button").assertDoesNotExist()

        // 1. Primo tap: seleziona la carta e mostra il pulsante di conferma
        composeRule.onNodeWithTag(cardTag).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("confirm_play_button").assertExists()

        // 2. Tap sul pulsante di conferma: gioca la carta
        composeRule.onNodeWithTag("confirm_play_button").performClick()
        composeRule.waitForIdle()

        assertEquals(cardToSelect, playedCard)
    }

    @Test
    fun opponentLastMoveBannerIsDisplayedWhenSummaryPresent() {
        val initial = GameReducer.initial(GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1))
        val summary = OpponentMoveSummary(
            card = Card(Rank.SETTE, Suit.DENARI),
            capture = setOf(Card(Rank.SETTE, Suit.COPPE)),
            isSweep = false,
        )
        val uiState = initial.state.toUiState().copy(
            opponentLastMove = summary,
            isHumanTurn = true,
        )

        composeRule.setContent {
            MaestroTheme {
                TableContent(
                    uiState = uiState,
                    onIntent = {},
                )
            }
        }

        // Il banner dell'ultima mossa avversario deve essere presente a schermo
        composeRule.onNodeWithTag("opponent_last_move_banner").assertExists()
    }

    @Test
    fun maestroSpeechBubbleIsNotShownForRoutineMoves() {
        val initial = GameReducer.initial(GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1))
        val uiState = initial.state.toUiState().copy(
            lastMove = ScopaMove(card = Card(Rank.QUATTRO, Suit.SPADE)),
            lastMover = Seat.P2,
            lastMoveId = 1L,
            suggestedMove = null, // Nessun consiglio richiesto
        )

        composeRule.setContent {
            MaestroTheme {
                TableContent(
                    uiState = uiState,
                    onIntent = {},
                )
            }
        }

        // Non deve apparire alcun fumetto del maestro
        composeRule.onNodeWithTag("game_hint_banner").assertDoesNotExist()
    }

    @Test
    fun dealAnimationDisablesAndHidesHandsUntilFinished() {
        val initial = GameReducer.initial(GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1))
        var uiState = initial.state.toUiState()
        if (!uiState.isHumanTurn) {
            val botMove = it.maestro.game.scopa.legalMoves(initial.state.scopaState).first()
            val afterBot = GameReducer.reduce(initial.state, GameIntent.ApplyBotMove(botMove))
            uiState = afterBot.state.toUiState()
        }

        composeRule.setContent {
            MaestroTheme {
                TableContent(
                    uiState = uiState,
                    onIntent = {},
                )
            }
        }

        val cardToSelect = uiState.humanHand.first()
        val cardTag = "card_${cardToSelect.suit.name}_${cardToSelect.rank.name}"

        // Durante la distribuzione pendente o attiva, la mano non è interagibile
        composeRule.onNodeWithTag(cardTag).performClick()
        composeRule.onNodeWithTag("confirm_play_button").assertDoesNotExist()

        // Una volta terminata l'animazione di smazzata, la mano diventa attiva e cliccabile
        composeRule.mainClock.advanceTimeBy(3000L)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(cardTag).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("confirm_play_button").assertExists()
    }

    @Test
    fun deckCountDecrementsGraduallyDuringDeal() {
        val initial = GameReducer.initial(GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1))
        val uiState = initial.state.toUiState()

        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MaestroTheme {
                TableContent(
                    uiState = uiState,
                    onIntent = {},
                )
            }
        }

        // All'inizio della smazzata, il mazzo mostra tutte le 40 carte
        composeRule.onNodeWithText("40").assertExists()

        // Avanziamo a metà della distribuzione iniziale (es. dopo 3 carte a terra: 220 + 3*160 = ~700ms)
        composeRule.mainClock.advanceTimeBy(700L)
        // 40 non c'è più perché le prime carte sono state distribuite
        composeRule.onNodeWithText("40").assertDoesNotExist()

        // Avanziamo al termine di tutta la distribuzione iniziale
        composeRule.mainClock.advanceTimeBy(3000L)
        composeRule.onNodeWithText("30").assertExists()
        composeRule.mainClock.autoAdvance = true
    }

    @org.junit.After
    fun tearDown() {
        org.koin.core.context.stopKoin()
    }
}
