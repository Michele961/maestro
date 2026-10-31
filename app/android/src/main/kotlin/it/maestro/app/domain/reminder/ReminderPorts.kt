package it.maestro.app.domain.reminder

import kotlinx.coroutines.flow.Flow

/** Persistenza dello stato del promemoria (task F1G-13). */
public interface ReminderRepository {
    /** Lo stato salvato, aggiornato a ogni modifica; senza dati è [ReminderState] di default. */
    public fun observe(): Flow<ReminderState>

    /** Lo stato salvato adesso. */
    public suspend fun load(): ReminderState

    /** Salva [state] sostituendo il precedente. */
    public suspend fun save(state: ReminderState)
}

/** Pianifica e annulla l'unica notifica attesa; l'implementazione è della piattaforma (AlarmManager inesatto). */
public interface ReminderScheduler {
    /** Pianifica la notifica per [slot], sostituendo l'eventuale pianificazione precedente. */
    public fun schedule(slot: ReminderSlot)

    /** Annulla la pianificazione. */
    public fun cancel()
}

/** Mostra la notifica del promemoria; l'implementazione è della piattaforma. */
public interface ReminderNotifier {
    /** Le notifiche sono consentite dal sistema (permesso concesso e non disattivate). */
    public fun canNotify(): Boolean

    /** Mostra la notifica. */
    public fun show()
}
