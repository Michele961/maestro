package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.maestro.app.domain.guide.GuideProgress
import it.maestro.app.domain.guide.GuideRepository
import it.maestro.app.domain.guide.GuidedHandStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * La guida su DataStore Preferences. Le chiavi sono nuove e senza dati valgono il default: nessuna migrazione.
 * Un valore sconosciuto (di una versione futura) si legge come «non fatta» invece di far fallire la lettura.
 */
public class DataStoreGuideRepository(private val dataStore: DataStore<Preferences>) : GuideRepository {
    private val handKey = stringPreferencesKey("guide_hand")
    private val reviewedKey = booleanPreferencesKey("guide_reviewed")

    override fun observe(): Flow<GuideProgress> = dataStore.data.map { preferences ->
        GuideProgress(
            hand = GuidedHandStatus.entries.firstOrNull { it.name == preferences[handKey] } ?: GuidedHandStatus.NOT_DONE,
            reviewed = preferences[reviewedKey] == true,
        )
    }

    override suspend fun setHand(status: GuidedHandStatus) {
        dataStore.edit { it[handKey] = status.name }
    }

    override suspend fun markReviewed() {
        dataStore.edit { it[reviewedKey] = true }
    }
}

/** Versione in memoria, per i test e le anteprime. */
public class InMemoryGuideRepository(initial: GuideProgress = GuideProgress()) : GuideRepository {
    private val state = MutableStateFlow(initial)

    override fun observe(): Flow<GuideProgress> = state

    override suspend fun setHand(status: GuidedHandStatus) {
        state.value = state.value.copy(hand = status)
    }

    override suspend fun markReviewed() {
        state.value = state.value.copy(reviewed = true)
    }
}
