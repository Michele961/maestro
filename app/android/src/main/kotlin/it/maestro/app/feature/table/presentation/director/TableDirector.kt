package it.maestro.app.feature.table.presentation.director

import it.maestro.app.designsystem.motion.MotionSpec
import it.maestro.app.designsystem.motion.MotionToken
import it.maestro.app.domain.match.Beat
import it.maestro.app.domain.match.PresentationGate
import it.maestro.app.domain.settings.GameSpeed
import it.maestro.game.model.Card
import it.maestro.game.model.Seat
import it.maestro.game.scopa.ScopaView
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Regia del tavolo (ADR-0015, `docs/app/table-director.md`): riproduce le [Beat] in coda, una alla volta,
 * e pubblica la [scene] da disegnare. È anche il [PresentationGate] del bot: una battuta è "presentata"
 * quando tutti i suoi passi sono finiti.
 *
 * Al riepilogo di smazzata aspetta il giocatore ([waitForPlayer], spento nei test senza interfaccia).
 * L'unica attesa di tempo è [MotionSpec.pause], con durate da `MotionSpec` scalate da [speed].
 */
public class TableDirector(
    private val scope: CoroutineScope,
    private val humanSeat: Seat,
    initialView: ScopaView,
    private val speed: () -> GameSpeed = { GameSpeed.NORMAL },
    private val waitForPlayer: Boolean = true,
) : PresentationGate {

    private val queue = ArrayDeque<Show>()
    private var drain: Job? = null
    private var queued = 0
    private var continueGate: CompletableDeferred<Unit>? = null
    private var skipCurrent = false
    private var skipQueue = false

    private val _scene = MutableStateFlow(TableScene(view = initialView, humanSeat = humanSeat))

    /** Scena corrente. */
    public val scene: StateFlow<TableScene> = _scene.asStateFlow()

    private val presented = MutableStateFlow(0L)

    /** Mette in coda [beat]: verrà mostrata dopo quelle già in coda. */
    public fun enqueue(beat: Beat) {
        enqueue(
            Show(
                id = beat.id,
                before = beat.viewBefore,
                after = beat.viewAfter,
                steps = BeatScript.steps(beat, humanSeat),
                actor = beat.actor,
                played = beat.move.card,
                deal = BeatScript.dealPlan(beat),
            ),
        )
    }

    /** Mette in scena la distribuzione di apertura di [view]: mescolata, carte a terra, mani. */
    public fun enqueueOpeningDeal(view: ScopaView, beatId: Long = 0L) {
        enqueue(
            Show(
                id = beatId,
                before = view,
                after = view,
                steps = BeatScript.openingDeal(view.dealer),
                actor = null,
                played = null,
                deal = DealPlan(tableCards = view.table.size, handCards = view.hand.size + view.opponentHandSize),
            ),
        )
    }

    private fun enqueue(show: Show) {
        queued++
        _scene.value = _scene.value.copy(busy = true)
        queue.addLast(show)
        if (drain?.isActive != true) {
            // Il job vive solo finché ci sono battute: nessuna coroutine in attesa a coda vuota.
            drain = scope.launch {
                while (queue.isNotEmpty()) play(queue.removeFirst())
            }
        }
    }

    /**
     * Tocco sul tavolo durante una scena: il primo porta alla fine della battuta corrente, il secondo salta
     * anche quelle in coda fino alla prossima scelta del giocatore (`table-director.md` §4.3).
     */
    public fun skip() {
        if (!_scene.value.busy) return
        if (skipCurrent) skipQueue = true else skipCurrent = true
    }

    /** Il giocatore ha letto il riepilogo e continua: la regia riprende dal passo successivo. */
    public fun resume() {
        continueGate?.complete(Unit)
    }

    /**
     * Mostra [view] a riposo senza animazioni: dopo una rotazione o la ripresa di una partita
     * (`table-director.md` §4.5).
     */
    public fun rest(view: ScopaView, beatId: Long) {
        drain?.cancel()
        continueGate = null
        queue.clear()
        queued = 0
        skipCurrent = false
        skipQueue = false
        _scene.value = TableScene(view = view, humanSeat = humanSeat, beatId = beatId, busy = false)
        if (presented.value < beatId) presented.value = beatId
    }

    /** Il bot attende che la battuta [moveId] sia stata mostrata per intero e la scena sia ferma. */
    override suspend fun awaitPresented(moveId: Long, dealId: Long) {
        presented.first { it >= moveId }
        _scene.first { !it.busy }
    }

    private suspend fun play(show: Show) {
        for (step in show.steps) {
            val waitsForPlayer = waitForPlayer && step is SceneStep.AwaitDealSummary
            val millis = if (skipCurrent || skipQueue || waitsForPlayer) 0L else stepMillis(step)
            _scene.value = TableScene(
                view = show.before,
                humanSeat = humanSeat,
                step = step,
                beatId = show.id,
                busy = true,
                after = show.after,
                actor = show.actor,
                played = show.played,
                deal = show.deal,
                stepMillis = millis,
            )
            if (waitsForPlayer) {
                val gate = CompletableDeferred<Unit>().also { continueGate = it }
                gate.await()
                continueGate = null
            } else {
                MotionSpec.pause(millis)
            }
        }
        queued--
        skipCurrent = false
        if (queued == 0) skipQueue = false
        _scene.value = TableScene(show.after, humanSeat, step = null, beatId = show.id, busy = queued > 0)
        presented.value = show.id
    }

    private fun stepMillis(step: SceneStep): Long {
        val base = MotionSpec.millis(step.token, speed())
        if (step !is SceneStep.CaptureFlight) return base
        return base + MotionSpec.millis(MotionToken.CAPTURE_FLIGHT_STAGGER, speed()) * (step.cards.size - 1).coerceAtLeast(0)
    }

    /** Una battuta (o l'apertura) da mettere in scena: i passi e le viste tra cui si muovono. */
    private class Show(
        val id: Long,
        val before: ScopaView,
        val after: ScopaView,
        val steps: List<SceneStep>,
        val actor: Seat?,
        val played: Card?,
        val deal: DealPlan?,
    )
}
