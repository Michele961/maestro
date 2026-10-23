package it.maestro.ai.bot

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView

private val SETTEBELLO: Card = Card(rank = Rank.SETTE, suit = Suit.DENARI)

/**
 * Bot euristico avido (`docs/ai/maestro.md`, task F0-08).
 *
 * Regola di scelta:
 * - Se esiste almeno una mossa con presa (`capture.isNotEmpty()`), sceglie tra le mosse con presa
 *   quella che massimizza in ordine lessicografico:
 *   1. **fa scopa** (svuota il tavolo secondo spec §5.1, esclusa l'ultima giocata dell'ultima
 *      distribuzione della smazzata secondo spec §5.2);
 *   2. **prende il settebello** (`7D` presente tra le carte che finiscono nel mazzetto delle prese:
 *      in `capture` oppure come carta giocata in una presa, spec §4.7);
 *   3. **più denari presi** (numero di carte di seme [Suit.DENARI] che finiscono nel mazzetto delle
 *      prese con la mossa, spec §4.7);
 *   4. **più carte prese** (numero totale di carte che finiscono nel mazzetto delle prese con la mossa);
 *   5. a parità di tutti i criteri, **prima in `legal`**.
 * - Se nessuna mossa effettua una presa (`capture.isEmpty()` per tutte le mosse), scarta la carta con
 *   **valore di presa più basso** ([Rank.value], spec §1.2); a parità, sceglie la **prima in `legal`**.
 *
 * Può essere utilizzato sia come istanza (`GreedyBot()`) sia tramite il companion object (`GreedyBot`).
 */
public class GreedyBot : Bot {

    override fun chooseMove(view: ScopaView, legal: List<ScopaMove>, rng: SeededRng): ScopaMove {
        require(legal.isNotEmpty()) { "La lista delle mosse legali non può essere vuota" }

        val capturingMoves = legal.filter { it.capture.isNotEmpty() }
        if (capturingMoves.isEmpty()) {
            return legal.minBy { it.card.rank.value }
        }

        // Spec §5.2: l'ultima giocata dell'ultima distribuzione della smazzata non vale mai scopa,
        // anche se svuota il tavolo.
        val isLastPlayOfDeal = view.deckSize == 0 && view.hand.size == 1 && view.opponentHandSize == 0

        var bestMove = capturingMoves.first()
        for (i in 1 until capturingMoves.size) {
            val candidate = capturingMoves[i]
            if (compareCapturingMoves(candidate, bestMove, view, isLastPlayOfDeal) > 0) {
                bestMove = candidate
            }
        }
        return bestMove
    }

    /** Istanza condivisa senza stato di [GreedyBot]. */
    public companion object : Bot {
        private val INSTANCE: GreedyBot = GreedyBot()

        override fun chooseMove(view: ScopaView, legal: List<ScopaMove>, rng: SeededRng): ScopaMove =
            INSTANCE.chooseMove(view, legal, rng)
    }
}

private fun compareCapturingMoves(
    a: ScopaMove,
    b: ScopaMove,
    view: ScopaView,
    isLastPlayOfDeal: Boolean,
): Int {
    val sweepDiff = a.makesSweep(view, isLastPlayOfDeal).compareTo(b.makesSweep(view, isLastPlayOfDeal))
    if (sweepDiff != 0) return sweepDiff

    val settebelloDiff = a.capturesSettebello().compareTo(b.capturesSettebello())
    if (settebelloDiff != 0) return settebelloDiff

    val denariDiff = a.denariCapturedCount().compareTo(b.denariCapturedCount())
    if (denariDiff != 0) return denariDiff

    return a.cardsCapturedCount().compareTo(b.cardsCapturedCount())
}

private fun ScopaMove.makesSweep(view: ScopaView, isLastPlayOfDeal: Boolean): Boolean =
    !isLastPlayOfDeal && capture.size == view.table.size && view.table.all { it in capture }

private fun ScopaMove.capturesSettebello(): Boolean =
    card == SETTEBELLO || SETTEBELLO in capture

private fun ScopaMove.denariCapturedCount(): Int =
    capture.count { it.suit == Suit.DENARI } + if (card.suit == Suit.DENARI) 1 else 0

private fun ScopaMove.cardsCapturedCount(): Int =
    capture.size + 1
