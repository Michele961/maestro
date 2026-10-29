package it.maestro.app.domain.settings

/**
 * Velocità di gioco scelta dall'utente (`docs/app/game-feel.md` §2, task F1R-09): divide tutte le durate di
 * `MotionSpec`. [INSTANT] azzera le attese ed è pensata per i test di partita completa.
 */
public enum class GameSpeed(public val multiplier: Double) {
    NORMAL(1.0),
    FAST(1.5),
    FASTER(2.0),
    INSTANT(Double.POSITIVE_INFINITY),
    ;

    public companion object {
        /** Velocità selezionabili dall'utente nelle impostazioni, in ordine. */
        public val USER_CHOICES: List<GameSpeed> = listOf(NORMAL, FAST, FASTER)
    }
}
