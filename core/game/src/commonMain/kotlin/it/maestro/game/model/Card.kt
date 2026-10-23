package it.maestro.game.model

/**
 * Carta del mazzo italiano da 40 carte, identificata da [rank] e [suit] (spec §1.1).
 *
 * L'ordinamento naturale delega a [CardOrder] (prima il seme nell'ordine canonico, poi il valore crescente).
 *
 * @property rank Valore della carta.
 * @property suit Seme della carta.
 */
public data class Card(
    public val rank: Rank,
    public val suit: Suit,
) : Comparable<Card> {
    override fun compareTo(other: Card): Int = CardOrder.compare(this, other)
}

private val CANONICAL_DECK: List<Card> = buildList(Suit.entries.size * Rank.entries.size) {
    for (suit in Suit.entries) {
        for (rank in Rank.entries) {
            add(Card(rank = rank, suit = suit))
        }
    }
}

/**
 * Restituisce il mazzo completo di 40 carte distinte in ordine canonico (spec §1.1, §3.1):
 * per ogni seme in [Suit.entries] (`DENARI`, `COPPE`, `SPADE`, `BASTONI`),
 * tutti i valori in [Rank.entries] da [Rank.ASSO] (1) a [Rank.RE] (10).
 */
public fun fullDeck(): List<Card> = CANONICAL_DECK
