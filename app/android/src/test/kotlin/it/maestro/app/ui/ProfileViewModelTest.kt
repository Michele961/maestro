package it.maestro.app.ui

import it.maestro.ai.mastery.AbilityRadar
import it.maestro.ai.mastery.MasteryProfile
import it.maestro.app.R
import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.ui.screens.ProfileTab
import it.maestro.app.ui.screens.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun mapEloToRankTitle_correctlyCategorizesAllTiers() {
        assertEquals(R.string.mastery_rank_principiante, ProfileViewModel.mapEloToRankTitle(800))
        assertEquals(R.string.mastery_rank_principiante, ProfileViewModel.mapEloToRankTitle(949))
        assertEquals(R.string.mastery_rank_dilettante, ProfileViewModel.mapEloToRankTitle(950))
        assertEquals(R.string.mastery_rank_dilettante, ProfileViewModel.mapEloToRankTitle(1100))
        assertEquals(R.string.mastery_rank_intermedio, ProfileViewModel.mapEloToRankTitle(1250))
        assertEquals(R.string.mastery_rank_intermedio, ProfileViewModel.mapEloToRankTitle(1400))
        assertEquals(R.string.mastery_rank_avanzato, ProfileViewModel.mapEloToRankTitle(1550))
        assertEquals(R.string.mastery_rank_avanzato, ProfileViewModel.mapEloToRankTitle(1700))
        assertEquals(R.string.mastery_rank_maestro, ProfileViewModel.mapEloToRankTitle(1850))
        assertEquals(R.string.mastery_rank_maestro, ProfileViewModel.mapEloToRankTitle(2000))
    }

    @Test
    fun computeMaestroAdvice_targetsLowestWeakness() {
        // Nuova registrazione (0 match)
        assertEquals(
            R.string.profile_advice_default,
            ProfileViewModel.computeMaestroAdvice(AbilityRadar.DEFAULT, matchesPlayed = 0),
        )

        // Sette come punto debole
        val sevensWeak = AbilityRadar(sevens = 30, denari = 80, primiera = 75, board = 70, endgame = 65)
        assertEquals(
            R.string.profile_advice_sevens,
            ProfileViewModel.computeMaestroAdvice(sevensWeak, matchesPlayed = 5),
        )

        // Denari come punto debole
        val denariWeak = AbilityRadar(sevens = 80, denari = 25, primiera = 75, board = 70, endgame = 65)
        assertEquals(
            R.string.profile_advice_denari,
            ProfileViewModel.computeMaestroAdvice(denariWeak, matchesPlayed = 5),
        )

        // Banco come punto debole
        val boardWeak = AbilityRadar(sevens = 80, denari = 85, primiera = 75, board = 40, endgame = 65)
        assertEquals(
            R.string.profile_advice_board,
            ProfileViewModel.computeMaestroAdvice(boardWeak, matchesPlayed = 5),
        )

        // Eccellenza globale (tutte le statistiche >= 85)
        val allMaster = AbilityRadar(sevens = 90, denari = 95, primiera = 88, board = 92, endgame = 89)
        assertEquals(
            R.string.profile_advice_master,
            ProfileViewModel.computeMaestroAdvice(allMaster, matchesPlayed = 15),
        )
    }

    @Test
    fun profileUiState_emitsDataFromRepository() = runTest(testDispatcher) {
        val customProfile = MasteryProfile(
            matchesPlayed = 8,
            accuracyEma = 78.5,
            cognitiveElo = 1742,
            radar = AbilityRadar(sevens = 85, denari = 90, primiera = 72, board = 65, endgame = 80),
        )
        val repository = InMemoryMasteryRepository(customProfile)
        val viewModel = ProfileViewModel(repository)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals(customProfile, state.profile)
        assertEquals(R.string.mastery_rank_avanzato, state.rankTitleRes)
        assertEquals(R.string.profile_advice_board, state.maestroAdviceRes)
    }

    @Test
    fun selectTab_updatesSelectedTab() = runTest(testDispatcher) {
        val repository = InMemoryMasteryRepository()
        val viewModel = ProfileViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialState = viewModel.uiState.first { !it.isLoading }
        assertEquals(ProfileTab.MASTERY, initialState.selectedTab)

        viewModel.selectTab(ProfileTab.HISTORY)
        testDispatcher.scheduler.advanceUntilIdle()

        val historyState = viewModel.uiState.first { it.selectedTab == ProfileTab.HISTORY }
        assertEquals(ProfileTab.HISTORY, historyState.selectedTab)
    }
}
