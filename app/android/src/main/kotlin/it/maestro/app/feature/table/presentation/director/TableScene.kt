package it.maestro.app.feature.table.presentation.director

import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaView

/**
 * Ciò che il tavolo mostra in questo momento (`docs/app/table-director.md` §2): l'unico input della UI del tavolo.
 *
 * @property view vista di riferimento: quella prima della battuta mentre la si mette in scena, quella dopo a riposo.
 * @property step passo in corso, `null` a riposo.
 * @property beatId ultima battuta messa in scena (o in scena).
 * @property busy `true` finché ci sono passi da mostrare, in corso o in coda.
 */
public data class TableScene(
    val view: ScopaView,
    val humanSeat: Seat,
    val step: SceneStep? = null,
    val beatId: Long = 0L,
    val busy: Boolean = false,
) {
    /** La mano è giocabile solo a scena ferma e al turno del giocatore (`table-director.md` §4.2). */
    val inputEnabled: Boolean
        get() = !busy && view.phase == DealPhase.PLAYING && view.toMove == humanSeat
}
