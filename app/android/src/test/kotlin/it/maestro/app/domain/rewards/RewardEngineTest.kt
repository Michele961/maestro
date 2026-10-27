package it.maestro.app.domain.rewards

import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MoveGrade
import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.data.local.datastore.InMemoryRetentionRepository
import it.maestro.app.data.local.missions.DailyMissionType
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.data.local.wallet.TransactionReason
import it.maestro.app.domain.time.FakeClock
import it.maestro.game.model.Seat
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Task F1R-14: l'unico punto da cui nascono crediti, missioni, streak e Maestria. */
class RewardEngineTest {

    private val today = LocalDate.of(2026, 10, 27)
    private val wallet = InMemoryWalletRepository()
    private val retention = InMemoryRetentionRepository()
    private val mastery = InMemoryMasteryRepository()
    private val engine = RewardEngine(wallet, retention, mastery, FakeClock(today))

    private fun analysis(accuracy: Int, blunders: Int) = MatchAnalysis(
        moves = emptyList(),
        accuracyBySeat = mapOf(Seat.P1 to accuracy, Seat.P2 to 50),
        meanLossBySeat = mapOf(Seat.P1 to 0.1, Seat.P2 to 0.2),
        countsBySeatAndGrade = mapOf(
            Seat.P1 to mapOf(MoveGrade.BEST to 5, MoveGrade.INACCURACY to 1, MoveGrade.BLUNDER to blunders),
            Seat.P2 to mapOf(MoveGrade.BEST to 0, MoveGrade.INACCURACY to 0, MoveGrade.BLUNDER to 0),
        ),
    )

    private val log = it.maestro.game.scopa.MatchLog(config = ScopaConfig(targetScore = 11), seed = 7L, moves = emptyList())

    @Test
    fun matchFinishedSummaryAgreesWithStreakMasteryAndMissions() = runTest {
        val before = mastery.getMasteryProfile().cognitiveElo
        val summary = engine.onMatchFinished(log, analysis(accuracy = 90, blunders = 0), Seat.P1, null, TrainingFundamental.NONE)

        assertEquals(0, summary.creditsEarned, "La partita non assegna crediti: solo le missioni riscosse")
        assertEquals(0, summary.streakBefore)
        assertEquals(1, summary.streakAfter)
        assertEquals(before, summary.masteryBefore)
        assertEquals(mastery.getMasteryProfile().cognitiveElo, summary.masteryAfter)
        assertEquals(summary.masteryAfter!! - before, summary.masteryDelta)
        assertEquals(1, retention.getStreak(today).currentStreak)

        // Le missioni avanzate nel riepilogo sono quelle che il repository mostra avanzate.
        val state = retention.getDailyMissions(today).missions
        summary.missions.forEach { advance ->
            assertEquals(state.first { it.type == advance.type }.progress, advance.progressAfter)
            assertTrue(advance.progressAfter > advance.progressBefore)
        }
        val types = state.map { it.type }
        if (DailyMissionType.COMPLETE_MATCH_WITH_ACCURACY_80 in types) {
            assertTrue(summary.missions.any { it.type == DailyMissionType.COMPLETE_MATCH_WITH_ACCURACY_80 && it.completedNow })
        }
        if (DailyMissionType.PLAY_MATCH_WITHOUT_BLUNDERS in types) {
            assertTrue(summary.missions.any { it.type == DailyMissionType.PLAY_MATCH_WITHOUT_BLUNDERS && it.completedNow })
        }
    }

    @Test
    fun lowAccuracyAndBlundersDoNotAdvanceQualityMissions() = runTest {
        val summary = engine.onMatchFinished(log, analysis(accuracy = 60, blunders = 2), Seat.P1, null, TrainingFundamental.NONE)
        assertFalse(summary.missions.any { it.type == DailyMissionType.COMPLETE_MATCH_WITH_ACCURACY_80 })
        assertFalse(summary.missions.any { it.type == DailyMissionType.PLAY_MATCH_WITHOUT_BLUNDERS })
    }

    @Test
    fun claimingACompletedMissionCreditsTheWalletOnce() = runTest {
        retention.getDailyMissions(today).missions.first().let { retention.updateMissionProgress(today, it.type, it.target) }
        val mission = retention.getDailyMissions(today).missions.first()

        val first = engine.claimMission(mission)
        assertEquals(mission.type.rewardCredits, first.creditsEarned)
        assertEquals(mission.type.rewardCredits, wallet.getBalance())
        assertTrue(wallet.hasClaimed(TransactionReason.DAILY_MISSION_REWARD, mission.id))

        val second = engine.claimMission(retention.getDailyMissions(today).missions.first())
        assertEquals(0, second.creditsEarned)
        assertEquals(mission.type.rewardCredits, wallet.getBalance(), "Nessun doppio accredito")
    }

    @Test
    fun claimingAnIncompleteMissionDoesNothing() = runTest {
        val mission = retention.getDailyMissions(today).missions.first()
        assertEquals(0, engine.claimMission(mission).creditsEarned)
        assertEquals(0, wallet.getBalance())
    }

    @Test
    fun puzzleSolvedCreditsTheTierOnlyOncePerDay() = runTest {
        val date = LocalDate.of(2026, 10, 26)
        val first = engine.onPuzzleSolved(date, tierIndex = 0, tierCredits = 15)
        assertEquals(15, first.creditsEarned)
        assertEquals(15, wallet.getBalance())
        assertNotNull(first.streakAfter)
        assertTrue(wallet.hasClaimed(TransactionReason.DAILY_PUZZLE_REWARD, "2026-10-26"))

        assertEquals(0, engine.onPuzzleSolved(date, tierIndex = 0, tierCredits = 15).creditsEarned)
        assertEquals(15, wallet.getBalance())

        assertEquals(25, engine.onPuzzleSolved(date, tierIndex = 1, tierCredits = 25).creditsEarned)
        assertTrue(wallet.hasClaimed(TransactionReason.DAILY_PUZZLE_REWARD, "2026-10-26-tier-1"))
    }

    @Test
    fun lessonCompletedCreditsFiveOncePerLesson() = runTest {
        assertEquals(5, engine.onLessonCompleted("LESSON_1_1").creditsEarned)
        assertEquals(0, engine.onLessonCompleted("LESSON_1_1").creditsEarned)
        assertEquals(5, wallet.getBalance())
    }

    @Test
    fun welcomeBonusIsGrantedOnlyToAnEmptyWallet() = runTest {
        assertTrue(engine.grantWelcomeBonus())
        assertEquals(50, wallet.getBalance())
        assertFalse(engine.grantWelcomeBonus())
        assertEquals(50, wallet.getBalance())
    }
}
