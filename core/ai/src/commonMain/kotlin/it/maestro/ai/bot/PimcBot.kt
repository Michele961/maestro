package it.maestro.ai.bot

import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView

/**
 * Bot basato su **PIMC (Perfect Information Monte Carlo)** e ricerca esatta nell'ultima distribuzione
 * (`docs/ai/maestro.md` §Bot, §PIMC, task F0-10).
 *
 * Delega la stima del valore atteso di ciascuna mossa legale a [evaluator] con il [budget] configurato
 * e sceglie la mossa con valore atteso massimo; a parità di valore, sceglie la prima mossa secondo
 * l'ordine canonico di `legal` (`docs/ai/maestro.md` §PIMC punto 4).
 *
 * Può essere utilizzato sia come istanza configurabile (`PimcBot(SearchBudget(samples = 50))`) sia
 * tramite il companion object con budget predefinito (`PimcBot`).
 *
 * @property budget Budget di ricerca in campioni e nodi per mossa.
 * @property evaluator Valutatore delle mosse legali (default [PimcEvaluator]).
 */
public class PimcBot(
    public val budget: SearchBudget = SearchBudget(),
    public val evaluator: MoveEvaluator = PimcEvaluator,
) : Bot {

    override fun chooseMove(view: ScopaView, legal: List<ScopaMove>, rng: SeededRng): ScopaMove {
        require(legal.isNotEmpty()) { "La lista delle mosse legali non può essere vuota" }

        val evaluations = evaluator.evaluate(
            view = view,
            legal = legal,
            rng = rng,
            budget = budget,
        )

        var bestMoveValue = evaluations.first()
        for (i in 1 until evaluations.size) {
            val candidate = evaluations[i]
            if (candidate.value > bestMoveValue.value) {
                bestMoveValue = candidate
            }
        }
        return bestMoveValue.move
    }

    /** Istanza condivisa senza stato di [PimcBot] con [SearchBudget] predefinito. */
    public companion object : Bot {
        private val INSTANCE: PimcBot = PimcBot()

        override fun chooseMove(view: ScopaView, legal: List<ScopaMove>, rng: SeededRng): ScopaMove =
            INSTANCE.chooseMove(view, legal, rng)
    }
}
