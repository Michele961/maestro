package it.maestro.game.scopa

/**
 * Configurazione di una partita di Scopa a 2 giocatori (spec §8, ADR-0011).
 *
 * Contiene esclusivamente le varianti previste dalla specifica normativa:
 * - [targetScore]: punteggio obiettivo di fine partita (`11`, `16` o `21`; default `11`, spec §7.1).
 * - [redealOnThreeKings]: annulla e ridistribuisce la smazzata se sul tavolo iniziale escono 3 o 4 Re
 *   (default `true`, spec §3.3).
 * - [reBello]: assegna 1 punto a fine smazzata a chi ha preso il Re di Denari (default `false`, spec §6.2).
 *
 * @property targetScore Punteggio obiettivo di fine partita (`11`, `16` o `21`).
 * @property redealOnThreeKings `true` se la regola dei 3 o 4 Re sul tavolo iniziale è attiva.
 * @property reBello `true` se la variante del Re bello (Re di Denari) è attiva.
 * @throws IllegalArgumentException se [targetScore] non è tra i valori ammessi (`11`, `16`, `21`).
 */
public data class ScopaConfig(
    public val targetScore: Int = DEFAULT_TARGET_SCORE,
    public val redealOnThreeKings: Boolean = true,
    public val reBello: Boolean = false,
) {
    init {
        require(targetScore in VALID_TARGET_SCORES) {
            "targetScore must be one of $VALID_TARGET_SCORES (spec §7.1, §8), was $targetScore"
        }
    }

    internal companion object {
        internal const val DEFAULT_TARGET_SCORE: Int = 11
        internal val VALID_TARGET_SCORES: Set<Int> = setOf(11, 16, 21)
    }
}
