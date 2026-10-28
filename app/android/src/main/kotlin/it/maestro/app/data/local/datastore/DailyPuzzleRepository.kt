package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Stato persistente del puzzle tattico quotidiano memorizzato su DataStore (task F2-03).
 *
 * @property lastCompletedDate Data locale dell'ultimo completamento con successo ("YYYY-MM-DD").
 * @property isSolvedToday Se il puzzle del giorno odierno è già stato risolto.
 * @property attemptsToday Numero di tentativi effettuati sul puzzle odierno.
 * @property streak Numero di giorni consecutivi di risoluzione del puzzle quotidiano.
 */
public data class DailyPuzzleRecord(
    val lastCompletedDate: String = "",
    val isSolvedToday: Boolean = false,
    val attemptsToday: Int = 0,
    val streak: Int = 0,
    val solvedTiersToday: Set<Int> = emptySet(),
)

/**
 * Serie di sfide dopo aver risolto il primo livello di [today]: continua se l'ultimo giorno risolto è ieri, altrimenti
 * riparte da 1. Misura una cosa diversa dalla serie di giorni di gioco (`RetentionRepository`): qui conta solo la Sfida.
 */
public fun nextChallengeStreak(lastCompletedDate: String, previousStreak: Int, today: String): Int =
    if (isDayBefore(lastCompletedDate, today)) previousStreak + 1 else 1

/** Serie di sfide da mostrare oggi: viva se l'ultima sfida risolta è di oggi o di ieri, altrimenti 0. */
public fun liveChallengeStreak(lastCompletedDate: String, streak: Int, today: String): Int =
    if (lastCompletedDate == today || isDayBefore(lastCompletedDate, today)) streak else 0

private fun isDayBefore(earlier: String, later: String): Boolean =
    runCatching { java.time.LocalDate.parse(earlier).plusDays(1) == java.time.LocalDate.parse(later) }.getOrDefault(false)

/**
 * Repository per la persistenza e osservazione dello stato della "Sfida del Giorno".
 */
public interface DailyPuzzleRepository {
    public fun observeDailyPuzzle(todayDate: String): Flow<DailyPuzzleRecord>
    public suspend fun getDailyPuzzleRecord(todayDate: String): DailyPuzzleRecord
    public suspend fun recordPuzzleCompletion(
        todayDate: String,
        attempts: Int,
        solved: Boolean,
        tierIndex: Int = 0,
    )
}

/**
 * Implementazione ufficiale basata su DataStore Preferences (ADR-0012, task F2-03).
 */
public class DataStoreDailyPuzzleRepository(
    private val dataStore: DataStore<Preferences>,
) : DailyPuzzleRepository {

    private object PreferencesKeys {
        val LAST_COMPLETED_DATE = stringPreferencesKey("daily_puzzle_last_completed_date")
        val LAST_PLAYED_DATE = stringPreferencesKey("daily_puzzle_last_played_date")
        val IS_SOLVED = booleanPreferencesKey("daily_puzzle_is_solved")
        val ATTEMPTS_COUNT = intPreferencesKey("daily_puzzle_attempts_count")
        val STREAK = intPreferencesKey("daily_puzzle_streak")
        val SOLVED_TIERS = stringPreferencesKey("daily_puzzle_solved_tiers")
    }

    override fun observeDailyPuzzle(todayDate: String): Flow<DailyPuzzleRecord> =
        dataStore.data.map { preferences ->
            mapToRecord(preferences, todayDate)
        }

    override suspend fun getDailyPuzzleRecord(todayDate: String): DailyPuzzleRecord {
        val preferences = dataStore.data.first()
        return mapToRecord(preferences, todayDate)
    }

    override suspend fun recordPuzzleCompletion(
        todayDate: String,
        attempts: Int,
        solved: Boolean,
        tierIndex: Int,
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_PLAYED_DATE] = todayDate
            preferences[PreferencesKeys.ATTEMPTS_COUNT] = attempts

            if (solved) {
                val previousCompleted = preferences[PreferencesKeys.LAST_COMPLETED_DATE].orEmpty()
                val existingTiers = if (previousCompleted == todayDate) {
                    preferences[PreferencesKeys.SOLVED_TIERS].orEmpty()
                        .split(",")
                        .filter { it.isNotBlank() }
                        .mapNotNull { it.toIntOrNull() }
                        .toSet()
                } else {
                    emptySet()
                }
                val newTiers = existingTiers + tierIndex
                preferences[PreferencesKeys.SOLVED_TIERS] = newTiers.joinToString(",")

                if (previousCompleted != todayDate) {
                    preferences[PreferencesKeys.LAST_COMPLETED_DATE] = todayDate
                    preferences[PreferencesKeys.IS_SOLVED] = true

                    val previousStreak = preferences[PreferencesKeys.STREAK] ?: 0
                    preferences[PreferencesKeys.STREAK] = nextChallengeStreak(previousCompleted, previousStreak, todayDate)
                }
            }
        }
    }

    private fun mapToRecord(preferences: Preferences, todayDate: String): DailyPuzzleRecord {
        val lastCompleted = preferences[PreferencesKeys.LAST_COMPLETED_DATE].orEmpty()
        val lastPlayed = preferences[PreferencesKeys.LAST_PLAYED_DATE].orEmpty()
        val isSolvedToday = (lastCompleted == todayDate)
        val attempts = if (lastPlayed == todayDate) {
            preferences[PreferencesKeys.ATTEMPTS_COUNT] ?: 0
        } else {
            0
        }
        val streak = liveChallengeStreak(lastCompleted, preferences[PreferencesKeys.STREAK] ?: 0, todayDate)
        val storedTiers = if (lastCompleted == todayDate) {
            preferences[PreferencesKeys.SOLVED_TIERS].orEmpty()
                .split(",")
                .filter { it.isNotBlank() }
                .mapNotNull { it.toIntOrNull() }
                .toSet()
        } else {
            emptySet()
        }
        val solvedTiersToday = if (isSolvedToday && storedTiers.isEmpty()) setOf(0) else storedTiers

        return DailyPuzzleRecord(
            lastCompletedDate = lastCompleted,
            isSolvedToday = isSolvedToday,
            attemptsToday = attempts,
            streak = streak,
            solvedTiersToday = solvedTiersToday,
        )
    }
}

/**
 * Implementazione in memoria per test unitari e screenshot Roborazzi.
 */
public class InMemoryDailyPuzzleRepository(
    initialRecord: DailyPuzzleRecord = DailyPuzzleRecord(),
) : DailyPuzzleRepository {

    private val _recordFlow = MutableStateFlow(initialRecord)

    override fun observeDailyPuzzle(todayDate: String): Flow<DailyPuzzleRecord> =
        _recordFlow.asStateFlow()

    override suspend fun getDailyPuzzleRecord(todayDate: String): DailyPuzzleRecord =
        _recordFlow.value

    override suspend fun recordPuzzleCompletion(
        todayDate: String,
        attempts: Int,
        solved: Boolean,
        tierIndex: Int,
    ) {
        val current = _recordFlow.value
        val isFirstSolveToday = solved && current.lastCompletedDate != todayDate
        val newStreak = if (isFirstSolveToday) nextChallengeStreak(current.lastCompletedDate, current.streak, todayDate) else current.streak
        val newTiers = if (solved) {
            if (current.lastCompletedDate == todayDate) current.solvedTiersToday + tierIndex else setOf(tierIndex)
        } else {
            current.solvedTiersToday
        }
        _recordFlow.value = current.copy(
            lastCompletedDate = if (solved) todayDate else current.lastCompletedDate,
            isSolvedToday = solved || current.isSolvedToday,
            attemptsToday = attempts,
            streak = newStreak,
            solvedTiersToday = newTiers,
        )
    }
}
