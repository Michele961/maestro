package it.maestro.app.domain.rewards

import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MoveGrade
import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.data.local.datastore.InMemoryRetentionRepository
import it.maestro.app.domain.retention.DailyMissionGenerator
import it.maestro.app.domain.retention.DailyMissionType
import it.maestro.app.domain.retention.MissionContext
import it.maestro.app.domain.retention.adaptiveMissionFor
import it.maestro.app.domain.retention.weakestMissionFocus
import it.maestro.app.data.local.datastore.InMemoryOpponentRepository
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.fullState
import it.maestro.app.feature.table.presentation.TestMatchGenerator
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.play
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.domain.wallet.TransactionReason
import it.maestro.app.domain.time.FakeClock
import it.maestro.game.model.Seat
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.training.TrainingFundamental
import it.maestro.app.data.local.datastore.InMemoryExperienceRepository
import it.maestro.app.domain.progress.Experience
import kotlinx.coroutines.flow.first
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
            // La missione allegata è quella di adesso e si può riscuotere dalla sequenza di fine partita.
            assertEquals(state.first { it.type == advance.type }, advance.mission)
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

    // --- F1R-25d: il punteggio di attenzione conta per le missioni ---

    private val sparringContext = MissionContext(recentSparring = true)

    private val checksMissionDay: LocalDate = generateSequence(today) { it.plusDays(1) }.take(60)
        .first { day -> DailyMissionGenerator.generateForDate(day, sparringContext).any { it.type == DailyMissionType.ANSWER_CHECKS } }

    private suspend fun checksMissionProgress(fundamental: TrainingFundamental): Int {
        val retention = InMemoryRetentionRepository()
        retention.recordMissionContext(checksMissionDay.minusDays(1), sparringPlayed = true, focus = null)
        val engine = RewardEngine(InMemoryWalletRepository(), retention, InMemoryMasteryRepository(), FakeClock(checksMissionDay))
        engine.onMatchFinished(log, analysis(90, 0), Seat.P1, 2 to 3, fundamental)
        return retention.getDailyMissions(checksMissionDay).missions.first { it.type == DailyMissionType.ANSWER_CHECKS }.progress
    }

    @Test
    fun correctChecksOnAnyObjectiveAdvanceTheChecksMission() = runTest {
        assertEquals(2, checksMissionProgress(TrainingFundamental.SEVENS_COUNT))
        assertEquals(2, checksMissionProgress(TrainingFundamental.DENARI_COUNT))
        assertEquals(2, checksMissionProgress(TrainingFundamental.PRIMIERA_LEAD))
    }

    @Test
    fun aClassicMatchDoesNotAdvanceTheChecksMission() = runTest {
        assertEquals(0, checksMissionProgress(TrainingFundamental.NONE))
    }

    @Test
    fun afterAMatchTheNextMissionsFollowTheWeakestAxis() = runTest {
        val repo = InMemoryRetentionRepository()
        val masteryRepo = InMemoryMasteryRepository()
        repeat(4) { // oltre la soglia di partite del radar (3)
            RewardEngine(InMemoryWalletRepository(), repo, masteryRepo, FakeClock(today))
                .onMatchFinished(log, analysis(90, 0), Seat.P1, null, TrainingFundamental.NONE)
        }
        val focus = masteryRepo.getMasteryProfile().radar.weakestMissionFocus()
        val tomorrow = today.plusDays(1)
        val adaptive = adaptiveMissionFor(focus, recentSparring = false)
        val types = repo.getDailyMissions(tomorrow).missions.map { it.type }
        if (adaptive != null) assertTrue(adaptive in types, "$adaptive manca in $types per $focus")
    }

    // --- F1R-18c: vittoria sulla scala degli avversari ---

    private val ladder = InMemoryOpponentRepository()
    private val ladderEngine = RewardEngine(wallet, retention, mastery, FakeClock(today), opponents = ladder)

    /** Una partita finita in cui vince [winner] (cercata tra i seed, il generatore è deterministico). */
    private fun finishedMatch(winner: Seat): MatchLog {
        for (seed in 1L..200L) {
            val session = TestMatchGenerator.generateEndgameMatch(seed)
            val state = session.fullState()
            val first = legalMoves(state).first()
            val afterFirst = (play(state, first) as PlayResult.Applied).state
            val second = legalMoves(afterFirst).first()
            val end = (play(afterFirst, second) as PlayResult.Applied).state
            if (end.winner == winner) return session.log.copy(moves = session.log.moves + listOf(first, second))
        }
        error("nessun seed con vincitore $winner")
    }

    private suspend fun finish(log: MatchLog, fundamental: TrainingFundamental, opponent: Opponent? = Opponent.GIGI) =
        ladderEngine.onMatchFinished(log, analysis(90, 0), Seat.P1, null, fundamental, opponent)

    @Test
    fun winningClassicaUnlocksTheNextOpponent() = runTest {
        val summary = finish(finishedMatch(Seat.P1), TrainingFundamental.NONE)

        val victory = assertNotNull(summary.ladderVictory)
        assertEquals(Opponent.GIGI, victory.defeated)
        assertTrue(victory.firstTime)
        assertEquals(Opponent.NONNA_ROSA, victory.newlyUnlocked)
    }

    @Test
    fun losingDoesNotRecordAVictory() = runTest {
        assertEquals(null, finish(finishedMatch(Seat.P2), TrainingFundamental.NONE).ladderVictory)
    }

    @Test
    fun sparringDoesNotRecordAVictory() = runTest {
        assertEquals(null, finish(finishedMatch(Seat.P1), TrainingFundamental.SEVENS_COUNT).ladderVictory)
    }

    @Test
    fun noOpponentMeansNoLadderVictory() = runTest {
        assertEquals(null, finish(finishedMatch(Seat.P1), TrainingFundamental.NONE, opponent = null).ladderVictory)
    }

    @Test
    fun beatingTheSameOpponentAgainIsNotAFirstTime() = runTest {
        val log = finishedMatch(Seat.P1)
        finish(log, TrainingFundamental.NONE)
        val again = assertNotNull(finish(log, TrainingFundamental.NONE).ladderVictory)
        assertFalse(again.firstTime)
        assertEquals(null, again.newlyUnlocked)
    }

    // --- F1R-29d: esperienza ---

    private val xp = InMemoryExperienceRepository()
    private val xpEngine = RewardEngine(wallet, retention, mastery, FakeClock(today), opponents = InMemoryOpponentRepository(), experience = xp)

    private suspend fun xpPoints() = xp.observePoints().first()

    @Test
    fun theFirstWinOverAnOpponentGivesExperienceOnlyOnce() = runTest {
        val log = finishedMatch(Seat.P1)
        val first = xpEngine.onMatchFinished(log, analysis(90, 0), Seat.P1, null, TrainingFundamental.NONE, Opponent.GIGI)
        assertEquals(Experience.ladderFirstWin(Opponent.GIGI), first.experienceGained)
        assertEquals(100, xpPoints())

        val again = xpEngine.onMatchFinished(log, analysis(90, 0), Seat.P1, null, TrainingFundamental.NONE, Opponent.GIGI)
        assertEquals(0, again.experienceGained)
        assertEquals(100, xpPoints())
    }

    @Test
    fun aLossOrSparringGivesNoExperience() = runTest {
        xpEngine.onMatchFinished(finishedMatch(Seat.P2), analysis(90, 0), Seat.P1, null, TrainingFundamental.NONE, Opponent.GIGI)
        xpEngine.onMatchFinished(finishedMatch(Seat.P1), analysis(90, 0), Seat.P1, null, TrainingFundamental.SEVENS_COUNT, Opponent.GIGI)
        assertEquals(0, xpPoints())
    }

    @Test
    fun solvingAPuzzleTierGivesItsExperienceOncePerTier() = runTest {
        val date = LocalDate.of(2026, 10, 26)
        assertEquals(10, xpEngine.onPuzzleSolved(date, tierIndex = 0, tierCredits = 15).experienceGained)
        assertEquals(0, xpEngine.onPuzzleSolved(date, tierIndex = 0, tierCredits = 15).experienceGained)
        assertEquals(15, xpEngine.onPuzzleSolved(date, tierIndex = 1, tierCredits = 20).experienceGained)
        assertEquals(25, xpPoints())
    }

    @Test
    fun claimingAMissionGivesTenAndTheThirdClaimAddsTheThreeOfThreeBonus() = runTest {
        retention.getDailyMissions(today).missions.forEach { retention.updateMissionProgress(today, it.type, it.target) }
        val gained = retention.getDailyMissions(today).missions.map { mission ->
            val current = retention.getDailyMissions(today).missions.first { it.id == mission.id }
            xpEngine.claimMission(current).experienceGained
        }
        assertEquals(listOf(10, 10, 10 + Experience.ALL_MISSIONS_CLAIMED), gained)
        assertEquals(10 + 10 + 10 + Experience.ALL_MISSIONS_CLAIMED, xpPoints())
    }

    @Test
    fun claimingTheSameMissionTwiceGivesExperienceOnce() = runTest {
        retention.getDailyMissions(today).missions.first().let { retention.updateMissionProgress(today, it.type, it.target) }
        val mission = retention.getDailyMissions(today).missions.first()
        xpEngine.claimMission(mission)
        assertEquals(0, xpEngine.claimMission(retention.getDailyMissions(today).missions.first()).experienceGained)
        assertEquals(10, xpPoints())
    }

    @Test
    fun withoutAnExperienceRepositoryNothingIsGiven() = runTest {
        val summary = engine.onPuzzleSolved(LocalDate.of(2026, 10, 26), tierIndex = 0, tierCredits = 15)
        assertEquals(0, summary.experienceGained)
    }
}
