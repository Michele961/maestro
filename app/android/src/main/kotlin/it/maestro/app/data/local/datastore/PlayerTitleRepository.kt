package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.maestro.app.domain.profile.PlayerTitleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** Titolo scelto dal giocatore su DataStore Preferences (ADR-0012). */
public class DataStorePlayerTitleRepository(
    private val dataStore: DataStore<Preferences>,
) : PlayerTitleRepository {

    private val selectedKey = stringPreferencesKey("player_title_selected")

    override fun observeSelected(): Flow<String?> = dataStore.data.map { it[selectedKey] }

    override suspend fun select(titleId: String) {
        dataStore.edit { it[selectedKey] = titleId }
    }
}

/** Versione in memoria, per i test e le anteprime. */
public class InMemoryPlayerTitleRepository(initial: String? = null) : PlayerTitleRepository {
    private val selected = MutableStateFlow(initial)

    override fun observeSelected(): Flow<String?> = selected.asStateFlow()

    override suspend fun select(titleId: String) {
        selected.value = titleId
    }
}
