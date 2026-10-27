package it.maestro.app.feature.table.presentation.director

import it.maestro.app.designsystem.motion.MotionToken
import it.maestro.game.model.Card
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealScore

/**
 * Un passo della messa in scena di una battuta (`docs/app/table-director.md` §3). Ogni passo dura il
 * [token] di `MotionSpec`, scalato dalla velocità di gioco.
 */
public sealed interface SceneStep {
    public val token: MotionToken

    /** Carta del giocatore dalla mano alla zona di gioco. */
    public data class CardToPlayZone(val card: Card) : SceneStep {
        override val token: MotionToken get() = MotionToken.CARD_TO_PLAY_ZONE
    }

    /** Carta avversaria che si gira nella zona di gioco. */
    public data class OpponentReveal(val card: Card) : SceneStep {
        override val token: MotionToken get() = MotionToken.OPPONENT_REVEAL
    }

    /** Pausa per leggere la carta avversaria. */
    public data class OpponentReadPause(val card: Card) : SceneStep {
        override val token: MotionToken get() = MotionToken.OPPONENT_READ_PAUSE
    }

    /** Le carte prese si illuminano. */
    public data class CaptureHighlight(val actor: Seat, val card: Card, val captured: Set<Card>) : SceneStep {
        override val token: MotionToken get() = MotionToken.CAPTURE_HIGHLIGHT
    }

    /** Carta giocata e carte prese volano insieme alla pila di [actor]. */
    public data class CaptureFlight(val actor: Seat, val cards: List<Card>) : SceneStep {
        override val token: MotionToken get() = MotionToken.CAPTURE_FLIGHT
    }

    /** Calata senza presa: la carta si posa sul tavolo. */
    public data class DropToTable(val actor: Seat, val card: Card) : SceneStep {
        override val token: MotionToken get() = MotionToken.DROP_TO_TABLE
    }

    /** Overlay "Scopa!" di [actor]. */
    public data class Sweep(val actor: Seat, val humanSeat: Seat) : SceneStep {
        override val token: MotionToken
            get() = if (actor == humanSeat) MotionToken.SWEEP_CELEBRATION else MotionToken.SWEEP_OPPONENT
    }

    /** Mescolata a inizio smazzata. */
    public data object Shuffle : SceneStep {
        override val token: MotionToken get() = MotionToken.SHUFFLE
    }

    /** Una carta dal mazzo: a terra ([toSeat] = null) o in mano a [toSeat]. */
    public data class DealCard(val index: Int, val toSeat: Seat?) : SceneStep {
        override val token: MotionToken get() = MotionToken.DEAL_CARD
    }

    /** Riepilogo di fine smazzata: una voce che si accende per volta ([row] su [rows]). */
    public data class DealSummaryRow(val score: DealScore, val row: Int, val rows: Int) : SceneStep {
        override val token: MotionToken get() = MotionToken.DEAL_SUMMARY_ROW
    }
}
