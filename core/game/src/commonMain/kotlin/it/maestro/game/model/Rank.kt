package it.maestro.game.model

/**
 * Valore di una carta nel mazzo da 40 carte e relativo valore numerico di presa (spec §1.2).
 *
 * @property value Valore numerico di presa da 1 ([ASSO]) a 10 ([RE]) secondo spec §1.2.
 */
public enum class Rank(public val value: Int) {
    /** Asso (valore di presa 1). */
    ASSO(1),

    /** Due (valore di presa 2). */
    DUE(2),

    /** Tre (valore di presa 3). */
    TRE(3),

    /** Quattro (valore di presa 4). */
    QUATTRO(4),

    /** Cinque (valore di presa 5). */
    CINQUE(5),

    /** Sei (valore di presa 6). */
    SEI(6),

    /** Sette (valore di presa 7). */
    SETTE(7),

    /** Fante (valore di presa 8). */
    FANTE(8),

    /** Cavallo (valore di presa 9). */
    CAVALLO(9),

    /** Re (valore di presa 10). */
    RE(10),
}
