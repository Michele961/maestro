package it.maestro.app.domain.reminder

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** F1G-13: quando notificare, quando chiedere, quando pianificare. Logica pura. */
class ReminderPolicyTest {
    private val today = LocalDate.of(2026, 10, 31)
    private val on = ReminderState(enabled = true, consentAsked = true)

    private fun notify(
        state: ReminderState = on,
        hour: Int = 19,
        lastPlayed: LocalDate? = today.minusDays(2),
        canNotify: Boolean = true,
    ) = ReminderPolicy.shouldNotify(state, today, hour, lastPlayed, canNotify)

    @Test
    fun consentIsNeverAskedBeforeTheFirstWin() {
        assertFalse(ReminderPolicy.shouldAskConsent(ReminderState(), hasWon = false))
        assertTrue(ReminderPolicy.shouldAskConsent(ReminderState(), hasWon = true))
    }

    @Test
    fun consentIsAskedOnlyOnceWhateverTheAnswer() {
        assertFalse(ReminderPolicy.shouldAskConsent(ReminderState(consentAsked = true, enabled = false), hasWon = true))
        assertFalse(ReminderPolicy.shouldAskConsent(ReminderState(consentAsked = true, enabled = true), hasWon = true))
    }

    @Test
    fun notifiesAtTheChosenHourWhenHasNotPlayedToday() {
        assertTrue(notify())
        assertTrue(notify(lastPlayed = null))
    }

    @Test
    fun neverNotifiesWhenAlreadyPlayedToday() {
        assertFalse(notify(lastPlayed = today))
    }

    @Test
    fun neverNotifiesTwiceTheSameDay() {
        assertFalse(notify(state = on.copy(lastNotified = today)))
        assertTrue(notify(state = on.copy(lastNotified = today.minusDays(1))))
    }

    @Test
    fun neverNotifiesWhenOffOrWhenTheSystemForbidsIt() {
        assertFalse(notify(state = on.copy(enabled = false)))
        assertFalse(notify(canNotify = false))
    }

    @Test
    fun onlyNotifiesInsideTheWindowAfterTheChosenHour() {
        assertFalse(notify(hour = 18))
        assertTrue(notify(hour = 21))
        assertFalse(notify(hour = 22))
        assertTrue(notify(state = on.copy(hourOfDay = 8), hour = 9))
    }

    @Test
    fun nextSlotIsTodayWhenTheHourHasNotComeYet() {
        assertEquals(ReminderSlot(today, 19), ReminderPolicy.nextSlot(on, today, localHour = 10))
    }

    @Test
    fun nextSlotIsTomorrowOnceTheHourHasPassedSoNothingFiresImmediately() {
        assertEquals(ReminderSlot(today.plusDays(1), 19), ReminderPolicy.nextSlot(on, today, localHour = 19))
        assertEquals(ReminderSlot(today.plusDays(1), 19), ReminderPolicy.nextSlot(on, today, localHour = 23))
    }

    @Test
    fun nextSlotIsTomorrowWhenTodayWasAlreadyNotified() {
        val notified = on.copy(lastNotified = today)
        assertEquals(ReminderSlot(today.plusDays(1), 19), ReminderPolicy.nextSlot(notified, today, localHour = 7))
    }

    @Test
    fun nextSlotUsesTheChosenHour() {
        assertEquals(ReminderSlot(today, 8), ReminderPolicy.nextSlot(on.copy(hourOfDay = 8), today, localHour = 6))
    }
}
