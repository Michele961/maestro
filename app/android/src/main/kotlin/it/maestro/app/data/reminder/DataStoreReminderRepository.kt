package it.maestro.app.data.reminder

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import it.maestro.app.domain.reminder.ReminderRepository
import it.maestro.app.domain.reminder.ReminderState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Stato del promemoria su DataStore Preferences. Le chiavi sono nuove e senza dati equivalgono al default: nessuna
 * migrazione. Una data illeggibile vale «mai notificato».
 */
public class DataStoreReminderRepository(private val dataStore: DataStore<Preferences>) : ReminderRepository {
    private val enabledKey = booleanPreferencesKey("reminder_enabled")
    private val askedKey = booleanPreferencesKey("reminder_consent_asked")
    private val hourKey = intPreferencesKey("reminder_hour")
    private val lastNotifiedKey = stringPreferencesKey("reminder_last_notified")

    override fun observe(): Flow<ReminderState> = dataStore.data.map(::decode)

    override suspend fun load(): ReminderState = decode(dataStore.data.first())

    override suspend fun save(state: ReminderState) {
        dataStore.edit { prefs ->
            prefs[enabledKey] = state.enabled
            prefs[askedKey] = state.consentAsked
            prefs[hourKey] = state.hourOfDay
            prefs[lastNotifiedKey] = state.lastNotified?.toString().orEmpty()
        }
    }

    private fun decode(prefs: Preferences): ReminderState = ReminderState(
        enabled = prefs[enabledKey] ?: false,
        consentAsked = prefs[askedKey] ?: false,
        hourOfDay = (prefs[hourKey] ?: ReminderState.DEFAULT_HOUR).coerceIn(0, 23),
        lastNotified = prefs[lastNotifiedKey]?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
    )
}
