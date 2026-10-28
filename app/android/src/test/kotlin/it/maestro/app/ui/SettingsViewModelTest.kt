package it.maestro.app.ui

import it.maestro.app.data.local.datastore.InMemorySettingsRepository
import it.maestro.app.designsystem.audio.NoOpSoundManager
import it.maestro.app.designsystem.audio.SoundEffect
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

    @Test
    fun effectsVolume_isHeardRightAwayAndSavedOnRelease() = runTest {
        val repository = InMemorySettingsRepository()
        val sound = NoOpSoundManager()
        val viewModel = SettingsViewModel(repository, sound)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.previewEffectsVolume(30)
        assertEquals(30, sound.effectsVolume)
        assertEquals(100, viewModel.uiState.value.effectsVolume)

        viewModel.commitEffectsVolume(30)
        advanceUntilIdle()
        assertEquals(30, viewModel.uiState.value.effectsVolume)
        assertEquals(listOf(SoundEffect.CARD_PLAY), sound.playedEffects)
    }

    @Test
    fun effectsVolume_atZeroStaysSilent() = runTest {
        val sound = NoOpSoundManager()
        val viewModel = SettingsViewModel(InMemorySettingsRepository(), sound)
        viewModel.commitEffectsVolume(0)
        assertEquals(emptyList(), sound.playedEffects)
    }

    @Test
    fun haptics_canBeSwitchedOff() = runTest {
        val viewModel = SettingsViewModel(InMemorySettingsRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        assertEquals(true, viewModel.uiState.value.hapticsEnabled)

        viewModel.setHapticsEnabled(false)
        advanceUntilIdle()
        assertEquals(false, viewModel.uiState.value.hapticsEnabled)
    }
}
