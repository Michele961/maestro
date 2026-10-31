package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import it.maestro.app.domain.home.HomeFeature
import it.maestro.app.domain.home.HomeFeaturesRepository
import it.maestro.app.domain.home.PresentedFeatures
import kotlinx.coroutines.flow.first

/**
 * «Funzioni presentate» su DataStore Preferences. Le chiavi sono nuove e senza dati equivalgono al default: nessuna
 * migrazione. I nomi sconosciuti (di una versione futura) si ignorano.
 */
public class DataStoreHomeFeaturesRepository(private val dataStore: DataStore<Preferences>) : HomeFeaturesRepository {
    private val initializedKey = booleanPreferencesKey("home_features_initialized")
    private val revealedKey = stringSetPreferencesKey("home_features_revealed")
    private val introducedKey = stringSetPreferencesKey("home_features_introduced")
    private val closingDayKey = stringPreferencesKey("home_features_closing_day")

    override suspend fun load(): PresentedFeatures {
        val prefs = dataStore.data.first()
        return PresentedFeatures(
            initialized = prefs[initializedKey] ?: false,
            revealed = decode(prefs[revealedKey]),
            introduced = decode(prefs[introducedKey]),
            closingDay = prefs[closingDayKey].orEmpty(),
        )
    }

    override suspend fun save(state: PresentedFeatures) {
        dataStore.edit { prefs ->
            prefs[initializedKey] = state.initialized
            prefs[revealedKey] = state.revealed.map { it.name }.toSet()
            prefs[introducedKey] = state.introduced.map { it.name }.toSet()
            prefs[closingDayKey] = state.closingDay
        }
    }

    private fun decode(names: Set<String>?): Set<HomeFeature> =
        HomeFeature.entries.filter { it.name in names.orEmpty() }.toSet()
}

/** Versione in memoria, per i test e le anteprime. */
public class InMemoryHomeFeaturesRepository(initial: PresentedFeatures = PresentedFeatures()) : HomeFeaturesRepository {
    private var state = initial

    override suspend fun load(): PresentedFeatures = state

    override suspend fun save(state: PresentedFeatures) {
        this.state = state
    }
}
