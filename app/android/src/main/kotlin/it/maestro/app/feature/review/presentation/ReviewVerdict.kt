package it.maestro.app.feature.review.presentation

import it.maestro.ai.analysis.MoveAnalysis
import it.maestro.ai.analysis.MoveGrade

/**
 * Giudizio mostrato al giocatore su una mossa: l'analizzatore distingue ottima, imprecisa e grave, ma una perdita
 * trascurabile non è un errore da far notare e non è nemmeno «la mossa migliore» (decisione di Michele, 2026-10-27).
 */
public enum class ReviewVerdict {
    /** La mossa migliore (o pari). */
    BEST,

    /** Una mossa buona: differenza trascurabile dalla migliore, niente errore. */
    GOOD,

    /** Imprecisione. */
    INACCURACY,

    /** Errore grave. */
    BLUNDER,
}

/** Perdita (in punti di smazzata attesi) sotto cui una differenza dalla mossa migliore è solo rumore della stima. */
public const val NEGLIGIBLE_LOSS: Double = 0.10

/** Giudizio mostrato per questa mossa. */
public fun MoveAnalysis.verdict(): ReviewVerdict = when (grade) {
    MoveGrade.BEST -> ReviewVerdict.BEST
    MoveGrade.INACCURACY -> if (loss <= NEGLIGIBLE_LOSS) ReviewVerdict.GOOD else ReviewVerdict.INACCURACY
    MoveGrade.BLUNDER -> ReviewVerdict.BLUNDER
}

/** `true` se la mossa va segnalata come errore (imprecisione o grave): si spiega, si può riprovare, si salta con «Errore». */
public fun MoveAnalysis.isMistake(): Boolean = verdict().let { it == ReviewVerdict.INACCURACY || it == ReviewVerdict.BLUNDER }
