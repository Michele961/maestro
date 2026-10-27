package it.maestro.app.data.local.datastore

import it.maestro.app.domain.retention.RetentionRepository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import it.maestro.app.domain.retention.DailyMission
import it.maestro.app.domain.retention.DailyMissionGenerator
import it.maestro.app.domain.retention.DailyMissionType
import it.maestro.app.domain.retention.DailyMissionsState
import it.maestro.app.domain.retention.StreakState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Implementazione ufficiale basata su DataStore Preferences (ADR-0012, task F2-05).
 */
public class DataStoreRetentionRepository(
    private val dataStore: DataStore<Preferences>,
) : RetentionRepository {

    private object PreferencesKeys {
        val STREAK_CURRENT = intPreferencesKey("streak_current")
        val STREAK_BEST = intPreferencesKey("streak_best")
        val STREAK_LAST_PLAYED = stringPreferencesKey("streak_last_played")
        val STREAK_FREEZE_TOKENS = intPreferencesKey("streak_freeze_tokens")
        val STREAK_LAST_FREEZE_USED = stringPreferencesKey("streak_last_freeze_used")

        val MISSIONS_DATE = stringPreferencesKey("missions_date")
        fun missionType(i: Int) = stringPreferencesKey("mission_${i}_type")
        fun missionProgress(i: Int) = intPreferencesKey("mission_${i}_progress")
        fun missionCompleted(i: Int) = booleanPreferencesKey("mission_${i}_completed")
        fun missionClaimed(i: Int) = booleanPreferencesKey("mission_${i}_claimed")
    }

    override fun observeStreak(today: LocalDate): Flow<StreakState> =
        dataStore.data.map { preferences ->
            val raw = mapToStreak(preferences)
            StreakState.evaluate(raw, today)
        }

    override suspend fun getStreak(today: LocalDate): StreakState {
        val preferences = dataStore.data.first()
        val raw = mapToStreak(preferences)
        val evaluated = StreakState.evaluate(raw, today)
        // Se durante la valutazione è stato consumato un gettone o azzerata la streak, persiste il nuovo stato
        if (evaluated != raw) {
            persistStreak(evaluated)
        }
        return evaluated
    }

    override suspend fun recordGameActivity(today: LocalDate): StreakState {
        val current = getStreak(today)
        val updated = StreakState.recordActivity(current, today)
        persistStreak(updated)
        return updated
    }

    override fun observeDailyMissions(today: LocalDate): Flow<DailyMissionsState> =
        dataStore.data.map { preferences ->
            mapToMissions(preferences, today)
        }

    override suspend fun getDailyMissions(today: LocalDate): DailyMissionsState {
        val preferences = dataStore.data.first()
        val current = mapToMissions(preferences, today)
        val todayStr = today.toString()
        if (preferences[PreferencesKeys.MISSIONS_DATE] != todayStr) {
            persistMissions(current)
        }
        return current
    }

    override suspend fun updateMissionProgress(
        today: LocalDate,
        type: DailyMissionType,
        increment: Int,
    ) {
        val state = getDailyMissions(today)
        val updatedMissions = state.missions.map { mission ->
            if (mission.type == type && !mission.isCompleted) {
                val newProgress = (mission.progress + increment).coerceAtMost(mission.target)
                mission.copy(
                    progress = newProgress,
                    isCompleted = newProgress >= mission.target,
                )
            } else {
                mission
            }
        }
        persistMissions(state.copy(missions = updatedMissions))
    }

    override suspend fun setMissionCompleted(today: LocalDate, type: DailyMissionType) {
        val state = getDailyMissions(today)
        val updatedMissions = state.missions.map { mission ->
            if (mission.type == type) {
                mission.copy(
                    progress = mission.target,
                    isCompleted = true,
                )
            } else {
                mission
            }
        }
        persistMissions(state.copy(missions = updatedMissions))
    }

    override suspend fun claimMissionReward(today: LocalDate, type: DailyMissionType): Boolean {
        val state = getDailyMissions(today)
        var claimed = false
        val updatedMissions = state.missions.map { mission ->
            if (mission.type == type && mission.isCompleted && !mission.isClaimed) {
                claimed = true
                mission.copy(isClaimed = true)
            } else {
                mission
            }
        }
        if (claimed) {
            persistMissions(state.copy(missions = updatedMissions))
        }
        return claimed
    }

    override suspend fun resetRetentionData() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.STREAK_CURRENT)
            preferences.remove(PreferencesKeys.STREAK_BEST)
            preferences.remove(PreferencesKeys.STREAK_LAST_PLAYED)
            preferences.remove(PreferencesKeys.STREAK_FREEZE_TOKENS)
            preferences.remove(PreferencesKeys.STREAK_LAST_FREEZE_USED)
            preferences.remove(PreferencesKeys.MISSIONS_DATE)
            for (i in 0 until 3) {
                preferences.remove(PreferencesKeys.missionType(i))
                preferences.remove(PreferencesKeys.missionProgress(i))
                preferences.remove(PreferencesKeys.missionCompleted(i))
                preferences.remove(PreferencesKeys.missionClaimed(i))
            }
        }
    }

    private suspend fun persistStreak(state: StreakState) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.STREAK_CURRENT] = state.currentStreak
            preferences[PreferencesKeys.STREAK_BEST] = state.bestStreak
            preferences[PreferencesKeys.STREAK_LAST_PLAYED] = state.lastPlayedDate
            preferences[PreferencesKeys.STREAK_FREEZE_TOKENS] = state.freezeTokens
            preferences[PreferencesKeys.STREAK_LAST_FREEZE_USED] = state.lastFreezeUsedDate
        }
    }

    private suspend fun persistMissions(state: DailyMissionsState) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MISSIONS_DATE] = state.date
            state.missions.forEachIndexed { i, mission ->
                preferences[PreferencesKeys.missionType(i)] = mission.type.name
                preferences[PreferencesKeys.missionProgress(i)] = mission.progress
                preferences[PreferencesKeys.missionCompleted(i)] = mission.isCompleted
                preferences[PreferencesKeys.missionClaimed(i)] = mission.isClaimed
            }
        }
    }

    private fun mapToStreak(preferences: Preferences): StreakState {
        return StreakState(
            currentStreak = preferences[PreferencesKeys.STREAK_CURRENT] ?: 0,
            bestStreak = preferences[PreferencesKeys.STREAK_BEST] ?: 0,
            lastPlayedDate = preferences[PreferencesKeys.STREAK_LAST_PLAYED].orEmpty(),
            freezeTokens = preferences[PreferencesKeys.STREAK_FREEZE_TOKENS] ?: StreakState.INITIAL_FREEZE_TOKENS,
            lastFreezeUsedDate = preferences[PreferencesKeys.STREAK_LAST_FREEZE_USED].orEmpty(),
        )
    }

    private fun mapToMissions(preferences: Preferences, today: LocalDate): DailyMissionsState {
        val todayStr = today.toString()
        val savedDate = preferences[PreferencesKeys.MISSIONS_DATE].orEmpty()

        if (savedDate != todayStr) {
            // Genera nuove missioni deterministiche per la data odierna
            val fresh = DailyMissionGenerator.generateForDate(today)
            return DailyMissionsState(date = todayStr, missions = fresh)
        }

        // Carica le missioni salvate
        val list = mutableListOf<DailyMission>()
        for (i in 0 until 3) {
            val typeName = preferences[PreferencesKeys.missionType(i)]
            val type = typeName?.let { runCatching { DailyMissionType.valueOf(it) }.getOrNull() }
            if (type != null) {
                val progress = preferences[PreferencesKeys.missionProgress(i)] ?: 0
                val completed = preferences[PreferencesKeys.missionCompleted(i)] ?: (progress >= type.defaultTarget)
                val claimed = preferences[PreferencesKeys.missionClaimed(i)] ?: false
                list.add(
                    DailyMission(
                        id = "${todayStr}_mission_$i",
                        type = type,
                        progress = progress,
                        target = type.defaultTarget,
                        isCompleted = completed,
                        isClaimed = claimed,
                    ),
                )
            }
        }

        return if (list.size == 3) {
            DailyMissionsState(date = todayStr, missions = list)
        } else {
            DailyMissionsState(date = todayStr, missions = DailyMissionGenerator.generateForDate(today))
        }
    }
}

/**
 * Implementazione in memoria per test e preview Compose.
 */
public class InMemoryRetentionRepository(
    initialStreak: StreakState = StreakState.INITIAL,
) : RetentionRepository {

    private val streakFlow = MutableStateFlow(initialStreak)
    private val missionsFlow = MutableStateFlow<DailyMissionsState?>(null)

    override fun observeStreak(today: LocalDate): Flow<StreakState> =
        streakFlow.map { StreakState.evaluate(it, today) }

    override suspend fun getStreak(today: LocalDate): StreakState {
        val raw = streakFlow.value
        val evaluated = StreakState.evaluate(raw, today)
        streakFlow.value = evaluated
        return evaluated
    }

    override suspend fun recordGameActivity(today: LocalDate): StreakState {
        val current = getStreak(today)
        val updated = StreakState.recordActivity(current, today)
        streakFlow.value = updated
        return updated
    }

    override fun observeDailyMissions(today: LocalDate): Flow<DailyMissionsState> =
        missionsFlow.map { it ?: getDailyMissions(today) }

    override suspend fun getDailyMissions(today: LocalDate): DailyMissionsState {
        val current = missionsFlow.value
        val todayStr = today.toString()
        if (current == null || current.date != todayStr) {
            val fresh = DailyMissionsState(
                date = todayStr,
                missions = DailyMissionGenerator.generateForDate(today),
            )
            missionsFlow.value = fresh
            return fresh
        }
        return current
    }

    override suspend fun updateMissionProgress(
        today: LocalDate,
        type: DailyMissionType,
        increment: Int,
    ) {
        val state = getDailyMissions(today)
        val updated = state.missions.map { mission ->
            if (mission.type == type && !mission.isCompleted) {
                val newProgress = (mission.progress + increment).coerceAtMost(mission.target)
                mission.copy(
                    progress = newProgress,
                    isCompleted = newProgress >= mission.target,
                )
            } else {
                mission
            }
        }
        missionsFlow.value = state.copy(missions = updated)
    }

    override suspend fun setMissionCompleted(today: LocalDate, type: DailyMissionType) {
        val state = getDailyMissions(today)
        val updated = state.missions.map { mission ->
            if (mission.type == type) {
                mission.copy(progress = mission.target, isCompleted = true)
            } else {
                mission
            }
        }
        missionsFlow.value = state.copy(missions = updated)
    }

    override suspend fun claimMissionReward(today: LocalDate, type: DailyMissionType): Boolean {
        val state = getDailyMissions(today)
        var claimed = false
        val updated = state.missions.map { mission ->
            if (mission.type == type && mission.isCompleted && !mission.isClaimed) {
                claimed = true
                mission.copy(isClaimed = true)
            } else {
                mission
            }
        }
        if (claimed) {
            missionsFlow.value = state.copy(missions = updated)
        }
        return claimed
    }

    override suspend fun resetRetentionData() {
        streakFlow.value = StreakState.INITIAL
        missionsFlow.value = null
    }
}
