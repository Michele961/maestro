package it.maestro.app.feature.table.presentation

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.domain.coach.Chattiness
import it.maestro.app.domain.coach.MomentKind
import it.maestro.game.model.Seat
import it.maestro.game.scopa.ScopaConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Task F1R-24d: la battuta del Maestro arriva dal ViewModel, si cancella quando si gioca e si può spegnere. */
class MaestroLineViewModelTest {

    private fun viewModel(chattiness: Chattiness = Chattiness.NORMAL, hintUsed: Boolean = false, onHintUsed: suspend () -> Unit = {}) =
        GameViewModel(
            ioDispatcher = Dispatchers.Unconfined,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
            chattiness = flowOf(chattiness),
            hintEverUsed = flowOf(hintUsed),
            onHintUsed = onHintUsed,
        )

    private fun start(vm: GameViewModel, seed: Long = 42L) =
        vm.onIntent(GameIntent.StartMatch(config = ScopaConfig(), level = DifficultyLevel.PRINCIPIANTE, seed = seed, humanSeat = Seat.P1))

    @Test
    fun greetsWhenTheMatchStarts() {
        val vm = viewModel()
        start(vm)
        assertEquals(MomentKind.GREETING, vm.uiState.value.maestroLine?.kind)
    }

    @Test
    fun staysSilentWhenSetToNever() {
        val vm = viewModel(Chattiness.OFF)
        start(vm)
        assertNull(vm.uiState.value.maestroLine)
    }

    @Test
    fun playingACardClearsTheLine() {
        val vm = viewModel(Chattiness.TALKATIVE)
        start(vm)
        assertNotNull(vm.uiState.value.maestroLine)
        vm.onIntent(GameIntent.PlayCard(vm.uiState.value.humanHand.first()))
        // La nuova mossa cancella la battuta di prima; il Maestro può averne una nuova solo se c'è un motivo.
        assertTrue(vm.uiState.value.maestroLine?.kind != MomentKind.GREETING)
    }

    @Test
    fun tappingTheBubbleDismissesTheLine() {
        val vm = viewModel()
        start(vm)
        vm.onIntent(GameIntent.DismissMaestroLine)
        assertNull(vm.uiState.value.maestroLine)
    }

    @Test
    fun theFirstHintRequestIsRememberedOnce() {
        var marked = 0
        val vm = viewModel(onHintUsed = { marked++ })
        start(vm)
        vm.onIntent(GameIntent.RequestHint)
        vm.onIntent(GameIntent.RequestHint)
        assertEquals(1, marked)
    }
}
