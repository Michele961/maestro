package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.maestro.app.domain.daily.DailyMatchHistory
import it.maestro.app.domain.daily.DailyMatchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** Cronologia locale della partita del giorno (task F1G-15). */
public interface DailyMatchRepository {
    /** Risultati ricordati, il più recente per primo. */
    public val history: Flow<List<DailyMatchResult>>

    /** Registra [result]: il primo risultato di una data resta (vedi [DailyMatchHistory.add]). */
    public suspend fun record(result: DailyMatchResult)
}

/** Implementazione su DataStore Preferences. */
public class DataStoreDailyMatchRepository(private val dataStore: DataStore<Preferences>) : DailyMatchRepository {
    private val key = stringPreferencesKey("daily_match_history")

    override val history: Flow<List<DailyMatchResult>> =
        dataStore.data.map { DailyMatchHistory.decode(it[key].orEmpty()) }

    override suspend fun record(result: DailyMatchResult) {
        dataStore.edit { prefs ->
            prefs[key] = DailyMatchHistory.encode(DailyMatchHistory.add(DailyMatchHistory.decode(prefs[key].orEmpty()), result))
        }
    }
}

/** Implementazione in memoria per test e screenshot. */
public class InMemoryDailyMatchRepository(initial: List<DailyMatchResult> = emptyList()) : DailyMatchRepository {
    private val state = MutableStateFlow(initial)

    override val history: Flow<List<DailyMatchResult>> = state.asStateFlow()

    override suspend fun record(result: DailyMatchResult) {
        state.value = DailyMatchHistory.add(state.value, result)
    }
}
