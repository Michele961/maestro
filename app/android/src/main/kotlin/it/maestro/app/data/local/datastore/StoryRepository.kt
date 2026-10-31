package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import it.maestro.app.domain.story.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Le scene viste su DataStore Preferences. La chiave è nuova e senza dati equivale all'insieme vuoto: nessuna
 * migrazione, nessun dato esistente toccato.
 */
public class DataStoreStoryRepository(private val dataStore: DataStore<Preferences>) : StoryRepository {
    private val seenKey = stringSetPreferencesKey("story_seen_scenes")

    override fun observeSeen(): Flow<Set<String>> = dataStore.data.map { it[seenKey].orEmpty() }

    override suspend fun markSeen(sceneId: String) {
        dataStore.edit { it[seenKey] = it[seenKey].orEmpty() + sceneId }
    }
}

/** Versione in memoria, per i test e le anteprime. */
public class InMemoryStoryRepository(initial: Set<String> = emptySet()) : StoryRepository {
    private val seen = MutableStateFlow(initial)

    override fun observeSeen(): Flow<Set<String>> = seen

    override suspend fun markSeen(sceneId: String) {
        seen.value = seen.value + sceneId
    }
}
