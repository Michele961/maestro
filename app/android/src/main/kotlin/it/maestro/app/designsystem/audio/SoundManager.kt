package it.maestro.app.designsystem.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import it.maestro.app.R
import it.maestro.app.domain.audio.AmbientTrack
import it.maestro.app.domain.audio.AudioMixer
import it.maestro.app.domain.audio.Backdrop
import it.maestro.app.domain.audio.MusicTrack
import it.maestro.app.domain.settings.SettingsRepository
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

    /** Due note ascendenti di mandolino: un obiettivo sbloccato (task F1R-20). */
    GOAL_UNLOCKED,

    /** Monete leggere e chiusura di cassetto: un acquisto nella Bottega (task F1R-20). */
    PURCHASE,
}

/**
 * Interfaccia del gestore audio per gli effetti sonori con supporto alla disattivazione (task F1-14).
 */
public interface SoundManager {
    public var isEnabled: Boolean

    /** Volume degli effetti, 0–100 (task F1R-31b): tutti i suoni passano da questo livello. */
    public var effectsVolume: Int

    /** Volume della musica, 0–100 (task F1R-20). */
    public var musicVolume: Int

    /** Volume dell'ambiente, 0–100 (task F1R-20). */
    public var ambientVolume: Int

    public fun play(effect: SoundEffect)

    /** Cosa suona sotto la schermata corrente; `null` per il silenzio (task F1R-20). */
    public fun setBackdrop(backdrop: Backdrop?)

    /** L'app è in primo piano o no: in secondo piano musica e ambiente si fermano. */
    public fun setAppVisible(visible: Boolean)
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
        set(value) {
            field = value
            refreshVolumes()
        }
    override var effectsVolume: Int = AudioMixer.MAX_VOLUME
    override var musicVolume: Int = 40
        set(value) {
            field = value
            refreshVolumes()
        }
    override var ambientVolume: Int = 40
        set(value) {
            field = value
            refreshVolumes()
        }

    private var settingsJob: Job? = null
    private var backdrop: Backdrop? = null
    private var appVisible: Boolean = true
    private var musicPlayer: Playing<MusicTrack>? = null
    private var ambientPlayer: Playing<AmbientTrack>? = null

    /** Un [MediaPlayer] e il brano che sta suonando. */
    private class Playing<T>(val track: T, val player: MediaPlayer)

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
                .onEach { settings ->
                    isEnabled = settings.soundEnabled
                    effectsVolume = settings.effectsVolume
                    musicVolume = settings.musicVolume
                    ambientVolume = settings.ambientVolume
                }
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
            soundMap[SoundEffect.GOAL_UNLOCKED] = soundPool.load(context, R.raw.sfx_goal_unlocked, 1)
            soundMap[SoundEffect.PURCHASE] = soundPool.load(context, R.raw.sfx_purchase, 1)
        } catch (_: Throwable) {
            // Fallback per ambienti simulati o test
        }
    }

    override fun play(effect: SoundEffect) {
        if (!isEnabled) return
        try {
            val soundId = soundMap[effect] ?: return
            if (soundId > 0) {
                val gain = AudioMixer.effectsGain(effectsVolume, soundEnabled = true)
                if (gain > 0f) soundPool.play(soundId, gain, gain, 1, 0, 1.0f)
            }
        } catch (_: Throwable) {
            // Protezione contro errori del sottosistema audio della piattaforma
        }
    }

    override fun setBackdrop(backdrop: Backdrop?) {
        this.backdrop = backdrop
        syncPlayers()
    }

    override fun setAppVisible(visible: Boolean) {
        appVisible = visible
        syncPlayers()
    }

    private fun syncPlayers() {
        val wanted = if (appVisible) backdrop else null
        musicPlayer = swap(musicPlayer, wanted?.music, true, ::musicResource)
        ambientPlayer = swap(ambientPlayer, wanted?.ambient, looping = true, ::ambientResource)
        refreshVolumes()
    }

    /** Se il brano voluto è già in corso lo lascia stare; altrimenti ferma il vecchio e avvia il nuovo. */
    private fun <T : Any> swap(current: Playing<T>?, wanted: T?, looping: Boolean, resource: (T) -> Int): Playing<T>? {
        if (current?.track == wanted) return current
        current?.player?.let { runCatching { it.stop() }; it.release() }
        if (wanted == null) return null
        return try {
            MediaPlayer.create(context, resource(wanted))?.let { player ->
                player.isLooping = looping
                player.start()
                Playing(wanted, player)
            }
        } catch (_: Throwable) {
            null // Ambienti simulati o test senza audio
        }
    }

    private fun refreshVolumes() {
        musicPlayer?.player?.let { player ->
            val gain = AudioMixer.musicGain(musicVolume, isEnabled, maestroSpeaking = false) // la musica non si abbassa (scelta di Michele)
            runCatching { player.setVolume(gain, gain) }
        }
        ambientPlayer?.player?.let { player ->
            val gain = AudioMixer.ambientGain(ambientVolume, isEnabled)
            runCatching { player.setVolume(gain, gain) }
        }
    }

    private fun musicResource(track: MusicTrack): Int = when (track) {
        MusicTrack.CIRCLE_DAY -> R.raw.music_circle_day
        MusicTrack.CIRCLE_EVENING -> R.raw.music_circle_evening
        MusicTrack.TENSE -> R.raw.music_tense
    }

    private fun ambientResource(track: AmbientTrack): Int = when (track) {
        AmbientTrack.BAR -> R.raw.ambient_bar
        AmbientTrack.TABLE -> R.raw.ambient_table
    }

    override fun release() {
        settingsJob?.cancel()
        musicPlayer?.player?.release()
        ambientPlayer?.player?.release()
        musicPlayer = null
        ambientPlayer = null
        soundPool.release()
    }
}

/**
 * Implementazione in memoria per test o ambienti di preview senza audio Android.
 */
public class NoOpSoundManager(
    override var isEnabled: Boolean = true,
    override var effectsVolume: Int = AudioMixer.MAX_VOLUME,
    override var musicVolume: Int = 40,
    override var ambientVolume: Int = 40,
) : SoundManager {
    public val playedEffects: MutableList<SoundEffect> = mutableListOf()

    /** Ultimo sfondo richiesto, per i test. */
    public var currentBackdrop: Backdrop? = null
        private set

    /** Ultimo stato di visibilità comunicato, per i test. */
    public var appVisible: Boolean = true
        private set

    override fun setBackdrop(backdrop: Backdrop?) {
        currentBackdrop = backdrop
    }

    override fun setAppVisible(visible: Boolean) {
        appVisible = visible
    }

    override fun play(effect: SoundEffect) {
        if (isEnabled && effectsVolume > 0) {
            playedEffects.add(effect)
        }
    }

    override fun release() {
        playedEffects.clear()
    }
}
