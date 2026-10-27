package it.maestro.app.feature.review.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.app.designsystem.motion.MotionSpec
import it.maestro.app.designsystem.motion.MotionToken
import it.maestro.app.domain.rewards.RewardSummary
import it.maestro.app.domain.settings.GameSpeed
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ReplayResult
import it.maestro.game.scopa.replay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * I passi della sequenza di fine partita (`docs/app/features/post-match.md` §3): ogni passo è un dato, la
 * schermata disegna quelli raggiunti.
 */
public enum class PostMatchStep {
    /** Targa Vittoria/Sconfitta con il punteggio. */
    PLAQUE,

    /** Il Maestro "segna i momenti chiave" finché l'analisi non è pronta. */
    ANALYSIS,

    /** Precisione e conteggi degli errori. */
    ACCURACY,

    /** Ciò che si è guadagnato: maestria, missioni, streak (F1R-15c). Non c'è per le partite riviste dalla cronologia. */
    REWARD,

    /** Azioni: Rivedi partita, Rivincita, Menu. */
    ACTIONS,
}

/** Stato della schermata di fine partita. */
public data class PostMatchUiState(
    val step: PostMatchStep = PostMatchStep.PLAQUE,
    val won: Boolean = false,
    val humanTotal: Int = 0,
    val opponentTotal: Int = 0,
    val analysis: MatchAnalysis? = null,
    val reward: RewardSummary? = null,
    val humanSeat: Seat = Seat.P1,
    val fromHistory: Boolean = false,
    /** Id delle missioni già riscosse da questa schermata. */
    val claimedMissions: Set<String> = emptySet(),
) {
    /** `true` se la schermata deve già mostrare [target]. */
    public fun reached(target: PostMatchStep): Boolean = step.ordinal >= target.ordinal
}

/**
 * Conduce la sequenza di fine partita (task F1R-15a): targa → attesa dell'analisi → precisione → ricompensa → azioni.
 * L'analisi e la ricompensa arrivano da fuori ([analysis], [reward]): la schermata non registra né calcola nulla.
 * Le attese usano solo [MotionSpec]; [skip] chiude il passo in corso.
 */
public class PostMatchViewModel(
    matchLog: MatchLog,
    humanSeat: Seat,
    fromHistory: Boolean,
    analysis: Flow<MatchAnalysis?>,
    reward: Flow<RewardSummary?>,
    private val speed: GameSpeed = GameSpeed.NORMAL,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState(matchLog, humanSeat, fromHistory))

    /** Stato corrente della sequenza. */
    public val uiState: StateFlow<PostMatchUiState> = _uiState.asStateFlow()

    private var pause: Job? = null

    init {
        viewModelScope.launch { analysis.filterNotNull().collect { result -> _uiState.update { it.copy(analysis = result) } } }
        viewModelScope.launch { reward.filterNotNull().collect { result -> _uiState.update { it.copy(reward = result) } } }
        viewModelScope.launch { run(analysis, reward, fromHistory) }
    }

    /** Segna come riscossa la missione [id]: il pulsante diventa «Riscattata». */
    public fun markClaimed(id: String) {
        _uiState.update { it.copy(claimedMissions = it.claimedMissions + id) }
    }

    /** Un tocco chiude la pausa del passo in corso; sull'attesa dell'analisi non fa nulla. */
    public fun skip() {
        pause?.cancel()
    }

    private suspend fun run(analysis: Flow<MatchAnalysis?>, reward: Flow<RewardSummary?>, fromHistory: Boolean) {
        show(PostMatchStep.PLAQUE, MotionToken.POST_MATCH_PLAQUE)
        show(PostMatchStep.ANALYSIS, token = null)
        analysis.filterNotNull().first()
        show(PostMatchStep.ACCURACY, MotionToken.POST_MATCH_STEP)
        if (!fromHistory) {
            reward.filterNotNull().first()
            show(PostMatchStep.REWARD, MotionToken.POST_MATCH_STEP)
        }
        _uiState.update { it.copy(step = PostMatchStep.ACTIONS) }
    }

    private suspend fun show(step: PostMatchStep, token: MotionToken?) {
        _uiState.update { it.copy(step = step) }
        if (token == null) return
        pause = viewModelScope.launch { MotionSpec.pause(MotionSpec.millis(token, speed)) }
        pause?.join()
    }

    private fun initialState(matchLog: MatchLog, humanSeat: Seat, fromHistory: Boolean): PostMatchUiState {
        val final = (replay(matchLog) as? ReplayResult.Ok)?.state
        val human = final?.totals?.get(humanSeat) ?: 0
        val opponent = final?.totals?.get(humanSeat.other()) ?: 0
        return PostMatchUiState(
            won = final?.winner?.let { it == humanSeat } ?: (human >= opponent),
            humanTotal = human,
            opponentTotal = opponent,
            humanSeat = humanSeat,
            fromHistory = fromHistory,
        )
    }
}
