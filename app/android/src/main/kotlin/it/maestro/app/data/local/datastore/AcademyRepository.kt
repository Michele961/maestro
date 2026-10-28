package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import it.maestro.app.data.local.academy.AcademyLessonId
import it.maestro.app.data.local.academy.AcademyLessonProgress
import it.maestro.app.data.local.academy.AcademyPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import it.maestro.app.domain.time.Clock
import it.maestro.app.domain.time.SystemClock

/**
 * Repository per la progressione a tappe e il salvataggio dei moduli e sotto-moduli dell'Accademia.
 */
public interface AcademyRepository {
    public fun observeProgress(): Flow<Map<AcademyLessonId, AcademyLessonProgress>>
    public suspend fun getProgress(): Map<AcademyLessonId, AcademyLessonProgress>
    public suspend fun completeLesson(lessonId: AcademyLessonId)
    public suspend fun resetProgress()
}

/**
 * Implementazione persistente su Android Jetpack DataStore Preferences (ADR-0012).
 */
public class DataStoreAcademyRepository(
    private val dataStore: DataStore<Preferences>,
    private val clock: Clock = SystemClock,
) : AcademyRepository {

    private fun completedKey(id: AcademyLessonId) = booleanPreferencesKey("academy_lesson_completed_${id.name}")
    private fun timestampKey(id: AcademyLessonId) = longPreferencesKey("academy_lesson_completed_at_${id.name}")

    override fun observeProgress(): Flow<Map<AcademyLessonId, AcademyLessonProgress>> =
        dataStore.data.map { preferences ->
            mapPreferencesToProgress(preferences)
        }

    override suspend fun getProgress(): Map<AcademyLessonId, AcademyLessonProgress> {
        val preferences = dataStore.data.first()
        return mapPreferencesToProgress(preferences)
    }

    override suspend fun completeLesson(lessonId: AcademyLessonId) {
        val now = clock.nowMillis()
        dataStore.edit { prefs ->
            prefs[completedKey(lessonId)] = true
            prefs[timestampKey(lessonId)] = now
        }
    }

    override suspend fun resetProgress() {
        dataStore.edit { prefs ->
            AcademyLessonId.entries.forEach { id ->
                prefs.remove(completedKey(id))
                prefs.remove(timestampKey(id))
            }
        }
    }

    private fun mapPreferencesToProgress(preferences: Preferences): Map<AcademyLessonId, AcademyLessonProgress> {
        val completed = AcademyLessonId.entries.filter { preferences[completedKey(it)] ?: false }.toSet()
        val unlocked = AcademyPath.unlocked(completed)
        return AcademyLessonId.entries.associateWith { lessonId ->
            AcademyLessonProgress(
                lessonId = lessonId,
                isUnlocked = lessonId in unlocked,
                isCompleted = lessonId in completed,
                completedAt = preferences[timestampKey(lessonId)] ?: 0L,
            )
        }
    }
}

/**
 * Implementazione in-memory per test unitari veloci e isolati.
 */
public class InMemoryAcademyRepository(
    initialCompleted: Set<AcademyLessonId> = emptySet(),
    private val clock: Clock = SystemClock,
) : AcademyRepository {

    private val completedLessons = MutableStateFlow(initialCompleted)
    private val completedTimes = mutableMapOf<AcademyLessonId, Long>()

    override fun observeProgress(): Flow<Map<AcademyLessonId, AcademyLessonProgress>> =
        completedLessons.asStateFlow().map { completed ->
            buildProgressMap(completed)
        }

    override suspend fun getProgress(): Map<AcademyLessonId, AcademyLessonProgress> =
        buildProgressMap(completedLessons.value)

    override suspend fun completeLesson(lessonId: AcademyLessonId) {
        completedTimes[lessonId] = clock.nowMillis()
        completedLessons.value = completedLessons.value + lessonId
    }

    override suspend fun resetProgress() {
        completedTimes.clear()
        completedLessons.value = emptySet()
    }

    private fun buildProgressMap(completed: Set<AcademyLessonId>): Map<AcademyLessonId, AcademyLessonProgress> {
        val unlocked = AcademyPath.unlocked(completed)
        return AcademyLessonId.entries.associateWith { lessonId ->
            AcademyLessonProgress(
                lessonId = lessonId,
                isUnlocked = lessonId in unlocked,
                isCompleted = lessonId in completed,
                completedAt = completedTimes[lessonId] ?: 0L,
            )
        }
    }
}
