package it.maestro.game.scopa

/**
 * Categorie di punteggio assegnate al termine di una smazzata di Scopa (`docs/rules/glossary.md`, spec §6.2).
 */
public enum class ScoreCategory {
    /**
     * Più carte prese (oltre 20; parità 20–20 assegna `0` a entrambi, spec §6.2).
     */
    CARTE,

    /**
     * Più carte di Denari prese (oltre 5; parità 5–5 assegna `0` a entrambi, spec §6.2).
     */
    DENARI,

    /**
     * Aver preso il 7 di Denari (spec §6.2).
     */
    SETTEBELLO,

    /**
     * Primiera più alta sui quattro semi (spec §6.2, §6.3).
     */
    PRIMIERA,

    /**
     * Un punto per ciascuna scopa realizzata durante la smazzata (spec §5.1, §6.2).
     */
    SCOPE,

    /**
     * Aver preso il Re di Denari, assegnato solo se la variante [ScopaConfig.reBello] è attiva (spec §6.2).
     */
    RE_BELLO,

    /**
     * I punti della napola (Asso, 2, 3… di Denari), assegnati solo se la variante [ScopaConfig.napola] è attiva (spec §6.4).
     */
    NAPOLA,
}
