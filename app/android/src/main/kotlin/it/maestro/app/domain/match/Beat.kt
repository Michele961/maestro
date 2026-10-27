package it.maestro.app.domain.match

import it.maestro.game.model.Seat
import it.maestro.game.scopa.GameEvent
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView

/**
 * Una mossa applicata, vista dal giocatore umano (ADR-0015, `docs/app/table-director.md` §2).
 *
 * Contiene solo informazioni pubbliche: la mossa giocata, gli eventi di `core:game` e la vista del
 * giocatore prima e dopo. Nessuna carta nascosta.
 */
public data class Beat(
    val id: Long,
    val actor: Seat,
    val move: ScopaMove,
    val events: List<GameEvent>,
    val viewBefore: ScopaView,
    val viewAfter: ScopaView,
)
