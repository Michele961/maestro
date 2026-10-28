package it.maestro.app.data

import it.maestro.app.data.local.datastore.DailyPuzzleRecord
import it.maestro.app.data.local.datastore.InMemoryDailyPuzzleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import it.maestro.app.data.local.datastore.liveChallengeStreak
import it.maestro.app.data.local.datastore.nextChallengeStreak
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test unitari per [DailyPuzzleRepository] e la persistenza dello stato della Sfida del Giorno (task F2-03).
 */
class DailyPuzzleRepositoryTest {

    @Test
    fun initialState_defaultsToUnsolvedAndZeroAttempts() = runTest {
        val repo = InMemoryDailyPuzzleRepository()
        val record = repo.getDailyPuzzleRecord("2026-10-26")

        assertFalse(record.isSolvedToday)
        assertEquals("", record.lastCompletedDate)
        assertEquals(0, record.attemptsToday)
        assertEquals(0, record.streak)
    }

    @Test
    fun recordWrongAttempt_updatesAttemptsCountWithoutMarkingSolved() = runTest {
        val repo = InMemoryDailyPuzzleRepository()
        val today = "2026-10-26"

        repo.recordPuzzleCompletion(todayDate = today, attempts = 1, solved = false)
        val record = repo.getDailyPuzzleRecord(today)

        assertFalse(record.isSolvedToday)
        assertEquals(1, record.attemptsToday)
        assertEquals(0, record.streak)
    }

    @Test
    fun recordSolvedAttempt_marksSolvedAndIncrementsStreak() = runTest {
        val repo = InMemoryDailyPuzzleRepository()
        val today = "2026-10-26"

        repo.recordPuzzleCompletion(todayDate = today, attempts = 2, solved = true)
        val record = repo.getDailyPuzzleRecord(today)

        assertTrue(record.isSolvedToday)
        assertEquals(today, record.lastCompletedDate)
        assertEquals(2, record.attemptsToday)
        assertEquals(1, record.streak)

        // Un ulteriore solve nello stesso giorno non deve incrementare doppiamente lo streak
        repo.recordPuzzleCompletion(todayDate = today, attempts = 2, solved = true)
        val updated = repo.getDailyPuzzleRecord(today)
        assertEquals(1, updated.streak)
    }

    @Test
    fun newDay_preservesStreakAndResetsSolvedStatus() = runTest {
        val repo = InMemoryDailyPuzzleRepository(
            initialRecord = DailyPuzzleRecord(
                lastCompletedDate = "2026-10-25",
                isSolvedToday = true,
                attemptsToday = 1,
                streak = 3,
            ),
        )
        val newDay = "2026-10-26"

        // Nel nuovo giorno, l'osservazione rileva che today non è ancora completato
        val flowRecord = repo.observeDailyPuzzle(newDay).first()
        assertEquals(3, flowRecord.streak)
        assertEquals("2026-10-25", flowRecord.lastCompletedDate)
    }

    @Test
    fun challengeStreakContinuesOnConsecutiveDaysAndRestartsAfterAGap() {
        assertEquals(1, nextChallengeStreak("", 0, "2026-10-26"))
        assertEquals(4, nextChallengeStreak("2026-10-25", 3, "2026-10-26"))
        assertEquals(1, nextChallengeStreak("2026-10-23", 3, "2026-10-26"), "Un giorno saltato azzera la serie")
    }

    @Test
    fun challengeStreakShownIsZeroOnceADayIsMissed() {
        assertEquals(3, liveChallengeStreak("2026-10-26", 3, "2026-10-26"))
        assertEquals(3, liveChallengeStreak("2026-10-25", 3, "2026-10-26"))
        assertEquals(0, liveChallengeStreak("2026-10-24", 3, "2026-10-26"))
    }
}
