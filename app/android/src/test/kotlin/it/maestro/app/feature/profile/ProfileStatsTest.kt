package it.maestro.app.feature.profile

import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.data.local.datastore.InMemoryRetentionRepository
import it.maestro.app.domain.mastery.MatchHistoryEntry
import it.maestro.app.domain.time.FakeClock
import it.maestro.app.feature.profile.presentation.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** F1R-26e: il Profilo mostra la serie di giorni e le partite recenti con avversario e modalità. */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileStatsTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun theStreakComesFromTheRetentionRepository() = runTest(dispatcher) {
        val retention = InMemoryRetentionRepository()
        val today = java.time.LocalDate.of(2026, 10, 28)
        retention.recordGameActivity(today)
        val viewModel = ProfileViewModel(
            InMemoryMasteryRepository(),
            retentionRepository = retention,
            clock = FakeClock(today),
        )
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.first { !it.isLoading }.streak.currentStreak)
    }

    @Test
    fun historyEntriesKeepOpponentAndObjectiveForTheCards() = runTest(dispatcher) {
        val entry = MatchHistoryEntry("m", 0L, 80, 1000, 5, 11, 4, true, opponent = "TONINO", sparringObjective = "DENARI_COUNT")
        val viewModel = ProfileViewModel(InMemoryMasteryRepository(initialHistory = listOf(entry)))
        dispatcher.scheduler.advanceUntilIdle()

        val shown = viewModel.uiState.first { !it.isLoading }.matchHistory.single()
        assertEquals("TONINO", shown.opponent)
        assertEquals("DENARI_COUNT", shown.sparringObjective)
    }
}
