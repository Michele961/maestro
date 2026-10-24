package it.maestro.ai.bot

import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView

/**
 * Bot basato su **PIMC (Perfect Information Monte Carlo)** e ricerca esatta nell'ultima distribuzione
 * (`docs/ai/maestro.md` §Bot, §PIMC, task F0-10, F1-03).
 *
 * Supporta configurazioni avanzate tramite [PimcConfig], inclusa l'iniezione di rumore decisionale
 * e la simulazione di memoria imperfetta sulle carte passate (decisione D3).
 *
 * @property config Configurazione operativa di parametri (budget, rumore, decadimento mnemonico).
 * @property evaluator Valutatore delle mosse legali (default [PimcEvaluator]).
 */
public class PimcBot(
    public val config: PimcConfig = PimcConfig(),
    public val evaluator: MoveEvaluator = PimcEvaluator,
) : Bot {

    public constructor(
        budget: SearchBudget,
        evaluator: MoveEvaluator = PimcEvaluator,
    ) : this(config = PimcConfig(budget = budget), evaluator = evaluator)

    public val budget: SearchBudget get() = config.budget

    override fun chooseMove(view: ScopaView, legal: List<ScopaMove>, rng: SeededRng): ScopaMove {
        require(legal.isNotEmpty()) { "La lista delle mosse legali non può essere vuota" }

        // 1. Rumore decisionale stocastico (decisione D3):
        if (config.noiseProbability > 0.0) {
            val noiseThreshold = (config.noiseProbability.coerceIn(0.0, 1.0) * 10000).toInt()
            if (rng.nextInt(10000) < noiseThreshold) {
                val randomIndex = rng.nextInt(legal.size)
                return legal[randomIndex]
            }
        }

        // 2. Memoria imperfetta (decisione D3, applicata rigorosamente solo sulla vista):
        val effectiveView = if (config.forgetRate > 0.0) {
            applyImperfectMemory(view = view, forgetRate = config.forgetRate, rng = rng)
        } else {
            view
        }

        val evaluations = evaluator.evaluate(
            view = effectiveView,
            legal = legal,
            rng = rng,
            budget = config.budget,
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
