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
 * @property phase Fase corrente della smazzata o della partita ([DealPhase.PLAYING], [DealPhase.DEAL_OVER]
 *   o [DealPhase.MATCH_OVER], task F0-05, F0-06).
 * @property distribution Numero progressivo della distribuzione corrente nella smazzata (`1..6`, spec §3.5).
 * @property capturedBy Mazzetto delle prese effettuate da ciascun giocatore nella smazzata corrente (spec §4.7).
 * @property sweeps Conteggio delle scope realizzate da ciascun giocatore nella smazzata corrente (spec §5.1).
 * @property lastCapturer Ultimo giocatore che ha effettuato una presa nella smazzata corrente, o `null` se
 *   nessuno ha ancora preso (`docs/rules/glossary.md`, spec §6.1).
 * @property totals Punteggi totali di partita accumulati da ciascun giocatore a fine smazzata (spec §2.1, §7).
 * @property winner Vincitore della partita quando [phase] è [DealPhase.MATCH_OVER], altrimenti `null` (spec §7.2).
 * @property playedCards Carte giocate dalla mano nella smazzata corrente, nell'ordine in cui sono state giocate (task F0-07).
 */
public data class ScopaState(
    public val config: ScopaConfig,
    public val dealer: Seat,
    public val toMove: Seat,
    public val hands: Map<Seat, List<Card>>,
    public val table: List<Card>,
    public val deck: List<Card>,
    public val rngState: Long,
    public val phase: DealPhase = DealPhase.PLAYING,
    public val distribution: Int = 1,
    public val capturedBy: Map<Seat, List<Card>> = mapOf(Seat.P1 to emptyList(), Seat.P2 to emptyList()),
    public val sweeps: Map<Seat, Int> = mapOf(Seat.P1 to 0, Seat.P2 to 0),
    public val lastCapturer: Seat? = null,
    public val totals: Map<Seat, Int> = mapOf(Seat.P1 to 0, Seat.P2 to 0),
    public val winner: Seat? = null,
    public val playedCards: List<Card> = emptyList(),
) {
    /**
     * Restituisce la lista delle carte in mano al giocatore seduto in [seat].
     */
    public fun hand(seat: Seat): List<Card> = hands.getValue(seat)

    /**
     * Restituisce il mazzetto delle carte prese nella smazzata corrente dal giocatore seduto in [seat].
     */
    public fun captured(seat: Seat): List<Card> = capturedBy.getValue(seat)

    /**
     * Restituisce il punteggio totale di partita accumulato dal giocatore seduto in [seat].
     */
    public fun totalScore(seat: Seat): Int = totals.getValue(seat)
}
