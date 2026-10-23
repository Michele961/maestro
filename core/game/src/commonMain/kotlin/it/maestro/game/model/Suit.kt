package it.maestro.game.model

/**
 * Seme delle carte nel mazzo italiano da 40 carte (spec §1.1).
 *
 * L'ordine di dichiarazione delle costanti definisce l'ordine canonico dei semi:
 * [DENARI], [COPPE], [SPADE], [BASTONI] (`docs/conventions.md`).
 */
public enum class Suit {
    /** Seme di Denari. */
    DENARI,

    /** Seme di Coppe. */
    COPPE,

    /** Seme di Spade. */
    SPADE,

    /** Seme di Bastoni. */
    BASTONI,
}
