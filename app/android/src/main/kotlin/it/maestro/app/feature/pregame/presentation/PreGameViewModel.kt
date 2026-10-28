package it.maestro.app.feature.pregame.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.domain.mastery.MasteryRepository
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Prepara la partita: ricava dalle lacune del giocatore l'obiettivo che il Maestro consiglia. */
public class PreGameViewModel(mastery: MasteryRepository) : ViewModel() {
    /** L'obiettivo consigliato, `null` finché non c'è abbastanza storia. */
    public val suggestedObjective: StateFlow<TrainingFundamental?> = mastery.observeMasteryProfile()
        .map(ObjectiveAdvisor::suggest)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
