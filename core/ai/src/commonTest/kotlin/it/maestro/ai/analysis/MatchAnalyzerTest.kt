package it.maestro.ai.analysis

import it.maestro.ai.bot.MoveEvaluator
import it.maestro.ai.bot.MoveValue
import it.maestro.ai.bot.PimcEvaluator
import it.maestro.ai.bot.SearchBudget
import it.maestro.game.CoreGame
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import it.maestro.game.scopa.viewFor
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MatchAnalyzerTest {

    @Test
    fun maestroPrincipio1_evaluatorReceivesOnlyViewForCurrentMoverAtEveryTurn() {
        val log = buildSampleLog(seed = 42L, maxTurns = 36)
        val recordedCalls = ArrayList<Pair<ScopaView, List<ScopaMove>>>()

        val recordingEvaluator = object : MoveEvaluator {
            override fun evaluate(
                view: ScopaView,
                legal: List<ScopaMove>,
                rng: SeededRng,
                budget: SearchBudget,
            ): List<MoveValue> {
                recordedCalls.add(view to legal)
                return PimcEvaluator.evaluate(view, legal, rng, budget)
            }
        }

        val analyzer = MatchAnalyzer(
            evaluator = recordingEvaluator,
            budget = SearchBudget(samples = 4, maxNodes = 60),
        )
        val analysis = analyzer.analyze(log, rng = SeededRng(99L))

        assertEquals(log.moves.size, recordedCalls.size)
        assertEquals(log.moves.size, analysis.moves.size)

        var state = newMatch(config = log.config, seed = log.seed)
        for ((index, move) in log.moves.withIndex()) {
            val expectedMover = state.toMove
            val expectedView = viewFor(state, expectedMover)
            val expectedLegal = legalMoves(state)
            val (actualView, actualLegal) = recordedCalls[index]

            assertEquals(
                expectedView,
                actualView,
                "Al turno $index il valutatore deve ricevere esattamente viewFor(state, state.toMove)",
            )
            assertEquals(expectedMover, actualView.seat)
            assertEquals(expectedMover, actualView.toMove)
            assertEquals(expectedLegal, actualLegal)

            // Verifica sul contenuto che la vista non contenga mai carte nascoste (mano avversaria o mazzo)
            val opponentHand = state.hand(expectedMover.other())
            val hiddenCards = opponentHand + state.deck
            for (hidden in hiddenCards) {
                assertTrue(
                    hidden !in actualView.hand &&
                        hidden !in actualView.table &&
                        hidden !in actualView.capturedBy.getValue(Seat.P1) &&
                        hidden !in actualView.capturedBy.getValue(Seat.P2) &&
                        hidden !in actualView.playedCards,
                    "Carta nascosta $hidden presente nella vista al turno $index",
                )
            }

            state = (play(state, move) as PlayResult.Applied).state
        }
    }

    @Test
    fun maestroAnalizzatore_thresholdsAndClassifyLossMatchProvisionalSpec() {
        assertEquals(0.05, MatchAnalyzer.BEST_MAX_LOSS)
        assertEquals(0.30, MatchAnalyzer.INACCURACY_MAX_LOSS)
        assertEquals(100.0, MatchAnalyzer.ACCURACY_MAX_PERCENT)
        assertEquals(3.0, MatchAnalyzer.ACCURACY_DECAY_FACTOR)
        assertEquals(3, MatchAnalyzer.TOP_ALTERNATIVES_COUNT)

        // BEST: perdita <= 0.05
        assertEquals(MoveGrade.BEST, MatchAnalyzer.classifyLoss(0.0))
        assertEquals(MoveGrade.BEST, MatchAnalyzer.classifyLoss(0.02))
        assertEquals(MoveGrade.BEST, MatchAnalyzer.classifyLoss(0.05))

        // INACCURACY: 0.05 < perdita <= 0.30
        assertEquals(MoveGrade.INACCURACY, MatchAnalyzer.classifyLoss(0.0501))
        assertEquals(MoveGrade.INACCURACY, MatchAnalyzer.classifyLoss(0.15))
        assertEquals(MoveGrade.INACCURACY, MatchAnalyzer.classifyLoss(0.30))

        // BLUNDER: perdita > 0.30
        assertEquals(MoveGrade.BLUNDER, MatchAnalyzer.classifyLoss(0.3001))
        assertEquals(MoveGrade.BLUNDER, MatchAnalyzer.classifyLoss(1.25))

        assertFailsWith<IllegalArgumentException> {
            MatchAnalyzer.classifyLoss(-0.01)
        }
        assertFailsWith<IllegalArgumentException> {
            MatchAnalyzer.classifyLoss(Double.NaN)
        }
    }

    @Test
    fun maestroAnalizzatore_computeAccuracyMatchesFixedReferenceValues() {
        // Formula provvisoria: round(100 * exp(-3 * perdita media))
        // meanLoss = 0.00 -> 100 * exp(0) = 100.0 -> 100
        assertEquals(100, MatchAnalyzer.computeAccuracy(0.0))
        // meanLoss = 0.02 -> 100 * exp(-0.06) = 94.1764... -> 94
        assertEquals(94, MatchAnalyzer.computeAccuracy(0.02))
        // meanLoss = 0.05 -> 100 * exp(-0.15) = 86.0707... -> 86
        assertEquals(86, MatchAnalyzer.computeAccuracy(0.05))
        // meanLoss = 0.10 -> 100 * exp(-0.30) = 74.0818... -> 74
        assertEquals(74, MatchAnalyzer.computeAccuracy(0.10))
        // meanLoss = 0.20 -> 100 * exp(-0.60) = 54.8811... -> 55
        assertEquals(55, MatchAnalyzer.computeAccuracy(0.20))
        // meanLoss = 0.30 -> 100 * exp(-0.90) = 40.6569... -> 41
        assertEquals(41, MatchAnalyzer.computeAccuracy(0.30))
        // meanLoss = 0.50 -> 100 * exp(-1.50) = 22.3130... -> 22
        assertEquals(22, MatchAnalyzer.computeAccuracy(0.50))
        // meanLoss = 1.00 -> 100 * exp(-3.00) = 4.9787... -> 5
        assertEquals(5, MatchAnalyzer.computeAccuracy(1.00))
        // meanLoss molto grande -> 0
        assertEquals(0, MatchAnalyzer.computeAccuracy(10.0))

        assertFailsWith<IllegalArgumentException> {
            MatchAnalyzer.computeAccuracy(-0.1)
        }
        assertFailsWith<IllegalArgumentException> {
            MatchAnalyzer.computeAccuracy(Double.NaN)
        }
    }

    @Test
    fun analyze_computesAlternativesLossesGradesAndSeatMetricsAccurately() {
        val log = buildSampleLog(seed = 7L, maxTurns = 4)

        // Valutatore controllato: nei primi 4 turni di una distribuzione a 3 carte per giocatore
        // (turni 0 e 1 hanno almeno 3 mosse legali; turni 2 e 3 hanno almeno 2 mosse legali)
        // assegna alla mossa migliore valore 1.0 e alla mossa giocata una perdita predefinita:
        // Turno 0 (P2): loss = 0.00 -> BEST
        // Turno 1 (P1): loss = 0.10 -> INACCURACY
        // Turno 2 (P2): loss = 0.20 -> INACCURACY
        // Turno 3 (P1): loss = 0.50 -> BLUNDER
        val targetLosses = listOf(0.00, 0.10, 0.20, 0.50)
        var callIndex = 0

        val controlledEvaluator = object : MoveEvaluator {
            override fun evaluate(
                view: ScopaView,
                legal: List<ScopaMove>,
                rng: SeededRng,
                budget: SearchBudget,
            ): List<MoveValue> {
                val turn = callIndex++
                val playedMove = log.moves[turn]
                val desiredLoss = targetLosses[turn]
                return legal.mapIndexed { idx, candidate ->
                    val value = when {
                        candidate == playedMove && desiredLoss == 0.0 -> 1.0
                        candidate == playedMove -> 1.0 - desiredLoss
                        idx == 0 || (idx == 1 && legal[0] == playedMove) -> 1.0
                        else -> 0.2 - idx * 0.05
                    }
                    MoveValue(move = candidate, value = value)
                }
            }
        }

        val analysis = MatchAnalyzer(evaluator = controlledEvaluator).analyze(log)

        assertEquals(4, analysis.moves.size)

        // Verifica mossa per mossa
        val expectedGrades = listOf(
            MoveGrade.BEST,
            MoveGrade.INACCURACY,
            MoveGrade.INACCURACY,
            MoveGrade.BLUNDER,
        )
        for (i in 0 until 4) {
            val moveAnalysis = analysis.moves[i]
            assertEquals(i, moveAnalysis.moveIndex)
            assertEquals(i + 1, moveAnalysis.moveNumber)
            assertEquals(log.moves[i], moveAnalysis.playedMove)
            assertTrue(abs(targetLosses[i] - moveAnalysis.loss) < 1e-9, "Perdita errata al turno $i")
            assertEquals(expectedGrades[i], moveAnalysis.grade)
            assertTrue(moveAnalysis.topAlternatives.size <= 3)
            assertEquals(moveAnalysis.alternatives.take(3), moveAnalysis.topAlternatives)

            // Verifica ordinamento decrescente delle alternative
            for (j in 0 until moveAnalysis.alternatives.lastIndex) {
                assertTrue(
                    moveAnalysis.alternatives[j].value >= moveAnalysis.alternatives[j + 1].value,
                    "Alternative non ordinate per valore decrescente al turno $i",
                )
            }
        }

        val firstMover = analysis.moves[0].seat
        val secondMover = firstMover.other()

        // firstMover ha giocato ai turni 0, 2 -> perdite 0.00, 0.20 -> media = 0.10 -> accuracy = 74
        // secondMover ha giocato ai turni 1, 3 -> perdite 0.10, 0.50 -> media = 0.30 -> accuracy = 41
        assertTrue(abs(0.10 - analysis.meanLoss(firstMover)) < 1e-9)
        assertTrue(abs(0.30 - analysis.meanLoss(secondMover)) < 1e-9)
        assertEquals(74, analysis.accuracy(firstMover))
        assertEquals(41, analysis.accuracy(secondMover))

        assertEquals(1, analysis.count(firstMover, MoveGrade.BEST))
        assertEquals(1, analysis.count(firstMover, MoveGrade.INACCURACY))
        assertEquals(0, analysis.count(firstMover, MoveGrade.BLUNDER))

        assertEquals(0, analysis.count(secondMover, MoveGrade.BEST))
        assertEquals(1, analysis.count(secondMover, MoveGrade.INACCURACY))
        assertEquals(1, analysis.count(secondMover, MoveGrade.BLUNDER))
    }

    @Test
    fun readableReport_containsMoveNumberPlayedMoveTopThreeAlternativesWithValuesAndGrade() {
        val log = buildSampleLog(seed = 15L, maxTurns = 4)
        val analyzer = MatchAnalyzer(
            evaluator = PimcEvaluator,
            budget = SearchBudget(samples = 5, maxNodes = 80),
        )
        val analysis = analyzer.analyze(log, rng = SeededRng(15L))
        val report = analysis.toReadableReport()
        val lines = report.lines()

        assertEquals("maestro-analysis report", lines[0])
        assertTrue(lines[1].startsWith("P1: accuracy=${analysis.accuracy(Seat.P1)}%"))
        assertTrue(lines[2].startsWith("P2: accuracy=${analysis.accuracy(Seat.P2)}%"))
        assertEquals(3 + log.moves.size, lines.size)

        for ((index, moveAnalysis) in analysis.moves.withIndex()) {
            val moveLine = lines[3 + index]
            val expectedMoveNotation = formatMoveNotation(moveAnalysis.playedMove)
            assertTrue(moveLine.startsWith("#${index + 1} ${moveAnalysis.seat.name} | "))
            assertTrue(moveLine.contains("played: $expectedMoveNotation ("))
            assertTrue(moveLine.contains("grade: ${moveAnalysis.grade.name}"))
            assertTrue(moveLine.contains("top3: ["))
            for (alt in moveAnalysis.topAlternatives) {
                assertTrue(
                    moveLine.contains("${formatMoveNotation(alt.move)}="),
                    "Alternativa ${alt.move} mancante nella riga: $moveLine",
                )
            }
        }
    }

    @Test
    fun analyze_isDeterministicGivenSameLogAndSeed() {
        val log = buildSampleLog(seed = 99L, maxTurns = 12)
        val analyzer = MatchAnalyzer(
            evaluator = PimcEvaluator,
            budget = SearchBudget(samples = 6, maxNodes = 100),
        )

        val analysis1 = analyzer.analyze(log, rng = SeededRng(1234L))
        val analysis2 = analyzer.analyze(log, rng = SeededRng(1234L))

        assertEquals(analysis1, analysis2)
        assertEquals(analysis1.toReadableReport(), analysis2.toReadableReport())
    }

    @Test
    fun analyze_rejectsWrongRulesVersionOrIllegalMove() {
        val validLog = buildSampleLog(seed = 1L, maxTurns = 4)
        val analyzer = MatchAnalyzer(budget = SearchBudget(samples = 2, maxNodes = 40))

        val wrongRulesLog = validLog.copy(rulesVersion = 999)
        assertFailsWith<IllegalArgumentException> {
            analyzer.analyze(wrongRulesLog)
        }

        // Sostituisce la prima mossa con la mossa del turno successivo (carta non in mano al primo giocatore)
        val illegalMovesList = listOf(validLog.moves[1]) + validLog.moves.drop(1)
        val illegalLog = validLog.copy(moves = illegalMovesList)
        assertFailsWith<IllegalArgumentException> {
            analyzer.analyze(illegalLog)
        }
    }

    private fun buildSampleLog(seed: Long, maxTurns: Int): MatchLog {
        val config = ScopaConfig()
        var state = newMatch(config = config, seed = seed)
        val moves = ArrayList<ScopaMove>(maxTurns)
        var turn = 0
        while (turn < maxTurns) {
            val legal = legalMoves(state)
            if (legal.isEmpty()) break
            // Sceglie l'ultima mossa legale per variare rispetto alla prima in ordine canonico
            val chosen = legal.last()
            moves.add(chosen)
            state = (play(state, chosen) as PlayResult.Applied).state
            turn++
        }
        return MatchLog(
            rulesVersion = CoreGame.RULES_VERSION,
            config = config,
            seed = seed,
            moves = moves,
        )
    }
}
