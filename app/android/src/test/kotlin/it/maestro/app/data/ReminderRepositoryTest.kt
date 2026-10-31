package it.maestro.app.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.maestro.app.data.reminder.DataStoreReminderRepository
import it.maestro.app.domain.reminder.ReminderState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.LocalDate

/** F1G-13: lo stato del promemoria ha un default sicuro e sopravvive alla chiusura del processo. */
class ReminderRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun open(scope: CoroutineScope) =
        PreferenceDataStoreFactory.create(scope = scope) { java.io.File(folder.root, "reminder.preferences_pb") }

    @Test
    fun withoutDataTheReminderIsOffAndNeverAsked() = runTest {
        val scope = CoroutineScope(Job() + Dispatchers.Unconfined)
        assertEquals(ReminderState(), DataStoreReminderRepository(open(scope)).load())
        scope.cancel()
    }

    @Test
    fun theStateSurvivesARestartOfTheProcess() = runTest {
        val saved = ReminderState(enabled = true, consentAsked = true, hourOfDay = 20, lastNotified = LocalDate.of(2026, 10, 30))
        val firstRun = CoroutineScope(Job() + Dispatchers.Unconfined)
        DataStoreReminderRepository(open(firstRun)).save(saved)
        firstRun.cancel()

        val secondRun = CoroutineScope(Job() + Dispatchers.Unconfined)
        assertEquals(saved, DataStoreReminderRepository(open(secondRun)).load())
        secondRun.cancel()
    }

    @Test
    fun anUnreadableDateMeansNeverNotified() = runTest {
        val scope = CoroutineScope(Job() + Dispatchers.Unconfined)
        val store = open(scope)
        store.edit { it[stringPreferencesKey("reminder_last_notified")] = "ieri" }
        assertEquals(null, DataStoreReminderRepository(store).load().lastNotified)
        scope.cancel()
    }
}
