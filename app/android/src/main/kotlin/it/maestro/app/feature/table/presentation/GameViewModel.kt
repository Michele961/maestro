package it.maestro.app.feature.table.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.domain.match.MatchRepository
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.MatchLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import it.maestro.app.domain.match.RestoreMatchResult
import it.maestro.game.training.TrainingFundamental
import it.maestro.app.designsystem.motion.MotionSpec
import it.maestro.app.domain.settings.GameSpeed
import it.maestro.app.domain.coach.Chattiness
import it.maestro.app.domain.match.Beat
import it.maestro.app.domain.match.PresentationGate
import it.maestro.app.feature.table.presentation.director.TableDirector
import it.maestro.app.feature.table.presentation.director.TableScene
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * ViewModel MVI per la schermata di gioco della Scopa (ADR-0012, task F1-05, F1-08).
 *
 * Mantiene il flusso unidirezionale dei dati:
 * `UI -> Intent -> Reducer -> State -> UI`
 *
 * Il turno del bot è eseguito fuori dal thread UI (tramite [ioDispatcher], default [Dispatchers.Default])
 * e passa da [it.maestro.app.domain.match.MatchSession.chooseBotMove], che gli dà solo la sua vista.
 */
public class GameViewModel(
    private val matchRepository: MatchRepository? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
    coroutineScope: kotlinx.coroutines.CoroutineScope? = null,
    // Il bot aspetta la fine della scena (ADR-0015): per default è la regia del tavolo.
    // Senza interfaccia (test con Unconfined) il cancello è aperto e la regia non aspetta.
    presentationGate: PresentationGate? = null,
    gameSpeed: Flow<GameSpeed> = flowOf(GameSpeed.NORMAL),
    chattiness: Flow<Chattiness> = flowOf(Chattiness.NORMAL),
    hintEverUsed: Flow<Boolean> = flowOf(false),
    private val onHintUsed: suspend () -> Unit = {},
) : ViewModel() {

    private val scope: kotlinx.coroutines.CoroutineScope = coroutineScope ?: viewModelScope
    private var internalState: GameInternalState
    private val _uiState: MutableStateFlow<GameUiState>
    public val uiState: StateFlow<GameUiState>

    private val withoutUi = ioDispatcher == Dispatchers.Unconfined
    private var currentSpeed: GameSpeed = GameSpeed.NORMAL
    private var currentChattiness: Chattiness = Chattiness.NORMAL
    private var hintUsedBefore: Boolean = false
    private var voice: MaestroVoice = MaestroVoice(seed = 0L, human = Seat.P1)
    private var botRng: SeededRng = SeededRng(12345L)
    private var botJob: Job? = null
    private var hintJob: Job? = null

    public val currentMatchLog: MatchLog
        get() = internalState.matchLog

    private val director = MutableStateFlow<TableDirector?>(null)
    private val gate: PresentationGate = presentationGate ?: object : PresentationGate {
        override suspend fun awaitPresented(moveId: Long, dealId: Long) {
            if (withoutUi) return
            director.value?.awaitPresented(moveId, dealId)
        }
    }

    /** Scena da disegnare (`docs/app/table-director.md` §2); cambia regia a ogni nuova partita. */
    @OptIn(ExperimentalCoroutinesApi::class)
    public val scene: Flow<TableScene> = director.filterNotNull().flatMapLatest { it.scene }

    /** Scena corrente, per il valore iniziale di [scene]. */
    public val currentScene: TableScene
        get() = checkNotNull(director.value) { "Regia non ancora creata" }.scene.value

    /** Un tocco sul tavolo durante una scena la salta (`table-director.md` §4.3). */
    public fun skipScene() {
        director.value?.skip()
    }

    /** Il giocatore chiude il riepilogo di smazzata: la partita prosegue e la regia riprende (nuova distribuzione). */
    public fun continueAfterSummary() {
        // La mano guidata finisce qui: la partita non prosegue, resta il saluto del Maestro (F1G-02).
        if (internalState.guide != null) return onIntent(GameIntent.FinishGuide)
        onIntent(GameIntent.DismissDealSummary)
        director.value?.resume()
    }

    /** Il Maestro commenta la partita (solo Sparring): saluto a inizio partita, poi una battuta dopo certe mosse. */
    private fun updateMaestroVoice(intent: GameIntent, previousBeat: Beat?) {
        val state = internalState
        when (intent) {
            is GameIntent.StartMatch -> {
                voice = MaestroVoice(intent.seed, state.humanSeat)
                if (state.isSparring && state.guide == null) voice.greeting(currentChattiness, hintUsedBefore)?.let { internalState = state.copy(maestroLine = it) }
            }
            is GameIntent.RestoreMatch -> voice = MaestroVoice(intent.session.log.seed + intent.session.log.moves.size, state.humanSeat)
            is GameIntent.RequestHint -> if (!hintUsedBefore) {
                hintUsedBefore = true
                scope.launch { onHintUsed() }
            }
            else -> {
                val beat = state.lastBeat?.takeIf { it !== previousBeat } ?: return
                if (state.isSparring && state.guide == null) {
                    voice.onBeat(beat, state.session, currentChattiness, hintUsedBefore)?.let { internalState = state.copy(maestroLine = it) }
                }
            }
        }
    }

    private fun resetDirector(restoreAt: Long?) {
        val human = internalState.humanSeat
        val view = internalState.session.view(human)
        director.value = TableDirector(
            scope = scope,
            humanSeat = human,
            initialView = view,
            speed = { if (withoutUi) GameSpeed.INSTANT else currentSpeed },
            waitForPlayer = !withoutUi,
        ).also { if (restoreAt != null) it.rest(view, restoreAt) }
    }

    /** `true` dopo l'avvio o la ripresa di una partita: dopo una ricreazione la schermata non la riavvia. */
    public var hasMatch: Boolean = false
        private set

    /**
     * Riprende la partita salvata (ADR-0003, task F1R-04). Se non è rigiocabile [onUnavailable] riceve il
     * controllo, ad esempio per tornare alla Home.
     */
    public fun resumeActiveMatch(
        onUnavailable: () -> Unit = {},
    ) {
        val repository = matchRepository ?: return onUnavailable()
        hasMatch = true
        scope.launch {
            when (val result = repository.restoreActiveMatch()) {
                is RestoreMatchResult.Success -> onIntent(
                    GameIntent.RestoreMatch(
                        session = result.session,
                        level = result.level,
                        trainingFundamental = result.trainingFundamental,
                        sparring = result.trainingFundamental != TrainingFundamental.NONE,
                    ),
                )
                else -> {
                    hasMatch = false
                    onUnavailable()
                }
            }
        }
    }

    init {
        scope.launch { gameSpeed.collect { currentSpeed = it } }
        scope.launch { chattiness.collect { currentChattiness = it } }
        scope.launch { hintEverUsed.collect { hintUsedBefore = it } }
        val initialResult = GameReducer.initial()
        internalState = initialResult.state
        _uiState = MutableStateFlow(internalState.toUiState())
        uiState = _uiState.asStateFlow()
        resetDirector(restoreAt = null)
        handleEffect(initialResult.effect)
    }

    public fun onIntent(intent: GameIntent) {
        if (intent is GameIntent.StartMatch || intent is GameIntent.RestoreMatch) hasMatch = true
        if (intent is GameIntent.StartMatch) {
            botRng = SeededRng(intent.seed + 9999L)
            botJob?.cancel()
            hintJob?.cancel()
        } else if (intent is GameIntent.RestoreMatch) {
            // Ripresa: generatore del bot derivato dal log, così resta riproducibile (ADR-0003, F1R-02).
            botRng = SeededRng(intent.session.log.seed + 9999L + intent.session.log.moves.size)
            botJob?.cancel()
            hintJob?.cancel()
        }
        val previousBeat = internalState.lastBeat
        val result = GameReducer.reduce(internalState, intent)
        internalState = result.state
        when (intent) {
            is GameIntent.StartMatch -> {
                resetDirector(restoreAt = null)
                director.value?.enqueueOpeningDeal(internalState.session.view(internalState.humanSeat))
            }
            is GameIntent.RestoreMatch -> resetDirector(restoreAt = internalState.lastMoveId)
            else -> internalState.lastBeat?.takeIf { it !== previousBeat }?.let { director.value?.enqueue(it) }
        }
        updateMaestroVoice(intent, previousBeat)
        _uiState.value = internalState.toUiState()

        // Persistenza automatica della partita (ADR-0003, task F1-08). La mano guidata non si salva: non è una partita.
        if (matchRepository != null && internalState.guide == null) {
            scope.launch {
                try {
                    val session = internalState.session
                    if (session.phase == DealPhase.PLAYING) {
                        matchRepository.saveActiveMatch(
                            log = internalState.matchLog,
                            level = internalState.botLevel,
                            targetScore = session.config.targetScore,
                            p1Score = session.totals[Seat.P1] ?: 0,
                            p2Score = session.totals[Seat.P2] ?: 0,
                            trainingFundamental = internalState.trainingFundamental,
                        )
                    } else if (session.phase == DealPhase.MATCH_OVER) {
                        matchRepository.markCompleted(
                            winner = session.winner,
                            p1Score = session.totals[Seat.P1] ?: 0,
                            p2Score = session.totals[Seat.P2] ?: 0,
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
                        // Prima la scena finisce, poi il bot "pensa" in proporzione alle sue scelte (ADR-0015).
                        gate.awaitPresented(internalState.lastMoveId, internalState.dealAnimationId)
                        if (ioDispatcher != Dispatchers.Unconfined) {
                            val choices = internalState.session.legalMovesFor(effect.botSeat).size
                            MotionSpec.pause(MotionSpec.botThinkMillis(choices, currentSpeed))
                        }
                        val session = internalState.session
                        val chosenMove = withContext(ioDispatcher) {
                            session.chooseBotMove(effect.level.toBot(), effect.botSeat, botRng)
                        } ?: return@launch

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
                        val session = internalState.session
                        val bestMove = withContext(ioDispatcher) { session.hint(effect.humanSeat) }
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
