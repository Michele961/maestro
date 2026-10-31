package it.maestro.app.domain.reminder

import it.maestro.app.domain.time.FakeClock
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** F1G-13: il coordinatore applica le regole e tiene la pianificazione allineata allo stato. */
class ReminderCoordinatorTest {
    private val today = LocalDate.of(2026, 10, 31)
    private val clock = FakeClock(today, hour = 19)
    private val repository = InMemoryReminderRepository()
    private val scheduler = FakeReminderScheduler()
    private val notifier = FakeReminderNotifier()
    private var lastPlayed: LocalDate? = today.minusDays(3)
    private val coordinator = ReminderCoordinator(repository, scheduler, notifier, clock) { lastPlayed }

    @Test
    fun acceptingTheConsentTurnsOnAndSchedulesAndNeverAsksAgain() = runTest {
        clock.hour = 10
        coordinator.answerConsent(accepted = true)

        assertEquals(ReminderState(enabled = true, consentAsked = true), repository.load())
        assertEquals(ReminderSlot(today, 19), scheduler.scheduled)
        assertTrue(!ReminderPolicy.shouldAskConsent(repository.load(), hasWon = true))
    }

    @Test
    fun decliningTheConsentLeavesEverythingOffAndNeverAsksAgain() = runTest {
        coordinator.answerConsent(accepted = false)

        assertEquals(ReminderState(enabled = false, consentAsked = true), repository.load())
        assertNull(scheduler.scheduled)
        assertTrue(!ReminderPolicy.shouldAskConsent(repository.load(), hasWon = true))
        coordinator.onAlarm()
        assertEquals(0, notifier.shown)
    }

    @Test
    fun theSwitchTurnsTheScheduleOffAndOnAgain() = runTest {
        clock.hour = 10
        coordinator.setEnabled(true)
        assertEquals(ReminderSlot(today, 19), scheduler.scheduled)

        coordinator.setEnabled(false)
        assertNull(scheduler.scheduled)

        coordinator.setEnabled(true)
        assertEquals(ReminderSlot(today, 19), scheduler.scheduled)
    }

    @Test
    fun theAlarmNotifiesOnceAndSchedulesTomorrow() = runTest {
        repository.save(ReminderState(enabled = true, consentAsked = true))
        coordinator.onAlarm()

        assertEquals(1, notifier.shown)
        assertEquals(today, repository.load().lastNotified)
        assertEquals(ReminderSlot(today.plusDays(1), 19), scheduler.scheduled)
    }

    @Test
    fun aSecondAlarmTheSameDayDoesNotNotifyAgain() = runTest {
        repository.save(ReminderState(enabled = true, consentAsked = true))
        coordinator.onAlarm()
        coordinator.onAlarm()

        assertEquals(1, notifier.shown)
    }

    @Test
    fun theAlarmStaysSilentWhenThePlayerAlreadyPlayedToday() = runTest {
        repository.save(ReminderState(enabled = true, consentAsked = true))
        lastPlayed = today
        coordinator.onAlarm()

        assertEquals(0, notifier.shown)
        assertEquals(ReminderSlot(today.plusDays(1), 19), scheduler.scheduled)
    }

    @Test
    fun theAlarmStaysSilentWhenTheSystemPermissionIsMissing() = runTest {
        repository.save(ReminderState(enabled = true, consentAsked = true))
        notifier.allowed = false
        coordinator.onAlarm()

        assertEquals(0, notifier.shown)
        assertNull(repository.load().lastNotified)
    }

    @Test
    fun afterAPhoneRestartTheScheduleStartsAgainFromTheSavedState() = runTest {
        clock.hour = 8
        repository.save(ReminderState(enabled = true, consentAsked = true))
        coordinator.reschedule()

        assertEquals(ReminderSlot(today, 19), scheduler.scheduled)
    }

    @Test
    fun afterAPhoneRestartNothingIsScheduledWhenTheReminderIsOff() = runTest {
        coordinator.reschedule()

        assertNull(scheduler.scheduled)
    }
}
