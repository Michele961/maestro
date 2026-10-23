package it.maestro.game.scopa

/**
 * Risultato tipizzato dell'applicazione di una mossa tramite [play] (`docs/architecture.md`, `CLAUDE.md` §4).
 *
 * Le mosse illegali non lanciano mai eccezioni ma restituiscono [Rejected].
 */
public sealed interface PlayResult {
    /**
     * La mossa era legale ed è stata applicata, producendo il nuovo [state] e la sequenza ordinata di [events].
     *
     * @property state Nuovo stato immutabile della partita dopo la giocata (ed eventuale ridistribuzione).
     * @property events Eventi di dominio generati nell'ordine in cui si sono verificati.
     */
    public data class Applied(
        public val state: ScopaState,
        public val events: List<GameEvent>,
    ) : PlayResult

    /**
     * La mossa non è valida nello stato corrente ed è stata rifiutata con motivo [reason].
     *
     * @property reason Causa del rifiuto della mossa.
     */
    public data class Rejected(
        public val reason: RejectReason,
    ) : PlayResult
}
