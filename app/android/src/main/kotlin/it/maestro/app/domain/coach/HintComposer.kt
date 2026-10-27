package it.maestro.app.domain.coach

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaMove

/** Consiglio composto, ancora senza testo: frasi in ordine e posa del Maestro. */
public data class HintLine(val phrases: List<Phrase>, val pose: Pose)

/**
 * Consiglio del Maestro su richiesta (task F1R-24e): nomina la carta da giocare e la ragione, in codici e argomenti
 * (il testo sta in `strings.xml`, ADR-0007). Sostituisce le sei frasi fisse scelte dal solo tipo di mossa.
 * Puro e deterministico: la variante dell'apertura dipende dalla carta, così a parità di mano il consiglio non cambia.
 */
public object HintComposer {

    private val settebello = Card(Rank.SETTE, Suit.DENARI)
    private const val LOW_TABLE_SUM = 10

    /** Perché [move] è la mossa consigliata, dal motivo più forte al più debole. */
    public enum class Reason { SWEEP, SETTEBELLO, DENARI, PRIMIERA, CAPTURE, SAFE_DISCARD, LEAST_BAD_DISCARD }

    /** La ragione di [move] giocata su un banco di [table]. */
    public fun reason(move: ScopaMove, table: List<Card>): Reason {
        val taken = move.capture
        if (taken.isEmpty()) {
            val left = table.sumOf { it.rank.value } + move.card.rank.value
            return if (left > LOW_TABLE_SUM) Reason.SAFE_DISCARD else Reason.LEAST_BAD_DISCARD
        }
        val involved = taken + move.card
        return when {
            taken.size == table.size -> Reason.SWEEP
            settebello in involved -> Reason.SETTEBELLO
            involved.any { it.suit == Suit.DENARI } -> Reason.DENARI
            involved.any { it.rank == Rank.SETTE || it.rank == Rank.SEI } -> Reason.PRIMIERA
            else -> Reason.CAPTURE
        }
    }

    /** Consiglio per [move] sul banco [table]: apertura con le carte nominate, poi la ragione. */
    public fun compose(move: ScopaMove, table: List<Card>): HintLine {
        val reason = reason(move, table)
        val taken = move.capture.toList()
        val (key, args) = when {
            taken.isEmpty() -> "hint_discard" to listOf(VoiceArg.CardArg(move.card))
            taken.size == 1 -> "hint_take_one" to listOf(VoiceArg.CardArg(move.card), VoiceArg.CardArg(taken.first()))
            else -> "hint_take_many" to listOf(VoiceArg.CardArg(move.card), VoiceArg.NumberArg(taken.size))
        }
        val variants = VoiceCatalog.hintLeads.getValue(key)
        val lead = variants[(move.card.hashCode() and Int.MAX_VALUE) % variants.size]
        val why = VoiceCatalog.hintReasons.getValue("hint_why_${reason.name.lowercase().removeSuffix("_discard")}").first()
        val pose = if (reason == Reason.SAFE_DISCARD || reason == Reason.LEAST_BAD_DISCARD) Pose.THINKING else Pose.PLEASED
        return HintLine(listOf(Phrase(lead.id, args), Phrase(why.id)), pose)
    }
}
