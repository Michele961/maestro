package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import it.maestro.app.domain.progress.ExperienceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** Esperienza su DataStore Preferences: il totale dei punti (ADR-0012). */
public class DataStoreExperienceRepository(
    private val dataStore: DataStore<Preferences>,
) : ExperienceRepository {

    private val pointsKey = intPreferencesKey("experience_points")

    override fun observePoints(): Flow<Int> = dataStore.data.map { it[pointsKey] ?: 0 }

    override suspend fun add(points: Int): Int {
        var total = 0
        dataStore.edit { preferences ->
            total = (preferences[pointsKey] ?: 0) + points.coerceAtLeast(0)
            preferences[pointsKey] = total
        }
        return total
    }
}

/** Versione in memoria, per i test e le anteprime. */
public class InMemoryExperienceRepository(initial: Int = 0) : ExperienceRepository {
    private val points = MutableStateFlow(initial)

    override fun observePoints(): Flow<Int> = points.asStateFlow()

    override suspend fun add(points: Int): Int {
        this.points.value += points.coerceAtLeast(0)
        return this.points.value
    }
}
