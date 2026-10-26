package it.maestro.ai.mastery

import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MoveAnalysis
import it.maestro.ai.analysis.MoveGrade
import it.maestro.ai.bot.MoveValue
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.training.TrainingFundamental
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MasteryCalculatorTest {

    @Test
    fun accuracyToCognitiveElo_mapsDirectlyToDifficultyLevels() {
        assertEquals(800, MasteryCalculator.accuracyToCognitiveElo(0.0))
        assertEquals(1100, MasteryCalculator.accuracyToCognitiveElo(25.0))
        assertEquals(1400, MasteryCalculator.accuracyToCognitiveElo(50.0))
        assertEquals(1700, MasteryCalculator.accuracyToCognitiveElo(75.0))
        assertEquals(2000, MasteryCalculator.accuracyToCognitiveElo(100.0))

        // Clamping ai limiti
        assertEquals(800, MasteryCalculator.accuracyToCognitiveElo(-15.0))
        assertEquals(2000, MasteryCalculator.accuracyToCognitiveElo(120.0))
    }

    @Test
    fun computeAlpha_calibratesDynamically() {
        assertEquals(1.0, MasteryCalculator.computeAlpha(0))
        // 2 / (1 + 2) = 0.666 -> clamped a 0.40
        assertEquals(0.40, MasteryCalculator.computeAlpha(1))
        // 2 / (10 + 2) = 0.1666...
        val alphaAt10 = MasteryCalculator.computeAlpha(10)
        assertTrue(alphaAt10 in 0.16..0.17)
        // 2 / (100 + 2) = 0.019 -> clamped al minimo 0.15
        assertEquals(0.15, MasteryCalculator.computeAlpha(100))
    }

    @Test
    fun updateMasteryProfile_firstMatchSetsBaselineImmediately() {
        val initial = MasteryProfile.INITIAL
        val matchRadar = AbilityRadar(sevens = 80, denari = 90, primiera = 70, board = 60, endgame = 85)
        val matchAccuracy = 80

        val updated = MasteryCalculator.updateMasteryProfile(
            current = initial,
            matchAccuracy = matchAccuracy,
            matchRadar = matchRadar,
        )

        assertEquals(1, updated.matchesPlayed)
        assertEquals(80.0, updated.accuracyEma)
        assertEquals(1760, updated.cognitiveElo) // 800 + 80 * 12 = 1760
        assertEquals(matchRadar, updated.radar)
    }

    @Test
    fun updateMasteryProfile_subsequentMatchesUpdateSmoothlyAndDeterministically() {
        var profile = MasteryProfile.INITIAL

        // Partita 1: 100%
        profile = MasteryCalculator.updateMasteryProfile(
            current = profile,
            matchAccuracy = 100,
            matchRadar = AbilityRadar(100, 100, 100, 100, 100),
        )
        assertEquals(1, profile.matchesPlayed)
        assertEquals(100.0, profile.accuracyEma)
        assertEquals(2000, profile.cognitiveElo)
        assertEquals(100, profile.radar.sevens)

        // Partita 2: 50%
        profile = MasteryCalculator.updateMasteryProfile(
            current = profile,
            matchAccuracy = 50,
            matchRadar = AbilityRadar(50, 50, 50, 50, 50),
        )
        assertEquals(2, profile.matchesPlayed)
        // alpha a matchesPlayed=1 è 0.40: (1 - 0.4)*100 + 0.4*50 = 80.0
        assertEquals(80.0, profile.accuracyEma)
        assertEquals(1760, profile.cognitiveElo)
        assertEquals(80, profile.radar.sevens)

        // Partita 3: 50%
        profile = MasteryCalculator.updateMasteryProfile(
            current = profile,
            matchAccuracy = 50,
            matchRadar = AbilityRadar(50, 50, 50, 50, 50),
        )
        assertEquals(3, profile.matchesPlayed)
        // alpha a matchesPlayed=2: 2 / (2 + 2) = 0.5 -> clamped a 0.40: (1 - 0.4)*80 + 0.4*50 = 68.0
        assertEquals(68.0, profile.accuracyEma)
        assertEquals(1616, profile.cognitiveElo)
        assertEquals(68, profile.radar.sevens)
    }

    @Test
    fun computeMatchRadar_computesAll5AxesFromMoves() {
        val p1 = Seat.P1
        val p2 = Seat.P2

        val card7Denari = Card(Rank.SETTE, Suit.DENARI)
        val card7Spade = Card(Rank.SETTE, Suit.SPADE)
        val card6Coppe = Card(Rank.SEI, Suit.COPPE)
        val cardAssoBastoni = Card(Rank.ASSO, Suit.BASTONI)
        val card2Bastoni = Card(Rank.DUE, Suit.BASTONI)
        val card3Coppe = Card(Rank.TRE, Suit.COPPE)

        // Move 1: P1 gioca Settebello con presa (tocca Sette, Denari, Primiera)
        val moveAnalysis1 = MoveAnalysis(
            moveIndex = 0,
            seat = p1,
            playedMove = ScopaMove(card7Denari, setOf(card7Spade)),
            playedValue = 1.0,
            bestMove = ScopaMove(card7Denari, setOf(card7Spade)),
            bestValue = 1.0,
            loss = 0.0,
            grade = MoveGrade.BEST,
            alternatives = listOf(MoveValue(ScopaMove(card7Denari, setOf(card7Spade)), 1.0)),
        )

        // Move 2: P1 gioca 6 di Coppe (Primiera)
        val moveAnalysis2 = MoveAnalysis(
            moveIndex = 2,
            seat = p1,
            playedMove = ScopaMove(card6Coppe, emptySet()),
            playedValue = 0.5,
            bestMove = ScopaMove(card6Coppe, emptySet()),
            bestValue = 0.5,
            loss = 0.0,
            grade = MoveGrade.BEST,
            alternatives = listOf(MoveValue(ScopaMove(card6Coppe, emptySet()), 0.5)),
        )

        // Move 3: P1 gioca calata sicura all'indice 34 (Endgame & Banco)
        val moveAnalysis3 = MoveAnalysis(
            moveIndex = 34,
            seat = p1,
            playedMove = ScopaMove(card2Bastoni, emptySet()),
            playedValue = 0.2,
            bestMove = ScopaMove(card2Bastoni, emptySet()),
            bestValue = 0.2,
            loss = 0.0,
            grade = MoveGrade.BEST,
            alternatives = listOf(MoveValue(ScopaMove(card2Bastoni, emptySet()), 0.2)),
        )

        val analysis = MatchAnalysis(
            moves = listOf(moveAnalysis1, moveAnalysis2, moveAnalysis3),
            accuracyBySeat = mapOf(p1 to 90, p2 to 60),
            meanLossBySeat = mapOf(p1 to 0.03, p2 to 0.15),
            countsBySeatAndGrade = mapOf(
                p1 to mapOf(MoveGrade.BEST to 3, MoveGrade.INACCURACY to 0, MoveGrade.BLUNDER to 0),
                p2 to mapOf(MoveGrade.BEST to 0, MoveGrade.INACCURACY to 0, MoveGrade.BLUNDER to 0),
            ),
        )

        val log = MatchLog(
            config = ScopaConfig(targetScore = 11),
            seed = 42L,
            moves = listOf(
                moveAnalysis1.playedMove,
                ScopaMove(card3Coppe, emptySet()),
                moveAnalysis2.playedMove,
                ScopaMove(cardAssoBastoni, emptySet()),
                moveAnalysis3.playedMove,
            ),
        )

        val radar = MasteryCalculator.computeMatchRadar(
            matchAnalysis = analysis,
            matchLog = log,
            humanSeat = p1,
            trainingScore = Pair(3, 3), // 100% nei check di allenamento
            trainingFundamental = TrainingFundamental.SEVENS_COUNT,
        )

        assertEquals(100, radar.sevens) // mosse perfette + 100% check
        assertEquals(100, radar.denari) // Settebello giocato perfettamente
        assertEquals(100, radar.primiera) // Sette e Sei giocati perfettamente
        assertEquals(100, radar.board) // calata sicura
        assertEquals(100, radar.endgame) // mossa 34 perfetta
    }

    @Test
    fun computeMatchRadar_withReplayedMatch_incorporatesObjectiveOutcomes() {
        val p1 = Seat.P1
        val config = ScopaConfig(targetScore = 11)
        var state = it.maestro.game.scopa.newMatch(config = config, seed = 42L)
        val moves = mutableListOf<ScopaMove>()

        for (i in 0 until 36) {
            val legal = it.maestro.game.scopa.legalMoves(state)
            if (legal.isEmpty()) break
            val move = legal.first()
            val result = it.maestro.game.scopa.play(state, move) as it.maestro.game.scopa.PlayResult.Applied
            moves.add(move)
            state = result.state
        }

        val log = MatchLog(
            rulesVersion = it.maestro.game.CoreGame.RULES_VERSION,
            config = config,
            seed = 42L,
            moves = moves,
        )

        val moveAnalyses = moves.mapIndexed { index, move ->
            val seat = if (index % 2 == 0) Seat.P1 else Seat.P2
            MoveAnalysis(
                moveIndex = index,
                seat = seat,
                playedMove = move,
                playedValue = 0.5,
                bestMove = move,
                bestValue = 0.5,
                loss = 0.0,
                grade = MoveGrade.BEST,
                alternatives = listOf(MoveValue(move, 0.5)),
            )
        }

        val analysis = MatchAnalysis(
            moves = moveAnalyses,
            accuracyBySeat = mapOf(Seat.P1 to 85, Seat.P2 to 75),
            meanLossBySeat = mapOf(Seat.P1 to 0.05, Seat.P2 to 0.10),
            countsBySeatAndGrade = mapOf(
                Seat.P1 to mapOf(MoveGrade.BEST to 18),
                Seat.P2 to mapOf(MoveGrade.BEST to 18),
            ),
        )

        val radar = MasteryCalculator.computeMatchRadar(
            matchAnalysis = analysis,
            matchLog = log,
            humanSeat = p1,
        )

        assertTrue(radar.sevens in 0..100)
        assertTrue(radar.denari in 0..100)
        assertTrue(radar.primiera in 0..100)
        assertTrue(radar.board in 0..100)
        assertTrue(radar.endgame in 0..100)
    }
}
