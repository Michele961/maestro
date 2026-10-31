package it.maestro.app.feature.season.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.domain.season.SeasonRepository
import it.maestro.app.domain.story.StoryDirector
import it.maestro.app.domain.story.StoryRepository
import it.maestro.app.domain.story.StoryScene
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Le scene della Scalata (task F1G-16a): mostra una scena alla volta, nell'ordine di [StoryDirector.pending]. Finita o
 * saltata, la scena si segna vista e non torna più; la successiva compare da sola.
 */
public class StoryViewModel(
    season: SeasonRepository,
    private val story: StoryRepository,
) : ViewModel() {

    /** La scena da mostrare ora, `null` se non ce n'è. */
    public val scene: StateFlow<StoryScene?> = combine(season.observeProgress(), story.observeSeen()) { progress, seen ->
        StoryDirector.pending(progress, seen).firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** La scena [sceneId] è finita o è stata saltata: non si rivede. */
    public fun onSceneDone(sceneId: String) {
        viewModelScope.launch { story.markSeen(sceneId) }
    }
}
