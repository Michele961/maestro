package it.maestro.app.domain.coach

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView

/** Perché la mossa migliore è meglio di quella giocata, dalla ragione più forte alla più debole (task F1R-24g). */
public enum class ReviewReason(public val key: String) {
    SWEEP("sweep"),
    SETTEBELLO_TAKEN("settebello_taken"),
    SETTEBELLO_PLAYED("settebello_played"),
    SETTEBELLO_DISCARDED("settebello_discarded"),
    ALLOWS_SWEEP("allows_sweep"),
    DENARI("denari"),
    PRIMIERA("primiera"),
    MORE_CARDS("more_cards"),
    DISCARD_VALUABLE("discard_valuable"),
    SAME_CAPTURE("same_capture"),
    IGNORED_CAPTURE("ignored_capture"),
    BEST_DISCARDS("best_discards"),

    /** Nessuna differenza visibile dalla sola mano: la spiegazione lo dice e rimanda al quadro della partita. */
    CONTEXT("context"),
}

/** Spiegazione di una mossa in codici e argomenti: titolo, corpo e regola da ricordare (testi in `strings.xml`). */
public data class ReviewNote(
    val reason: ReviewReason,
    val title: Phrase,
    val body: Phrase,
    /** Dato in più che aiuta a capire, quando c'è: cosa si guadagna prendendo, o cosa non si vede dalla sola mano. */
    val extra: Phrase?,
    val rule: Phrase,
) {
    /** Titolo, corpo ed eventuale dato in più, nell'ordine in cui si leggono. */
    public val explanation: List<Phrase> get() = listOfNotNull(body, extra)
}

/**
 * Spiega perché [ReviewNote.reason] rende la mossa migliore preferibile a quella giocata, confrontando i fatti delle due
 * mosse sulla sola vista del giocatore (`docs/app/features/review.md` §3, task F1R-24g). Ogni affermazione è verificabile
 * dalla vista; se la differenza non si vede dalla mano, lo dice ([ReviewReason.CONTEXT], [ReviewReason.BEST_DISCARDS]).
 */
public object ReviewCoach {

    private val settebello = Card(Rank.SETTE, Suit.DENARI)
    private const val LOW_TABLE_SUM = 10
    private const val MIN_CHOICES_TO_RETRY = 3
    private const val SCORE_GAP_MATTERS = 3
    private const val PLAYED_MATTERS = 10

    /** Nota per il confronto tra [played] e [best] nella posizione [view]. */
    public fun explain(played: ScopaMove, best: ScopaMove, view: ScopaView): ReviewNote {
        val (reason, args) = reasonAndArgs(played, best, view)
        return ReviewNote(
            reason = reason,
            title = Phrase("voice_review_title_${reason.key}"),
            body = Phrase(bodyId(reason, args), args),
            extra = extraFor(reason, best, view),
            rule = Phrase("voice_review_rule_${reason.key}"),
        )
    }

    /** `true` se vale la pena far riprovare la mano: con una o due scelte il giocatore vede già la soluzione. */
    public fun isWorthRetrying(choices: Int): Boolean = choices >= MIN_CHOICES_TO_RETRY

    private fun extraFor(reason: ReviewReason, best: ScopaMove, view: ScopaView): Phrase? = when (reason) {
        ReviewReason.IGNORED_CAPTURE -> when {
            best.capture.any { it.rank == Rank.SETTE || it.rank == Rank.SEI } -> Phrase("voice_review_extra_primiera")
            best.capture.any { it.suit == Suit.DENARI } || (best.card.suit == Suit.DENARI) -> Phrase("voice_review_extra_denari")
            else -> null
        }
        ReviewReason.CONTEXT, ReviewReason.BEST_DISCARDS -> hiddenContext(view)
        else -> null
    }

    /** Il dato che la sola mano non mostra e che può spiegare la scelta: ultima distribuzione, punteggio o carte uscite. */
    private fun hiddenContext(view: ScopaView): Phrase? {
        val mine = view.totals[view.seat] ?: 0
        val theirs = view.totals[view.seat.other()] ?: 0
        return when {
            view.deckSize == 0 -> Phrase("voice_review_extra_last_deal")
            kotlin.math.abs(mine - theirs) >= SCORE_GAP_MATTERS -> Phrase("voice_review_extra_score", listOf(VoiceArg.NumberArg(mine), VoiceArg.NumberArg(theirs)))
            view.playedCards.size >= PLAYED_MATTERS -> Phrase("voice_review_extra_played", listOf(VoiceArg.NumberArg(view.playedCards.size)))
            else -> null
        }
    }

    private fun bodyId(reason: ReviewReason, args: List<VoiceArg>): String =
        if (reason == ReviewReason.IGNORED_CAPTURE) {
            "voice_review_body_ignored_capture_${if (args[1] is VoiceArg.CardArg) "one" else "many"}"
        } else {
            "voice_review_body_${reason.key}"
        }

    private fun reasonAndArgs(played: ScopaMove, best: ScopaMove, view: ScopaView): Pair<ReviewReason, List<VoiceArg>> {
        val bestCard = VoiceArg.CardArg(best.card)
        val playedCard = VoiceArg.CardArg(played.card)
        val lastPlay = view.deckSize == 0 && view.hand.size == 1 && view.opponentHandSize == 0
        fun sweeps(m: ScopaMove) = !lastPlay && view.table.isNotEmpty() && m.capture.isNotEmpty() && m.capture.size == view.table.size
        fun takesSettebello(m: ScopaMove) = settebello in m.capture || (m.card == settebello && m.capture.isNotEmpty())
        fun left(m: ScopaMove) = if (m.capture.isEmpty()) view.table + m.card else view.table.filterNot { it in m.capture }
        fun leavesSweepChance(m: ScopaMove) = left(m).let { it.isNotEmpty() && it.sumOf { c -> c.rank.value } <= LOW_TABLE_SUM }
        fun denari(m: ScopaMove) = if (m.capture.isEmpty()) 0 else m.capture.count { it.suit == Suit.DENARI } + if (m.card.suit == Suit.DENARI) 1 else 0
        fun sevensSixes(m: ScopaMove) = m.capture.count { it.rank == Rank.SETTE || it.rank == Rank.SEI }
        fun gained(m: ScopaMove) = if (m.capture.isEmpty()) 0 else m.capture.size + 1

        return when {
            sweeps(best) && !sweeps(played) -> ReviewReason.SWEEP to listOf(bestCard)
            takesSettebello(best) && !takesSettebello(played) ->
                if (best.card == settebello) ReviewReason.SETTEBELLO_PLAYED to emptyList() else ReviewReason.SETTEBELLO_TAKEN to listOf(bestCard)
            played.card == settebello && played.capture.isEmpty() && best.card != settebello ->
                ReviewReason.SETTEBELLO_DISCARDED to emptyList()
            leavesSweepChance(played) && !leavesSweepChance(best) ->
                ReviewReason.ALLOWS_SWEEP to listOf(VoiceArg.NumberArg(left(played).sumOf { it.rank.value }))
            played.capture.isEmpty() && best.capture.isNotEmpty() -> ReviewReason.IGNORED_CAPTURE to listOf(
                bestCard,
                best.capture.singleOrNull()?.let { VoiceArg.CardArg(it) } ?: VoiceArg.NumberArg(best.capture.size),
            )
            denari(best) > denari(played) -> ReviewReason.DENARI to listOf(bestCard, VoiceArg.NumberArg(denari(best) - denari(played)))
            sevensSixes(best) > sevensSixes(played) -> ReviewReason.PRIMIERA to listOf(bestCard)
            gained(best) > gained(played) && played.capture.isNotEmpty() ->
                ReviewReason.MORE_CARDS to listOf(bestCard, VoiceArg.NumberArg(gained(best)), VoiceArg.NumberArg(gained(played)))
            played.capture.isEmpty() && best.capture.isEmpty() && worth(played.card) > worth(best.card) ->
                ReviewReason.DISCARD_VALUABLE to listOf(playedCard, bestCard)
            played.capture.isNotEmpty() && played.capture == best.capture && worth(played.card) > worth(best.card) ->
                ReviewReason.SAME_CAPTURE to listOf(bestCard, playedCard)
            played.capture.isNotEmpty() && best.capture.isEmpty() -> ReviewReason.BEST_DISCARDS to listOf(bestCard)
            else -> ReviewReason.CONTEXT to listOf(bestCard)
        }
    }

    /** Quanto una carta conta per i punti (da tenere o da non regalare): Settebello, poi 7, 6, Denari, Asso, il resto. */
    private fun worth(card: Card): Int = when {
        card == settebello -> 100
        card.rank == Rank.SETTE -> 90
        card.rank == Rank.SEI -> 80
        card.suit == Suit.DENARI -> 50
        card.rank == Rank.ASSO -> 40
        else -> 10
    }
}
