package it.maestro.app.feature.season.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.domain.rewards.RewardEngine
import it.maestro.app.domain.season.SeasonProgress
import it.maestro.app.domain.season.SeasonRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Presenta la Scalata dei Circoli (task F1G-07): il capitolo corrente e quelli fatti, derivati dallo stato.
 * I capitoli finiti che non hanno ancora dato la ricompensa la danno qui, una volta, tramite `RewardEngine`.
 */
public class SeasonViewModel(
    private val repository: SeasonRepository,
    private val rewards: RewardEngine,
) : ViewModel() {

    public val state: StateFlow<SeasonProgress> = repository.observeProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SeasonProgress(emptyList()))

    init {
        viewModelScope.launch {
            repository.observeProgress().collect { progress ->
                progress.toClaim.forEach { rewards.onSeasonChapterCompleted(it) }
            }
        }
    }
}
