package it.maestro.app.shell

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MoveGrade
import it.maestro.app.designsystem.audio.SoundEffect
import it.maestro.app.designsystem.audio.SoundManager
import it.maestro.app.data.local.datastore.MasteryRepository
import it.maestro.app.data.local.datastore.RetentionRepository
import it.maestro.app.data.local.missions.DailyMission
import it.maestro.app.data.local.missions.DailyMissionType
import it.maestro.app.data.local.missions.MatchMissionStatsExtractor
import it.maestro.app.data.local.wallet.TransactionReason
import it.maestro.app.data.local.wallet.WalletRepository
import it.maestro.app.data.repository.MatchRepository
import it.maestro.app.data.repository.RestoreMatchResult
import it.maestro.app.domain.time.Clock
import it.maestro.app.feature.table.presentation.TestMatchGenerator
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Esito della richiesta di riprendere la partita salvata. */
public enum class ResumeOutcome { RESUMED, WRONG_RULES_VERSION, NOTHING_TO_RESUME }

/**
 * ViewModel del guscio dell'app (task F1R-04): ciò che prima viveva in `MainActivity`.
 *
 * Tiene la logica di fine partita (Maestria, streak, missioni) e il riscatto delle missioni finché
 * non passano a `RewardEngine` (F1R-14). Le partite già registrate stanno in [SavedStateHandle]:
 * una rotazione o la ricreazione del processo sulla schermata di fine partita non le conta due volte.
 */
public class ShellViewModel(
    private val matchRepository: MatchRepository,
    private val masteryRepository: MasteryRepository,
    private val retentionRepository: RetentionRepository,
    private val walletRepository: WalletRepository,
    private val soundManager: SoundManager,
    private val clock: Clock,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _hasActiveMatch = MutableStateFlow(false)

    /** `true` se c'è una partita interrotta da riprendere. */
    public val hasActiveMatch: StateFlow<Boolean> = _hasActiveMatch.asStateFlow()

    private val analyses = mutableMapOf<String, MatchAnalysis>()

    init {
        viewModelScope.launch {
            runCatching { walletRepository.ensureWelcomeBonus(bonusAmount = WELCOME_BONUS) }
        }
    }

    /** Ricontrolla se esiste una partita da riprendere (alla comparsa della Home). */
    public fun refreshActiveMatch() {
        viewModelScope.launch {
            _hasActiveMatch.value = matchRepository.restoreActiveMatch() is RestoreMatchResult.Success
        }
    }

    /** Verifica la partita salvata prima di aprire il tavolo; cancella quelle non più rigiocabili. */
    public suspend fun checkResumable(): ResumeOutcome =
        when (matchRepository.restoreActiveMatch()) {
            is RestoreMatchResult.Success -> ResumeOutcome.RESUMED
            is RestoreMatchResult.WrongRulesVersion -> {
                matchRepository.clearActiveMatch()
                _hasActiveMatch.value = false
                ResumeOutcome.WRONG_RULES_VERSION
            }
            else -> {
                matchRepository.clearActiveMatch()
                _hasActiveMatch.value = false
                ResumeOutcome.NOTHING_TO_RESUME
            }
        }

    /** Salva come partita in corso una partita all'ultima mano, per collaudo (solo build di debug). */
    public suspend fun prepareTestEndgameMatch(): MatchLog {
        val log = TestMatchGenerator.generateEndgameMatch().log
        matchRepository.saveActiveMatch(log = log, level = TEST_LEVEL, targetScore = log.config.targetScore)
        return log
    }

    /** Seed per una nuova partita. */
    public fun newSeed(): Long = clock.newSeed()

    /** Analisi già calcolata per [matchLog] in questa sessione, per non ricalcolarla in review. */
    public fun cachedAnalysis(matchLog: String): MatchAnalysis? = analyses[matchLog]

    /**
     * Registra una partita appena analizzata: Maestria, attività del giorno e missioni.
     * Ogni partita (identificata dal log) si registra una volta sola.
     */
    public fun onMatchAnalyzed(
        matchLog: MatchLog,
        analysis: MatchAnalysis,
        humanSeat: Seat,
        trainingScore: Pair<Int, Int>?,
        trainingFundamental: TrainingFundamental,
        fromHistory: Boolean,
    ) {
        val key = matchLog.toText()
        analyses[key] = analysis
        if (fromHistory) return
        val recorded = savedStateHandle.get<ArrayList<String>>(RECORDED_KEY) ?: arrayListOf()
        if (key in recorded) return
        savedStateHandle[RECORDED_KEY] = ArrayList(recorded + key)

        viewModelScope.launch {
            val today = clock.today()
            masteryRepository.recordMatch(
                matchAnalysis = analysis,
                matchLog = matchLog,
                humanSeat = humanSeat,
                trainingScore = trainingScore,
                trainingFundamental = trainingFundamental,
            )
            retentionRepository.recordGameActivity(today)
            updateMissions(today, matchLog, analysis, humanSeat, trainingScore, trainingFundamental)
        }
    }

    /** Riscatta la ricompensa di una missione completata. */
    public fun claimMissionReward(mission: DailyMission) {
        viewModelScope.launch {
            val claimed = retentionRepository.claimMissionReward(clock.today(), mission.type)
            if (claimed) {
                walletRepository.addCredits(
                    amount = mission.type.rewardCredits,
                    reason = TransactionReason.DAILY_MISSION_REWARD,
                    referenceId = mission.id,
                    description = "Ricompensa missione: ${mission.id}",
                )
                soundManager.play(SoundEffect.CHECK_CORRECT)
            }
        }
    }

    private suspend fun updateMissions(
        today: LocalDate,
        matchLog: MatchLog,
        analysis: MatchAnalysis,
        humanSeat: Seat,
        trainingScore: Pair<Int, Int>?,
        trainingFundamental: TrainingFundamental,
    ) {
        val stats = MatchMissionStatsExtractor.extract(matchLog, humanSeat)
        if (stats.sweepsCount > 0) {
            retentionRepository.updateMissionProgress(today, DailyMissionType.MAKE_SWEEPS, stats.sweepsCount)
        }
        if (stats.denariCount > 0) {
            retentionRepository.updateMissionProgress(today, DailyMissionType.CAPTURE_DENARI, stats.denariCount)
        }
        if (analysis.accuracy(humanSeat) >= 80) {
            retentionRepository.updateMissionProgress(today, DailyMissionType.COMPLETE_MATCH_WITH_ACCURACY_80)
        }
        if (analysis.count(humanSeat, MoveGrade.BLUNDER) == 0) {
            retentionRepository.updateMissionProgress(today, DailyMissionType.PLAY_MATCH_WITHOUT_BLUNDERS)
        }
        if (trainingFundamental == TrainingFundamental.SEVENS_COUNT && trainingScore != null) {
            retentionRepository.updateMissionProgress(today, DailyMissionType.ANSWER_SEVENS_CHECKS, trainingScore.first)
        }
    }

    private companion object {
        const val WELCOME_BONUS = 50
        const val RECORDED_KEY = "recorded_matches"
        val TEST_LEVEL = it.maestro.ai.bot.DifficultyLevel.INTERMEDIO
    }
}
