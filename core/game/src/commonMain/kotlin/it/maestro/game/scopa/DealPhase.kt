package it.maestro.game.scopa

/**
 * Fase corrente della smazzata (task F0-05).
 */
public enum class DealPhase {
    /**
     * La smazzata è in corso e il giocatore di turno deve effettuare una giocata.
     */
    PLAYING,

    /**
     * Tutte le carte delle mani e del mazzo della smazzata sono state giocate (comportamento di confine di F0-05).
     */
    DEAL_OVER,
}
