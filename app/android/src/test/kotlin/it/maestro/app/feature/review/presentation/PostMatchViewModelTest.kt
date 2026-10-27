package it.maestro.app.feature.review.presentation

import it.maestro.ai.analysis.MatchAnalysis
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

/** Task F1R-15a: la sequenza di fine partita avanza a passi, senza mai restare bloccata e saltabile con un tocco. */
@OptIn(ExperimentalCoroutinesApi::class)
class PostMatchViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val analysis = MutableStateFlow<MatchAnalysis?>(null)
    private val reward = MutableStateFlow<RewardSummary?>(null)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(fromHistory: Boolean = false) = PostMatchViewModel(
        matchLog = PostMatchFixtures.log,
        humanSeat = Seat.P1,
        fromHistory = fromHistory,
        analysis = analysis,
        reward = reward,
    )

    private fun TestScope.step(vm: PostMatchViewModel): PostMatchStep {
        runCurrent()
        return vm.uiState.value.step
    }

    @Test
    fun opensOnThePlaqueThenWaitsForTheAnalysisWithoutAnySpinnerTimer() = runTest(dispatcher) {
        val vm = viewModel()
        assertEquals(PostMatchStep.PLAQUE, step(vm))
        advanceTimeBy(1_300)
        assertEquals(PostMatchStep.ANALYSIS, step(vm))
        advanceTimeBy(60_000)
        assertEquals(PostMatchStep.ANALYSIS, step(vm))
        assertNull(vm.uiState.value.analysis)
    }

    @Test
    fun anAlreadyReadyAnalysisMovesOnAsSoonAsThePlaqueIsDone() = runTest(dispatcher) {
        analysis.value = PostMatchFixtures.analysis
        val vm = viewModel(fromHistory = true)
        advanceTimeBy(1_300)
        assertEquals(PostMatchStep.ACCURACY, step(vm))
        assertNotNull(vm.uiState.value.analysis)
    }

    @Test
    fun aMatchSeenFromHistoryHasNoRewardStep() = runTest(dispatcher) {
        analysis.value = PostMatchFixtures.analysis
        val vm = viewModel(fromHistory = true)
        advanceTimeBy(1_300 + 1_000)
        assertEquals(PostMatchStep.ACTIONS, step(vm))
    }

    @Test
    fun aFinishedMatchShowsTheRewardWhenItArrivesThenTheActions() = runTest(dispatcher) {
        analysis.value = PostMatchFixtures.analysis
        val vm = viewModel()
        advanceTimeBy(1_300 + 1_000)
        assertEquals(PostMatchStep.ACCURACY, step(vm))
        reward.value = RewardSummary(masteryBefore = 1200, masteryAfter = 1218)
        assertEquals(PostMatchStep.REWARD, step(vm))
        assertEquals(18, vm.uiState.value.reward?.masteryDelta)
        advanceTimeBy(1_000)
        assertEquals(PostMatchStep.ACTIONS, step(vm))
    }

    @Test
    fun aTapSkipsThePauseOfTheCurrentStep() = runTest(dispatcher) {
        analysis.value = PostMatchFixtures.analysis
        val vm = viewModel(fromHistory = true)
        runCurrent()
        assertEquals(PostMatchStep.PLAQUE, vm.uiState.value.step)
        vm.skip()
        assertEquals(PostMatchStep.ACCURACY, step(vm))
        vm.skip()
        assertEquals(PostMatchStep.ACTIONS, step(vm))
    }

    @Test
    fun tappingWhileWaitingForTheAnalysisDoesNothing() = runTest(dispatcher) {
        val vm = viewModel()
        advanceTimeBy(1_300)
        step(vm)
        vm.skip()
        assertEquals(PostMatchStep.ANALYSIS, step(vm))
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
}
