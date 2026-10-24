package it.maestro.app.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit

/**
 * Tema per la resa grafica del mazzo di carte regionali (ADR-0008, task F1-13).
 * Separa le regole di gioco dalla rappresentazione grafica delle carte.
 */
public interface DeckTheme {
    public val id: String
    public val name: String
    public fun getCardDrawableResId(context: Context, card: Card): Int?
    public fun getCardBackDrawableResId(context: Context): Int?
}

/**
 * Tema predefinito per il mazzo di carte Napoletane (ADR-0008, task F1-13).
 * Cerca automaticamente drawable con nome convenzionale:
 * - `card_<seme>_<valore>` (es: `card_denari_7`, `card_denari_asso`, etc.)
 * - `card_back`
 * Se l'asset grafico non è presente, restituisce null per attivare il fallback vettoriale.
 */
public object NeapolitanDeckTheme : DeckTheme {
    override val id: String = "napoletane"
    override val name: String = "Napoletane"

    @SuppressLint("DiscouragedApi")
    override fun getCardDrawableResId(context: Context, card: Card): Int? {
        val suitName = card.suit.name.lowercase()
        val rankName = when (card.rank) {
            Rank.ASSO -> "asso"
            Rank.DUE -> "2"
            Rank.TRE -> "3"
            Rank.QUATTRO -> "4"
            Rank.CINQUE -> "5"
            Rank.SEI -> "6"
            Rank.SETTE -> "7"
            Rank.FANTE -> "fante"
            Rank.CAVALLO -> "cavallo"
            Rank.RE -> "re"
        }
        val resName = "card_${suitName}_$rankName"
        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
        return if (resId != 0) resId else null
    }

    @SuppressLint("DiscouragedApi")
    override fun getCardBackDrawableResId(context: Context): Int? {
        val resId = context.resources.getIdentifier("card_back", "drawable", context.packageName)
        return if (resId != 0) resId else null
    }
}
