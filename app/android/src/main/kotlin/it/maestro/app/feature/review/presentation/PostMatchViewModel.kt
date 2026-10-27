package it.maestro.app.feature.review.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.app.domain.rewards.RewardSummary
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ReplayResult
import it.maestro.game.scopa.replay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Le due fasi della fine partita (`docs/app/features/post-match.md` §3): finché l'analisi e la ricompensa non sono
 * pronte c'è solo il caricamento; poi la schermata compare intera, tutta insieme.
 */
public enum class PostMatchStep {
    /** Il Maestro "segna i momenti chiave": niente altro è disegnato. */
    LOADING,

    /** Targa, precisione, ricompensa e azioni, tutte insieme. */
    READY,
}

/** Stato della schermata di fine partita. */
public data class PostMatchUiState(
    val step: PostMatchStep = PostMatchStep.LOADING,
    val won: Boolean = false,
    val humanTotal: Int = 0,
    val opponentTotal: Int = 0,
    val analysis: MatchAnalysis? = null,
    val reward: RewardSummary? = null,
    val humanSeat: Seat = Seat.P1,
    val fromHistory: Boolean = false,
    /** Id delle missioni già riscosse da questa schermata. */
    val claimedMissions: Set<String> = emptySet(),
)

/**
 * Conduce la fine partita (task F1R-15a): carica fino a quando l'analisi e, per una partita appena giocata, la
 * ricompensa sono pronte, poi mostra tutto insieme. L'analisi e la ricompensa arrivano da fuori ([analysis],
 * [reward]): la schermata non registra né calcola nulla.
 */
public class PostMatchViewModel(
    matchLog: MatchLog,
    humanSeat: Seat,
    fromHistory: Boolean,
    analysis: Flow<MatchAnalysis?>,
    reward: Flow<RewardSummary?>,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState(matchLog, humanSeat, fromHistory))

    /** Stato corrente. */
    public val uiState: StateFlow<PostMatchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val result = analysis.filterNotNull().first()
            val summary = if (fromHistory) null else reward.filterNotNull().first()
            _uiState.update { it.copy(analysis = result, reward = summary, step = PostMatchStep.READY) }
        }
    }

    /** Segna come riscossa la missione [id]: il pulsante diventa «Riscattata». */
    public fun markClaimed(id: String) {
        _uiState.update { it.copy(claimedMissions = it.claimedMissions + id) }
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
