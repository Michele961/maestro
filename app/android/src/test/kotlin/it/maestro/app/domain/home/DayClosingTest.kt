package it.maestro.app.domain.home

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** F1G-12: la condizione della chiusura del giorno. */
class DayClosingTest {
    private val today = LocalDate.of(2026, 10, 31)
    private val allDone = DayClosingSignals(puzzleSolvedToday = true, dailyMatchPlayedToday = true, missionsCompleted = true)
    private val shown = setOf(HomeFeature.DAILY_PUZZLE, HomeFeature.DAILY_MATCH, HomeFeature.MISSIONS)

    @Test
    fun theDayClosesOnlyWhenPuzzleMatchAndMissionsAreAllDone() {
        assertTrue(DayClosing.isComplete(allDone, shown))
        assertFalse(DayClosing.isComplete(allDone.copy(puzzleSolvedToday = false), shown))
        assertFalse(DayClosing.isComplete(allDone.copy(dailyMatchPlayedToday = false), shown))
        assertFalse(DayClosing.isComplete(allDone.copy(missionsCompleted = false), shown))
        assertFalse(DayClosing.isComplete(DayClosingSignals(), shown))
    }

    @Test
    fun aFunctionNotYetShownInHomeCannotCloseTheDay() {
        HomeFeature.entries.filter { it in shown }.forEach { missing ->
            assertFalse(DayClosing.isComplete(allDone, shown - missing), "senza $missing")
        }
        assertTrue(DayClosing.isComplete(allDone, HomeFeature.ALL))
    }

    @Test
    fun theClosingIsDueOncePerDay() {
        assertTrue(DayClosing.isDue(allDone, shown, shownOn = "", today = today))
        assertTrue(DayClosing.isDue(allDone, shown, shownOn = today.minusDays(1).toString(), today = today))
        assertFalse(DayClosing.isDue(allDone, shown, shownOn = today.toString(), today = today))
    }

    @Test
    fun theClosingIsNotDueWhileTheDayIsIncomplete() {
        assertFalse(DayClosing.isDue(allDone.copy(missionsCompleted = false), shown, shownOn = "", today = today))
    }

    @Test
    fun theSavedClosingDayDefaultsToNeverAndRemembersTheDay() {
        assertEquals("", PresentedFeatures().closingDay)
        assertEquals("2026-10-31", PresentedFeatures().withClosingShown("2026-10-31").closingDay)
    }
}
