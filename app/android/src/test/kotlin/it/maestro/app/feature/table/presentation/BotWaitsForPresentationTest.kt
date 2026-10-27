package it.maestro.app.feature.table.presentation

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.domain.match.PresentationGate
import it.maestro.game.model.Seat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Task F1R-08 (ADR-0015): il bot non muove finché il tavolo non ha finito di mostrare la scena. */
@OptIn(ExperimentalCoroutinesApi::class)
class BotWaitsForPresentationTest {

    private class ManualGate : PresentationGate {
        val opened = CompletableDeferred<Unit>()
        var requests = 0
        override suspend fun awaitPresented(moveId: Long, dealId: Long) {
            requests++
            opened.await()
        }
    }

    /** Un seed in cui apre il bot (l'umano è P1). */
    private fun seedWhereBotStarts(): Long = (1L..200L).first { seed ->
        val result = GameReducer.initial(GameIntent.StartMatch(seed = seed, humanSeat = Seat.P1))
        result.effect is GameEffect.ScheduleBotMove
    }

    @Test
    fun botWaitsForTheGateThenMoves() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val gate = ManualGate()
        val viewModel = GameViewModel(
            ioDispatcher = dispatcher,
            coroutineScope = TestScope(dispatcher),
            presentationGate = gate,
        )
        viewModel.onIntent(
            GameIntent.StartMatch(seed = seedWhereBotStarts(), level = DifficultyLevel.PRINCIPIANTE, humanSeat = Seat.P1),
        )

        advanceUntilIdle()
        assertTrue(gate.requests >= 1, "Il bot deve chiedere al cancello")
        assertEquals(0, viewModel.currentMatchLog.moves.size, "Senza scena finita il bot non muove")

        gate.opened.complete(Unit)
        advanceUntilIdle()
        assertEquals(1, viewModel.currentMatchLog.moves.size, "A scena finita il bot muove")
    }

    @Test
    fun withTheDirectorAsGateTheBotMovesOnlyAfterTheHumanSceneEnds() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = GameViewModel(ioDispatcher = dispatcher, coroutineScope = TestScope(dispatcher))
        // Seed in cui apre l'umano: dopo la sua mossa tocca al bot.
        val seed = (1L..200L).first { GameReducer.initial(GameIntent.StartMatch(seed = it, humanSeat = Seat.P1)).effect == null }
        viewModel.onIntent(GameIntent.StartMatch(seed = seed, level = DifficultyLevel.PRINCIPIANTE, humanSeat = Seat.P1))
        advanceUntilIdle()

        val card = viewModel.uiState.value.humanHand.first()
        viewModel.onIntent(GameIntent.PlayCard(card))
        // Il bot non muove finché la regia non ha finito di mettere in scena la mossa dell'umano.
        assertTrue(viewModel.currentScene.busy, "La regia sta mostrando la mossa")
        assertEquals(1, viewModel.currentMatchLog.moves.size)

        advanceUntilIdle()
        assertTrue(viewModel.currentMatchLog.moves.size >= 2, "A scena finita il bot muove")
    }
}
