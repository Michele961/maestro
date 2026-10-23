package it.maestro.ai.analysis

/**
 * Classificazione della qualità di una mossa giocata rispetto alla migliore alternativa legale
 * nella vista del giocatore di turno (`docs/ai/maestro.md` §Analizzatore, `docs/rules/glossary.md`, task F0-11).
 *
 * Le soglie provvisorie di perdita (`loss = bestValue - playedValue`, espressa in punti di smazzata attesi)
 * sono centralizzate in [MatchAnalyzer]:
 * - [BEST]: perdita `<=` [MatchAnalyzer.BEST_MAX_LOSS] (`0.05`).
 * - [INACCURACY]: [MatchAnalyzer.BEST_MAX_LOSS] `<` perdita `<=` [MatchAnalyzer.INACCURACY_MAX_LOSS] (`0.30`).
 * - [BLUNDER]: perdita `>` [MatchAnalyzer.INACCURACY_MAX_LOSS] (`0.30`).
 */
public enum class MoveGrade {
    /** Mossa Magistrale: perdita entro la soglia minima (`<= 0.05`). */
    BEST,

    /** Imprecisione: perdita piccola (`0.05 < perdita <= 0.30`). */
    INACCURACY,

    /** Errore grave: perdita superiore alla soglia di imprecisione (`> 0.30`). */
    BLUNDER,
}
