package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import it.maestro.app.data.local.academy.AcademyModuleId
import it.maestro.app.data.local.academy.AcademyProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Repository per la progressione a tappe e il salvataggio dei moduli dell'Accademia (task F2-06).
 */
public interface AcademyRepository {
    public fun observeProgress(): Flow<Map<AcademyModuleId, AcademyProgress>>
    public suspend fun getProgress(): Map<AcademyModuleId, AcademyProgress>
    public suspend fun completeModule(moduleId: AcademyModuleId)
    public suspend fun resetProgress()
}

/**
 * Implementazione persistente su Android Jetpack DataStore Preferences (ADR-0012).
 */
public class DataStoreAcademyRepository(
    private val dataStore: DataStore<Preferences>,
) : AcademyRepository {

    private fun completedKey(id: AcademyModuleId) = booleanPreferencesKey("academy_completed_${id.name}")
    private fun timestampKey(id: AcademyModuleId) = longPreferencesKey("academy_completed_at_${id.name}")

    override fun observeProgress(): Flow<Map<AcademyModuleId, AcademyProgress>> =
        dataStore.data.map { preferences ->
            mapPreferencesToProgress(preferences)
        }

    override suspend fun getProgress(): Map<AcademyModuleId, AcademyProgress> {
        val preferences = dataStore.data.first()
        return mapPreferencesToProgress(preferences)
    }

    override suspend fun completeModule(moduleId: AcademyModuleId) {
        val now = System.currentTimeMillis()
        dataStore.edit { prefs ->
            prefs[completedKey(moduleId)] = true
            prefs[timestampKey(moduleId)] = now
        }
    }

    override suspend fun resetProgress() {
        dataStore.edit { prefs ->
            AcademyModuleId.entries.forEach { id ->
                prefs.remove(completedKey(id))
                prefs.remove(timestampKey(id))
            }
        }
    }

    private fun mapPreferencesToProgress(preferences: Preferences): Map<AcademyModuleId, AcademyProgress> {
        val mod1Completed = preferences[completedKey(AcademyModuleId.MODULE_1_ABBECEDARIO)] ?: false
        val mod1Time = preferences[timestampKey(AcademyModuleId.MODULE_1_ABBECEDARIO)] ?: 0L

        val mod2Completed = preferences[completedKey(AcademyModuleId.MODULE_2_PRIMIERA)] ?: false
        val mod2Time = preferences[timestampKey(AcademyModuleId.MODULE_2_PRIMIERA)] ?: 0L

        val mod3Completed = preferences[completedKey(AcademyModuleId.MODULE_3_DISPARI)] ?: false
        val mod3Time = preferences[timestampKey(AcademyModuleId.MODULE_3_DISPARI)] ?: 0L

        return mapOf(
            AcademyModuleId.MODULE_1_ABBECEDARIO to AcademyProgress(
                moduleId = AcademyModuleId.MODULE_1_ABBECEDARIO,
                isUnlocked = true, // Il primo modulo è sempre sbloccato
                isCompleted = mod1Completed,
                completedAt = mod1Time,
            ),
            AcademyModuleId.MODULE_2_PRIMIERA to AcademyProgress(
                moduleId = AcademyModuleId.MODULE_2_PRIMIERA,
                isUnlocked = mod1Completed, // Sbloccato se il modulo 1 è completato
                isCompleted = mod2Completed,
                completedAt = mod2Time,
            ),
            AcademyModuleId.MODULE_3_DISPARI to AcademyProgress(
                moduleId = AcademyModuleId.MODULE_3_DISPARI,
                isUnlocked = mod2Completed, // Sbloccato se il modulo 2 è completato
                isCompleted = mod3Completed,
                completedAt = mod3Time,
            ),
        )
    }
}

/**
 * Implementazione in-memory per test unitari veloci e isolati.
 */
public class InMemoryAcademyRepository(
    initialCompleted: Set<AcademyModuleId> = emptySet(),
) : AcademyRepository {

    private val completedModules = MutableStateFlow(initialCompleted)
    private val completedTimes = mutableMapOf<AcademyModuleId, Long>()

    override fun observeProgress(): Flow<Map<AcademyModuleId, AcademyProgress>> =
        completedModules.asStateFlow().map { completed ->
            buildProgressMap(completed)
        }

    override suspend fun getProgress(): Map<AcademyModuleId, AcademyProgress> =
        buildProgressMap(completedModules.value)

    override suspend fun completeModule(moduleId: AcademyModuleId) {
        completedTimes[moduleId] = System.currentTimeMillis()
        completedModules.value = completedModules.value + moduleId
    }

    override suspend fun resetProgress() {
        completedTimes.clear()
        completedModules.value = emptySet()
    }

    private fun buildProgressMap(completed: Set<AcademyModuleId>): Map<AcademyModuleId, AcademyProgress> {
        val mod1Completed = completed.contains(AcademyModuleId.MODULE_1_ABBECEDARIO)
        val mod2Completed = completed.contains(AcademyModuleId.MODULE_2_PRIMIERA)
        val mod3Completed = completed.contains(AcademyModuleId.MODULE_3_DISPARI)

        return mapOf(
            AcademyModuleId.MODULE_1_ABBECEDARIO to AcademyProgress(
                moduleId = AcademyModuleId.MODULE_1_ABBECEDARIO,
                isUnlocked = true,
                isCompleted = mod1Completed,
                completedAt = completedTimes[AcademyModuleId.MODULE_1_ABBECEDARIO] ?: 0L,
            ),
            AcademyModuleId.MODULE_2_PRIMIERA to AcademyProgress(
                moduleId = AcademyModuleId.MODULE_2_PRIMIERA,
                isUnlocked = mod1Completed,
                isCompleted = mod2Completed,
                completedAt = completedTimes[AcademyModuleId.MODULE_2_PRIMIERA] ?: 0L,
            ),
            AcademyModuleId.MODULE_3_DISPARI to AcademyProgress(
                moduleId = AcademyModuleId.MODULE_3_DISPARI,
                isUnlocked = mod2Completed,
                isCompleted = mod3Completed,
                completedAt = completedTimes[AcademyModuleId.MODULE_3_DISPARI] ?: 0L,
            ),
        )
    }
}
