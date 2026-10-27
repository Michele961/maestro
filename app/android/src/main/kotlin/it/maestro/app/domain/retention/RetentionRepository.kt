package it.maestro.app.domain.retention

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository per la persistenza della Streak ("Fiamma") e delle Missioni Didattiche giornaliere (task F2-05).
 */
public interface RetentionRepository {
    public fun observeStreak(today: LocalDate): Flow<StreakState>
    public suspend fun getStreak(today: LocalDate): StreakState
    public suspend fun recordGameActivity(today: LocalDate): StreakState

    public fun observeDailyMissions(today: LocalDate): Flow<DailyMissionsState>
    public suspend fun getDailyMissions(today: LocalDate): DailyMissionsState
    public suspend fun updateMissionProgress(today: LocalDate, type: DailyMissionType, increment: Int = 1)
    public suspend fun setMissionCompleted(today: LocalDate, type: DailyMissionType)
    public suspend fun claimMissionReward(today: LocalDate, type: DailyMissionType): Boolean

    public suspend fun resetRetentionData()
}
