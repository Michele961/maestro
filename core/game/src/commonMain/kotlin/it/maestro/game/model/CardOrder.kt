package it.maestro.game.model

/**
 * Comparatore canonico delle carte (`docs/conventions.md`, spec §4.9).
 *
 * Ordina prima per seme nell'ordine canonico ([Suit.DENARI], [Suit.COPPE], [Suit.SPADE], [Suit.BASTONI])
 * e, a parità di seme, per valore di presa crescente ([Rank.value] da 1 a 10).
 */
public object CardOrder : Comparator<Card> {
    override fun compare(a: Card, b: Card): Int {
        val suitComparison = a.suit.ordinal.compareTo(b.suit.ordinal)
        if (suitComparison != 0) return suitComparison
        return a.rank.value.compareTo(b.rank.value)
    }
}
