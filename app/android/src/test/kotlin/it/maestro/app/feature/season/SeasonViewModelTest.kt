package it.maestro.app.feature.season

import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.data.local.datastore.InMemoryRetentionRepository
import it.maestro.app.data.local.datastore.InMemorySeasonRepository
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.rewards.RewardEngine
import it.maestro.app.domain.season.SeasonFacts
import it.maestro.app.domain.time.FakeClock
import it.maestro.app.feature.season.presentation.SeasonViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class SeasonViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val wallet = InMemoryWalletRepository()
    private val repository = InMemorySeasonRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(): SeasonViewModel {
        val engine = RewardEngine(
            wallet, InMemoryRetentionRepository(), InMemoryMasteryRepository(), FakeClock(LocalDate.of(2026, 10, 31)),
            season = repository,
        )
        val vm = SeasonViewModel(repository, engine)
        backgroundScope.launch { vm.state.collect {} }
        return vm
    }

    @Test
    fun showsTheDerivedCurrentChapter() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(2, vm.state.value.current?.chapter?.number)
    }

    @Test
    fun aFinishedChapterPaysItsRewardOnceAndMovesOn() = runTest(dispatcher) {
        val before = wallet.observeBalance().first()
        val vm = viewModel()
        repository.setFacts(SeasonFacts(defeated = setOf(Opponent.GIGI)))
        advanceUntilIdle()
        assertEquals(3, vm.state.value.current?.chapter?.number)
        assertEquals(before + 20, wallet.observeBalance().first())
        // Una seconda schermata sullo stesso stato non paga di nuovo.
        viewModel()
        advanceUntilIdle()
        assertEquals(before + 20, wallet.observeBalance().first())
    }
}
