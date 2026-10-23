package it.maestro.game.scopa

/**
 * Fase corrente della smazzata o della partita (task F0-05, F0-06, spec §7.5–7.6).
 */
public enum class DealPhase {
    /**
     * La smazzata è in corso e il giocatore di turno deve effettuare una giocata.
     */
    PLAYING,

    /**
     * Tutte le carte delle mani e del mazzo della smazzata sono state giocate.
     *
     * A partire da F0-06 (spec §7.5) costituisce uno stato di passaggio non osservabile dall'esterno
     * durante il normale flusso di gioco, poiché l'ultima giocata della smazzata avvia automaticamente
     * la smazzata successiva ([PLAYING]) oppure conclude la partita ([MATCH_OVER]).
     */
    DEAL_OVER,

    /**
     * La partita è terminata: almeno un giocatore ha raggiunto o superato [ScopaConfig.targetScore]
     * a fine smazzata senza parità nei punteggi totali (spec §7.2, §7.3, §7.6).
     */
    MATCH_OVER,
}
