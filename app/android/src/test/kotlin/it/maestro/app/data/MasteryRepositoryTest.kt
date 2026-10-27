package it.maestro.app.data

import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MoveAnalysis
import it.maestro.ai.analysis.MoveGrade
import it.maestro.ai.bot.MoveValue
import it.maestro.ai.mastery.MasteryProfile
import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MasteryRepositoryTest {

    @Test
    fun initialProfile_isUncalibratedDefault() = runTest {
        val repository = InMemoryMasteryRepository()
        val profile = repository.getMasteryProfile()

        assertEquals(0, profile.matchesPlayed)
        assertEquals(50.0, profile.accuracyEma, 0.001)
        assertEquals(1000, profile.cognitiveElo)
        assertEquals(50, profile.radar.sevens)
    }

    @Test
    fun recordMatch_updatesProfileAndPersistsCorrectly() = runTest {
        val repository = InMemoryMasteryRepository()

        val p1 = Seat.P1
        val p2 = Seat.P2
        val card7D = Card(Rank.SETTE, Suit.DENARI)
        val card7S = Card(Rank.SETTE, Suit.SPADE)

        val move1 = MoveAnalysis(
            moveIndex = 0,
            seat = p1,
            playedMove = ScopaMove(card7D, setOf(card7S)),
            playedValue = 1.0,
            bestMove = ScopaMove(card7D, setOf(card7S)),
            bestValue = 1.0,
            loss = 0.0,
            grade = MoveGrade.BEST,
            alternatives = listOf(MoveValue(ScopaMove(card7D, setOf(card7S)), 1.0)),
        )

        val analysis = MatchAnalysis(
            moves = listOf(move1),
            accuracyBySeat = mapOf(p1 to 90, p2 to 60),
            meanLossBySeat = mapOf(p1 to 0.03, p2 to 0.15),
            countsBySeatAndGrade = mapOf(
                p1 to mapOf(MoveGrade.BEST to 1, MoveGrade.INACCURACY to 0, MoveGrade.BLUNDER to 0),
                p2 to mapOf(MoveGrade.BEST to 0, MoveGrade.INACCURACY to 0, MoveGrade.BLUNDER to 0),
            ),
        )

        val log = MatchLog(
            config = ScopaConfig(targetScore = 11),
            seed = 12345L,
            moves = listOf(move1.playedMove),
        )

        val updated = repository.recordMatch(
            matchAnalysis = analysis,
            matchLog = log,
            humanSeat = p1,
            trainingScore = Pair(2, 2),
            trainingFundamental = TrainingFundamental.SEVENS_COUNT,
        )

        assertEquals(1, updated.matchesPlayed)
        assertEquals(90.0, updated.accuracyEma, 0.001)
        assertEquals(1880, updated.cognitiveElo) // 800 + 90 * 12 = 1880
        assertEquals(100, updated.radar.sevens)

        val observed = repository.observeMasteryProfile().first()
        assertEquals(updated, observed)
    }

    @Test
    fun resetProfile_resetsToInitial() = runTest {
        val repository = InMemoryMasteryRepository()
        repository.recordMatch(
            matchAnalysis = MatchAnalysis(
                moves = emptyList(),
                accuracyBySeat = mapOf(Seat.P1 to 80, Seat.P2 to 50),
                meanLossBySeat = mapOf(Seat.P1 to 0.05, Seat.P2 to 0.2),
                countsBySeatAndGrade = mapOf(
                    Seat.P1 to mapOf(MoveGrade.BEST to 0, MoveGrade.INACCURACY to 0, MoveGrade.BLUNDER to 0),
                    Seat.P2 to mapOf(MoveGrade.BEST to 0, MoveGrade.INACCURACY to 0, MoveGrade.BLUNDER to 0),
                ),
            ),
            matchLog = MatchLog(
                config = ScopaConfig(targetScore = 11),
                seed = 1L,
                moves = emptyList(),
            ),
            humanSeat = Seat.P1,
        )

        assertTrue(repository.getMasteryProfile().matchesPlayed > 0)
        repository.resetProfile()
        assertEquals(MasteryProfile.INITIAL, repository.getMasteryProfile())
    }

    @Test
    fun recordMatch_appendsToHistoryAndCalculatesEloDelta() = runTest {
        val repository = InMemoryMasteryRepository()
        val p1 = Seat.P1
        val p2 = Seat.P2

        val (state, partialLog) = it.maestro.app.feature.table.presentation.TestMatchGenerator.generateEndgameMatch(seed = 101L)
        val move1 = it.maestro.game.scopa.legalMoves(state).first()
        val res1 = it.maestro.game.scopa.play(state, move1) as it.maestro.game.scopa.PlayResult.Applied
        val move2 = it.maestro.game.scopa.legalMoves(res1.state).first()
        val res2 = it.maestro.game.scopa.play(res1.state, move2) as it.maestro.game.scopa.PlayResult.Applied
        val log = partialLog.copy(moves = partialLog.moves + listOf(move1, move2))
        val expectedP1 = res2.state.totals.getValue(p1)
        val expectedP2 = res2.state.totals.getValue(p2)

        val analysis = MatchAnalysis(
            moves = emptyList(),
            accuracyBySeat = mapOf(p1 to 85, p2 to 60),
            meanLossBySeat = mapOf(p1 to 0.05, p2 to 0.2),
            countsBySeatAndGrade = mapOf(
                p1 to mapOf(MoveGrade.BEST to 1, MoveGrade.INACCURACY to 0, MoveGrade.BLUNDER to 0),
                p2 to mapOf(MoveGrade.BEST to 0, MoveGrade.INACCURACY to 0, MoveGrade.BLUNDER to 0),
            ),
        )

        repository.recordMatch(
            matchAnalysis = analysis,
            matchLog = log,
            humanSeat = p1,
            trainingScore = Pair(3, 5), // Quiz 3 su 5 non deve sovrascrivere il punteggio partita!
        )

        val history = repository.observeMatchHistory().first()
        assertEquals(1, history.size)
        val entry = history.first()
        assertEquals(85, entry.accuracy)
        assertEquals(expectedP1, entry.humanScore)
        assertEquals(expectedP2, entry.botScore)
        assertEquals(res2.state.winner == p1, entry.isWin)
        org.junit.Assert.assertNotNull(entry.matchLogText)
        assertTrue("Elo delta deve essere positivo per una vittoria ad alta precisione", entry.eloDelta > 0)

        // Verifica idempotenza: una seconda registrazione dello stesso identico match non crea duplicati
        repository.recordMatch(
            matchAnalysis = analysis,
            matchLog = log,
            humanSeat = p1,
        )
        val historyAfterDuplicate = repository.observeMatchHistory().first()
        assertEquals(1, historyAfterDuplicate.size)

        // Se registriamo un secondo match diverso...
        val (_, partialLog2) = it.maestro.app.feature.table.presentation.TestMatchGenerator.generateEndgameMatch(seed = 202L)
        val log2 = partialLog2.copy(moves = partialLog2.moves + listOf(move1))
        repository.recordMatch(
            matchAnalysis = analysis,
            matchLog = log2,
            humanSeat = p1,
        )
        val historyTwoMatches = repository.observeMatchHistory().first()
        assertEquals(2, historyTwoMatches.size)

        // Ora ri-registrare il primo match (che ora è al secondo posto nell'elenco) deve essere ignorato
        repository.recordMatch(
            matchAnalysis = analysis,
            matchLog = log,
            humanSeat = p1,
        )
        val historyStillTwo = repository.observeMatchHistory().first()
        assertEquals(2, historyStillTwo.size)
    }
}
