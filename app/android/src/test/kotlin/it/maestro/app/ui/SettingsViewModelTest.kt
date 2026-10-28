package it.maestro.app.ui

import it.maestro.app.data.local.datastore.InMemorySettingsRepository
import it.maestro.app.domain.coach.Chattiness
import it.maestro.app.domain.settings.GameSpeed
import it.maestro.app.feature.settings.presentation.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/** Impostazioni (F1R-31a): ogni scelta si applica e si salva subito, senza «Salva». */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

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
    fun choicesAreAppliedAndSavedImmediately() = runTest {
        val repository = InMemorySettingsRepository()
        val viewModel = SettingsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        assertEquals(true, viewModel.uiState.value.soundEnabled)

        viewModel.setSoundEnabled(false)
        viewModel.setGameSpeed(GameSpeed.FAST)
        viewModel.setMaestroChattiness(Chattiness.QUIET)
        viewModel.setShowPointsRace(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.soundEnabled)
        assertEquals(GameSpeed.FAST, state.gameSpeed)
        assertEquals(Chattiness.QUIET, state.maestroChattiness)
        assertEquals(true, state.showPointsRace)
    }
}
