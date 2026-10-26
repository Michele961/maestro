package it.maestro.ai.puzzle

import it.maestro.ai.bot.GreedyBot
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaView
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import it.maestro.game.scopa.viewFor

/**
 * Generatore deterministico di puzzle tattici di Scopa (`docs/backlog/fase-2.md` F2-02, ADR-0009).
 *
 * A partire da un [seed], simula partite di Scopa fino alla smazzata finale o all'endgame a mazzo vuoto,
 * individuando posizioni critiche che ammettono una soluzione univoca rispetto a un [PuzzleGoal].
 */
public object TacticalPuzzleGenerator {

    private const val MAX_ATTEMPTS: Int = 200

    /**
     * Genera deterministicamente un [TacticalPuzzle] a partire dal [seed] fornito.
     *
     * @param seed Seme pseudocasuale a 64 bit ([SeededRng]).
     * @param goal Obiettivo tattico richiesto, o `null` per selezionare automaticamente
     *             un obiettivo tra [PuzzleGoal.MAKE_SWEEP], [PuzzleGoal.MAXIMIZE_POINTS] e [PuzzleGoal.WIN_MATCH].
     * @return [TacticalPuzzle] garantito come risolvibile ed esattamente univoco.
     */
    public fun generate(
        seed: Long,
        goal: PuzzleGoal? = null,
    ): TacticalPuzzle {
        val rng = SeededRng(seed)
        var attempt = 0

        while (attempt < MAX_ATTEMPTS) {
            val dealSeed = rng.nextLong()
            val targetGoal = goal ?: when (rng.nextInt(3)) {
                0 -> PuzzleGoal.MAKE_SWEEP
                1 -> PuzzleGoal.MAXIMIZE_POINTS
                else -> PuzzleGoal.WIN_MATCH
            }

            val puzzle = searchPuzzleInDeal(
                dealSeed = dealSeed,
                targetGoal = targetGoal,
                sourceSeed = seed,
                attemptIndex = attempt,
            )
            if (puzzle != null) {
                return puzzle
            }
            attempt++
        }

        error("Impossibile generare deterministicamente un puzzle per il seed $seed entro $MAX_ATTEMPTS tentativi")
    }

    /**
     * Genera una sequenza deterministica di [count] puzzle tattici a partire dal [seed] specificato.
     */
    public fun generateBatch(
        seed: Long,
        count: Int,
        goal: PuzzleGoal? = null,
    ): List<TacticalPuzzle> {
        require(count > 0) { "Il numero di puzzle richiesti deve essere maggiore di zero, trovato: $count" }
        val rng = SeededRng(seed)
        return List(count) {
            generate(seed = rng.nextLong(), goal = goal)
        }
    }

    private fun searchPuzzleInDeal(
        dealSeed: Long,
        targetGoal: PuzzleGoal,
        sourceSeed: Long,
        attemptIndex: Int,
    ): TacticalPuzzle? {
        var state = newMatch(seed = dealSeed)
        if (targetGoal == PuzzleGoal.WIN_MATCH) {
            // Per i puzzle di vittoria partita, predispone un punteggio decisivo a ridosso degli 11 punti
            state = state.copy(totals = mapOf(Seat.P1 to 10, Seat.P2 to 10))
        }

        // Simula le prime 5 distribuzioni con GreedyBot fino a svuotare il tallone (30 carte giocate)
        val botRng = SeededRng(dealSeed)
        while (state.phase == DealPhase.PLAYING && state.deck.isNotEmpty()) {
            val legal = legalMoves(state)
            if (legal.isEmpty()) break
            val botView = viewFor(state, state.toMove)
            val move = GreedyBot.chooseMove(botView, legal, botRng)
            val playResult = play(state, move) as? PlayResult.Applied ?: break
            state = playResult.state
        }

        // Esplora i turni dell'ultima distribuzione (deckSize == 0)
        var turnInEndgame = 0
        while (state.phase == DealPhase.PLAYING && state.deck.isEmpty()) {
            val view = viewFor(state, state.toMove)
            val legal = legalMoves(state)

            if (view.hand.size >= 2 && legal.size >= 2) {
                val solution = TacticalPuzzleSolver.solve(view, targetGoal)
                if (solution != null && solution.isNotEmpty()) {
                    val puzzleId = "puzzle_${sourceSeed}_${attemptIndex}_$turnInEndgame"
                    val titleKey = "puzzle_title_${targetGoal.name.lowercase()}"
                    return TacticalPuzzle(
                        id = puzzleId,
                        title = titleKey,
                        initialView = view,
                        goal = targetGoal,
                        solutionMoves = solution,
                    )
                }
            }

            val nextMove = TacticalPuzzleSolver.findBestMove(view) ?: legal.firstOrNull() ?: break
            val playResult = play(state, nextMove) as? PlayResult.Applied ?: break
            state = playResult.state
            turnInEndgame++
        }

        return null
    }
}
