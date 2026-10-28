package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import it.maestro.app.domain.opponents.LadderVictory
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.opponents.OpponentProgress
import it.maestro.app.domain.opponents.OpponentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** Avanzamento sulla scala su DataStore Preferences: gli avversari battuti, per nome (ADR-0012). */
public class DataStoreOpponentRepository(
    private val dataStore: DataStore<Preferences>,
) : OpponentRepository {

    private val defeatedKey = stringSetPreferencesKey("opponents_defeated")

    override fun observeProgress(): Flow<OpponentProgress> =
        dataStore.data.map { preferences -> toProgress(preferences[defeatedKey].orEmpty()) }

    override suspend fun recordVictory(opponent: Opponent): LadderVictory {
        lateinit var result: LadderVictory
        dataStore.edit { preferences ->
            val (updated, victory) = toProgress(preferences[defeatedKey].orEmpty()).afterVictory(opponent)
            preferences[defeatedKey] = updated.defeated.map { it.name }.toSet()
            result = victory
        }
        return result
    }

    // I nomi sconosciuti (di una versione futura) si ignorano invece di far fallire la lettura.
    private fun toProgress(names: Set<String>): OpponentProgress =
        OpponentProgress(Opponent.entries.filter { it.name in names }.toSet())
}

/** Versione in memoria, per i test e le anteprime. */
public class InMemoryOpponentRepository(initial: OpponentProgress = OpponentProgress()) : OpponentRepository {
    private val progress = MutableStateFlow(initial)

    override fun observeProgress(): Flow<OpponentProgress> = progress.asStateFlow()

    override suspend fun recordVictory(opponent: Opponent): LadderVictory {
        val (updated, victory) = progress.value.afterVictory(opponent)
        progress.value = updated
        return victory
    }
}
