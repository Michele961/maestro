package it.maestro.app.feature.table.presentation

import it.maestro.app.domain.match.MatchSession
import it.maestro.app.domain.match.SessionPlay
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.DealScore
import it.maestro.game.scopa.GameEvent
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.training.TrainingFundamental
import it.maestro.game.training.buildTrainingQuestion
import it.maestro.game.training.isCriticalTrainingMoment


/**
 * Reducer puro per la gestione dello stato della partita di Scopa (ADR-0012, task F1-05).
 *
 * È privo di dipendenze dalla piattaforma Android e può essere testato interamente
 * come funzione pura `(State, Intent) -> (State, Effect?)`.
 */
public object GameReducer {

    public fun initial(intent: GameIntent.StartMatch = GameIntent.StartMatch()): GameReducerResult {
        val state = MatchSession.start(config = intent.config, seed = intent.seed)
        val internalState = GameInternalState(
            session = state,
            humanSeat = intent.humanSeat,
            botLevel = intent.level,
            pendingCapture = null,
            isBotThinking = false,
            trainingFundamental = intent.trainingFundamental,
            lastError = null,
            lastEvent = null,
            dealAnimationId = 1L,
            isInitialDeal = true,
        )
        val effect = if (state.toMove != intent.humanSeat && state.phase == DealPhase.PLAYING) {
            GameEffect.ScheduleBotMove(
                level = intent.level,
                botSeat = intent.humanSeat.other(),
            )
        } else {
            null
        }
        return GameReducerResult(
            state = internalState.copy(isBotThinking = effect != null),
            effect = effect,
        )
    }

    public fun reduce(state: GameInternalState, intent: GameIntent): GameReducerResult {
        return when (intent) {
            is GameIntent.StartMatch -> initial(intent)

            is GameIntent.RestoreMatch -> handleRestoreMatch(intent)

            is GameIntent.PlayCard -> handlePlayCard(state, intent)

            is GameIntent.SelectCapture -> handleSelectCapture(state, intent)

            is GameIntent.CancelCaptureChoice -> {
                GameReducerResult(state.copy(pendingCapture = null, lastError = null))
            }

            is GameIntent.DismissDealSummary -> handleDismissDealSummary(state)

            is GameIntent.RequestBotMove -> handleRequestBotMove(state)

            is GameIntent.ApplyBotMove -> handleApplyBotMove(state, intent)

            is GameIntent.RequestHint -> handleRequestHint(state)

            is GameIntent.ApplyHint -> {
                GameReducerResult(
                    state = state.copy(suggestedMove = intent.move, isHintLoading = false),
                )
            }

            is GameIntent.ClearHint -> {
                GameReducerResult(
                    state = state.copy(suggestedMove = null, isHintLoading = false),
                )
            }

            is GameIntent.SelectTrainingFundamental -> {
                GameReducerResult(state.copy(trainingFundamental = intent.fundamental))
            }

            is GameIntent.AnswerTrainingQuestion -> handleAnswerTrainingQuestion(state, intent)

            is GameIntent.DismissTrainingQuestion -> {
                GameReducerResult(state.copy(activeTrainingQuestion = null, trainingQuestionFeedback = null))
            }

            is GameIntent.DismissError -> {
                GameReducerResult(state.copy(lastError = null))
            }

            is GameIntent.DismissEvent -> {
                GameReducerResult(state.copy(lastEvent = null))
            }
        }
    }

    private fun handleRequestHint(state: GameInternalState): GameReducerResult {
        if (!state.isSparring ||
            state.session.phase != DealPhase.PLAYING ||
            state.session.toMove != state.humanSeat ||
            state.pendingDealScore != null
        ) {
            return GameReducerResult(state)
        }
        return GameReducerResult(
            state = state.copy(isHintLoading = true, suggestedMove = null),
            effect = GameEffect.ComputeHint(humanSeat = state.humanSeat),
        )
    }

    private fun handlePlayCard(state: GameInternalState, intent: GameIntent.PlayCard): GameReducerResult {
        if (state.activeTrainingQuestion != null) {
            return GameReducerResult(state.copy(lastError = "Rispondi prima alla domanda del Maestro"))
        }
        if (state.session.phase != DealPhase.PLAYING) {
            return GameReducerResult(state.copy(lastError = "Partita non in corso"))
        }
        if (state.session.toMove != state.humanSeat) {
            return GameReducerResult(state.copy(lastError = "Non è il tuo turno"))
        }

        val legal = state.session.legalMovesFor(state.humanSeat).filter { it.card == intent.card }
        if (legal.isEmpty()) {
            return GameReducerResult(state.copy(lastError = "Mossa non valida: carta non giocabile"))
        }

        if (legal.size == 1) {
            return applyMove(state, legal.first())
        }

        // Più opzioni di presa per la stessa carta: mostra la selezione all'utente
        val pendingChoice = PendingCaptureChoice(
            card = intent.card,
            options = legal.map { it.capture },
        )
        return GameReducerResult(state.copy(pendingCapture = pendingChoice, lastError = null))
    }

    private fun handleSelectCapture(state: GameInternalState, intent: GameIntent.SelectCapture): GameReducerResult {
        if (state.activeTrainingQuestion != null) {
            return GameReducerResult(state.copy(lastError = "Rispondi prima alla domanda del Maestro"))
        }
        if (state.session.phase != DealPhase.PLAYING || state.session.toMove != state.humanSeat) {
            return GameReducerResult(state.copy(lastError = "Non è il tuo turno"))
        }
        val move = ScopaMove(card = intent.card, capture = intent.capture)
        val legal = state.session.legalMovesFor(state.humanSeat)
        if (move !in legal) {
            return GameReducerResult(state.copy(lastError = "Mossa non valida: combinazione di presa non consentita"))
        }
        return applyMove(state.copy(pendingCapture = null), move)
    }

    private fun handleDismissDealSummary(state: GameInternalState): GameReducerResult {
        val isNewDeal = state.session.phase == DealPhase.PLAYING
        val nextDealAnimId = if (isNewDeal) state.dealAnimationId + 1L else state.dealAnimationId
        val cleared = state.copy(
            pendingDealScore = null,
            hasAskedQuestionThisDeal = false,
            activeTrainingQuestion = null,
            trainingQuestionFeedback = null,
            opponentLastMove = null,
            dealAnimationId = nextDealAnimId,
            isInitialDeal = isNewDeal,
            sweepCards = if (isNewDeal) emptyMap() else state.sweepCards,
        )
        val botSeat = state.humanSeat.other()
        val shouldScheduleBot = cleared.session.phase == DealPhase.PLAYING && cleared.session.toMove == botSeat
        val effect = if (shouldScheduleBot) {
            GameEffect.ScheduleBotMove(
                level = state.botLevel,
                botSeat = botSeat,
            )
        } else {
            null
        }
        return GameReducerResult(
            state = cleared.copy(isBotThinking = shouldScheduleBot),
            effect = effect,
        )
    }

    private fun handleRestoreMatch(intent: GameIntent.RestoreMatch): GameReducerResult {
        val internalState = GameInternalState(
            session = intent.session,
            humanSeat = intent.humanSeat,
            botLevel = intent.level,
            trainingFundamental = intent.trainingFundamental,
        )
        val botSeat = intent.humanSeat.other()
        val botPlays = intent.session.phase == DealPhase.PLAYING && intent.session.toMove == botSeat
        val effect = if (botPlays) {
            GameEffect.ScheduleBotMove(
                level = intent.level,
                botSeat = botSeat,
            )
        } else {
            null
        }
        return GameReducerResult(
            state = internalState.copy(isBotThinking = botPlays),
            effect = effect,
        )
    }

    private fun handleAnswerTrainingQuestion(
        state: GameInternalState,
        intent: GameIntent.AnswerTrainingQuestion,
    ): GameReducerResult {
        val question = state.activeTrainingQuestion ?: return GameReducerResult(state)
        val isCorrect = intent.selectedIndex == question.correctIndex
        val feedback = TrainingAnswerFeedback(
            selectedIndex = intent.selectedIndex,
            isCorrect = isCorrect,
            correctIndex = question.correctIndex,
            explanationValues = question.explanationValues,
        )
        val result = TrainingCheckResult(
            fundamental = question.fundamental,
            question = question,
            selectedIndex = intent.selectedIndex,
            isCorrect = isCorrect,
        )
        return GameReducerResult(
            state = state.copy(
                trainingQuestionFeedback = feedback,
                trainingResults = state.trainingResults + result,
            ),
        )
    }

    private fun handleRequestBotMove(state: GameInternalState): GameReducerResult {
        if (state.session.phase != DealPhase.PLAYING) {
            return GameReducerResult(state.copy(isBotThinking = false))
        }
        val botSeat = state.humanSeat.other()
        if (state.session.toMove != botSeat) {
            return GameReducerResult(state.copy(isBotThinking = false))
        }
        return GameReducerResult(
            state = state.copy(isBotThinking = true),
            effect = GameEffect.ScheduleBotMove(
                level = state.botLevel,
                botSeat = botSeat,
            ),
        )
    }

    private fun handleApplyBotMove(state: GameInternalState, intent: GameIntent.ApplyBotMove): GameReducerResult {
        val botSeat = state.humanSeat.other()
        if (state.session.toMove != botSeat) {
            return GameReducerResult(state.copy(isBotThinking = false))
        }
        return applyMove(state.copy(isBotThinking = false), intent.move)
    }

    private fun applyMove(state: GameInternalState, move: ScopaMove): GameReducerResult {
        return when (val result = state.session.play(move, state.humanSeat)) {
            is SessionPlay.Applied -> {
                val mover = result.beat.actor
                val nextMoveId = state.lastMoveId + 1L
                val next = result.session
                var dealScore: DealScore? = null
                var lastEvent: UiGameEvent? = null
                var sweepCards = state.sweepCards
                for (event in result.beat.events) {
                    when (event) {
                        is GameEvent.Sweep -> {
                            val previousSweeps = sweepCards[event.seat]?.size ?: 0
                            val count = previousSweeps + 1
                            lastEvent = UiGameEvent.Sweep(event.seat, sweepCountThisDeal = count)
                            sweepCards = sweepCards + (event.seat to sweepCards[event.seat].orEmpty() + move.card)
                        }
                        is GameEvent.DealEnded -> {
                            dealScore = event.score
                            lastEvent = UiGameEvent.DealEnded(event.score)
                        }
                        is GameEvent.MatchEnded -> lastEvent = UiGameEvent.MatchEnded(event.winner, event.totals)
                        else -> {}
                    }
                }

                val isDealOver = dealScore != null
                val botSeat = state.humanSeat.other()
                val nextPhase = next.phase
                val nextToMove = next.toMove
                val isBotMove = (mover == botSeat)
                val hadCardsDealt = result.beat.events.any { it is GameEvent.CardsDealt }
                val nextDealAnimId = if (hadCardsDealt) state.dealAnimationId + 1L else state.dealAnimationId
                val newOpponentLastMove = if (isBotMove) {
                    OpponentMoveSummary(
                        card = move.card,
                        capture = move.capture,
                        isSweep = result.beat.events.any { it is GameEvent.Sweep },
                    )
                } else {
                    state.opponentLastMove
                }

                // Verifica se attivare la domanda di allenamento per il giocatore umano
                var activeQuestion: it.maestro.game.training.TrainingQuestion? = null
                var questionAskedNow = false
                if (!isDealOver && nextPhase == DealPhase.PLAYING && nextToMove == state.humanSeat &&
                    state.isSparring && state.trainingFundamental != TrainingFundamental.NONE && !state.hasAskedQuestionThisDeal
                ) {
                    val view = result.beat.viewAfter
                    if (isCriticalTrainingMoment(state.trainingFundamental, view)) {
                        activeQuestion = buildTrainingQuestion(state.trainingFundamental, view)
                        questionAskedNow = true
                    }
                }

                val newState = state.copy(
                    session = next,
                    pendingCapture = null,
                    pendingDealScore = dealScore ?: state.pendingDealScore,
                    suggestedMove = null,
                    isHintLoading = false,
                    activeTrainingQuestion = activeQuestion ?: state.activeTrainingQuestion,
                    hasAskedQuestionThisDeal = if (isDealOver) false else (state.hasAskedQuestionThisDeal || questionAskedNow),
                    lastError = null,
                    lastEvent = lastEvent ?: state.lastEvent,
                    sweepCards = sweepCards,
                    lastMove = move,
                    lastMover = mover,
                    lastMoveId = nextMoveId,
                    opponentLastMove = newOpponentLastMove,
                    dealAnimationId = nextDealAnimId,
                    isInitialDeal = if (hadCardsDealt) false else state.isInitialDeal,
                )

                val nextEffect = if (dealScore == null && nextPhase == DealPhase.PLAYING && nextToMove == botSeat) {
                    GameEffect.ScheduleBotMove(
                        level = state.botLevel,
                        botSeat = botSeat,
                    )
                } else {
                    null
                }

                GameReducerResult(
                    state = newState.copy(isBotThinking = nextEffect != null),
                    effect = nextEffect,
                )
            }
            is SessionPlay.Rejected -> {
                // Mossa rifiutata: produce un errore mostrabile, mai un crash (criterio di accettazione F1-05)
                GameReducerResult(
                    state = state.copy(
                        lastError = "Mossa rifiutata: ${result.reason}",
                        pendingCapture = null,
                        isBotThinking = false,
                    ),
                    effect = null,
                )
            }
        }
    }
}
