package it.maestro.app.feature.review.presentation

import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.app.designsystem.audio.NoOpSoundManager
import it.maestro.app.designsystem.audio.SoundEffect
import it.maestro.app.domain.progress.AchievementId
import it.maestro.app.domain.rewards.RewardSummary
import it.maestro.game.model.Seat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** Task F1R-15a: la fine partita carica finché tutto è pronto, poi mostra tutto insieme. */
@OptIn(ExperimentalCoroutinesApi::class)
class PostMatchViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val analysis = MutableStateFlow<MatchAnalysis?>(null)
    private val reward = MutableStateFlow<RewardSummary?>(null)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val sound = NoOpSoundManager()

    private fun viewModel(fromHistory: Boolean = false) = PostMatchViewModel(
        matchLog = PostMatchFixtures.log,
        humanSeat = Seat.P1,
        fromHistory = fromHistory,
        analysis = analysis,
        reward = reward,
        soundManager = sound,
    )

    private fun TestScope.step(vm: PostMatchViewModel): PostMatchStep {
        runCurrent()
        return vm.uiState.value.step
    }

    @Test
    fun itLoadsUntilTheAnalysisIsReadyWithoutAnySpinnerTimer() = runTest(dispatcher) {
        val vm = viewModel()
        assertEquals(PostMatchStep.LOADING, step(vm))
        advanceTimeBy(60_000)
        assertEquals(PostMatchStep.LOADING, step(vm))
        assertNull(vm.uiState.value.analysis)
    }

    @Test
    fun aMatchSeenFromHistoryIsReadyAsSoonAsTheAnalysisIs() = runTest(dispatcher) {
        val vm = viewModel(fromHistory = true)
        assertEquals(PostMatchStep.LOADING, step(vm))
        analysis.value = PostMatchFixtures.analysis
        assertEquals(PostMatchStep.READY, step(vm))
        assertNotNull(vm.uiState.value.analysis)
        assertNull(vm.uiState.value.reward)
    }

    @Test
    fun aFinishedMatchShowsEverythingTogetherOnlyWhenTheRewardArrivesToo() = runTest(dispatcher) {
        analysis.value = PostMatchFixtures.analysis
        val vm = viewModel()
        assertEquals(PostMatchStep.LOADING, step(vm))
        assertNull(vm.uiState.value.analysis)
        reward.value = RewardSummary(masteryBefore = 1200, masteryAfter = 1218)
        assertEquals(PostMatchStep.READY, step(vm))
        assertNotNull(vm.uiState.value.analysis)
        assertEquals(18, vm.uiState.value.reward?.masteryDelta)
    }

    @Test
    fun aClaimedMissionIsRemembered() = runTest(dispatcher) {
        val vm = viewModel()
        vm.markClaimed("m1")
        assertEquals(setOf("m1"), vm.uiState.value.claimedMissions)
    }

    @Test
    fun theOutcomeComesFromReplayingTheLog() = runTest(dispatcher) {
        val state = viewModel().uiState.value
        assertEquals(state.humanTotal >= state.opponentTotal, state.won)
        assertEquals(Seat.P1, state.humanSeat)
    }

    @Test
    fun anUnlockedGoalPlaysTheGoalSoundOnlyWhenTheRewardArrives() = runTest(dispatcher) {
        val vm = viewModel()
        analysis.value = PostMatchFixtures.analysis
        runCurrent()
        assertEquals(emptyList(), sound.playedEffects)

        reward.value = RewardSummary(achievementsUnlocked = listOf(AchievementId.FIRST_WIN))
        runCurrent()
        assertEquals(listOf(SoundEffect.GOAL_UNLOCKED), sound.playedEffects)
    }
}
