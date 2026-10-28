package it.maestro.app.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.domain.coach.Chattiness
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
    val gameSpeed: GameSpeed = GameSpeed.NORMAL,
    val maestroChattiness: Chattiness = Chattiness.NORMAL,
    val showPointsRace: Boolean = false,
)

/**
 * Stato e azioni della schermata Impostazioni (task F1R-31a). Ogni scelta si applica e si salva subito:
 * non c'è «Salva». Le azioni di sviluppo (crash di prova, partita rapida) restano alla regia della navigazione.
 */
public class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    public val uiState: StateFlow<SettingsUiState> = settingsRepository.settingsFlow
        .map { settings ->
            SettingsUiState(
                soundEnabled = settings.soundEnabled,
                gameSpeed = settings.gameSpeed,
                maestroChattiness = settings.maestroChattiness,
                showPointsRace = settings.showPointsRace,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    public fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateSoundEnabled(enabled) }
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
}
