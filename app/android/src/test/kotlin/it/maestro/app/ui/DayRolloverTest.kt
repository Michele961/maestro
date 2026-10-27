package it.maestro.app.ui

import it.maestro.app.data.local.datastore.InMemoryDailyPuzzleRepository
import it.maestro.app.data.local.datastore.InMemoryRetentionRepository
import it.maestro.app.domain.time.FakeClock
import it.maestro.app.ui.screens.DailyMissionsViewModel
import it.maestro.app.ui.screens.DailyPuzzleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import it.maestro.app.data.local.datastore.RetentionRepository
import it.maestro.app.data.local.missions.DailyMissionsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.time.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/** Task F1R-02: con l'app aperta a mezzanotte, puzzle e missioni passano al giorno nuovo. */
@OptIn(ExperimentalCoroutinesApi::class)
class DayRolloverTest {

    private val testDispatcher = StandardTestDispatcher()
    private val day1 = LocalDate.of(2026, 10, 26)
    private val day2 = day1.plusDays(1)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun dailyPuzzleMovesToTheNewDayAfterMidnight() = runTest {
        val clock = FakeClock(day1)
        val viewModel = DailyPuzzleViewModel(repository = InMemoryDailyPuzzleRepository(), clock = clock)
        advanceUntilIdle()
        val before = viewModel.uiState.value

        clock.date = day2
        viewModel.refreshDay()
        advanceUntilIdle()
        val after = viewModel.uiState.value

        assertEquals(day1.toString(), before.dateString)
        assertEquals(day2.toString(), after.dateString)
        assertEquals(before.puzzleNumber + 1, after.puzzleNumber)
        assertNotEquals(before.puzzle, after.puzzle)
    }

    @Test
    fun dailyPuzzleStaysOnTheSameDayWithoutMidnight() = runTest {
        val clock = FakeClock(day1)
        val viewModel = DailyPuzzleViewModel(repository = InMemoryDailyPuzzleRepository(), clock = clock)
        advanceUntilIdle()
        val before = viewModel.uiState.value

        viewModel.refreshDay()
        advanceUntilIdle()

        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun missionsMoveToTheNewDayAfterMidnight() = runTest {
        val clock = FakeClock(day1)
        val repo = RecordingRetentionRepository(InMemoryRetentionRepository())
        val viewModel = DailyMissionsViewModel(retentionRepository = repo, clock = clock)
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        clock.date = day2
        viewModel.refreshDay()
        advanceUntilIdle()

        assertEquals(listOf(day1, day2), repo.observedMissionDays)
    }

    /** Registra per quali giorni la UI osserva le missioni. */
    private class RecordingRetentionRepository(
        private val inner: InMemoryRetentionRepository,
    ) : RetentionRepository by inner {
        val observedMissionDays = mutableListOf<LocalDate>()

        override fun observeDailyMissions(today: LocalDate): Flow<DailyMissionsState> {
            observedMissionDays += today
            return inner.observeDailyMissions(today)
        }
    }
}
