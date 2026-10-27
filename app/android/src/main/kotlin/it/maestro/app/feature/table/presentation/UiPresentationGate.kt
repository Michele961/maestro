package it.maestro.app.feature.table.presentation

import it.maestro.app.domain.match.PresentationGate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

/**
 * [PresentationGate] alimentato dal tavolo (task F1R-08): la UI segnala l'ultima mossa e l'ultima distribuzione
 * che ha finito di mostrare. Verrà sostituito dal `TableDirector` (F1R-10), che conosce la scena direttamente.
 */
public class UiPresentationGate : PresentationGate {
    private val presented = MutableStateFlow(Presented(moveId = 0L, dealId = 0L))

    /** Chiamato dal tavolo quando non ci sono animazioni in corso. */
    public fun onPresented(moveId: Long, dealId: Long) {
        presented.value = Presented(moveId, dealId)
    }

    override suspend fun awaitPresented(moveId: Long, dealId: Long) {
        presented.first { it.moveId >= moveId && it.dealId >= dealId }
    }

    private data class Presented(val moveId: Long, val dealId: Long)
}
