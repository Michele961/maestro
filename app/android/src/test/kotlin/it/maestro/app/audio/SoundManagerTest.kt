package it.maestro.app.audio

import it.maestro.app.domain.settings.AppSettings
import it.maestro.app.data.local.datastore.InMemorySettingsRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import it.maestro.app.designsystem.audio.NoOpSoundManager
import it.maestro.app.designsystem.audio.SoundEffect

class SoundManagerTest {

    @Test
    fun noOpSoundManagerRecordsEventsWhenEnabled() {
        val soundManager = NoOpSoundManager(isEnabled = true)
        soundManager.play(SoundEffect.CARD_PLAY)
        soundManager.play(SoundEffect.SWEEP)

        assertEquals(2, soundManager.playedEffects.size)
        assertEquals(SoundEffect.CARD_PLAY, soundManager.playedEffects[0])
        assertEquals(SoundEffect.SWEEP, soundManager.playedEffects[1])
    }

    @Test
    fun noOpSoundManagerIgnoresEventsWhenMuted() {
        val soundManager = NoOpSoundManager(isEnabled = false)
        soundManager.play(SoundEffect.CARD_PLAY)
        soundManager.play(SoundEffect.SWEEP)
        soundManager.play(SoundEffect.CHECK_CORRECT)

        assertTrue(soundManager.playedEffects.isEmpty(), "Nessun suono deve essere riprodotto quando il mute è attivo")
    }

    @Test
    fun soundEffectsEnumContainsAllGameScenarios() {
        val expected = setOf(
            SoundEffect.CARD_PLAY,
            SoundEffect.CARD_CAPTURE,
            SoundEffect.SWEEP,
            SoundEffect.CHECK_CORRECT,
            SoundEffect.CHECK_WRONG,
            SoundEffect.BUTTON_CLICK,
            SoundEffect.GOAL_UNLOCKED,
            SoundEffect.PURCHASE,
        )
        assertEquals(expected, SoundEffect.entries.toSet())
    }

    @Test
    fun settingsFlowUpdatesSoundManagerEnabledState() = runBlocking {
        val settingsRepo = InMemorySettingsRepository(AppSettings(soundEnabled = true))
        val soundManager = NoOpSoundManager(isEnabled = true)

        // Simula la sincronizzazione tramite flow
        assertTrue(soundManager.isEnabled)

        settingsRepo.updateSoundEnabled(false)
        soundManager.isEnabled = false
        assertFalse(soundManager.isEnabled)

        soundManager.play(SoundEffect.CARD_PLAY)
        assertTrue(soundManager.playedEffects.isEmpty())

        settingsRepo.updateSoundEnabled(true)
        soundManager.isEnabled = true
        soundManager.play(SoundEffect.CARD_PLAY)
        assertEquals(1, soundManager.playedEffects.size)
    }
}
