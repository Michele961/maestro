package it.maestro.ai.puzzle

import it.maestro.ai.bot.unseenCards
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.GameEvent
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScoreCategory
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaState
import it.maestro.game.scopa.ScopaView
import it.maestro.game.scopa.determinize
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.play

/**
 * Risolutore e validatore esatto di puzzle tattici di Scopa (`docs/ai/maestro.md`, task F2-02).
 *
 * Determina se uno stato di smazzata ammette una soluzione univoca rispetto a un [PuzzleGoal].
 * Nelle situazioni di endgame (`deckSize == 0`), sfrutta la deduzione certa delle carte avversarie
 * e l'albero di ricerca minimax esatto con potatura alfa-beta.
 */
public object TacticalPuzzleSolver {

    /**
     * Risolve il puzzle calcolando la sequenza di mosse vincenti per [view] dato l'obiettivo [goal].
     *
     * Restituisce la lista ordinata delle mosse vincenti del giocatore di turno se e solo se esiste
     * un'**unica** prima mossa vincente che soddisfa [goal]. Se esistono soluzioni multiple (parità)
     * o nessuna mossa soddisfa l'obiettivo, restituisce `null`.
     */
    public fun solve(view: ScopaView, goal: PuzzleGoal): List<ScopaMove>? {
        if (view.phase != DealPhase.PLAYING || view.seat != view.toMove || view.hand.size < 2) {
            return null
        }

        return when {
            view.deckSize == 0 -> solveEndgame(view, goal)
            goal == PuzzleGoal.MAKE_SWEEP -> solveImmediateSweep(view)
            else -> null
        }
    }

    /**
     * Risolve il puzzle specificato verificando che la vista iniziale conduca esattamente alla soluzione attesa.
     */
    public fun solve(puzzle: TacticalPuzzle): List<ScopaMove>? = solve(puzzle.initialView, puzzle.goal)

    /**
     * Verifica se una mossa [candidateMove] proposta dal giocatore corrisponde alla mossa corretta del puzzle
     * per il turno corrispondente a [plyIndex] (default `0`, prima mossa da individuare).
     */
    public fun validateMove(puzzle: TacticalPuzzle, candidateMove: ScopaMove, plyIndex: Int = 0): Boolean {
        if (plyIndex !in puzzle.solutionMoves.indices) {
            return false
        }
        return candidateMove == puzzle.solutionMoves[plyIndex]
    }

    /**
     * Determina se la posizione [view] costituisce un puzzle tattico valido per [goal].
     */
    public fun isTacticalPuzzle(view: ScopaView, goal: PuzzleGoal): Boolean = solve(view, goal) != null

    /**
     * Individua la mossa minimax ottimale per il giocatore di turno nella vista [view] a mazzo vuoto.
     * Restituisce `null` se il mazzo non è vuoto o se non ci sono mosse legali.
     */
    public fun findBestMove(view: ScopaView): ScopaMove? {
        if (view.phase != DealPhase.PLAYING || view.deckSize != 0 || view.hand.isEmpty()) {
            return null
        }
        val unseen = unseenCards(view)
        val state = determinize(view = view, opponentHand = unseen, deckOrder = emptyList())
        val legal = legalMoves(state)
        if (legal.isEmpty()) {
            return null
        }
        val evaluated = legal.map { move ->
            evaluateRootMove(state = state, rootMove = move, perspective = state.toMove)
        }
        return evaluated.maxByOrNull { it.netPoints }?.move
    }

    private fun solveEndgame(view: ScopaView, goal: PuzzleGoal): List<ScopaMove>? {
        val unseen = unseenCards(view)
        val state = determinize(view = view, opponentHand = unseen, deckOrder = emptyList())
        val legal = legalMoves(state)
        if (legal.size < 2) {
            return null
        }

        val perspective = state.toMove
        val evaluated = legal.map { move ->
            evaluateRootMove(state = state, rootMove = move, perspective = perspective)
        }

        return when (goal) {
            PuzzleGoal.MAKE_SWEEP -> resolveSweepPuzzle(evaluated)
            PuzzleGoal.WIN_MATCH -> resolveWinMatchPuzzle(evaluated)
            PuzzleGoal.MAXIMIZE_POINTS -> resolveMaximizePointsPuzzle(evaluated)
        }
    }

    private fun resolveSweepPuzzle(evaluated: List<EvaluatedRootMove>): List<ScopaMove>? {
        val sweepMoves = evaluated.filter { it.makesSweep }
        if (sweepMoves.size != 1) {
            return null
        }
        val winning = sweepMoves.first()
        return listOf(winning.move) + winning.playerContinuation
    }

    private fun resolveWinMatchPuzzle(evaluated: List<EvaluatedRootMove>): List<ScopaMove>? {
        val winningMoves = evaluated.filter { it.matchWin }
        if (winningMoves.size != 1) {
            return null
        }
        val winning = winningMoves.first()
        return listOf(winning.move) + winning.playerContinuation
    }

    private fun resolveMaximizePointsPuzzle(evaluated: List<EvaluatedRootMove>): List<ScopaMove>? {
        val maxPoints = evaluated.maxOf { it.netPoints }
        val bestMoves = evaluated.filter { it.netPoints == maxPoints }
        if (bestMoves.size != 1) {
            return null
        }
        val winning = bestMoves.first()
        val secondBestPoints = evaluated.filter { it.move != winning.move }.maxOf { it.netPoints }
        // Un puzzle di massimizzazione richiede un vantaggio netto di almeno 1 punto rispetto a qualunque alternativa
        if (maxPoints - secondBestPoints < 1) {
            return null
        }
        return listOf(winning.move) + winning.playerContinuation
    }

    private fun solveImmediateSweep(view: ScopaView): List<ScopaMove>? {
        if (view.table.isEmpty() || view.hand.size < 2) {
            return null
        }
        val dummyState = ScopaState(
            config = view.config,
            dealer = view.dealer,
            toMove = view.toMove,
            hands = mapOf(view.toMove to view.hand, view.toMove.other() to emptyList()),
            table = view.table,
            deck = emptyList(),
            rngState = 0L,
            phase = DealPhase.PLAYING,
            distribution = 1,
            capturedBy = emptyMap(),
            sweeps = emptyMap(),
            lastCapturer = null,
            totals = emptyMap(),
            winner = null,
            playedCards = emptyList(),
        )
        val legal = legalMoves(dummyState)
        if (legal.size < 2) {
            return null
        }

        val tableSet = view.table.toSet()
        val sweepMoves = legal.filter { it.capture.isNotEmpty() && it.capture == tableSet }

        if (sweepMoves.size != 1) {
            return null
        }
        return listOf(sweepMoves.first())
    }

    private fun evaluateRootMove(
        state: ScopaState,
        rootMove: ScopaMove,
        perspective: Seat,
    ): EvaluatedRootMove {
        val applied = play(state, rootMove) as? PlayResult.Applied
            ?: error("Mossa legale rifiutata nel risolutore: $rootMove")
        val dealEnded = applied.events.filterIsInstance<GameEvent.DealEnded>().firstOrNull()
        val sweepOnRoot = applied.events.any { it is GameEvent.Sweep && it.seat == perspective }

        if (dealEnded != null) {
            val myPts = dealEnded.score.totalPoints(perspective)
            val oppPts = dealEnded.score.totalPoints(perspective.other())
            val net = myPts - oppPts
            val myTotal = state.totals.getValue(perspective) + myPts
            val oppTotal = state.totals.getValue(perspective.other()) + oppPts
            val win = myTotal >= state.config.targetScore && myTotal > oppTotal
            return EvaluatedRootMove(
                move = rootMove,
                netPoints = net,
                matchWin = win,
                makesSweep = sweepOnRoot,
                playerContinuation = emptyList(),
            )
        }

        val searchResult = search(
            state = applied.state,
            perspective = perspective,
            alpha = Int.MIN_VALUE,
            beta = Int.MAX_VALUE,
        )

        // Nel child PV, il turno iniziale è dell'avversario (indice 0);
        // le mosse successive del giocatore analizzato sono agli indici dispari (1, 3, ...).
        val playerContinuation = searchResult.principalVariation.filterIndexed { index, _ -> index % 2 == 1 }

        return EvaluatedRootMove(
            move = rootMove,
            netPoints = searchResult.netPoints,
            matchWin = searchResult.matchWin,
            makesSweep = sweepOnRoot || searchResult.sweepsCount > state.sweeps.getValue(perspective),
            playerContinuation = playerContinuation,
        )
    }

    private fun search(
        state: ScopaState,
        perspective: Seat,
        alpha: Int,
        beta: Int,
    ): SearchResult {
        val mover = state.toMove
        val isMaximizing = (mover == perspective)
        val legal = legalMoves(state)
        check(legal.isNotEmpty()) { "Stato di gioco senza mosse legali durante la ricerca: $state" }

        var currentAlpha = alpha
        var currentBeta = beta

        if (isMaximizing) {
            var bestValue = Int.MIN_VALUE
            var bestResult: SearchResult? = null

            for (move in legal) {
                val applied = play(state, move) as? PlayResult.Applied
                    ?: error("Mossa legale rifiutata durante minimax: $move")
                val dealEnded = applied.events.filterIsInstance<GameEvent.DealEnded>().firstOrNull()
                val sweep = applied.events.any { it is GameEvent.Sweep && it.seat == perspective }

                val result = if (dealEnded != null) {
                    val myPts = dealEnded.score.totalPoints(perspective)
                    val oppPts = dealEnded.score.totalPoints(perspective.other())
                    val myTot = state.totals.getValue(perspective) + myPts
                    val oppTot = state.totals.getValue(perspective.other()) + oppPts
                    SearchResult(
                        netPoints = myPts - oppPts,
                        matchWin = myTot >= state.config.targetScore && myTot > oppTot,
                        sweepsCount = dealEnded.score.points(perspective, ScoreCategory.SCOPE),
                        principalVariation = listOf(move),
                    )
                } else {
                    val child = search(applied.state, perspective, currentAlpha, currentBeta)
                    SearchResult(
                        netPoints = child.netPoints,
                        matchWin = child.matchWin,
                        sweepsCount = child.sweepsCount + (if (sweep) 1 else 0),
                        principalVariation = listOf(move) + child.principalVariation,
                    )
                }

                if (result.netPoints > bestValue) {
                    bestValue = result.netPoints
                    bestResult = result
                }
                currentAlpha = maxOf(currentAlpha, bestValue)
                if (currentAlpha >= currentBeta) {
                    break
                }
            }

            return bestResult ?: error("Nessun risultato prodotto nel ramo massimizzante")
        } else {
            var bestValue = Int.MAX_VALUE
            var bestResult: SearchResult? = null

            for (move in legal) {
                val applied = play(state, move) as? PlayResult.Applied
                    ?: error("Mossa legale rifiutata durante minimax: $move")
                val dealEnded = applied.events.filterIsInstance<GameEvent.DealEnded>().firstOrNull()
                val sweep = applied.events.any { it is GameEvent.Sweep && it.seat == perspective }

                val result = if (dealEnded != null) {
                    val myPts = dealEnded.score.totalPoints(perspective)
                    val oppPts = dealEnded.score.totalPoints(perspective.other())
                    val myTot = state.totals.getValue(perspective) + myPts
                    val oppTot = state.totals.getValue(perspective.other()) + oppPts
                    SearchResult(
                        netPoints = myPts - oppPts,
                        matchWin = myTot >= state.config.targetScore && myTot > oppTot,
                        sweepsCount = dealEnded.score.points(perspective, ScoreCategory.SCOPE),
                        principalVariation = listOf(move),
                    )
                } else {
                    val child = search(applied.state, perspective, currentAlpha, currentBeta)
                    SearchResult(
                        netPoints = child.netPoints,
                        matchWin = child.matchWin,
                        sweepsCount = child.sweepsCount + (if (sweep) 1 else 0),
                        principalVariation = listOf(move) + child.principalVariation,
                    )
                }

                if (result.netPoints < bestValue) {
                    bestValue = result.netPoints
                    bestResult = result
                }
                currentBeta = minOf(currentBeta, bestValue)
                if (currentAlpha >= currentBeta) {
                    break
                }
            }

            return bestResult ?: error("Nessun risultato prodotto nel ramo minimizzante")
        }
    }
}

private data class EvaluatedRootMove(
    val move: ScopaMove,
    val netPoints: Int,
    val matchWin: Boolean,
    val makesSweep: Boolean,
    val playerContinuation: List<ScopaMove>,
)

private data class SearchResult(
    val netPoints: Int,
    val matchWin: Boolean,
    val sweepsCount: Int,
    val principalVariation: List<ScopaMove>,
)
