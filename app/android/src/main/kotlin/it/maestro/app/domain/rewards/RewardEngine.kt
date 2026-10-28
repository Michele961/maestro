package it.maestro.app.domain.rewards

import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MoveGrade
import it.maestro.ai.mastery.MasteryProfile
import it.maestro.app.domain.mastery.MasteryRepository
import it.maestro.app.domain.opponents.LadderVictory
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.opponents.OpponentRepository
import it.maestro.app.domain.retention.RetentionRepository
import it.maestro.app.domain.retention.DailyMission
import it.maestro.app.domain.retention.DailyMissionType
import it.maestro.app.domain.progress.AchievementCounter
import it.maestro.app.domain.progress.AchievementFacts
import it.maestro.app.domain.progress.AchievementId
import it.maestro.app.domain.progress.AchievementRepository
import it.maestro.app.domain.progress.Experience
import it.maestro.app.domain.progress.ExperienceRepository
import it.maestro.app.domain.progress.achievementsFor
import it.maestro.app.domain.retention.MatchMissionStatsExtractor
import it.maestro.app.domain.retention.missionIncrement
import it.maestro.app.domain.retention.weakestMissionFocus
import it.maestro.app.domain.wallet.TransactionReason
import it.maestro.app.domain.wallet.WalletOperationResult
import it.maestro.app.domain.wallet.WalletRepository
import it.maestro.app.domain.time.Clock
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ReplayResult
import it.maestro.game.scopa.replay
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/** Avanzamento di una missione causato da un evento di gioco. */
public data class MissionAdvance(
    val type: DailyMissionType,
    val progressBefore: Int,
    val progressAfter: Int,
    val target: Int,
    /** La missione dopo l'evento: serve a riscuoterla dalla sequenza di fine partita. */
    val mission: DailyMission,
) {
    /** La missione è stata completata proprio da questo evento. */
    val completedNow: Boolean get() = progressBefore < target && progressAfter >= target
}

/**
 * Ciò che un evento di gioco ha dato al giocatore (`docs/app/game-feel.md` §6): è l'unico input della sequenza di
 * fine partita. Un campo `null` significa che l'evento non lo tocca.
 *
 * @property creditsEarned crediti accreditati dall'evento (0 se nessuno).
 * @property missions missioni avanzate.
 * @property streakBefore giorni di streak prima dell'evento.
 * @property streakAfter giorni di streak dopo l'evento.
 * @property masteryBefore Indice di Maestria prima dell'evento, se l'evento lo cambia.
 * @property masteryAfter Indice di Maestria dopo l'evento.
 * @property achievementsUnlocked obiettivi ottenuti proprio da questo evento.
 * @property experienceGained punti esperienza dati dall'evento (0 se nessuno).
 * @property freezeTokenEarned l'evento ha completato 7 giorni di serie e ricaricato un Gettone Riposo.
 * @property ladderVictory vittoria sulla scala degli avversari registrata dall'evento (solo partita completa in Classica).
 */
public data class RewardSummary(
    val creditsEarned: Int = 0,
    val missions: List<MissionAdvance> = emptyList(),
    val streakBefore: Int? = null,
    val streakAfter: Int? = null,
    val masteryBefore: Int? = null,
    val masteryAfter: Int? = null,
    val ladderVictory: LadderVictory? = null,
    val freezeTokenEarned: Boolean = false,
    val experienceGained: Int = 0,
    val achievementsUnlocked: List<AchievementId> = emptyList(),
) {
    /** Variazione dell'Indice di Maestria, se l'evento lo tocca. */
    val masteryDelta: Int? get() = if (masteryBefore != null && masteryAfter != null) masteryAfter - masteryBefore else null
}

/**
 * Unico punto in cui nascono crediti, missioni, streak e Maestria (`docs/app/layers.md` §3, task F1R-14).
 * Fine partita, puzzle, lezione e riscossione delle missioni passano da qui; il resto dell'app non accredita nulla.
 *
 * Transitorio: le interfacce dei repository stanno ancora in `data` (F1R-14b le sposta nel dominio).
 */
public class RewardEngine(
    private val wallet: WalletRepository,
    private val retention: RetentionRepository,
    private val mastery: MasteryRepository,
    private val clock: Clock,
    private val opponents: OpponentRepository? = null,
    private val experience: ExperienceRepository? = null,
    private val achievements: AchievementRepository? = null,
) {

    /**
     * Registra una partita finita e analizzata: Maestria, attività del giorno e missioni. Nessun credito:
     * la spec non assegna crediti alla partita, solo alle missioni riscosse (`docs/app/game-feel.md` §6).
     * Se [opponent] è dato, la partita è in Classica (nessun fondamentale) e l'umano l'ha vinta, la vittoria
     * si registra sulla scala degli avversari (`docs/app/features/pre-game.md`).
     */
    public suspend fun onMatchFinished(
        log: MatchLog,
        analysis: MatchAnalysis,
        humanSeat: Seat,
        trainingScore: Pair<Int, Int>?,
        trainingFundamental: TrainingFundamental,
        opponent: Opponent? = null,
    ): RewardSummary {
        val today = clock.today()
        val masteryBefore = mastery.getMasteryProfile()
        val streakBefore = retention.getStreak(today)
        val missionsBefore = retention.getDailyMissions(today).missions

        val masteryAfter: MasteryProfile = mastery.recordMatch(
            matchAnalysis = analysis,
            matchLog = log,
            humanSeat = humanSeat,
            trainingScore = trainingScore,
            trainingFundamental = trainingFundamental,
        )
        val streakAfter = retention.recordGameActivity(today)

        val stats = MatchMissionStatsExtractor.extract(log, humanSeat)
        advanceByScope(today, missionsBefore, DailyMissionType.MAKE_SWEEPS, stats.sweepsCount)
        advanceByScope(today, missionsBefore, DailyMissionType.CAPTURE_DENARI, stats.denariCount)
        advanceByScope(today, missionsBefore, DailyMissionType.WIN_PRIMIERA, stats.primieraDeals)
        if (analysis.accuracy(humanSeat) >= ACCURACY_MISSION_THRESHOLD) {
            retention.updateMissionProgress(today, DailyMissionType.COMPLETE_MATCH_WITH_ACCURACY_80)
        }
        if (analysis.count(humanSeat, MoveGrade.BLUNDER) == 0) {
            retention.updateMissionProgress(today, DailyMissionType.PLAY_MATCH_WITHOUT_BLUNDERS)
        }
        // Sparring su qualunque obiettivo: le risposte giuste ai Check fanno avanzare la missione dei Check.
        if (trainingFundamental != TrainingFundamental.NONE && trainingScore != null) {
            retention.updateMissionProgress(today, DailyMissionType.ANSWER_CHECKS, trainingScore.first)
        }
        retention.recordMissionContext(
            today = today,
            sparringPlayed = trainingFundamental != TrainingFundamental.NONE,
            focus = masteryAfter.radar.takeIf { masteryAfter.matchesPlayed >= MISSION_FOCUS_MIN_MATCHES }?.weakestMissionFocus(),
        )

        val ladderVictory = if (
            opponent != null && opponents != null && trainingFundamental == TrainingFundamental.NONE &&
            (replay(log) as? ReplayResult.Ok)?.state?.winner == humanSeat
        ) {
            opponents.recordVictory(opponent)
        } else {
            null
        }

        val sweepsTotal = achievements?.addToCounter(AchievementCounter.SWEEPS, stats.sweepsCount) ?: 0
        val unlocked = unlockAchievements(
            AchievementFacts(
                winOverOpponent = ladderVictory?.defeated,
                firstTimeOverOpponent = ladderVictory?.firstTime == true,
                defeatedOpponents = opponents?.observeProgress()?.first()?.defeated?.size ?: 0,
                perfectMatch = analysis.accuracy(humanSeat) >= PERFECT_MATCH_ACCURACY &&
                    analysis.count(humanSeat, MoveGrade.BLUNDER) == 0,
                matchesPlayed = masteryAfter.matchesPlayed,
                sweepsTotal = sweepsTotal,
                streak = streakAfter.currentStreak,
                sparringPlayed = trainingFundamental != TrainingFundamental.NONE,
            ),
        )

        return RewardSummary(
            missions = advances(missionsBefore, retention.getDailyMissions(today).missions),
            streakBefore = streakBefore.currentStreak,
            streakAfter = streakAfter.currentStreak,
            achievementsUnlocked = unlocked,
            masteryBefore = masteryBefore.cognitiveElo,
            masteryAfter = masteryAfter.cognitiveElo,
            ladderVictory = ladderVictory,
            freezeTokenEarned = streakAfter.freezeTokens > streakBefore.freezeTokens,
            experienceGained = ladderVictory?.takeIf { it.firstTime }
                ?.let { addExperience(Experience.ladderFirstWin(it.defeated)) } ?: 0,
        )
    }

    /**
     * Sfida del giorno [date] risolta: crediti del livello [tierIndex] (una sola volta per data e livello),
     * missione "risolvi la sfida" e attività del giorno, tutte riferite a [date].
     */
    public suspend fun onPuzzleSolved(date: LocalDate, tierIndex: Int, tierCredits: Int): RewardSummary {
        val today = date
        val dateKey = date.toString()
        val streakBefore = retention.getStreak(today)
        val missionsBefore = retention.getDailyMissions(today).missions
        retention.updateMissionProgress(today, DailyMissionType.SOLVE_DAILY_PUZZLE)
        val streakAfter = retention.recordGameActivity(today)
        val referenceId = if (tierIndex == 0) dateKey else "$dateKey-tier-$tierIndex"
        val credited = credit(tierCredits, TransactionReason.DAILY_PUZZLE_REWARD, referenceId, "Sfida del Giorno completata: ${tierIndex + 1}")
        val puzzlesSolved = if (credited > 0 && tierIndex == 0) {
            achievements?.addToCounter(AchievementCounter.PUZZLES_SOLVED, 1) ?: 0
        } else {
            0
        }
        val unlocked = unlockAchievements(
            AchievementFacts(
                streak = streakAfter.currentStreak,
                puzzleFullDay = credited > 0 && tierIndex == PUZZLE_LAST_TIER,
                puzzlesSolved = puzzlesSolved,
            ),
        )
        return RewardSummary(
            creditsEarned = credited,
            missions = advances(missionsBefore, retention.getDailyMissions(today).missions),
            streakBefore = streakBefore.currentStreak,
            streakAfter = streakAfter.currentStreak,
            achievementsUnlocked = unlocked,
            freezeTokenEarned = streakAfter.freezeTokens > streakBefore.freezeTokens,
            experienceGained = if (credited > 0) addExperience(Experience.puzzleTier(tierIndex)) else 0,
        )
    }

    /** Lezione dell'Accademia superata: crediti una sola volta per lezione. */
    public suspend fun onLessonCompleted(lessonId: String): RewardSummary =
        RewardSummary(
            creditsEarned = credit(LESSON_CREDITS, TransactionReason.ACADEMY_LESSON_REWARD, lessonId, "Lezione Accademia superata"),
        )

    /** Riscuote la ricompensa di una missione completata; non fa nulla se è già stata riscossa. */
    public suspend fun claimMission(mission: DailyMission): RewardSummary {
        if (!mission.isCompleted || mission.isClaimed) return RewardSummary()
        if (!retention.claimMissionReward(clock.today(), mission.type)) return RewardSummary()
        val allClaimed = retention.getDailyMissions(clock.today()).missions.all { it.isClaimed }
        return RewardSummary(
            creditsEarned = credit(
                mission.type.rewardCredits,
                TransactionReason.DAILY_MISSION_REWARD,
                mission.id,
                "Ricompensa missione: ${mission.id}",
            ),
            experienceGained = addExperience(
                Experience.MISSION_CLAIMED + if (allClaimed) Experience.ALL_MISSIONS_CLAIMED else 0,
            ),
        )
    }

    /** Bonus di benvenuto, una volta sola al primo avvio (wallet vuoto). */
    public suspend fun grantWelcomeBonus(): Boolean = wallet.ensureWelcomeBonus(WELCOME_BONUS)

    /** Segna il giorno come giocato (streak) senza altri effetti: apertura della sfida, ritorno in app. */
    public suspend fun recordActivity() {
        retention.recordGameActivity(clock.today())
    }

    /** Sblocca gli obiettivi che [facts] soddisfa e restituisce quelli ottenuti adesso (mai due volte). */
    private suspend fun unlockAchievements(facts: AchievementFacts): List<AchievementId> =
        achievements?.unlock(achievementsFor(facts))?.sortedBy { it.ordinal } ?: emptyList()

    /** Aggiunge [points] all'esperienza (se il motore ne ha una) e li restituisce. */
    private suspend fun addExperience(points: Int): Int {
        experience?.add(points) ?: return 0
        return points
    }

    private suspend fun credit(amount: Int, reason: TransactionReason, referenceId: String, description: String): Int =
        when (wallet.addCredits(amount, reason, referenceId, description)) {
            is WalletOperationResult.Success -> amount
            else -> 0
        }

    private suspend fun advanceByScope(
        today: java.time.LocalDate,
        missions: List<DailyMission>,
        type: DailyMissionType,
        value: Int,
    ) {
        val mission = missions.firstOrNull { it.type == type } ?: return
        val increment = missionIncrement(mission, value)
        if (increment > 0) retention.updateMissionProgress(today, type, increment)
    }

    private fun advances(before: List<DailyMission>, after: List<DailyMission>): List<MissionAdvance> =
        after.mapNotNull { now ->
            val was = before.firstOrNull { it.id == now.id }?.progress ?: 0
            if (now.progress == was) null else MissionAdvance(now.type, was, now.progress, now.target, now)
        }

    private companion object {
        const val ACCURACY_MISSION_THRESHOLD = 80
        const val LESSON_CREDITS = 5
        const val WELCOME_BONUS = 50

        /** Partite analizzate prima che il radar scelga la missione adattiva (come il consiglio in preparazione). */
        const val MISSION_FOCUS_MIN_MATCHES = 3

        /** Precisione minima (con nessun errore grave) della «partita perfetta». */
        const val PERFECT_MATCH_ACCURACY = 90

        /** Indice dell'ultimo livello della Sfida del Giorno. */
        const val PUZZLE_LAST_TIER = 2
    }
}
