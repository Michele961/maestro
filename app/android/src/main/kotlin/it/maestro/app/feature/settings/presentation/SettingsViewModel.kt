package it.maestro.app.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.designsystem.audio.SoundEffect
import it.maestro.app.designsystem.audio.SoundManager
import it.maestro.app.domain.audio.AudioMixer
import it.maestro.app.domain.coach.Chattiness
import it.maestro.app.domain.audio.MusicChoice
import it.maestro.app.domain.settings.AppLanguage
import it.maestro.app.domain.settings.GameSpeed
import it.maestro.app.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Valori delle impostazioni mostrati dalla schermata (task F1R-31a). */
public data class SettingsUiState(
    val soundEnabled: Boolean = true,
    val effectsVolume: Int = 100,
    val musicVolume: Int = 40,
    val ambientVolume: Int = 40,
    val musicChoice: MusicChoice = MusicChoice.AUTO,
    val hapticsEnabled: Boolean = true,
    val gameSpeed: GameSpeed = GameSpeed.NORMAL,
    val maestroChattiness: Chattiness = Chattiness.NORMAL,
    val showPointsRace: Boolean = false,
    val language: AppLanguage = AppLanguage.SYSTEM,
)

/**
 * Stato e azioni della schermata Impostazioni (task F1R-31a). Ogni scelta si applica e si salva subito:
 * non c'è «Salva». Le azioni di sviluppo (crash di prova, partita rapida) restano alla regia della navigazione.
 */
public class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val soundManager: SoundManager? = null,
) : ViewModel() {

    public val uiState: StateFlow<SettingsUiState> = settingsRepository.settingsFlow
        .map { settings ->
            SettingsUiState(
                soundEnabled = settings.soundEnabled,
                effectsVolume = settings.effectsVolume,
                musicVolume = settings.musicVolume,
                ambientVolume = settings.ambientVolume,
                musicChoice = settings.musicChoice,
                hapticsEnabled = settings.hapticsEnabled,
                gameSpeed = settings.gameSpeed,
                maestroChattiness = settings.maestroChattiness,
                showPointsRace = settings.showPointsRace,
                language = AppLanguage.fromTag(settings.language),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    public fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateSoundEnabled(enabled) }
    }

    /** Il cursore si muove: il volume vale subito per il prossimo suono, ma non si salva a ogni pixel. */
    public fun previewEffectsVolume(volume: Int) {
        soundManager?.effectsVolume = volume.coerceIn(0, AudioMixer.MAX_VOLUME)
    }

    /** Il cursore è stato lasciato: salva il volume e fa sentire un suono di prova (task F1R-31b). */
    public fun commitEffectsVolume(volume: Int) {
        val clamped = volume.coerceIn(0, AudioMixer.MAX_VOLUME)
        soundManager?.effectsVolume = clamped
        soundManager?.play(SoundEffect.CARD_PLAY)
        viewModelScope.launch { settingsRepository.updateEffectsVolume(clamped) }
    }

    /** Il cursore della musica si muove: si sente subito, ma si salva solo al rilascio (task F1R-20). */
    public fun previewMusicVolume(volume: Int) {
        soundManager?.musicVolume = volume.coerceIn(0, AudioMixer.MAX_VOLUME)
    }

    public fun commitMusicVolume(volume: Int) {
        val clamped = volume.coerceIn(0, AudioMixer.MAX_VOLUME)
        soundManager?.musicVolume = clamped
        viewModelScope.launch { settingsRepository.updateMusicVolume(clamped) }
    }

    /** Come [previewMusicVolume], per il rumore di fondo. */
    public fun previewAmbientVolume(volume: Int) {
        soundManager?.ambientVolume = volume.coerceIn(0, AudioMixer.MAX_VOLUME)
    }

    public fun commitAmbientVolume(volume: Int) {
        val clamped = volume.coerceIn(0, AudioMixer.MAX_VOLUME)
        soundManager?.ambientVolume = clamped
        viewModelScope.launch { settingsRepository.updateAmbientVolume(clamped) }
    }

    /** Il giocatore sceglie il brano: vale subito e in tutta l'app. */
    public fun setMusicChoice(choice: MusicChoice) {
        viewModelScope.launch { settingsRepository.updateMusicChoice(choice) }
    }

    public fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateHapticsEnabled(enabled) }
    }

    public fun setGameSpeed(speed: GameSpeed) {
        viewModelScope.launch { settingsRepository.updateGameSpeed(speed) }
    }

    public fun setMaestroChattiness(level: Chattiness) {
        viewModelScope.launch { settingsRepository.updateMaestroChattiness(level) }
    }

    public fun setShowPointsRace(show: Boolean) {
        viewModelScope.launch { settingsRepository.updateShowPointsRace(show) }
    }

    public fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsRepository.updateLanguage(language.tag) }
    }
}
