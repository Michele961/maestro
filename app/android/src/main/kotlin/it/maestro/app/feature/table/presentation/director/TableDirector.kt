package it.maestro.app.feature.table.presentation.director

import it.maestro.app.designsystem.motion.MotionSpec
import it.maestro.app.domain.match.Beat
import it.maestro.app.domain.match.PresentationGate
import it.maestro.app.domain.settings.GameSpeed
import it.maestro.game.model.Seat
import it.maestro.game.scopa.ScopaView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
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
 * L'unica attesa di tempo è [MotionSpec.pause], con durate da `MotionSpec` scalate da [speed].
 */
public class TableDirector(
    scope: CoroutineScope,
    private val humanSeat: Seat,
    initialView: ScopaView,
    private val speed: () -> GameSpeed = { GameSpeed.NORMAL },
) : PresentationGate {

    private val queue = Channel<Beat>(Channel.UNLIMITED)
    private var queued = 0
    private var skipCurrent = false
    private var skipQueue = false

    private val _scene = MutableStateFlow(TableScene(view = initialView, humanSeat = humanSeat))

    /** Scena corrente. */
    public val scene: StateFlow<TableScene> = _scene.asStateFlow()

    private val presented = MutableStateFlow(0L)

    init {
        scope.launch {
            for (beat in queue) play(beat)
        }
    }

    /** Mette in coda [beat]: verrà mostrata dopo quelle già in coda. */
    public fun enqueue(beat: Beat) {
        queued++
        _scene.value = _scene.value.copy(busy = true)
        queue.trySend(beat)
    }

    /**
     * Tocco sul tavolo durante una scena: il primo porta alla fine della battuta corrente, il secondo salta
     * anche quelle in coda fino alla prossima scelta del giocatore (`table-director.md` §4.3).
     */
    public fun skip() {
        if (!_scene.value.busy) return
        if (skipCurrent) skipQueue = true else skipCurrent = true
    }

    /**
     * Mostra [view] a riposo senza animazioni: dopo una rotazione o la ripresa di una partita
     * (`table-director.md` §4.5).
     */
    public fun rest(view: ScopaView, beatId: Long) {
        _scene.value = TableScene(view = view, humanSeat = humanSeat, beatId = beatId, busy = queued > 0)
        if (presented.value < beatId) presented.value = beatId
    }

    /** Il bot attende che la battuta [moveId] sia stata mostrata per intero; [dealId] è incluso nella battuta. */
    override suspend fun awaitPresented(moveId: Long, dealId: Long) {
        presented.first { it >= moveId }
    }

    private suspend fun play(beat: Beat) {
        for (step in BeatScript.steps(beat, humanSeat)) {
            _scene.value = TableScene(beat.viewBefore, humanSeat, step = step, beatId = beat.id, busy = true)
            if (!skipCurrent && !skipQueue) MotionSpec.pause(MotionSpec.millis(step.token, speed()))
        }
        queued--
        skipCurrent = false
        if (queued == 0) skipQueue = false
        _scene.value = TableScene(beat.viewAfter, humanSeat, step = null, beatId = beat.id, busy = queued > 0)
        presented.value = beat.id
    }
}
