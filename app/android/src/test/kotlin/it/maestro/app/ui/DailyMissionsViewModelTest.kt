package it.maestro.app.ui

import it.maestro.app.data.local.datastore.InMemoryRetentionRepository
import it.maestro.app.data.local.missions.StreakState
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.data.local.wallet.TransactionReason
import it.maestro.app.ui.screens.DailyMissionsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DailyMissionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testDate = LocalDate.of(2026, 10, 26)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_emitsStreakAndMissions() = runTest(testDispatcher) {
        val repo = InMemoryRetentionRepository(
            initialStreak = StreakState(
                currentStreak = 4,
                bestStreak = 4,
                lastPlayedDate = testDate.minusDays(1).toString(),
                freezeTokens = 1,
            ),
        )
        val viewModel = DailyMissionsViewModel(
            retentionRepository = repo,
            dateProvider = { testDate },
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first { !it.isLoading }
        assertFalse(state.isLoading)
        assertEquals(4, state.streak.currentStreak)
        assertEquals(1, state.streak.freezeTokens)
        assertEquals(3, state.missionsState.missions.size)
        assertEquals(testDate.toString(), state.missionsState.date)
    }

    @Test
    fun recordActivity_advancesStreak() = runTest(testDispatcher) {
        val repo = InMemoryRetentionRepository(
            initialStreak = StreakState(
                currentStreak = 4,
                bestStreak = 4,
                lastPlayedDate = testDate.minusDays(1).toString(),
                freezeTokens = 1,
            ),
        )
        val viewModel = DailyMissionsViewModel(
            retentionRepository = repo,
            dateProvider = { testDate },
        )

        viewModel.recordActivity()
        testDispatcher.scheduler.advanceUntilIdle()

        val streakAfter = repo.getStreak(testDate)
        assertEquals(5, streakAfter.currentStreak)
        assertEquals(5, streakAfter.bestStreak)
    }

    @Test
    fun completedMissions_canBeClaimedAndCreditedToWallet() = runTest(testDispatcher) {
        val repo = InMemoryRetentionRepository()
        val walletRepo = InMemoryWalletRepository()
        val viewModel = DailyMissionsViewModel(
            retentionRepository = repo,
            walletRepository = walletRepo,
            dateProvider = { testDate },
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, walletRepo.getBalance())

        // Avanza una missione a compimento
        val initialMissions = repo.getDailyMissions(testDate)
        val firstMission = initialMissions.missions.first()
        repo.updateMissionProgress(testDate, firstMission.type, firstMission.target)
        testDispatcher.scheduler.advanceUntilIdle()

        // Prima del riscatto esplicito, il saldo è ancora 0
        assertEquals(0, walletRepo.getBalance())

        // Riscatta esplicitamente
        val completedMission = repo.getDailyMissions(testDate).missions.first()
        viewModel.claimReward(completedMission)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(firstMission.type.rewardCredits, walletRepo.getBalance())
        org.junit.Assert.assertTrue(walletRepo.hasClaimed(TransactionReason.DAILY_MISSION_REWARD, firstMission.id))
    }
}
