package it.maestro.ai.bot

import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView

/**
 * Bot di riferimento che sceglie uniformemente a caso una mossa legale usando il [SeededRng]
 * ricevuto (`docs/ai/maestro.md`, task F0-08).
 *
 * A parità di [legal] e dello stato di [rng], restituisce sempre la stessa mossa.
 * Può essere utilizzato sia come istanza (`RandomBot()`) sia tramite il companion object (`RandomBot`).
 */
public class RandomBot : Bot {

    override fun chooseMove(view: ScopaView, legal: List<ScopaMove>, rng: SeededRng): ScopaMove {
        require(legal.isNotEmpty()) { "La lista delle mosse legali non può essere vuota" }
        return legal[rng.nextInt(legal.size)]
    }

    /** Istanza condivisa senza stato di [RandomBot]. */
    public companion object : Bot {
        private val INSTANCE: RandomBot = RandomBot()

        override fun chooseMove(view: ScopaView, legal: List<ScopaMove>, rng: SeededRng): ScopaMove =
            INSTANCE.chooseMove(view, legal, rng)
    }
}
