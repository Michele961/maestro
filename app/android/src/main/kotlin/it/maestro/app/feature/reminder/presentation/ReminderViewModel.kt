package it.maestro.app.feature.reminder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.domain.mastery.MasteryRepository
import it.maestro.app.domain.reminder.ReminderCoordinator
import it.maestro.app.domain.reminder.ReminderPolicy
import it.maestro.app.domain.reminder.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Cosa mostrare del promemoria (task F1G-13).
 *
 * @property askConsent mostrare la spiegazione con la richiesta di consenso (una volta sola, dopo la prima vittoria).
 * @property switchAvailable mostrare l'interruttore nelle Impostazioni: non prima della prima vittoria.
 * @property enabled il promemoria è acceso.
 */
public data class ReminderUiState(
    val askConsent: Boolean = false,
    val switchAvailable: Boolean = false,
    val enabled: Boolean = false,
)

/**
 * Stato e azioni del promemoria per la Home (richiesta di consenso) e le Impostazioni (interruttore). Le decisioni
 * stanno in [ReminderPolicy]; il permesso di sistema lo chiede la UI e ne riporta l'esito con [onPermissionResult].
 */
public class ReminderViewModel(
    repository: ReminderRepository,
    mastery: MasteryRepository,
    private val coordinator: ReminderCoordinator,
) : ViewModel() {

    public val uiState: StateFlow<ReminderUiState> = combine(
        repository.observe(),
        mastery.observeMatchHistory(),
    ) { state, history ->
        val hasWon = history.any { it.isWin }
        ReminderUiState(
            askConsent = ReminderPolicy.shouldAskConsent(state, hasWon),
            // La cronologia tiene le ultime partite: chi ha già risposto o acceso il promemoria non perde l'interruttore.
            switchAvailable = hasWon || state.consentAsked || state.enabled,
            enabled = state.enabled,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReminderUiState())

    /** Il giocatore ha detto sì alla spiegazione: la richiesta non si ripete; il permesso di sistema segue. */
    public fun onConsentAccepted() {
        viewModelScope.launch { coordinator.answerConsent(accepted = true) }
    }

    /** Il giocatore ha detto no: nessuna notifica e nessuna insistenza. */
    public fun onConsentDeclined() {
        viewModelScope.launch { coordinator.answerConsent(accepted = false) }
    }

    /** Interruttore delle Impostazioni (lo spegnimento, o l'accensione quando il permesso c'è già). */
    public fun setEnabled(enabled: Boolean) {
        viewModelScope.launch { coordinator.setEnabled(enabled) }
    }

    /** Esito del permesso di sistema: se negato il promemoria si spegne, senza altre richieste. */
    public fun onPermissionResult(granted: Boolean) {
        viewModelScope.launch { coordinator.setEnabled(granted) }
    }
}
