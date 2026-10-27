package it.maestro.app.domain.match

/**
 * Cancello tra la partita e la sua messa in scena (ADR-0015, task F1R-08).
 *
 * Il bot muove solo dopo che il tavolo ha finito di mostrare la mossa [moveId] e la distribuzione [dealId]:
 * nessun timer fisso decide quando un'animazione è finita.
 */
public interface PresentationGate {
    /** Ritorna quando la mossa [moveId] e la distribuzione [dealId] sono state mostrate per intero. */
    public suspend fun awaitPresented(moveId: Long, dealId: Long)

    /** Cancello sempre aperto: per i test senza interfaccia e per le partite senza animazioni. */
    public object Immediate : PresentationGate {
        override suspend fun awaitPresented(moveId: Long, dealId: Long): Unit = Unit
    }
}
