package it.maestro.game.scopa

/**
 * Motivo per cui una giocata passata a [play] è stata rifiutata (task F0-05, `docs/architecture.md`).
 */
public enum class RejectReason {
    /**
     * La mossa non appartiene alla lista restituita da [legalMoves] per lo stato corrente.
     */
    NOT_IN_LEGAL_MOVES,

    /**
     * La smazzata è già terminata ([DealPhase.DEAL_OVER]).
     */
    DEAL_OVER,

    /**
     * La partita è già terminata (spec §7.6).
     */
    MATCH_OVER,
}
