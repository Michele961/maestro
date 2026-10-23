package it.maestro.ai.bot

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.model.fullDeck
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.GameEvent
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaState
import it.maestro.game.scopa.ScopaView
import it.maestro.game.scopa.determinize
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.play
import it.maestro.game.scopa.primieraPoints

private val SETTEBELLO_CARD: Card = Card(rank = Rank.SETTE, suit = Suit.DENARI)

/**
 * Profondità predefinita in semimosse (ply) per la ricerca minimax alfa-beta quando il mazzo non è vuoto
 * (6 semimosse = fino a 3 turni per giocatore, pari a un'intera distribuzione di 3 carte a testa).
 */
private const val NON_FINAL_DEAL_SEARCH_DEPTH: Int = 6

/**
 * Numero massimo di semimosse rimanenti nell'ultima distribuzione (`deckSize == 0`, al più 3 + 3 carte).
 */
private const val FINAL_DISTRIBUTION_MAX_DEPTH: Int = 6

/**
 * Pesi provvisori della funzione di valutazione euristica a profondità limitata (`docs/ai/maestro.md` §PIMC).
 *
 * I pesi sono definiti come costanti nominate in questo unico punto e verranno tarati in F0-13:
 * `scope fatte × 1 + settebello preso × 1 + 0,1 × (differenza denari presi) + 0,05 × (differenza carte prese)
 * + 0,02 × (differenza delle somme di primiera parziali)`, dal punto di vista del giocatore valutato.
 */
internal object ProvisionalHeuristicWeights {
    internal const val SWEEP: Double = 1.0
    internal const val SETTEBELLO: Double = 1.0
    internal const val DENARI_DIFF: Double = 0.1
    internal const val CARDS_DIFF: Double = 0.05
    internal const val PRIMIERA_DIFF: Double = 0.02
}

/**
 * Distribuzione ipotetica delle carte non viste (mano avversaria e ordine del mazzo coperto)
 * compatibile con una [ScopaView] (`docs/ai/maestro.md` §PIMC, task F0-10).
 */
internal data class SampledDistribution(
    val opponentHand: List<Card>,
    val deckOrder: List<Card>,
)

/**
 * Implementazione del valutatore [MoveEvaluator] basata su **PIMC (Perfect Information Monte Carlo)**
 * e risolutore esatto dell'ultima distribuzione (`docs/ai/maestro.md` §PIMC, task F0-10).
 *
 * Algoritmo:
 * 1. Dalla [ScopaView] ricava l'insieme delle carte non viste (`unseenCards` = mano avversaria + mazzo).
 * 2. **Ultima distribuzione (`view.deckSize == 0`):** la mano avversaria è dedotta per esclusione senza
 *    consumare campioni casuali; lo stato completo è ricostruito con [determinize] e risolto **esattamente**
 *    con ricerca minimax e potatura alfa-beta fino a fine smazzata (`punti giocatore − punti avversario`, spec §6).
 * 3. **Mazzo non vuoto (`view.deckSize > 0`):** genera `budget.samples` distribuzioni compatibili mescolando
 *    `unseenCards` tramite [SeededRng.shuffle], ricostruisce lo stato con [determinize] e valuta ogni mossa
 *    legale tramite minimax con potatura alfa-beta entro [SearchBudget.maxNodes] e profondità limitata,
 *    usando a fine smazzata il punteggio esatto (spec §6) e ai nodi interni di taglio la valutazione provvisoria
 *    con i pesi di [ProvisionalHeuristicWeights].
 * 4. Il valore atteso di ciascuna mossa è la media aritmetica dei valori ottenuti sui campioni.
 */
public object PimcEvaluator : MoveEvaluator {

    override fun evaluate(
        view: ScopaView,
        legal: List<ScopaMove>,
        rng: SeededRng,
        budget: SearchBudget,
    ): List<MoveValue> {
        require(legal.isNotEmpty()) { "La lista delle mosse legali non può essere vuota" }
        require(view.phase == DealPhase.PLAYING) {
            "La vista deve essere in fase PLAYING per valutare le mosse, trovato: ${view.phase}"
        }
        require(view.seat == view.toMove) {
            "La vista deve appartenere al giocatore di turno (${view.toMove}), trovato: ${view.seat}"
        }

        val perspective = view.toMove
        val unseen = unseenCards(view)

        // Ultima distribuzione (mazzo vuoto): la mano avversaria è nota per esclusione.
        // Ricerca esatta fino a fine smazzata senza campionamento (docs/ai/maestro.md §PIMC).
        if (view.deckSize == 0) {
            val exactState = determinize(
                view = view,
                opponentHand = unseen,
                deckOrder = emptyList(),
            )
            return legal.map { move ->
                val exactValue = evaluateRootMove(
                    rootState = exactState,
                    move = move,
                    perspective = perspective,
                    maxDepth = FINAL_DISTRIBUTION_MAX_DEPTH,
                    nodeBudget = NodeBudget(maxNodes = Int.MAX_VALUE),
                )
                MoveValue(move = move, value = exactValue)
            }
        }

        val samplesCount = budget.samples
        val sumValues = DoubleArray(legal.size)
        val remainingPliesInDeal = view.hand.size + view.opponentHandSize + view.deckSize
        val searchDepth = minOf(NON_FINAL_DEAL_SEARCH_DEPTH, remainingPliesInDeal)
        val nodesPerRootMove = maxOf(1, budget.maxNodes / legal.size)

        for (sampleIndex in 0 until samplesCount) {
            val distribution = sampleCompatibleDistribution(view = view, unseen = unseen, rng = rng)
            val determinizedState = determinize(
                view = view,
                opponentHand = distribution.opponentHand,
                deckOrder = distribution.deckOrder,
            )

            for (moveIndex in legal.indices) {
                val move = legal[moveIndex]
                val nodeBudget = NodeBudget(maxNodes = nodesPerRootMove)
                val sampleValue = evaluateRootMove(
                    rootState = determinizedState,
                    move = move,
                    perspective = perspective,
                    maxDepth = searchDepth,
                    nodeBudget = nodeBudget,
                )
                sumValues[moveIndex] += sampleValue
            }
        }

        return List(legal.size) { index ->
            MoveValue(
                move = legal[index],
                value = sumValues[index] / samplesCount.toDouble(),
            )
        }
    }
}

/**
 * Ricava in ordine canonico tutte le carte non ancora viste dal giocatore nella smazzata corrente
 * (`docs/ai/maestro.md` §PIMC punto 1).
 *
 * Una carta è vista se appartiene a `view.hand`, `view.table`, `view.capturedBy[P1]`,
 * `view.capturedBy[P2]` o `view.playedCards`.
 */
internal fun unseenCards(view: ScopaView): List<Card> {
    val p1Captured = view.capturedBy[Seat.P1].orEmpty()
    val p2Captured = view.capturedBy[Seat.P2].orEmpty()
    val seenCards = buildSet {
        addAll(view.hand)
        addAll(view.table)
        addAll(p1Captured)
        addAll(p2Captured)
        addAll(view.playedCards)
    }
    val unseen = fullDeck().filterNot { it in seenCards }
    require(unseen.size == view.opponentHandSize + view.deckSize) {
        "Numero di carte non viste (${unseen.size}) non coerente con opponentHandSize (${view.opponentHandSize}) " +
            "+ deckSize (${view.deckSize})"
    }
    return unseen
}

/**
 * Campiona una distribuzione compatibile con [view] a partire dalle carte non viste [unseen]
 * usando [rng] (`docs/ai/maestro.md` §PIMC punto 2).
 */
internal fun sampleCompatibleDistribution(
    view: ScopaView,
    unseen: List<Card> = unseenCards(view),
    rng: SeededRng,
): SampledDistribution {
    if (unseen.isEmpty()) {
        return SampledDistribution(opponentHand = emptyList(), deckOrder = emptyList())
    }
    val shuffled = rng.shuffle(unseen)
    val opponentHand = shuffled.subList(0, view.opponentHandSize).toList()
    val deckOrder = shuffled.subList(view.opponentHandSize, shuffled.size).toList()
    return SampledDistribution(
        opponentHand = opponentHand,
        deckOrder = deckOrder,
    )
}

/**
 * Calcola la valutazione provvisoria dello stato [state] a profondità limitata dal punto di vista
 * di [perspective] secondo la formula normativa di `docs/ai/maestro.md` §PIMC punto 3:
 *
 * `scope fatte × 1 + settebello preso × 1 + 0,1 × (differenza denari presi)
 * + 0,05 × (differenza carte prese) + 0,02 × (differenza delle somme di primiera parziali)`.
 */
internal fun evaluateProvisionalState(state: ScopaState, perspective: Seat): Double {
    val opponent = perspective.other()
    val myCaptured = state.captured(perspective)
    val oppCaptured = state.captured(opponent)

    val sweepDiff = state.sweeps.getValue(perspective) - state.sweeps.getValue(opponent)

    val mySettebello = if (SETTEBELLO_CARD in myCaptured) 1 else 0
    val oppSettebello = if (SETTEBELLO_CARD in oppCaptured) 1 else 0
    val settebelloDiff = mySettebello - oppSettebello

    val myDenari = myCaptured.count { it.suit == Suit.DENARI }
    val oppDenari = oppCaptured.count { it.suit == Suit.DENARI }
    val denariDiff = myDenari - oppDenari

    val cardsDiff = myCaptured.size - oppCaptured.size

    val myPartialPrimiera = partialPrimieraSum(myCaptured)
    val oppPartialPrimiera = partialPrimieraSum(oppCaptured)
    val primieraDiff = myPartialPrimiera - oppPartialPrimiera

    return sweepDiff * ProvisionalHeuristicWeights.SWEEP +
        settebelloDiff * ProvisionalHeuristicWeights.SETTEBELLO +
        denariDiff * ProvisionalHeuristicWeights.DENARI_DIFF +
        cardsDiff * ProvisionalHeuristicWeights.CARDS_DIFF +
        primieraDiff * ProvisionalHeuristicWeights.PRIMIERA_DIFF
}

/**
 * Calcola la somma di primiera parziale per un mazzetto di prese: somma sui 4 semi del massimo
 * valore [primieraPoints] tra le carte prese in ciascun seme (`0` per i semi non ancora coperti).
 */
internal fun partialPrimieraSum(captured: List<Card>): Int {
    var maxDenari = 0
    var maxCoppe = 0
    var maxSpade = 0
    var maxBastoni = 0
    for (card in captured) {
        val pts = card.rank.primieraPoints
        when (card.suit) {
            Suit.DENARI -> if (pts > maxDenari) maxDenari = pts
            Suit.COPPE -> if (pts > maxCoppe) maxCoppe = pts
            Suit.SPADE -> if (pts > maxSpade) maxSpade = pts
            Suit.BASTONI -> if (pts > maxBastoni) maxBastoni = pts
        }
    }
    return maxDenari + maxCoppe + maxSpade + maxBastoni
}

private class NodeBudget(val maxNodes: Int) {
    var visitedNodes: Int = 0

    fun consumeNode(): Boolean {
        visitedNodes++
        return visitedNodes <= maxNodes
    }

    val isExhausted: Boolean
        get() = visitedNodes >= maxNodes
}

private fun evaluateRootMove(
    rootState: ScopaState,
    move: ScopaMove,
    perspective: Seat,
    maxDepth: Int,
    nodeBudget: NodeBudget,
): Double {
    nodeBudget.consumeNode()
    val applied = play(rootState, move) as? PlayResult.Applied
        ?: error("Mossa legale rifiutata durante la ricerca PIMC: $move")

    val dealEnded = applied.events.filterIsInstance<GameEvent.DealEnded>().firstOrNull()
    if (dealEnded != null) {
        return exactDealValue(dealEnded, perspective)
    }

    return alphaBeta(
        state = applied.state,
        perspective = perspective,
        remainingDepth = maxDepth - 1,
        alpha = Double.NEGATIVE_INFINITY,
        beta = Double.POSITIVE_INFINITY,
        nodeBudget = nodeBudget,
    )
}

private fun alphaBeta(
    state: ScopaState,
    perspective: Seat,
    remainingDepth: Int,
    alpha: Double,
    beta: Double,
    nodeBudget: NodeBudget,
): Double {
    if (remainingDepth <= 0 || nodeBudget.isExhausted) {
        return evaluateProvisionalState(state, perspective)
    }

    val legal = legalMoves(state)
    if (legal.isEmpty()) {
        return evaluateProvisionalState(state, perspective)
    }

    val isMaximizing = state.toMove == perspective
    var currentAlpha = alpha
    var currentBeta = beta

    if (isMaximizing) {
        var bestValue = Double.NEGATIVE_INFINITY
        for (move in legal) {
            if (!nodeBudget.consumeNode()) {
                return if (bestValue == Double.NEGATIVE_INFINITY) {
                    evaluateProvisionalState(state, perspective)
                } else {
                    bestValue
                }
            }
            val applied = play(state, move) as? PlayResult.Applied
                ?: error("Mossa legale rifiutata in alphaBeta: $move")
            val dealEnded = applied.events.filterIsInstance<GameEvent.DealEnded>().firstOrNull()
            val childValue = if (dealEnded != null) {
                exactDealValue(dealEnded, perspective)
            } else {
                alphaBeta(
                    state = applied.state,
                    perspective = perspective,
                    remainingDepth = remainingDepth - 1,
                    alpha = currentAlpha,
                    beta = currentBeta,
                    nodeBudget = nodeBudget,
                )
            }
            if (childValue > bestValue) {
                bestValue = childValue
            }
            if (bestValue > currentAlpha) {
                currentAlpha = bestValue
            }
            if (currentAlpha >= currentBeta) {
                break
            }
        }
        return bestValue
    } else {
        var bestValue = Double.POSITIVE_INFINITY
        for (move in legal) {
            if (!nodeBudget.consumeNode()) {
                return if (bestValue == Double.POSITIVE_INFINITY) {
                    evaluateProvisionalState(state, perspective)
                } else {
                    bestValue
                }
            }
            val applied = play(state, move) as? PlayResult.Applied
                ?: error("Mossa legale rifiutata in alphaBeta: $move")
            val dealEnded = applied.events.filterIsInstance<GameEvent.DealEnded>().firstOrNull()
            val childValue = if (dealEnded != null) {
                exactDealValue(dealEnded, perspective)
            } else {
                alphaBeta(
                    state = applied.state,
                    perspective = perspective,
                    remainingDepth = remainingDepth - 1,
                    alpha = currentAlpha,
                    beta = currentBeta,
                    nodeBudget = nodeBudget,
                )
            }
            if (childValue < bestValue) {
                bestValue = childValue
            }
            if (bestValue < currentBeta) {
                currentBeta = bestValue
            }
            if (currentAlpha >= currentBeta) {
                break
            }
        }
        return bestValue
    }
}

private fun exactDealValue(dealEnded: GameEvent.DealEnded, perspective: Seat): Double {
    val opponent = perspective.other()
    val myPoints = dealEnded.score.totalPoints(perspective)
    val oppPoints = dealEnded.score.totalPoints(opponent)
    return (myPoints - oppPoints).toDouble()
}
