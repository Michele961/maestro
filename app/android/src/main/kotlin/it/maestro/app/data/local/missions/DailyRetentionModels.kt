package it.maestro.app.data.local.missions

import androidx.annotation.StringRes
import it.maestro.app.R
import it.maestro.game.rng.SeededRng
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

/**
 * Stato della Streak quotidiana ("Fiamma") e del Gettone Riposo (Freeze) (task F2-05).
 *
 * @property currentStreak Numero di giorni consecutivi attivi.
 * @property bestStreak Record massimo storico di giorni consecutivi.
 * @property lastPlayedDate Data dell'ultima attività registrata ("YYYY-MM-DD").
 * @property freezeTokens Numero di gettoni riposo salvavita disponibili (default: 1).
 * @property lastFreezeUsedDate Data dell'ultimo giorno in cui è stato consumato un gettone ("YYYY-MM-DD").
 */
public data class StreakState(
    public val currentStreak: Int = 0,
    public val bestStreak: Int = 0,
    public val lastPlayedDate: String = "",
    public val freezeTokens: Int = INITIAL_FREEZE_TOKENS,
    public val lastFreezeUsedDate: String = "",
) {
    public val isStreakActive: Boolean
        get() = currentStreak > 0

    public companion object {
        public const val INITIAL_FREEZE_TOKENS: Int = 1

        public val INITIAL: StreakState = StreakState(
            currentStreak = 0,
            bestStreak = 0,
            lastPlayedDate = "",
            freezeTokens = INITIAL_FREEZE_TOKENS,
            lastFreezeUsedDate = "",
        )

        /**
         * Valuta lo stato della streak per il giorno [today], applicando l'eventuale gettone riposo
         * se l'utente ha saltato un singolo giorno.
         */
        public fun evaluate(state: StreakState, today: LocalDate): StreakState {
            if (state.lastPlayedDate.isEmpty()) {
                return state
            }
            val lastPlayed = try {
                LocalDate.parse(state.lastPlayedDate)
            } catch (_: Exception) {
                return state
            }

            val daysDiff = ChronoUnit.DAYS.between(lastPlayed, today)
            return when {
                daysDiff <= 1L -> {
                    // Ha giocato oggi o ieri: streak intatta
                    state
                }
                daysDiff == 2L -> {
                    // Ha saltato ieri: verifica se ha un gettone riposo salvavita
                    val yesterdayStr = today.minusDays(1).toString()
                    if (state.lastFreezeUsedDate == yesterdayStr) {
                        // Gettone già consumato per ieri
                        state
                    } else if (state.freezeTokens > 0) {
                        // Consuma 1 gettone riposo e salva la streak
                        state.copy(
                            freezeTokens = state.freezeTokens - 1,
                            lastFreezeUsedDate = yesterdayStr,
                        )
                    } else {
                        // Nessun gettone riposo: streak persa
                        state.copy(currentStreak = 0)
                    }
                }
                else -> {
                    // Ha saltato 2 o più giorni consecutivi: streak persa comunque
                    state.copy(currentStreak = 0)
                }
            }
        }

        /**
         * Registra un'attività di gioco per la data [today], incrementando la streak se opportuno.
         */
        public fun recordActivity(state: StreakState, today: LocalDate): StreakState {
            val evaluated = evaluate(state, today)
            val todayStr = today.toString()

            if (evaluated.lastPlayedDate == todayStr) {
                // Attività già registrata oggi
                return evaluated
            }

            val yesterdayStr = today.minusDays(1).toString()
            val isContinuation = evaluated.lastPlayedDate == yesterdayStr ||
                evaluated.lastFreezeUsedDate == yesterdayStr

            val newStreak = if (isContinuation && evaluated.currentStreak > 0) {
                evaluated.currentStreak + 1
            } else {
                1
            }

            return evaluated.copy(
                currentStreak = newStreak,
                bestStreak = max(evaluated.bestStreak, newStreak),
                lastPlayedDate = todayStr,
            )
        }
    }
}

/**
 * Tipologie di missioni didattiche quotidiane (task F2-05).
 */
public enum class DailyMissionType(
    @StringRes public val titleRes: Int,
    @StringRes public val descriptionRes: Int,
    public val defaultTarget: Int,
    public val rewardCredits: Int,
) {
    SOLVE_DAILY_PUZZLE(
        titleRes = R.string.mission_daily_puzzle_title,
        descriptionRes = R.string.mission_daily_puzzle_desc,
        defaultTarget = 1,
        rewardCredits = 10,
    ),
    COMPLETE_MATCH_WITH_ACCURACY_80(
        titleRes = R.string.mission_accuracy_80_title,
        descriptionRes = R.string.mission_accuracy_80_desc,
        defaultTarget = 1,
        rewardCredits = 15,
    ),
    ANSWER_SEVENS_CHECKS(
        titleRes = R.string.mission_sevens_check_title,
        descriptionRes = R.string.mission_sevens_check_desc,
        defaultTarget = 2,
        rewardCredits = 10,
    ),
    MAKE_SWEEPS(
        titleRes = R.string.mission_sweeps_title,
        descriptionRes = R.string.mission_sweeps_desc,
        defaultTarget = 2,
        rewardCredits = 10,
    ),
    CAPTURE_DENARI(
        titleRes = R.string.mission_denari_title,
        descriptionRes = R.string.mission_denari_desc,
        defaultTarget = 6,
        rewardCredits = 10,
    ),
    PLAY_MATCH_WITHOUT_BLUNDERS(
        titleRes = R.string.mission_no_blunders_title,
        descriptionRes = R.string.mission_no_blunders_desc,
        defaultTarget = 1,
        rewardCredits = 15,
    ),
}

/**
 * Istanza di una missione didattica giornaliera con relativo stato di avanzamento.
 */
public data class DailyMission(
    public val id: String,
    public val type: DailyMissionType,
    public val progress: Int = 0,
    public val target: Int = type.defaultTarget,
    public val isCompleted: Boolean = progress >= target,
    public val isClaimed: Boolean = false,
) {
    public val progressFraction: Float
        get() = if (target > 0) (progress.toFloat() / target).coerceIn(0f, 1f) else 1f
}

/**
 * Stato complessivo delle 3 missioni giornaliere attive in data [date].
 */
public data class DailyMissionsState(
    public val date: String,
    public val missions: List<DailyMission>,
) {
    public val allCompleted: Boolean
        get() = missions.isNotEmpty() && missions.all { it.isCompleted }

    public val completedCount: Int
        get() = missions.count { it.isCompleted }

    public val unclaimedRewardsCount: Int
        get() = missions.count { it.isCompleted && !it.isClaimed }
}

/**
 * Generatore deterministico delle 3 missioni didattiche del giorno (task F2-05).
 */
public object DailyMissionGenerator {

    public fun generateForDate(date: LocalDate): List<DailyMission> {
        val seed = date.year * 10000L + date.monthValue * 100L + date.dayOfMonth
        val rng = SeededRng(seed)

        // Mescola deterministicamente le missioni disponibili e seleziona le prime 3
        val allTypes = DailyMissionType.entries.toMutableList()
        // Fisher-Yates deterministico con SeededRng
        for (i in allTypes.indices.reversed()) {
            val j = (rng.nextLong() and Long.MAX_VALUE % (i + 1)).toInt()
            val tmp = allTypes[i]
            allTypes[i] = allTypes[j]
            allTypes[j] = tmp
        }

        return allTypes.take(3).mapIndexed { index, type ->
            DailyMission(
                id = "${date}_mission_$index",
                type = type,
                progress = 0,
                target = type.defaultTarget,
                isCompleted = false,
            )
        }
    }
}
