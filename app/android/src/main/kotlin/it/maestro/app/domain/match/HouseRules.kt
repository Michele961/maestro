package it.maestro.app.domain.match

import it.maestro.game.scopa.ScopaConfig

/**
 * Le regole della casa che il giocatore sceglie prima della partita (`docs/rules/scopa.md` §8): solo le varianti già in
 * specifica, con i loro valori predefiniti.
 */
public data class HouseRules(
    /** Re bello: 1 punto a chi prende il Re di Denari (§6.2). */
    val reBello: Boolean = false,
    /** Smazzata annullata se sul tavolo iniziale ci sono 3 o 4 Re (§3.3). */
    val redealOnThreeKings: Boolean = true,
) {
    /** La configurazione di partita con queste regole e il [targetScore] scelto. */
    public fun toConfig(targetScore: Int): ScopaConfig =
        ScopaConfig(targetScore = targetScore, redealOnThreeKings = redealOnThreeKings, reBello = reBello)
}
