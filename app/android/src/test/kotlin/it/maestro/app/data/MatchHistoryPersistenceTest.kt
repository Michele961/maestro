package it.maestro.app.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MoveGrade
import it.maestro.app.data.local.datastore.DataStoreMasteryRepository
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** F1R-26a: l'avversario e l'obiettivo di Sparring sopravvivono alla scrittura e alla lettura su disco. */
class MatchHistoryPersistenceTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val analysis = MatchAnalysis(
        moves = emptyList(),
        accuracyBySeat = mapOf(Seat.P1 to 80, Seat.P2 to 50),
        meanLossBySeat = mapOf(Seat.P1 to 0.05, Seat.P2 to 0.2),
        countsBySeatAndGrade = mapOf(
            Seat.P1 to mapOf(MoveGrade.BEST to 0, MoveGrade.INACCURACY to 0, MoveGrade.BLUNDER to 0),
            Seat.P2 to mapOf(MoveGrade.BEST to 0, MoveGrade.INACCURACY to 0, MoveGrade.BLUNDER to 0),
        ),
    )

    private fun repository(scope: TestScope) = DataStoreMasteryRepository(
        PreferenceDataStoreFactory.create(scope = scope.backgroundScope) { folder.newFile("mastery.preferences_pb") },
    )

    @Test
    fun opponentAndObjectiveAreStoredAndRead() = runTest(UnconfinedTestDispatcher()) {
        val repository = repository(this)
        repository.recordMatch(
            analysis,
            MatchLog(config = ScopaConfig(targetScore = 11), seed = 3L, moves = emptyList()),
            opponent = "TONINO",
            trainingFundamental = TrainingFundamental.SEVENS_COUNT,
        )

        val entry = repository.observeMatchHistory().first().single()
        assertEquals("TONINO", entry.opponent)
        assertEquals("SEVENS_COUNT", entry.sparringObjective)
    }

    @Test
    fun anEntryWithoutOpponentIsReadBackWithoutOne() = runTest(UnconfinedTestDispatcher()) {
        val repository = repository(this)
        repository.recordMatch(
            analysis,
            MatchLog(config = ScopaConfig(targetScore = 11), seed = 4L, moves = emptyList()),
        )

        val entry = repository.observeMatchHistory().first().single()
        assertEquals(null, entry.opponent)
        assertEquals(null, entry.sparringObjective)
    }
}
