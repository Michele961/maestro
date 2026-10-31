package it.maestro.app.feature.pregame.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.domain.guide.GuideRepository
import it.maestro.app.domain.mastery.MasteryRepository
import it.maestro.app.domain.match.HouseRule
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Prepara la partita: ricava dalle lacune del giocatore l'obiettivo che il Maestro consiglia. */
public class PreGameViewModel(mastery: MasteryRepository, private val guide: GuideRepository) : ViewModel() {
    /** L'obiettivo consigliato, `null` finché non c'è abbastanza storia. */
    public val suggestedObjective: StateFlow<TrainingFundamental?> = mastery.observeMasteryProfile()
        .map(ObjectiveAdvisor::suggest)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Le mini-lezioni del Maestro sulle regole della casa già mostrate (F1G-09); `null` finché non si sa. */
    public val seenRuleLessons: StateFlow<Set<HouseRule>?> = guide.observe()
        .map { it.seenRuleLessons }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Registra che la mini-lezione di [rule] è stata mostrata e la partita è partita: non compare più. */
    public fun onRuleLessonSeen(rule: HouseRule) {
        viewModelScope.launch { guide.markRuleLessonSeen(rule) }
    }
}
