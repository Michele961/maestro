package it.maestro.app.feature.review.presentation

import androidx.lifecycle.ViewModel
import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MoveGrade
import it.maestro.app.R
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ScopaMove
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import it.maestro.app.feature.review.ui.ReviewStep
import it.maestro.app.feature.review.ui.buildReviewSteps
import it.maestro.app.feature.review.ui.findNextMistake
import it.maestro.app.feature.review.ui.findPreviousMistake

/**
 * Feedback restituito dal Maestro al giocatore durante la modalità "Riprova la mano" (task F2-01).
 */
public sealed interface RetryFeedback {
    /**
     * Mossa corretta: il giocatore ha individuato la mossa migliore (o di pari valore atteso).
     *
     * @property messageRes Identificativo risorsa stringa del messaggio di congratulazioni.
     * @property bestMove La mossa migliore identificata dall'analizzatore.
     */
    public data class Correct(
        val messageRes: Int,
        val bestMove: ScopaMove,
    ) : RetryFeedback

    /**
     * Mossa imperfetta: il giocatore ha giocato una carta subottimale; riceve uno stimolo e un suggerimento.
     *
     * @property messageRes Identificativo risorsa stringa del messaggio di riprova.
     * @property hintRes Identificativo risorsa stringa del suggerimento didattico.
     */
    public data class Imperfect(
        val messageRes: Int,
        val hintRes: Int,
    ) : RetryFeedback
}

/**
 * Stato della UI per la schermata di Game Review e la modalità interattiva "Riprova la mano" (task F2-01, F1-10).
 *
 * Rispetta rigorosamente i criteri di sicurezza dell'informazione:
 * le carte dell'avversario non sono presenti e non vengono mai esposte all'utente prima o durante il gioco.
 */
public data class GameReviewUiState(
    val steps: List<ReviewStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val isRetryMode: Boolean = false,
    val retryFeedback: RetryFeedback? = null,
    val solvedStepIndices: Set<Int> = emptySet(),
    val revealedStepIndices: Set<Int> = emptySet(),
    val selectedRetryCard: Card? = null,
    val humanSeat: Seat = Seat.P1,
    val matchSeed: Long = 0L,
) {
    public val currentStep: ReviewStep?
        get() = steps.getOrNull(currentStepIndex)

    public val isCurrentStepSolved: Boolean
        get() = currentStepIndex in solvedStepIndices

    public val isCurrentStepRevealed: Boolean
        get() = currentStepIndex in revealedStepIndices
}

/**
 * ViewModel per la Game Review e la funzione didattica "Rigioca l'errore" (task F2-01, ADR-0005, ADR-0012).
 */
public class GameReviewViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameReviewUiState())
    public val uiState: StateFlow<GameReviewUiState> = _uiState.asStateFlow()

    /**
     * Inizializza gli step di review a partire da [matchLog], [analysis] e [humanSeat].
     */
    public fun initialize(
        matchLog: MatchLog,
        analysis: MatchAnalysis,
        humanSeat: Seat = Seat.P1,
    ) {
        if (_uiState.value.matchSeed == matchLog.seed && _uiState.value.steps.isNotEmpty()) {
            return
        }

        val steps = buildReviewSteps(matchLog, analysis, humanSeat)
        _uiState.value = GameReviewUiState(
            steps = steps,
            currentStepIndex = 0,
            isRetryMode = false,
            retryFeedback = null,
            solvedStepIndices = emptySet(),
            revealedStepIndices = emptySet(),
            selectedRetryCard = null,
            humanSeat = humanSeat,
            matchSeed = matchLog.seed,
        )
    }

    /**
     * Svela la soluzione e l'analisi per lo step corrente.
     */
    public fun revealSolution() {
        _uiState.update { current ->
            current.copy(
                revealedStepIndices = current.revealedStepIndices + current.currentStepIndex,
            )
        }
    }

    /**
     * Seleziona uno step specifico tramite indice [index].
     */
    public fun selectStep(index: Int) {
        _uiState.update { current ->
            if (current.steps.isEmpty()) return@update current
            val clamped = index.coerceIn(0, current.steps.size - 1)
            current.copy(
                currentStepIndex = clamped,
                isRetryMode = false,
                retryFeedback = null,
                selectedRetryCard = null,
            )
        }
    }

    /**
     * Sposta la visualizzazione allo step precedente.
     */
    public fun previousStep() {
        _uiState.update { current ->
            if (current.steps.isEmpty() || current.currentStepIndex <= 0) return@update current
            current.copy(
                currentStepIndex = current.currentStepIndex - 1,
                isRetryMode = false,
                retryFeedback = null,
                selectedRetryCard = null,
            )
        }
    }

    /**
     * Sposta la visualizzazione allo step successivo.
     */
    public fun nextStep() {
        _uiState.update { current ->
            if (current.steps.isEmpty() || current.currentStepIndex >= current.steps.size - 1) return@update current
            current.copy(
                currentStepIndex = current.currentStepIndex + 1,
                isRetryMode = false,
                retryFeedback = null,
                selectedRetryCard = null,
            )
        }
    }

    /**
     * Salta all'errore precedente commesso da [seat].
     */
    public fun previousMistake(seat: Seat = Seat.P1) {
        _uiState.update { current ->
            val prev = findPreviousMistake(current.currentStepIndex, current.steps, seat)
            current.copy(
                currentStepIndex = prev,
                isRetryMode = false,
                retryFeedback = null,
                selectedRetryCard = null,
            )
        }
    }

    /**
     * Salta all'errore successivo commesso da [seat].
     */
    public fun nextMistake(seat: Seat = Seat.P1) {
        _uiState.update { current ->
            val next = findNextMistake(current.currentStepIndex, current.steps, seat)
            current.copy(
                currentStepIndex = next,
                isRetryMode = false,
                retryFeedback = null,
                selectedRetryCard = null,
            )
        }
    }

    /**
     * Attiva la modalità interattiva "Riprova la mano" per lo step corrente.
     */
    public fun startRetry() {
        _uiState.update { current ->
            val step = current.currentStep ?: return@update current
            if (step.analysis.grade == MoveGrade.BEST && !current.isCurrentStepSolved) {
                return@update current
            }
            current.copy(
                isRetryMode = true,
                retryFeedback = null,
                selectedRetryCard = null,
            )
        }
    }

    /**
     * Esce dalla modalità "Riprova la mano" tornando alla visualizzazione standard della review.
     */
    public fun exitRetry() {
        _uiState.update { current ->
            current.copy(
                isRetryMode = false,
                retryFeedback = null,
                selectedRetryCard = null,
            )
        }
    }

    /**
     * Seleziona e gioca una carta nella modalità "Riprova la mano", valutando se corrisponde
     * alla mossa migliore o a una scelta imperfetta.
     */
    public fun playRetryCard(card: Card) {
        _uiState.update { current ->
            val step = current.currentStep ?: return@update current
            val isHumanMistake = step.mover == current.humanSeat && step.analysis.grade != MoveGrade.BEST
            if (!current.isRetryMode && !isHumanMistake) return@update current

            val bestValue = step.analysis.bestValue
            val bestMove = step.analysis.bestMove
            val alternatives = step.analysis.alternatives

            val matchingMoves = alternatives.filter { it.move.card == card }
            val cardBestValue = matchingMoves.maxOfOrNull { it.value } ?: Double.NEGATIVE_INFINITY

            val isOptimal = cardBestValue >= bestValue - 1e-6

            if (isOptimal) {
                current.copy(
                    isRetryMode = true,
                    solvedStepIndices = current.solvedStepIndices + current.currentStepIndex,
                    revealedStepIndices = current.revealedStepIndices + current.currentStepIndex,
                    retryFeedback = RetryFeedback.Correct(
                        messageRes = R.string.review_retry_correct,
                        bestMove = bestMove,
                    ),
                    selectedRetryCard = card,
                )
            } else {
                val hintRes = determineHint(step, card)
                current.copy(
                    isRetryMode = true,
                    retryFeedback = RetryFeedback.Imperfect(
                        messageRes = R.string.review_retry_imperfect,
                        hintRes = hintRes,
                    ),
                    selectedRetryCard = card,
                )
            }
        }
    }

    private fun determineHint(step: ReviewStep, playedCard: Card): Int {
        val bestMove = step.analysis.bestMove
        val table = step.playerView.table

        return when {
            table.isNotEmpty() && bestMove.capture.size == table.size -> {
                R.string.review_hint_scopa
            }
            bestMove.capture.any { it.suit == Suit.DENARI && it.rank == Rank.SETTE } ||
                (bestMove.card.suit == Suit.DENARI && bestMove.card.rank == Rank.SETTE) -> {
                R.string.review_hint_settebello
            }
            bestMove.capture.any { it.rank == Rank.SETTE } -> {
                R.string.review_hint_primiera_sette
            }
            bestMove.capture.any { it.suit == Suit.DENARI } || bestMove.card.suit == Suit.DENARI -> {
                R.string.review_hint_denari
            }
            bestMove.capture.isNotEmpty() && step.analysis.alternatives.filter { it.move.card == playedCard }.all { it.move.capture.isEmpty() } -> {
                R.string.review_hint_capture_available
            }
            bestMove.capture.isEmpty() -> {
                R.string.review_hint_safe_discard
            }
            else -> {
                R.string.review_hint_generic
            }
        }
    }
}
