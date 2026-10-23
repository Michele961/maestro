package it.maestro.ai.bot

import it.maestro.game.scopa.ScopaMove

/**
 * Valore atteso associato a una mossa legale per il giocatore di turno nella sua vista
 * (`docs/ai/maestro.md`, task F0-10, F0-11).
 *
 * Il valore è espresso in punti di smazzata attesi netti (`punti giocatore − punti avversario`).
 *
 * @property move Mossa legale valutata.
 * @property value Valore atteso stimato (o esatto nell'ultima distribuzione) della mossa.
 */
public data class MoveValue(
    public val move: ScopaMove,
    public val value: Double,
) {
    /** Alias di [value] per il valore atteso della mossa. */
    public val expectedValue: Double
        get() = value
}
