package it.maestro.ai.puzzle

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView
import it.maestro.game.scopa.legalMoves
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Suite di test per il generatore, risolutore e modello dati di puzzle tattici
 * (`docs/backlog/fase-2.md` F2-02, ADR-0007, ADR-0009).
 */
class TacticalPuzzleTest {

    @Test
    fun dataModel_exposesPureImmutablePropertiesAndValidatesInvariants() {
        val dummyView = createTestView(
            hand = listOf(Card(Rank.SETTE, Suit.DENARI), Card(Rank.ASSO, Suit.COPPE)),
            table = listOf(Card(Rank.SETTE, Suit.SPADE)),
        )
        val move = ScopaMove(
            card = Card(Rank.SETTE, Suit.DENARI),
            capture = setOf(Card(Rank.SETTE, Suit.SPADE)),
        )

        val puzzle = TacticalPuzzle(
            id = "test_01",
            title = "puzzle_title_make_sweep",
            initialView = dummyView,
            goal = PuzzleGoal.MAKE_SWEEP,
            solutionMoves = listOf(move),
        )

        assertEquals("test_01", puzzle.id)
        assertEquals("puzzle_title_make_sweep", puzzle.title)
        assertEquals(dummyView, puzzle.initialView)
        assertEquals(PuzzleGoal.MAKE_SWEEP, puzzle.goal)
        assertEquals(listOf(move), puzzle.solutionMoves)
        assertEquals(move, puzzle.firstMove)

        // Deve richiedere almeno 2 carte in mano per offrire una scelta deliberata
        val singleCardView = dummyView.copy(hand = listOf(Card(Rank.SETTE, Suit.DENARI)))
        assertFailsWith<IllegalArgumentException> {
            TacticalPuzzle(
                id = "invalid_01",
                title = "puzzle_invalid",
                initialView = singleCardView,
                goal = PuzzleGoal.MAKE_SWEEP,
                solutionMoves = listOf(move),
            )
        }

        // Deve richiedere almeno una mossa nella sequenza di soluzione
        assertFailsWith<IllegalArgumentException> {
            TacticalPuzzle(
                id = "invalid_02",
                title = "puzzle_invalid",
                initialView = dummyView,
                goal = PuzzleGoal.MAKE_SWEEP,
                solutionMoves = emptyList(),
            )
        }
    }

    @Test
    fun generator_isDeterministicAcrossIdenticalSeeds() {
        val seed = 42L
        val puzzleA = TacticalPuzzleGenerator.generate(seed = seed)
        val puzzleB = TacticalPuzzleGenerator.generate(seed = seed)

        assertEquals(puzzleA.id, puzzleB.id)
        assertEquals(puzzleA.title, puzzleB.title)
        assertEquals(puzzleA.goal, puzzleB.goal)
        assertEquals(puzzleA.initialView.hand, puzzleB.initialView.hand)
        assertEquals(puzzleA.initialView.table, puzzleB.initialView.table)
        assertEquals(puzzleA.solutionMoves, puzzleB.solutionMoves)
    }

    @Test
    fun generator_producesAtLeastTenVerifiedSolvablePuzzles() {
        // Criterio di accettazione F2-02: suite con almeno 10 puzzle verificati e risolvibili
        val batchSize = 10
        val baseSeed = 1000L
        val puzzles = TacticalPuzzleGenerator.generateBatch(seed = baseSeed, count = batchSize)

        assertEquals(batchSize, puzzles.size)

        for ((index, puzzle) in puzzles.withIndex()) {
            assertNotNull(puzzle.id, "Il puzzle #$index deve avere un id non nullo")
            assertTrue(puzzle.title.startsWith("puzzle_title_"), "Il titolo deve essere una chiave ADR-0007")
            assertEquals(DealPhase.PLAYING, puzzle.initialView.phase)
            assertTrue(puzzle.initialView.hand.size >= 2, "Il puzzle deve presentare almeno 2 carte in mano")
            assertTrue(puzzle.solutionMoves.isNotEmpty(), "La soluzione non può essere vuota")

            // Risoluzione esatta con il solver
            val solvedMoves = TacticalPuzzleSolver.solve(puzzle)
            assertNotNull(solvedMoves, "Il puzzle #$index (${puzzle.id}) deve essere risolvibile dal solver")
            assertEquals(puzzle.solutionMoves, solvedMoves, "La soluzione del solver deve coincidere con il puzzle")

            // Validazione mossa corretta
            val winningMove = puzzle.solutionMoves.first()
            assertTrue(
                TacticalPuzzleSolver.validateMove(puzzle, winningMove),
                "La mossa vincente deve essere validata positivamente",
            )
            assertTrue(
                TacticalPuzzleSolver.isTacticalPuzzle(puzzle.initialView, puzzle.goal),
                "La vista deve essere confermata come puzzle tattico valido",
            )
        }
    }

    @Test
    fun generator_generatesSpecificPuzzlesForRequestedGoals() {
        val sweepPuzzle = TacticalPuzzleGenerator.generate(seed = 2026L, goal = PuzzleGoal.MAKE_SWEEP)
        assertEquals(PuzzleGoal.MAKE_SWEEP, sweepPuzzle.goal)
        assertEquals("puzzle_title_make_sweep", sweepPuzzle.title)
        val solvedSweep = TacticalPuzzleSolver.solve(sweepPuzzle)
        assertNotNull(solvedSweep)
        assertEquals(sweepPuzzle.solutionMoves, solvedSweep)

        val pointsPuzzle = TacticalPuzzleGenerator.generate(seed = 3030L, goal = PuzzleGoal.MAXIMIZE_POINTS)
        assertEquals(PuzzleGoal.MAXIMIZE_POINTS, pointsPuzzle.goal)
        assertEquals("puzzle_title_maximize_points", pointsPuzzle.title)
        val solvedPoints = TacticalPuzzleSolver.solve(pointsPuzzle)
        assertNotNull(solvedPoints)
        assertEquals(pointsPuzzle.solutionMoves, solvedPoints)

        val winPuzzle = TacticalPuzzleGenerator.generate(seed = 4040L, goal = PuzzleGoal.WIN_MATCH)
        assertEquals(PuzzleGoal.WIN_MATCH, winPuzzle.goal)
        assertEquals("puzzle_title_win_match", winPuzzle.title)
        val solvedWin = TacticalPuzzleSolver.solve(winPuzzle)
        assertNotNull(solvedWin)
        assertEquals(winPuzzle.solutionMoves, solvedWin)
    }

    @Test
    fun solver_rejectsAmbiguousPositionsWhereMultipleMovesAchieveTheGoal() {
        // Posizione costruita in cui due diverse carte possono entrambe fare scopa per presa diretta
        // Tavolo: 7D. Mano: [7C, 7S]. Entrambe possono prendere il 7D e svuotare il tavolo.
        val ambiguousView = createTestView(
            hand = listOf(
                Card(Rank.SETTE, Suit.COPPE),
                Card(Rank.SETTE, Suit.SPADE),
            ),
            table = listOf(Card(Rank.SETTE, Suit.DENARI)),
            deckSize = 10,
        )

        val solution = TacticalPuzzleSolver.solve(ambiguousView, PuzzleGoal.MAKE_SWEEP)
        assertNull(solution, "Una posizione con due mosse vincenti equivalenti NON deve essere accettata come puzzle")
        assertFalse(TacticalPuzzleSolver.isTacticalPuzzle(ambiguousView, PuzzleGoal.MAKE_SWEEP))
    }

    @Test
    fun solver_rejectsWrongMovesInValidation() {
        val puzzle = TacticalPuzzleGenerator.generate(seed = 777L, goal = PuzzleGoal.MAXIMIZE_POINTS)
        val correctMove = puzzle.solutionMoves.first()

        // Costruisce una mossa deliberatamente diversa
        val alternateCard = puzzle.initialView.hand.first { it != correctMove.card }
        val fakeMove = ScopaMove(card = alternateCard, capture = emptySet())

        assertFalse(
            TacticalPuzzleSolver.validateMove(puzzle, fakeMove),
            "Una mossa alternativa errata deve fallire la validazione",
        )
        assertFalse(
            TacticalPuzzleSolver.validateMove(puzzle, correctMove, plyIndex = 99),
            "Un indice di semimossa fuori intervallo deve restituire false",
        )
    }

    @Test
    fun solver_returnsNullForViewsNotPlayingOrWithoutChoice() {
        val validPuzzle = TacticalPuzzleGenerator.generate(seed = 555L, goal = PuzzleGoal.MAXIMIZE_POINTS)
        val matchOverView = validPuzzle.initialView.copy(phase = DealPhase.MATCH_OVER)
        assertNull(TacticalPuzzleSolver.solve(matchOverView, PuzzleGoal.MAXIMIZE_POINTS))

        val singleCardView = validPuzzle.initialView.copy(hand = listOf(validPuzzle.initialView.hand.first()))
        assertNull(TacticalPuzzleSolver.solve(singleCardView, PuzzleGoal.MAXIMIZE_POINTS))
    }

    private fun createTestView(
        hand: List<Card>,
        table: List<Card>,
        deckSize: Int = 0,
    ): ScopaView {
        val remainingCards = buildSet {
            addAll(hand)
            addAll(table)
        }
        val dummyCaptured = it.maestro.game.model.fullDeck().filterNot { it in remainingCards }

        return ScopaView(
            seat = Seat.P1,
            config = ScopaConfig(),
            dealer = Seat.P2,
            toMove = Seat.P1,
            phase = DealPhase.PLAYING,
            hand = hand,
            table = table,
            capturedBy = mapOf(
                Seat.P1 to dummyCaptured,
                Seat.P2 to emptyList(),
            ),
            playedCards = emptyList(),
            opponentHandSize = 0,
            deckSize = deckSize,
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
            totals = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        )
    }
}
