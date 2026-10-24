package it.maestro.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import it.maestro.app.R
import it.maestro.app.data.local.datastore.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Effetti sonori di gioco supportati da Maestro (task F1-14).
 */
public enum class SoundEffect {
    CARD_PLAY,
    CARD_CAPTURE,
    SWEEP,
    CHECK_CORRECT,
    CHECK_WRONG,
    BUTTON_CLICK,
}

/**
 * Interfaccia del gestore audio per gli effetti sonori con supporto alla disattivazione (task F1-14).
 */
public interface SoundManager {
    public var isEnabled: Boolean
    public fun play(effect: SoundEffect)
    public fun release()
}

/**
 * Implementazione su [SoundPool] di Android per audio a bassa latenza (task F1-14).
 */
public class AndroidSoundManager(
    private val context: Context,
    settingsRepository: SettingsRepository? = null,
    scope: CoroutineScope? = null,
) : SoundManager {

    override var isEnabled: Boolean = true

    private var settingsJob: Job? = null

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val soundMap = mutableMapOf<SoundEffect, Int>()

    init {
        loadSounds()
        if (settingsRepository != null && scope != null) {
            settingsJob = settingsRepository.settingsFlow
                .onEach { settings -> isEnabled = settings.soundEnabled }
                .launchIn(scope)
        }
    }

    private fun loadSounds() {
        try {
            soundMap[SoundEffect.CARD_PLAY] = soundPool.load(context, R.raw.card_play, 1)
            soundMap[SoundEffect.CARD_CAPTURE] = soundPool.load(context, R.raw.card_capture, 1)
            soundMap[SoundEffect.SWEEP] = soundPool.load(context, R.raw.sweep, 1)
            soundMap[SoundEffect.CHECK_CORRECT] = soundPool.load(context, R.raw.check_correct, 1)
            soundMap[SoundEffect.CHECK_WRONG] = soundPool.load(context, R.raw.check_wrong, 1)
            soundMap[SoundEffect.BUTTON_CLICK] = soundPool.load(context, R.raw.click, 1)
        } catch (_: Throwable) {
            // Fallback per ambienti simulati o test
        }
    }

    override fun play(effect: SoundEffect) {
        if (!isEnabled) return
        try {
            val soundId = soundMap[effect] ?: return
            if (soundId > 0) {
                soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
            }
        } catch (_: Throwable) {
            // Protezione contro errori del sottosistema audio della piattaforma
        }
    }

    override fun release() {
        settingsJob?.cancel()
        soundPool.release()
    }
}

/**
 * Implementazione in memoria per test o ambienti di preview senza audio Android.
 */
public class NoOpSoundManager(
    override var isEnabled: Boolean = true,
) : SoundManager {
    public val playedEffects: MutableList<SoundEffect> = mutableListOf()

    override fun play(effect: SoundEffect) {
        if (isEnabled) {
            playedEffects.add(effect)
        }
    }

    override fun release() {
        playedEffects.clear()
    }
}
