package it.maestro.app.domain.audio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Livelli audio (F1R-31b): tre volumi, interruttore generale e abbassamento della musica quando parla il Maestro. */
class AudioMixerTest {

    @Test
    fun `il guadagno segue il volume e si annulla con l'audio spento`() {
        assertEquals(1f, AudioMixer.effectsGain(100, soundEnabled = true))
        assertEquals(0.5f, AudioMixer.effectsGain(50, soundEnabled = true))
        assertEquals(0f, AudioMixer.effectsGain(100, soundEnabled = false))
        assertEquals(0f, AudioMixer.ambientGain(80, soundEnabled = false))
    }

    @Test
    fun `i volumi fuori scala vengono riportati tra 0 e 100`() {
        assertEquals(1f, AudioMixer.effectsGain(250, soundEnabled = true))
        assertEquals(0f, AudioMixer.effectsGain(-5, soundEnabled = true))
    }

    @Test
    fun `la musica si abbassa mentre parla il Maestro e torna piena dopo`() {
        val speaking = AudioMixer.musicGain(80, soundEnabled = true, maestroSpeaking = true)
        val silent = AudioMixer.musicGain(80, soundEnabled = true, maestroSpeaking = false)
        assertEquals(0.8f, silent)
        assertTrue(speaking < silent)
        assertEquals(1f, AudioMixer.duckMusicWhile(maestroSpeaking = false))
        assertEquals(AudioMixer.DUCK_FACTOR, AudioMixer.duckMusicWhile(maestroSpeaking = true))
    }

    @Test
    fun `con l'audio spento la musica resta spenta anche se il Maestro tace`() {
        assertEquals(0f, AudioMixer.musicGain(80, soundEnabled = false, maestroSpeaking = false))
    }
}
