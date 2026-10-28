package it.maestro.app.domain.retention

import it.maestro.ai.mastery.AbilityRadar
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
 * Ambito in cui si misura una missione (task F1R-29a): la descrizione dice ciò che si misura.
 */
public enum class MissionScope {
    /** Conta il risultato di una singola partita (il migliore del giorno), non la somma delle partite. */
    PER_MATCH,

    /** Somma i risultati di tutte le partite e le attività del giorno. */
    PER_DAY,
}

/**
 * Tipologie di missioni didattiche quotidiane (task F2-05).
 */
public enum class DailyMissionType(
    public val defaultTarget: Int,
    public val rewardCredits: Int,
    public val scope: MissionScope = MissionScope.PER_DAY,
) {
    SOLVE_DAILY_PUZZLE(
        defaultTarget = 1,
        rewardCredits = 10,
    ),
    COMPLETE_MATCH_WITH_ACCURACY_80(
        defaultTarget = 1,
        rewardCredits = 15,
    ),
    /** Risposte giuste ai Check dello Sparring, su qualunque obiettivo (task F1R-29b). */
    ANSWER_CHECKS(
        defaultTarget = 2,
        rewardCredits = 10,
    ),
    MAKE_SWEEPS(
        defaultTarget = 2,
        rewardCredits = 10,
    ),
    CAPTURE_DENARI(
        defaultTarget = 6,
        rewardCredits = 10,
        scope = MissionScope.PER_MATCH,
    ),
    PLAY_MATCH_WITHOUT_BLUNDERS(
        defaultTarget = 1,
        rewardCredits = 15,
    ),

    /** Smazzate di una stessa partita in cui il giocatore vince la Primiera (task F1R-29b). */
    WIN_PRIMIERA(
        defaultTarget = 2,
        rewardCredits = 10,
        scope = MissionScope.PER_MATCH,
    ),
}

/**
 * Asse del radar su cui una missione adattiva può far lavorare il giocatore (task F1R-29b).
 */
public enum class MissionFocus { SEVENS, DENARI, PRIMIERA, BOARD, ENDGAME }

/**
 * Ciò che il generatore sa del giocatore per scegliere le missioni di domani (task F1R-29b).
 *
 * @property recentSparring Ha giocato Sparring negli ultimi [RECENT_SPARRING_DAYS] giorni: solo allora
 *   può capitare la missione dei Check.
 * @property focus Asse più debole del radar, `null` finché il radar non dice nulla.
 */
public data class MissionContext(
    public val recentSparring: Boolean = false,
    public val focus: MissionFocus? = null,
) {
    public companion object {
        public val NONE: MissionContext = MissionContext()
        public const val RECENT_SPARRING_DAYS: Long = 14L
    }
}

/** L'asse più debole del radar; a parità vince il primo in ordine Sette, Denari, Primiera, Banco, Finale. */
public fun AbilityRadar.weakestMissionFocus(): MissionFocus = listOf(
    MissionFocus.SEVENS to sevens,
    MissionFocus.DENARI to denari,
    MissionFocus.PRIMIERA to primiera,
    MissionFocus.BOARD to board,
    MissionFocus.ENDGAME to endgame,
).minBy { it.second }.first

/** La missione che allena [focus], se ne esiste una adatta a chi gioca (o no) in Sparring. */
public fun adaptiveMissionFor(focus: MissionFocus, recentSparring: Boolean): DailyMissionType? = when (focus) {
    MissionFocus.SEVENS -> DailyMissionType.ANSWER_CHECKS.takeIf { recentSparring }
    MissionFocus.DENARI -> DailyMissionType.CAPTURE_DENARI
    MissionFocus.PRIMIERA -> DailyMissionType.WIN_PRIMIERA
    MissionFocus.BOARD -> DailyMissionType.MAKE_SWEEPS
    MissionFocus.ENDGAME -> DailyMissionType.PLAY_MATCH_WITHOUT_BLUNDERS
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
 * Di quanto avanza [mission] dopo un risultato di [value]: nell'ambito [MissionScope.PER_MATCH] conta
 * solo il miglior risultato di una partita (task F1R-29a), in [MissionScope.PER_DAY] si somma.
 */
public fun missionIncrement(mission: DailyMission, value: Int): Int = when (mission.type.scope) {
    MissionScope.PER_DAY -> value
    MissionScope.PER_MATCH -> (value - mission.progress).coerceAtLeast(0)
}

/**
 * Generatore deterministico delle 3 missioni didattiche del giorno (task F2-05).
 */
public object DailyMissionGenerator {

    private const val MISSIONS_SEED_SALT: Long = 0x4D495353L // "MISS"

    /**
     * Le tre missioni del giorno. Con un [context] una segue l'asse più debole del radar e la missione
     * dei Check capita solo a chi ha giocato Sparring di recente (task F1R-29b); le altre sono casuali.
     */
    public fun generateForDate(date: LocalDate, context: MissionContext = MissionContext.NONE): List<DailyMission> {
        // Seme proprio, diverso da quello della Sfida del Giorno (stessa data).
        val seed = MISSIONS_SEED_SALT xor (date.year * 10000L + date.monthValue * 100L + date.dayOfMonth)
        val rng = SeededRng(seed)

        // Mescola deterministicamente le missioni disponibili e seleziona le prime 3
        val allTypes = DailyMissionType.entries
            .filter { it != DailyMissionType.ANSWER_CHECKS || context.recentSparring }
            .toMutableList()
        // Fisher-Yates deterministico con SeededRng
        for (i in allTypes.indices.reversed()) {
            val j = ((rng.nextLong() and Long.MAX_VALUE) % (i + 1)).toInt()
            val tmp = allTypes[i]
            allTypes[i] = allTypes[j]
            allTypes[j] = tmp
        }

        val adaptive = context.focus?.let { adaptiveMissionFor(it, context.recentSparring) }
        val chosen = if (adaptive == null) allTypes.take(3) else listOf(adaptive) + (allTypes - adaptive).take(2)

        return chosen.mapIndexed { index, type ->
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
