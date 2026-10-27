package it.maestro.app.domain.match

import it.maestro.game.model.Card
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaView
import it.maestro.game.training.SeatLead
import it.maestro.game.training.SettebelloLocation
import it.maestro.game.training.calculatePartialPrimiera
import it.maestro.game.training.determineSettebelloLocation

/** Valori di una categoria per i due giocatori e chi è in vantaggio (`docs/app/game-feel.md` P4). */
public data class RaceLine(val human: Int, val opponent: Int) {
    /** In vantaggio chi ha di più; a parità nessuno (spec 6.2: le parità non assegnano il punto). */
    val lead: SeatLead
        get() = when {
            human > opponent -> SeatLead.HUMAN
            human < opponent -> SeatLead.OPPONENT
            else -> SeatLead.TIE
        }
}

/**
 * Corsa ai punti della smazzata (game-feel P4): chi va verso Carte, Denari, Settebello e Primiera,
 * calcolata dalla sola vista del giocatore (`view.seat` è l'umano). Le scope non servono: si vedono già nelle pile.
 *
 * La primiera è quella parziale sulle carte già prese (spec 6.3): è un indicatore di tendenza, non il punto assegnato.
 */
public data class PointsRace(
    val cards: RaceLine,
    val denari: RaceLine,
    val primiera: RaceLine,
    val settebello: SettebelloLocation,
)

/** Corsa ai punti per la vista [view]. */
public fun pointsRaceOf(view: ScopaView): PointsRace {
    val mine = view.capturedBy[view.seat].orEmpty()
    val theirs = view.capturedBy[view.seat.other()].orEmpty()
    fun denari(cards: List<Card>) = cards.count { it.suit == Suit.DENARI }
    return PointsRace(
        cards = RaceLine(mine.size, theirs.size),
        denari = RaceLine(denari(mine), denari(theirs)),
        primiera = RaceLine(calculatePartialPrimiera(mine), calculatePartialPrimiera(theirs)),
        settebello = determineSettebelloLocation(view),
    )
}
