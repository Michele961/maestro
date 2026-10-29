package it.maestro.app.feature.review.presentation

import it.maestro.app.domain.settings.GameSpeed
import it.maestro.app.feature.review.ui.ReviewStep
import it.maestro.app.feature.table.presentation.director.TableDirector
import it.maestro.app.feature.table.presentation.director.TableScene
import it.maestro.game.model.Seat
import it.maestro.game.scopa.ScopaView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * La revisione sulla regia del tavolo (`docs/app/features/review.md` §3, task F1R-17a): mette in scena la mossa
 * giocata e, a richiesta, la mossa migliore con lo stesso [TableDirector] della partita. Non aspetta il giocatore
 * (non ci sono riepiloghi da chiudere) e non conosce la UI.
 */
public class ReviewStage(scope: CoroutineScope, humanSeat: Seat, first: ScopaView) {
    private val director = TableDirector(
        scope = scope,
        humanSeat = humanSeat,
        initialView = first,
        speed = { if (slow) GameSpeed.SLOW else GameSpeed.NORMAL },
        waitForPlayer = false,
    )

    /** Con il rallentatore le scene durano il doppio, per poterle rivedere con calma. */
    public var slow: Boolean = false

    /** Ciò che il tavolo mostra ora. */
    public val scene: StateFlow<TableScene> = director.scene

    /** La posizione di [step] com'era prima della mossa, ferma. */
    public fun rest(step: ReviewStep) {
        director.rest(step.playerView, step.beat.id)
    }

    /** La posizione di [step] e poi la mossa giocata che si anima. */
    public fun showPlayed(step: ReviewStep) {
        rest(step)
        director.enqueue(step.beat)
    }

    /** La posizione di [step] e poi la mossa migliore che si anima; la giocata se non c'è un errore. */
    public fun showBest(step: ReviewStep) {
        rest(step)
        director.enqueue(step.bestBeat ?: step.beat)
    }

    /** Un tocco salta la scena in corso. */
    public fun skip() {
        director.skip()
    }
}
