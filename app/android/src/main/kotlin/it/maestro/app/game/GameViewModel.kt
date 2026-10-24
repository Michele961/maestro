package it.maestro.app.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.data.repository.MatchRepository
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.viewFor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
) : ViewModel() {

    private val scope: kotlinx.coroutines.CoroutineScope = coroutineScope ?: viewModelScope
    private var internalState: GameInternalState
    private val _uiState: MutableStateFlow<GameUiState>
    public val uiState: StateFlow<GameUiState>

    private var botRng: SeededRng = SeededRng(12345L)

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
        }
        val result = GameReducer.reduce(internalState, intent)
        internalState = result.state
        _uiState.value = internalState.toUiState()

        // Persistenza automatica della partita (ADR-0003, task F1-08)
        if (matchRepository != null) {
            scope.launch {
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
            }
        }

        handleEffect(result.effect)
    }

    private fun handleEffect(effect: GameEffect?) {
        when (effect) {
            is GameEffect.ScheduleBotMove -> {
                scope.launch {
                    val bot = effect.level.toBot()
                    val botSeat = effect.botSeat
                    // Il bot riceve SOLO viewFor e legalMoves, mai lo stato globale (ADR-0012, task F1-05)
                    val view = viewFor(internalState.scopaState, botSeat)
                    val legal = legalMoves(internalState.scopaState)

                    val chosenMove = withContext(ioDispatcher) {
                        bot.chooseMove(view = view, legal = legal, rng = botRng)
                    }

                    onIntent(GameIntent.ApplyBotMove(chosenMove))
                }
            }
            null -> {}
        }
    }
}
