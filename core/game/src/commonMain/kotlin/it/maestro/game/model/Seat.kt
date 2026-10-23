package it.maestro.game.model

/**
 * Posto al tavolo in una partita a 2 giocatori (`docs/rules/glossary.md`).
 */
public enum class Seat {
    /** Primo giocatore. */
    P1,

    /** Secondo giocatore. */
    P2,
    ;

    /** Restituisce l'altro posto al tavolo ([P2] per [P1], [P1] per [P2]). */
    public fun other(): Seat = when (this) {
        P1 -> P2
        P2 -> P1
    }
}
