package it.maestro.app.feature.table.presentation

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.game.model.Card
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.DealScore
import it.maestro.game.scopa.MatchLog
import it.maestro.app.domain.coach.HintComposer
import it.maestro.app.domain.coach.HintLine
import it.maestro.app.domain.coach.VoiceLine
import it.maestro.app.domain.guide.GuideLine
import it.maestro.app.domain.guide.GuideStep
import it.maestro.app.domain.guide.GuideTone
import it.maestro.app.domain.guide.GuidedHand
import it.maestro.app.domain.match.Beat
import it.maestro.app.domain.match.MatchSession
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView
import it.maestro.game.training.TrainingFundamental
import it.maestro.game.training.TrainingQuestion

/**
 * Riepilogo dell'ultima mossa effettuata dall'avversario per il registro e l'HUD a schermo.
 */
public data class OpponentMoveSummary(
    val card: Card,
    val capture: Set<Card>,
    val isSweep: Boolean,
)

/**
 * Risultato memorizzato di un quesito di allenamento completato durante la partita (task F1-12).
 */
public data class TrainingCheckResult(
    val fundamental: TrainingFundamental,
    val question: TrainingQuestion,
    val selectedIndex: Int,
    val isCorrect: Boolean,
)

/**
 * Feedback mostrato all'utente subito dopo aver risposto al quesito di allenamento (task F1-12).
 * Se [retryAvailable] il Maestro dà un indizio e la domanda resta aperta per un secondo tentativo, senza le
 * risposte già provate ([triedIndexes]); l'esito per il punteggio di attenzione è quello del primo tentativo.
 */
public data class TrainingAnswerFeedback(
    val selectedIndex: Int,
    val isCorrect: Boolean,
    val correctIndex: Int,
    val explanationValues: List<Int> = emptyList(),
    val retryAvailable: Boolean = false,
    val triedIndexes: Set<Int> = setOf(selectedIndex),
)

/**
 * Rappresenta la scelta di presa multipla per il giocatore umano quando una carta giocata

 * può effettuare più prese distinte dal tavolo (spec §4.2–4.3, ADR-0012, task F1-05).
 *
 * @property card Carta giocata dalla mano che ha generato la scelta.
 * @property options Lista di insiemi alternativi di carte sul tavolo catturabili.
 */
public data class PendingCaptureChoice(
    val card: Card,
    val options: List<Set<Card>>,
)

/**
 * Eventi visivi/notifiche di partita per la UI (ADR-0012, task F1-05).
 */
public sealed interface UiGameEvent {
    public data class Sweep(
        val seat: Seat,
        val sweepCountThisDeal: Int = 1,
    ) : UiGameEvent
    public data class DealEnded(val score: DealScore) : UiGameEvent
    public data class MatchEnded(val winner: Seat, val totals: Map<Seat, Int>) : UiGameEvent
}

/**
 * Stato della schermata di gioco esposto alla UI (ADR-0012, task F1-05).
 *
 * **Principio di sicurezza e architettura:**
 * La UI non riceve mai lo stato completo né alcuna informazione nascosta (mano avversaria, tallone coperto).
 * Tutti i campi derivano rigorosamente dalla [it.maestro.game.scopa.ScopaView] del giocatore umano.
 */
public data class GameUiState(
    val phase: DealPhase,
    val humanSeat: Seat,
    val humanHand: List<Card>,
    val tableCards: List<Card>,
    val opponentCardCount: Int,
    val deckCardCount: Int,
    val humanCapturedCount: Int,
    val opponentCapturedCount: Int,
    val humanSweeps: Int,
    val opponentSweeps: Int,
    /** Carte con cui sono state fatte le scope della smazzata, mostrate scoperte sotto le prese (F1-26). */
    val humanSweepCards: List<Card> = emptyList(),
    val opponentSweepCards: List<Card> = emptyList(),
    val humanScore: Int,
    val opponentScore: Int,
    val isHumanTurn: Boolean,
    val isBotThinking: Boolean,
    val isSparring: Boolean = true,
    /** Soluzione del consiglio: c'è solo dopo che il giocatore l'ha chiesta (secondo tocco). */
    val suggestedMove: ScopaMove? = null,
    /** Primo livello del consiglio: uno stimolo a ragionare, senza la mossa. */
    val hintNudge: HintLine? = null,
    val isHintLoading: Boolean = false,
    val trainingFundamental: TrainingFundamental = TrainingFundamental.NONE,
    val activeTrainingQuestion: TrainingQuestion? = null,
    val trainingQuestionFeedback: TrainingAnswerFeedback? = null,
    val trainingResults: List<TrainingCheckResult> = emptyList(),
    val pendingCaptureChoice: PendingCaptureChoice?,
    val pendingDealScore: DealScore? = null,
    val lastEvent: UiGameEvent?,
    val errorMessage: String?,
    /** Battuta spontanea del Maestro da mostrare (Sparring), `null` se tace. */
    val maestroLine: VoiceLine? = null,
    /** Ultima mossa giocata, utile per l'animazione al tavolo (task F1-24). */
    val lastMove: ScopaMove? = null,
    val lastMover: Seat? = null,
    val lastMoveId: Long = 0L,
    /** Ultima mossa effettuata dall'avversario per il registro visivo a schermo. */
    val opponentLastMove: OpponentMoveSummary? = null,
    /** ID progressivo per l'animazione di smazzata e distribuzione carte dal mazzo. */
    val dealAnimationId: Long = 0L,
    /** True se l'animazione di distribuzione include shuffle e 4 carte a terra (smazzata iniziale). */
    val isInitialDeal: Boolean = false,
    /** La mano d'esordio guidata (F1G-02); `null` in ogni altra partita. */
    val guide: GuideUi? = null,
)

/**
 * Ciò che la mano d'esordio guidata mostra: la battuta [line] del Maestro (`null` se tace), se l'introduzione
 * deve ancora comparire ([intro]) e se la mano è finita e resta solo il saluto ([finished]).
 */
public data class GuideUi(val line: GuideLine?, val intro: Boolean, val finished: Boolean)

/**
 * Stato interno della mano guidata: i passi già insegnati, le mosse fatte dal giocatore e la reazione all'ultima.
 */
public data class GuideSession(
    val taught: Set<GuideStep> = emptySet(),
    val moves: Int = 0,
    val feedback: GuideLine? = null,
    val introSeen: Boolean = false,
    val finished: Boolean = false,
)

/**
 * Intenti dell'utente o del motore di gioco nel modello MVI (ADR-0012, task F1-05).
 */
public sealed interface GameIntent {
    public data class StartMatch(
        val config: ScopaConfig = ScopaConfig(),
        val level: DifficultyLevel = DifficultyLevel.INTERMEDIO,
        val seed: Long = 42L,
        val humanSeat: Seat = Seat.P1,
        val trainingFundamental: TrainingFundamental = TrainingFundamental.NONE,
        /** Sparring (con Maestro, consigli e Check) o Classica (nessun aiuto, solo pose); da `docs/app/features/maestro-voice.md` §6. */
        val sparring: Boolean = true,
        /** Mano d'esordio guidata (F1G-02): il Maestro guida passo per passo e la partita non si registra. */
        val guided: Boolean = false,
    ) : GameIntent

    public data class RestoreMatch(
        val session: MatchSession,
        val level: DifficultyLevel,
        val humanSeat: Seat = Seat.P1,
        val trainingFundamental: TrainingFundamental = TrainingFundamental.NONE,
        val sparring: Boolean = true,
    ) : GameIntent

    public data class PlayCard(val card: Card) : GameIntent
    public data class SelectCapture(val card: Card, val capture: Set<Card>) : GameIntent
    public data object CancelCaptureChoice : GameIntent
    public data object DismissDealSummary : GameIntent
    public data object RequestBotMove : GameIntent
    public data class ApplyBotMove(val move: ScopaMove) : GameIntent
    public data object RequestHint : GameIntent
    public data class ApplyHint(val move: ScopaMove) : GameIntent
    public data object ClearHint : GameIntent
    public data class SelectTrainingFundamental(val fundamental: TrainingFundamental) : GameIntent
    public data class AnswerTrainingQuestion(val selectedIndex: Int) : GameIntent
    public data object DismissTrainingQuestion : GameIntent
    public data object DismissError : GameIntent
    public data object DismissEvent : GameIntent
    public data class ShowMaestroLine(val line: VoiceLine) : GameIntent
    public data object DismissMaestroLine : GameIntent

    /** L'introduzione della mano guidata è stata vista. */
    public data object DismissGuideIntro : GameIntent

    /** Il giocatore ha chiuso il riepilogo della mano guidata: la guida è finita. */
    public data object FinishGuide : GameIntent
}

/**
 * Stato autoritativo interno gestito dal Reducer (ADR-0012, task F1-05).
 */
public data class GameInternalState(
    val session: MatchSession,
    val humanSeat: Seat,
    val botLevel: DifficultyLevel,
    val pendingCapture: PendingCaptureChoice? = null,
    val pendingDealScore: DealScore? = null,
    val isBotThinking: Boolean = false,
    val isSparring: Boolean = true,
    val suggestedMove: ScopaMove? = null,
    /** Il consiglio c'è ma il giocatore non ha ancora chiesto la soluzione: si mostra solo lo stimolo. */
    val hintRevealed: Boolean = false,
    val isHintLoading: Boolean = false,
    val trainingFundamental: TrainingFundamental = TrainingFundamental.NONE,
    val activeTrainingQuestion: TrainingQuestion? = null,
    val trainingQuestionFeedback: TrainingAnswerFeedback? = null,
    val trainingResults: List<TrainingCheckResult> = emptyList(),
    val hasAskedQuestionThisDeal: Boolean = false,
    val lastError: String? = null,
    val lastEvent: UiGameEvent? = null,
    /** Carte giocate (quindi pubbliche) che hanno fatto scopa, per giocatore, in ordine (F1-26). */
    val sweepCards: Map<Seat, List<Card>> = emptyMap(),
    val lastMove: ScopaMove? = null,
    val lastMover: Seat? = null,
    val lastMoveId: Long = 0L,
    /** Ultima battuta prodotta dalla sessione: la presentazione la inoltra alla regia del tavolo. */
    val lastBeat: Beat? = null,
    val maestroLine: VoiceLine? = null,
    val opponentLastMove: OpponentMoveSummary? = null,
    val dealAnimationId: Long = 0L,
    val isInitialDeal: Boolean = false,
    val guide: GuideSession? = null,
) {
    /** Log della partita: seed + configurazione + mosse (ADR-0003). */
    val matchLog: MatchLog get() = session.log

    public fun toUiState(): GameUiState {
        val view = session.view(humanSeat)
        val opponentSeat = humanSeat.other()
        return GameUiState(
            phase = view.phase,
            humanSeat = humanSeat,
            humanHand = view.hand,
            tableCards = view.table,
            opponentCardCount = view.opponentHandSize,
            deckCardCount = view.deckSize,
            humanCapturedCount = view.capturedBy[humanSeat]?.size ?: 0,
            opponentCapturedCount = view.capturedBy[opponentSeat]?.size ?: 0,
            humanSweeps = view.sweeps[humanSeat] ?: 0,
            opponentSweeps = view.sweeps[opponentSeat] ?: 0,
            humanSweepCards = sweepCards[humanSeat].orEmpty().takeLast(view.sweeps[humanSeat] ?: 0),
            opponentSweepCards = sweepCards[opponentSeat].orEmpty().takeLast(view.sweeps[opponentSeat] ?: 0),
            humanScore = view.totals[humanSeat] ?: 0,
            opponentScore = view.totals[opponentSeat] ?: 0,
            isHumanTurn = view.phase == DealPhase.PLAYING &&
                view.toMove == humanSeat &&
                pendingDealScore == null &&
                activeTrainingQuestion == null,
            isBotThinking = isBotThinking,
            isSparring = isSparring,
            suggestedMove = suggestedMove.takeIf { hintRevealed },
            hintNudge = suggestedMove?.takeIf { !hintRevealed }?.let { HintComposer.nudge(it, view.table) },
            isHintLoading = isHintLoading,
            trainingFundamental = trainingFundamental,
            activeTrainingQuestion = activeTrainingQuestion,
            trainingQuestionFeedback = trainingQuestionFeedback,
            trainingResults = trainingResults,
            pendingCaptureChoice = pendingCapture,
            pendingDealScore = pendingDealScore,
            lastEvent = lastEvent,
            errorMessage = lastError,
            maestroLine = maestroLine,
            lastMove = lastMove,
            lastMover = lastMover,
            lastMoveId = lastMoveId,
            opponentLastMove = opponentLastMove,
            dealAnimationId = dealAnimationId,
            isInitialDeal = isInitialDeal,
            guide = guide?.let { guideUi(it, view) },
        )
    }

    // La battuta tocca al Maestro solo a mano in corso: nel proprio turno insegna, dopo la mossa reagisce, a fine mano chiude.
    private fun guideUi(progress: GuideSession, view: ScopaView): GuideUi {
        val line = when {
            progress.finished -> null
            view.phase != DealPhase.PLAYING || pendingDealScore != null ->
                GuideLine(GuideStep.END, GuideTone.INSTRUCTION)
            view.toMove == humanSeat ->
                GuidedHand.step(progress.taught, progress.moves, session.legalMovesFor(humanSeat), view.table)
            else -> progress.feedback
        }
        return GuideUi(line, intro = !progress.introSeen, finished = progress.finished)
    }
}

/**
 * Effetto collaterale generato dal Reducer per il runtime asincrono (ADR-0012, task F1-05).
 */
public sealed interface GameEffect {
    public data class ScheduleBotMove(
        val level: DifficultyLevel,
        val botSeat: Seat,
    ) : GameEffect

    public data class ComputeHint(
        val humanSeat: Seat,
    ) : GameEffect
}

public data class GameReducerResult(
    val state: GameInternalState,
    val effect: GameEffect? = null,
)
