package it.maestro.app.shell

import androidx.lifecycle.SavedStateHandle
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.data.repository.InMemoryMatchRepository
import it.maestro.app.designsystem.audio.NoOpSoundManager
import it.maestro.app.domain.rewards.RewardEngine
import it.maestro.app.feature.table.presentation.TestMatchGenerator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.Dispatchers
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Riprendi (F1R-04): la partita ripresa ha l'avversario con cui era iniziata, non uno di default. */
@OptIn(ExperimentalCoroutinesApi::class)
class ShellViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun shell(repository: InMemoryMatchRepository) = ShellViewModel(
        matchRepository = repository,
        rewardEngine = RewardEngine(
            InMemoryWalletRepository(),
            it.maestro.app.data.local.datastore.InMemoryRetentionRepository(),
            InMemoryMasteryRepository(),
            it.maestro.app.domain.time.FakeClock(java.time.LocalDate.of(2026, 10, 29)),
        ),
        soundManager = NoOpSoundManager(),
        clock = it.maestro.app.domain.time.FakeClock(java.time.LocalDate.of(2026, 10, 29)),
        savedStateHandle = SavedStateHandle(),
        analysisDispatcher = StandardTestDispatcher(),
    )

    @Test
    fun theSavedMatchKeepsItsOpponentLevelOnResume() = runTest {
        val repository = InMemoryMatchRepository()
        val log = TestMatchGenerator.generateEndgameMatch().log
        repository.saveActiveMatch(log = log, level = DifficultyLevel.PRINCIPIANTE, targetScore = log.config.targetScore)
        assertEquals(DifficultyLevel.PRINCIPIANTE, shell(repository).savedMatchLevel())
    }

    @Test
    fun withoutASavedMatchThereIsNoLevel() = runTest {
        assertNull(shell(InMemoryMatchRepository()).savedMatchLevel())
    }
}
