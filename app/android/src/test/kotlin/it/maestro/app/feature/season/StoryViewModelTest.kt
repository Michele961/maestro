package it.maestro.app.feature.season

import it.maestro.app.data.local.datastore.InMemorySeasonRepository
import it.maestro.app.data.local.datastore.InMemoryStoryRepository
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.season.SeasonFacts
import it.maestro.app.feature.season.presentation.StoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** F1G-16a: le scene della Scalata compaiono una alla volta e una sola volta. */
@OptIn(ExperimentalCoroutinesApi::class)
class StoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val season = InMemorySeasonRepository()
    private val story = InMemoryStoryRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(): StoryViewModel {
        val vm = StoryViewModel(season, story)
        backgroundScope.launch { vm.scene.collect {} }
        return vm
    }

    @Test
    fun theWelcomeComesFirstThenTheOpeningOfTheCurrentChapterThenNothing() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals("welcome", vm.scene.value?.id)
        vm.onSceneDone("welcome")
        advanceUntilIdle()
        assertEquals("ch02_open", vm.scene.value?.id)
        vm.onSceneDone("ch02_open")
        advanceUntilIdle()
        assertNull(vm.scene.value)
    }

    @Test
    fun aSkippedSceneIsMarkedSeenAndDoesNotReturnOnTheNextVisit() = runTest(dispatcher) {
        viewModel().onSceneDone("welcome")
        advanceUntilIdle()
        val nextVisit = viewModel()
        advanceUntilIdle()
        assertEquals("ch02_open", nextVisit.scene.value?.id)
    }

    @Test
    fun finishingAChapterBringsItsClosingScene() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onSceneDone("welcome")
        vm.onSceneDone("ch02_open")
        season.setFacts(SeasonFacts(defeated = setOf(Opponent.GIGI)))
        advanceUntilIdle()
        assertEquals("ch02_close", vm.scene.value?.id)
        vm.onSceneDone("ch02_close")
        advanceUntilIdle()
        assertEquals("ch03_open", vm.scene.value?.id)
    }
}
