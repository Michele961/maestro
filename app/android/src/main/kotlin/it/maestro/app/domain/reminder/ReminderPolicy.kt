package it.maestro.app.domain.reminder

import java.time.LocalDate

/** Regole del promemoria: dominio puro, senza orologio né I/O (task F1G-13). */
public object ReminderPolicy {

    /** Ore dopo l'orario previsto oltre le quali la notifica non ha più senso (telefono spento, sonno profondo). */
    public const val MAX_DELAY_HOURS: Int = 3

    /** Il consenso si chiede una volta sola, e solo dopo la prima vittoria. Un rifiuto non si richiede. */
    public fun shouldAskConsent(state: ReminderState, hasWon: Boolean): Boolean = hasWon && !state.consentAsked

    /**
     * Se mostrare la notifica adesso: acconsentito, notifiche consentite dal sistema, mai due volte nello stesso
     * giorno, mai se oggi ha già giocato e solo vicino all'ora scelta.
     */
    public fun shouldNotify(
        state: ReminderState,
        today: LocalDate,
        localHour: Int,
        lastPlayed: LocalDate?,
        canNotify: Boolean,
    ): Boolean = state.enabled &&
        canNotify &&
        state.lastNotified != today &&
        lastPlayed != today &&
        localHour >= state.hourOfDay &&
        localHour < state.hourOfDay + MAX_DELAY_HOURS

    /**
     * La prossima notifica: oggi se l'ora scelta non è ancora arrivata e oggi non è già stata data, altrimenti
     * domani. Mai nel passato: chi apre l'app dopo l'ora non riceve una notifica subito.
     */
    public fun nextSlot(state: ReminderState, today: LocalDate, localHour: Int): ReminderSlot {
        val todayStillOpen = state.lastNotified != today && localHour < state.hourOfDay
        return ReminderSlot(if (todayStillOpen) today else today.plusDays(1), state.hourOfDay)
    }
}
