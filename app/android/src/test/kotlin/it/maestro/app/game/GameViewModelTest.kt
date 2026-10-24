package it.maestro.app.game

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.legalMoves
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameViewModelTest {

    @Test
    fun viewModelEmitsInitialUiStateAndHandlesCardPlay() {
        val viewModel = GameViewModel(ioDispatcher = Dispatchers.Unconfined)
        val initialUi = viewModel.uiState.value

        assertEquals(DealPhase.PLAYING, initialUi.phase)
        assertEquals(3, initialUi.humanHand.size)
        assertEquals(4, initialUi.tableCards.size)
        assertEquals(3, initialUi.opponentCardCount)
        assertEquals(30, initialUi.deckCardCount)

        // Reset con match deterministico
        viewModel.onIntent(
            GameIntent.StartMatch(
                config = ScopaConfig(),
                level = DifficultyLevel.PRINCIPIANTE,
                seed = 42L,
                humanSeat = Seat.P1,
            ),
        )

        val updated = viewModel.uiState.value
        assertNotNull(updated)
        assertEquals(Seat.P1, updated.humanSeat)
    }

    @Test
    fun viewModelHandlesDismissError() {
        val viewModel = GameViewModel(ioDispatcher = Dispatchers.Unconfined)
        viewModel.onIntent(GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1))

        // Giochiamo una carta inesistente in mano per generare un errore
        val invalidCard = viewModel.uiState.value.tableCards.first()
        viewModel.onIntent(GameIntent.PlayCard(invalidCard))

        val errorState = viewModel.uiState.value
        assertNotNull(errorState.errorMessage)

        viewModel.onIntent(GameIntent.DismissError)
        val cleared = viewModel.uiState.value
        assertEquals(null, cleared.errorMessage)
    }
}
