package it.maestro.app.game

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.legalMoves
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameViewModelTest {

    @Test
    fun viewModelEmitsInitialUiStateAndHandlesCardPlay() {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = GameViewModel(
            ioDispatcher = Dispatchers.Unconfined,
            coroutineScope = testScope,
        )
        val initialUi = viewModel.uiState.value

        assertEquals(DealPhase.PLAYING, initialUi.phase)
        assertEquals(3, initialUi.humanHand.size)
        assertTrue(initialUi.tableCards.isNotEmpty())
        assertEquals(30, initialUi.deckCardCount)
        assertTrue(initialUi.isHumanTurn)

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
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = GameViewModel(
            ioDispatcher = Dispatchers.Unconfined,
            coroutineScope = testScope,
        )
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
