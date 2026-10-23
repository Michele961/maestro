package it.maestro.game.scopa

import it.maestro.game.model.Card
import it.maestro.game.model.Seat

/**
 * Stato completo e immutabile di una partita di Scopa a 2 giocatori (`docs/architecture.md`, ADR-0009).
 *
 * Include le informazioni nascoste ai singoli giocatori (mano avversaria, ordine delle carte nel [deck]
 * e [rngState] del generatore pseudocasuale) e costituisce lo stato autoritativo del motore.
 *
 * @property config Configurazione delle regole e varianti della partita (spec §8).
 * @property dealer Giocatore che distribuisce le carte nella smazzata corrente (spec §2.2, §2.3).
 * @property toMove Giocatore di turno (spec §4.1).
 * @property hands Carte attualmente in mano a ciascun giocatore ([Seat.P1] e [Seat.P2]).
 * @property table Carte scoperte al centro del tavolo, nell'ordine di uscita (spec §3.2).
 * @property deck Carte rimaste nel tallone coperto; l'indice `0` è la carta in cima (spec §3.1).
 * @property rngState Stato interno a 64 bit di `SeededRng` al termine dell'ultima operazione casuale (ADR-0009).
 */
public data class ScopaState(
    public val config: ScopaConfig,
    public val dealer: Seat,
    public val toMove: Seat,
    public val hands: Map<Seat, List<Card>>,
    public val table: List<Card>,
    public val deck: List<Card>,
    public val rngState: Long,
) {
    /**
     * Restituisce la lista delle carte in mano al giocatore seduto in [seat].
     */
    public fun hand(seat: Seat): List<Card> = hands.getValue(seat)
}
