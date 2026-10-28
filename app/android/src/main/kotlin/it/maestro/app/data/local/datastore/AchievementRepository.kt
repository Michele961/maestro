package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import it.maestro.app.domain.progress.AchievementCounter
import it.maestro.app.domain.progress.AchievementId
import it.maestro.app.domain.progress.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** Obiettivi su DataStore Preferences: gli identificatori stabili ottenuti e i contatori (ADR-0012). */
public class DataStoreAchievementRepository(
    private val dataStore: DataStore<Preferences>,
) : AchievementRepository {

    private val unlockedKey = stringSetPreferencesKey("achievements_unlocked")

    private fun counterKey(counter: AchievementCounter) = intPreferencesKey("achievement_counter_${counter.name.lowercase()}")

    override fun observeUnlocked(): Flow<Set<AchievementId>> =
        dataStore.data.map { toIds(it[unlockedKey].orEmpty()) }

    override suspend fun unlock(ids: Set<AchievementId>): Set<AchievementId> {
        var added: Set<AchievementId> = emptySet()
        dataStore.edit { preferences ->
            val before = toIds(preferences[unlockedKey].orEmpty())
            added = ids - before
            // Gli identificatori sconosciuti (di una versione futura) restano nell'insieme salvato.
            preferences[unlockedKey] = preferences[unlockedKey].orEmpty() + added.map { it.id }
        }
        return added
    }

    override suspend fun addToCounter(counter: AchievementCounter, delta: Int): Int {
        var total = 0
        dataStore.edit { preferences ->
            total = (preferences[counterKey(counter)] ?: 0) + delta.coerceAtLeast(0)
            preferences[counterKey(counter)] = total
        }
        return total
    }

    private fun toIds(stored: Set<String>): Set<AchievementId> =
        AchievementId.entries.filter { it.id in stored }.toSet()
}

/** Versione in memoria, per i test e le anteprime. */
public class InMemoryAchievementRepository : AchievementRepository {
    private val unlocked = MutableStateFlow<Set<AchievementId>>(emptySet())
    private val counters = mutableMapOf<AchievementCounter, Int>()

    override fun observeUnlocked(): Flow<Set<AchievementId>> = unlocked.asStateFlow()

    override suspend fun unlock(ids: Set<AchievementId>): Set<AchievementId> {
        val added = ids - unlocked.value
        unlocked.value = unlocked.value + added
        return added
    }

    override suspend fun addToCounter(counter: AchievementCounter, delta: Int): Int {
        val total = (counters[counter] ?: 0) + delta.coerceAtLeast(0)
        counters[counter] = total
        return total
    }
}
