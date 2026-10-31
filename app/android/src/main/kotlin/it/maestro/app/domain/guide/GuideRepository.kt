package it.maestro.app.domain.guide

import kotlinx.coroutines.flow.Flow

/** Come è andata la mano d'esordio guidata (F1G-02). */
public enum class GuidedHandStatus {
    /** Non ancora fatta né saltata: chi è nuovo vede il primo avvio. */
    NOT_DONE,

    /** Giocata fino alla fine. */
    DONE,

    /** Saltata («So già giocare», «Salta la guida») o non dovuta a chi aveva già giocato: il capitolo 1 è fatto. */
    SKIPPED,
}

/**
 * Ciò che la guida ricorda (task F1G-16b).
 *
 * @property hand la mano d'esordio guidata; è anche il flag «primo avvio completato» (F1G-01).
 * @property reviewed il giocatore ha aperto la revisione di una partita: è il segnale del capitolo 7.
 */
public data class GuideProgress(
    val hand: GuidedHandStatus = GuidedHandStatus.NOT_DONE,
    val reviewed: Boolean = false,
)

/** Persistenza dello stato della guida. Senza dati vale il valore di default: nulla di esistente viene toccato. */
public interface GuideRepository {
    public fun observe(): Flow<GuideProgress>

    /** Registra l'esito della mano guidata. */
    public suspend fun setHand(status: GuidedHandStatus)

    /** Registra che una revisione è stata aperta; ripeterla non cambia nulla. */
    public suspend fun markReviewed()
}
