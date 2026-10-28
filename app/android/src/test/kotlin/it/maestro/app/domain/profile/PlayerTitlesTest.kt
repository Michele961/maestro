package it.maestro.app.domain.profile

import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.data.local.datastore.InMemoryOpponentRepository
import it.maestro.app.data.local.datastore.InMemoryPlayerTitleRepository
import it.maestro.app.domain.mastery.MatchHistoryEntry
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.opponents.OpponentProgress
import it.maestro.app.feature.profile.presentation.ProfileViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** F1R-26b: titoli guadagnati, titolo scelto dal giocatore e andamento della Maestria. */
@OptIn(ExperimentalCoroutinesApi::class)
class PlayerTitlesTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val twoBeaten = OpponentProgress(setOf(Opponent.GIGI, Opponent.NONNA_ROSA))

    private fun entry(delta: Int) = MatchHistoryEntry("m", 0L, 80, 1000, delta, 11, 5, true)

    @Test
    fun noVictoriesMeansNoTitle() {
        assertEquals(emptyList<Opponent>(), PlayerTitles.earned(OpponentProgress()))
        assertEquals(null, PlayerTitles.current(OpponentProgress(), selected = null))
    }

    @Test
    fun theLatestEarnedTitleIsTheDefault() {
        assertEquals(listOf(Opponent.GIGI, Opponent.NONNA_ROSA), PlayerTitles.earned(twoBeaten))
        assertEquals(Opponent.NONNA_ROSA, PlayerTitles.current(twoBeaten, selected = null))
    }

    @Test
    fun aSelectedEarnedTitleWins() {
        assertEquals(Opponent.GIGI, PlayerTitles.current(twoBeaten, selected = Opponent.GIGI.name))
    }

    @Test
    fun aSelectedTitleNotEarnedFallsBackToTheLatest() {
        assertEquals(Opponent.NONNA_ROSA, PlayerTitles.current(twoBeaten, selected = Opponent.MAESTRO.name))
    }

    @Test
    fun trendFollowsTheLastFiveMatches() {
        assertEquals(MasteryTrend.FLAT, masteryTrendOf(emptyList()))
        assertEquals(MasteryTrend.UP, masteryTrendOf(listOf(entry(+18), entry(-5))))
        assertEquals(MasteryTrend.DOWN, masteryTrendOf(listOf(entry(-12), entry(+3))))
        // Dalla sesta partita in poi non conta più: -100 è fuori finestra.
        assertEquals(MasteryTrend.UP, masteryTrendOf(List(5) { entry(+1) } + entry(-100)))
    }

    @Test
    fun choosingATitleUpdatesTheProfileState() = runTest(dispatcher) {
        val viewModel = ProfileViewModel(
            InMemoryMasteryRepository(),
            InMemoryOpponentRepository(twoBeaten),
            InMemoryPlayerTitleRepository(),
        )
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(Opponent.NONNA_ROSA, viewModel.uiState.first { !it.isLoading }.currentTitle)

        viewModel.selectTitle(Opponent.GIGI)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(Opponent.GIGI, viewModel.uiState.first { !it.isLoading }.currentTitle)
    }
}
