package it.maestro.app.domain.match

import it.maestro.app.domain.opponents.Opponent

/** Le quattro varianti della casa già nel motore (`docs/rules/scopa.md` §8). */
public enum class HouseRule {
    /** Re bello (§6.2). */
    RE_BELLO,

    /** Ridistribuzione con 3 o 4 Re sul tavolo (§3.3). */
    REDEAL_ON_THREE_KINGS,

    /** Napola (§6.4). */
    NAPOLA,

    /** Asso che taglia (§4.10). */
    ASSO_PIGLIATUTTO,
}

/**
 * Le regole della casa fisse della Scalata (task F1G-09, decisione di Michele del 2026-10-31): battere un avversario
 * «sblocca» una regola, e da quel momento la regola vale, senza interruttore, in tutte le sfide contro gli avversari
 * successivi. Gigi: Re bello; Nonna Rosa: tre Re; Tonino: napola; Don Alfonso: asso che taglia.
 *
 * Contro Gigi non c'è nessuna regola in più (valgono i default della specifica). Logica pura: la schermata
 * pre-partita usa queste funzioni e passa al tavolo la configurazione già risolta, così non si può aggirare.
 */
public object StoryHouseRules {
    private val ruleOf: Map<Opponent, HouseRule> = mapOf(
        Opponent.GIGI to HouseRule.RE_BELLO,
        Opponent.NONNA_ROSA to HouseRule.REDEAL_ON_THREE_KINGS,
        Opponent.TONINO to HouseRule.NAPOLA,
        Opponent.DON_ALFONSO to HouseRule.ASSO_PIGLIATUTTO,
    )

    /** Le regole fisse in vigore contro [opponent]: quelle degli avversari che lo precedono sulla scala, in ordine. */
    public fun inForce(opponent: Opponent): List<HouseRule> =
        Opponent.entries.takeWhile { it != opponent }.mapNotNull(ruleOf::get)

    /** La regola che entra in vigore proprio contro [opponent] (quella dell'avversario prima di lui), se c'è. */
    public fun enteringWith(opponent: Opponent): HouseRule? =
        Opponent.entries.getOrNull(Opponent.entries.indexOf(opponent) - 1)?.let(ruleOf::get)

    /**
     * Le regole della casa di una sfida contro [opponent]. La ridistribuzione con 3 Re resta quella della specifica
     * (accesa, §8) anche prima del suo avversario: da quel punto in poi la schermata la mostra come regola fissa.
     */
    public fun forOpponent(opponent: Opponent): HouseRules {
        val fixed = inForce(opponent)
        return HouseRules(
            reBello = HouseRule.RE_BELLO in fixed,
            redealOnThreeKings = true,
            napola = HouseRule.NAPOLA in fixed,
            assoPigliatutto = HouseRule.ASSO_PIGLIATUTTO in fixed,
        )
    }

    /**
     * Le regole con cui si gioca davvero. In Classica (le partite della Scalata) sono sempre [forOpponent] e la scelta
     * del giocatore ([chosen]) è ignorata; in Sparring, che è allenamento e non fa avanzare la scala, resta libera.
     */
    public fun resolve(opponent: Opponent, sparring: Boolean, chosen: HouseRules): HouseRules =
        if (sparring) chosen else forOpponent(opponent)
}
