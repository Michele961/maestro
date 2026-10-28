package it.maestro.app.domain.puzzle

import it.maestro.app.R

/**
 * Modello descrittivo per i livelli di difficoltà progressivi della Sfida del Giorno.
 */
public data class DailyPuzzleTier(
    val index: Int,
    val titleRes: Int,
    val subtitleRes: Int,
    val rewardCredits: Int,
)

/** Cosa insegna un puzzle risolto, con il testo del concetto tattico e quello della regola d'oro del Maestro. */
public enum class PuzzleLesson(
    public val conceptRes: Int,
    public val ruleRes: Int,
) {
    SWEEP(R.string.puzzle_concept_sweep, R.string.puzzle_rule_sweep),
    SETTEBELLO(R.string.puzzle_concept_settebello, R.string.puzzle_rule_settebello),
    SEVEN_OR_SIX(R.string.puzzle_concept_seven_or_six, R.string.puzzle_rule_seven_or_six),
    DENARI(R.string.puzzle_concept_denari, R.string.puzzle_rule_denari),
    WIN_MATCH(R.string.puzzle_concept_win_match, R.string.puzzle_rule_win_match),
    BALANCE(R.string.puzzle_concept_balance, R.string.puzzle_rule_balance),
}
