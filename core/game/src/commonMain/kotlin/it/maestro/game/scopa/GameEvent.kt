package it.maestro.game.scopa

import it.maestro.game.model.Card
import it.maestro.game.model.Seat

/**
 * Evento di dominio prodotto dall'esecuzione di una giocata valida tramite [play] (task F0-05).
 */
public sealed interface GameEvent {
    /**
     * Il giocatore seduto in [seat] ha giocato la carta [card] dalla propria mano (spec §4.1).
     *
     * @property seat Giocatore che ha giocato la carta.
     * @property card Carta giocata dalla mano.
     */
    public data class CardPlayed(
        public val seat: Seat,
        public val card: Card,
    ) : GameEvent

    /**
     * Il giocatore seduto in [seat] ha preso dal tavolo le carte in [cards] (spec §4.2, §4.3, §4.7).
     *
     * @property seat Giocatore che ha effettuato la presa.
     * @property cards Insieme delle carte prese dal tavolo, in ordine canonico.
     */
    public data class Captured(
        public val seat: Seat,
        public val cards: Set<Card>,
    ) : GameEvent

    /**
     * Il giocatore seduto in [seat] ha realizzato una scopa svuotando il tavolo (spec §5.1, §5.2).
     *
     * @property seat Giocatore che ha realizzato la scopa.
     */
    public data class Sweep(
        public val seat: Seat,
    ) : GameEvent

    /**
     * Il mazziere ha distribuito una nuova mano di 3 carte a testa dal mazzo (spec §3.4, §3.5).
     *
     * @property distribution Numero progressivo della distribuzione nella smazzata (`2..6`, poiché
     *   la distribuzione iniziale è la `1`).
     */
    public data class CardsDealt(
        public val distribution: Int,
    ) : GameEvent
}
