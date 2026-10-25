package it.maestro.app.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.ai.bot.PimcEvaluator
import it.maestro.ai.bot.SearchBudget
import it.maestro.app.data.repository.MatchRepository
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.viewFor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel MVI per la schermata di gioco della Scopa (ADR-0012, task F1-05, F1-08).
 *
 * Mantiene il flusso unidirezionale dei dati:
 * `UI -> Intent -> Reducer -> State -> UI`
 *
 * Il turno del bot è eseguito rigorosamente fuori dal thread UI (tramite [ioDispatcher],
 * default [Dispatchers.Default]) passando unicamente [viewFor] e [legalMoves].
 */
public class GameViewModel(
    private val matchRepository: MatchRepository? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
    coroutineScope: kotlinx.coroutines.CoroutineScope? = null,
    private val botThinkDelayMillis: Long = if (ioDispatcher == Dispatchers.Unconfined) 0L else 1000L,
) : ViewModel() {

    private val scope: kotlinx.coroutines.CoroutineScope = coroutineScope ?: viewModelScope
    private var internalState: GameInternalState
    private val _uiState: MutableStateFlow<GameUiState>
    public val uiState: StateFlow<GameUiState>

    private var botRng: SeededRng = SeededRng(12345L)
    private var botJob: Job? = null
    private var hintJob: Job? = null

    public val currentMatchLog: MatchLog
        get() = internalState.matchLog

    init {
        val initialResult = GameReducer.initial()
        internalState = initialResult.state
        _uiState = MutableStateFlow(internalState.toUiState())
        uiState = _uiState.asStateFlow()
        handleEffect(initialResult.effect)
    }

    public fun onIntent(intent: GameIntent) {
        if (intent is GameIntent.StartMatch) {
            botRng = SeededRng(intent.seed + 9999L)
            botJob?.cancel()
            hintJob?.cancel()
        } else if (intent is GameIntent.RestoreMatch) {
            botJob?.cancel()
            hintJob?.cancel()
        }
        val result = GameReducer.reduce(internalState, intent)
        internalState = result.state
        _uiState.value = internalState.toUiState()

        // Persistenza automatica della partita (ADR-0003, task F1-08)
        if (matchRepository != null) {
            scope.launch {
                try {
                    val scopaState = internalState.scopaState
                    if (scopaState.phase == DealPhase.PLAYING) {
                        matchRepository.saveActiveMatch(
                            log = internalState.matchLog,
                            level = internalState.botLevel,
                            targetScore = scopaState.config.targetScore,
                            p1Score = scopaState.totals[Seat.P1] ?: 0,
                            p2Score = scopaState.totals[Seat.P2] ?: 0,
                        )
                    } else if (scopaState.phase == DealPhase.MATCH_OVER) {
                        matchRepository.markCompleted(
                            winner = scopaState.winner,
                            p1Score = scopaState.totals[Seat.P1] ?: 0,
                            p2Score = scopaState.totals[Seat.P2] ?: 0,
                        )
                    }
                } catch (_: Throwable) {
                    // Protezione contro errori di I/O o SQLite durante la partita
                }
            }
        }

        handleEffect(result.effect)
    }

    private fun handleEffect(effect: GameEffect?) {
        when (effect) {
            is GameEffect.ScheduleBotMove -> {
                botJob?.cancel()
                botJob = scope.launch {
                    try {
                        if (botThinkDelayMillis > 0L) {
                            kotlinx.coroutines.delay(botThinkDelayMillis)
                        }
                        val botSeat = effect.botSeat
                        val state = internalState.scopaState
                        if (state.phase != DealPhase.PLAYING || state.toMove != botSeat) {
                            return@launch
                        }

                        val bot = effect.level.toBot()
                        val view = viewFor(state, botSeat)
                        val legal = legalMoves(state)
                        if (legal.isEmpty()) {
                            return@launch
                        }

                        val chosenMove = withContext(ioDispatcher) {
                            bot.chooseMove(view = view, legal = legal, rng = botRng)
                        }

                        onIntent(GameIntent.ApplyBotMove(chosenMove))
                    } catch (_: CancellationException) {
                        // Normale annullamento job bot
                    } catch (e: Throwable) {
                        android.util.Log.e("GameViewModel", "Errore esecuzione mossa bot", e)
                    }
                }
            }
            is GameEffect.ComputeHint -> {
                hintJob?.cancel()
                hintJob = scope.launch {
                    try {
                        val state = internalState.scopaState
                        if (state.phase != DealPhase.PLAYING || state.toMove != effect.humanSeat) {
                            onIntent(GameIntent.ClearHint)
                            return@launch
                        }

                        val humanView = viewFor(state, effect.humanSeat)
                        val legal = legalMoves(state)
                        if (legal.isEmpty()) {
                            onIntent(GameIntent.ClearHint)
                            return@launch
                        }

                        val bestMove = withContext(ioDispatcher) {
                            val evaluations = PimcEvaluator.evaluate(
                                view = humanView,
                                legal = legal,
                                rng = SeededRng(state.rngState xor 0x7E7E7EL),
                                budget = SearchBudget(samples = 15, maxNodes = 60),
                            )
                            evaluations.maxByOrNull { it.value }?.move ?: legal.firstOrNull()
                        }
                        if (bestMove != null) {
                            onIntent(GameIntent.ApplyHint(bestMove))
                        } else {
                            onIntent(GameIntent.ClearHint)
                        }
                    } catch (_: CancellationException) {
                        // Normale annullamento job suggerimento
                    } catch (e: Throwable) {
                        android.util.Log.e("GameViewModel", "Errore calcolo suggerimento", e)
                        onIntent(GameIntent.ClearHint)
                    }
                }
            }
            null -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        botJob?.cancel()
        hintJob?.cancel()
    }
}
