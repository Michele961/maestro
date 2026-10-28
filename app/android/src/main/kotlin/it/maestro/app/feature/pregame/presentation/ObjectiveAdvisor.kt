package it.maestro.app.feature.pregame.presentation

import it.maestro.ai.mastery.MasteryProfile
import it.maestro.game.training.TrainingFundamental

/**
 * Sceglie l'obiettivo di allenamento da consigliare in base alle lacune del giocatore
 * (`docs/app/features/coach-in-game.md` §6, decisione 3): è l'obiettivo dell'asse più basso del radar tra quelli
 * che hanno un Check (Sette, Denari, Primiera). Gli assi Banco e Finale non hanno ancora un Check e non contano.
 */
public object ObjectiveAdvisor {
    /** Partite analizzate necessarie perché il radar dica qualcosa: prima, nessun consiglio. */
    public const val MIN_MATCHES: Int = 3

    /**
     * L'obiettivo consigliato per [profile], `null` se le partite giocate sono meno di [MIN_MATCHES].
     * A parità di livello vince l'ordine Sette, Denari, Primiera.
     */
    public fun suggest(profile: MasteryProfile): TrainingFundamental? {
        if (profile.matchesPlayed < MIN_MATCHES) return null
        val radar = profile.radar
        return listOf(
            TrainingFundamental.SEVENS_COUNT to radar.sevens,
            TrainingFundamental.DENARI_COUNT to radar.denari,
            TrainingFundamental.PRIMIERA_LEAD to radar.primiera,
        ).minBy { it.second }.first
    }
}
