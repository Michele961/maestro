package it.maestro.app.domain.reminder

import it.maestro.app.domain.time.Clock
import java.time.LocalDate

/**
 * Unico punto che decide e pianifica il promemoria (task F1G-13): lo chiamano l'avvio dell'app, il ricevitore
 * dell'allarme e quello del riavvio del telefono, e le schermate quando il giocatore sceglie.
 *
 * Nessun dato esce dal telefono: tutto è locale.
 */
public class ReminderCoordinator(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val notifier: ReminderNotifier,
    private val clock: Clock,
    /** Ultimo giorno in cui il giocatore ha giocato, dal registro della serie; `null` se mai. */
    private val lastPlayed: suspend () -> LocalDate?,
) {
    /** Registra la risposta alla richiesta di consenso: da qui in poi non si chiede più. */
    public suspend fun answerConsent(accepted: Boolean) {
        apply(repository.load().copy(consentAsked = true, enabled = accepted))
    }

    /** Accende o spegne il promemoria (interruttore nelle Impostazioni, o permesso di sistema negato). */
    public suspend fun setEnabled(enabled: Boolean) {
        apply(repository.load().copy(enabled = enabled))
    }

    /** Rimette in piedi la pianificazione: all'avvio dell'app e al riavvio del telefono. */
    public suspend fun reschedule() {
        apply(repository.load())
    }

    /** L'allarme è scattato: mostra la notifica se le regole lo consentono, poi pianifica la prossima. */
    public suspend fun onAlarm() {
        val state = repository.load()
        val today = clock.today()
        val notify = ReminderPolicy.shouldNotify(state, today, clock.localHour(), lastPlayed(), notifier.canNotify())
        if (notify) notifier.show()
        apply(if (notify) state.copy(lastNotified = today) else state)
    }

    private suspend fun apply(state: ReminderState) {
        repository.save(state)
        if (state.enabled) {
            scheduler.schedule(ReminderPolicy.nextSlot(state, clock.today(), clock.localHour()))
        } else {
            scheduler.cancel()
        }
    }
}
