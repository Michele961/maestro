package it.maestro.app.ui.screenshot

import it.maestro.app.scopaState

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import it.maestro.app.feature.table.presentation.GameIntent
import it.maestro.app.feature.table.presentation.GameReducer
import it.maestro.app.feature.table.presentation.OpponentMoveSummary
import it.maestro.app.feature.table.presentation.director.SceneStep
import it.maestro.app.feature.table.ui.TableContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import it.maestro.app.designsystem.theme.MaestroTheme
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
                    scene = sceneOf(uiState),
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
    fun opponentRevealShowsThePlayedCardOnTheTableWithoutLastMoveBanner() {
        val initial = GameReducer.initial(GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1))
        val uiState = initial.state.toUiState()
        val shown = Suit.entries.flatMap { suit -> Rank.entries.map { Card(it, suit) } }
            .first { it !in uiState.tableCards && it !in uiState.humanHand }

        composeRule.setContent {
            MaestroTheme {
                TableContent(
                    uiState = uiState,
                    scene = sceneOf(uiState, step = SceneStep.OpponentReveal(shown)),
                    onIntent = {},
                )
            }
        }

        // "Ultima giocata" è stata tolta (dossier del tavolo, 2026-10-27): racconta la carta girata sul tavolo.
        composeRule.onNodeWithTag("opponent_last_move_banner").assertDoesNotExist()
        composeRule.onNodeWithTag("card_${shown.suit.name}_${shown.rank.name}").assertExists()
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
                    scene = sceneOf(uiState),
                    onIntent = {},
                )
            }
        }

        // Non deve apparire alcun fumetto del maestro
        composeRule.onNodeWithTag("game_hint_banner").assertDoesNotExist()
    }

    @Test
    fun handIsNotPlayableWhileTheSceneIsBusyAndPlayableAtRest() {
        val initial = GameReducer.initial(GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1))
        var uiState = initial.state.toUiState()
        if (!uiState.isHumanTurn) {
            val botMove = it.maestro.game.scopa.legalMoves(initial.state.scopaState).first()
            val afterBot = GameReducer.reduce(initial.state, GameIntent.ApplyBotMove(botMove))
            uiState = afterBot.state.toUiState()
        }
        val state = uiState
        var scene by androidx.compose.runtime.mutableStateOf(sceneOf(state, step = SceneStep.DealCard(0, Seat.P1)))

        composeRule.setContent { MaestroTheme { TableContent(uiState = state, scene = scene, onIntent = {}) } }

        val cardToSelect = state.humanHand.first()
        val cardTag = "card_${cardToSelect.suit.name}_${cardToSelect.rank.name}"

        // Durante una scena (qui una distribuzione) la mano non è interagibile.
        composeRule.onNodeWithTag(cardTag).performClick()
        composeRule.onNodeWithTag("confirm_play_button").assertDoesNotExist()

        // A scena ferma la mano torna attiva e cliccabile.
        scene = sceneOf(state)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(cardTag).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("confirm_play_button").assertExists()
    }

    @Test
    fun deckShowsTheCardsLeftInTheScene() {
        val initial = GameReducer.initial(GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1))
        val uiState = initial.state.toUiState()

        composeRule.setContent { MaestroTheme { TableContent(uiState = uiState, scene = sceneOf(uiState), onIntent = {}) } }

        composeRule.onNodeWithText(uiState.deckCardCount.toString()).assertExists()
    }

    @org.junit.After
    fun tearDown() {
        org.koin.core.context.stopKoin()
    }
}
