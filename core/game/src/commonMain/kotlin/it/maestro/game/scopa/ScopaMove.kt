package it.maestro.game.scopa

import it.maestro.game.model.Card

/**
 * Rappresenta una mossa di gioco nella Scopa: una [card] giocata dalla mano del giocatore di turno
 * e l'insieme [capture] delle carte prese dal tavolo (spec §4.9).
 *
 * Se la carta giocata non effettua alcuna presa (spec §4.5, §4.6), [capture] è vuoto.
 *
 * @property card Carta giocata dalla mano.
 * @property capture Insieme delle carte prese dal tavolo (vuoto se la giocata è uno scarto).
 */
public data class ScopaMove(
    public val card: Card,
    public val capture: Set<Card> = emptySet(),
)
