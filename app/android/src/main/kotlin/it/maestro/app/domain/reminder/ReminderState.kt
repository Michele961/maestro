package it.maestro.app.domain.reminder

import java.time.LocalDate

/**
 * Stato del promemoria quotidiano, salvato in DataStore (task F1G-13). Il default è «spento, mai chiesto».
 *
 * @property enabled il giocatore ha acconsentito e l'interruttore è acceso.
 * @property consentAsked la richiesta di consenso dopo la prima vittoria è già stata fatta, qualunque sia stata la
 *   risposta: non si ripete mai.
 * @property hourOfDay ora locale (0–23) alla quale il promemoria è atteso.
 * @property lastNotified giorno dell'ultima notifica mostrata, `null` se mai.
 */
public data class ReminderState(
    val enabled: Boolean = false,
    val consentAsked: Boolean = false,
    val hourOfDay: Int = DEFAULT_HOUR,
    val lastNotified: LocalDate? = null,
) {
    public companion object {
        /** Ora di default: dopo cena, quando si ha tempo per una mano. */
        public const val DEFAULT_HOUR: Int = 19
    }
}

/** Il momento in cui pianificare la prossima notifica: [hour] del giorno [date] (ora locale). */
public data class ReminderSlot(val date: LocalDate, val hour: Int)
