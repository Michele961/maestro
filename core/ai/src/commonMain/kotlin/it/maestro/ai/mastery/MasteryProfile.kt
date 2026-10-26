package it.maestro.ai.mastery

import kotlin.math.roundToInt

/**
 * Profilo di competenza cognitiva del giocatore (task F2-04, `docs/vision.md` §5.A).
 *
 * Include l'Indice di Maestria (Elo Cognitivo 800..2000 calcolato come media mobile
 * ponderata della precisione delle partite, slegata dalla fortuna delle carte)
 * e i 5 parametri del Radar delle Abilità.
 *
 * @property matchesPlayed Numero totale di partite competitive/allenamento analizzate.
 * @property accuracyEma Media mobile esponenziale (EMA) della precisione percentuale (`0.0..100.0`).
 * @property cognitiveElo Punteggio Elo cognitivo (`800..2000`), calibrato sui livelli del Maestro.
 * @property radar Profilo a 5 assi delle abilità strategiche.
 */
public data class MasteryProfile(
    public val matchesPlayed: Int = 0,
    public val accuracyEma: Double = 50.0,
    public val cognitiveElo: Int = 1000,
    public val radar: AbilityRadar = AbilityRadar.DEFAULT,
) {
    init {
        require(matchesPlayed >= 0) { "matchesPlayed deve essere >= 0: $matchesPlayed" }
        require(accuracyEma in 0.0..100.0) { "accuracyEma deve essere in 0.0..100.0: $accuracyEma" }
        require(cognitiveElo in 800..2000) { "cognitiveElo deve essere in 800..2000: $cognitiveElo" }
    }

    /** Precisione complessiva arrotondata all'intero (`0..100`). */
    public val accuracyPercentage: Int
        get() = accuracyEma.roundToInt().coerceIn(0, 100)

    public companion object {
        /** Profilo iniziale di default per un utente appena iscritto (non ancora calibrato). */
        public val INITIAL: MasteryProfile = MasteryProfile(
            matchesPlayed = 0,
            accuracyEma = 50.0,
            cognitiveElo = 1000,
            radar = AbilityRadar.DEFAULT,
        )
    }
}
