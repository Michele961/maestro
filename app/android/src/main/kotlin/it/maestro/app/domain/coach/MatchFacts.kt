package it.maestro.app.domain.coach

import it.maestro.app.domain.match.Beat
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.GameEvent
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView
import it.maestro.game.training.SeatLead

/**
 * Qualcosa di vero nella partita in questo momento, in codici e argomenti (mai testo): è la materia prima da cui
 * il Maestro costruisce le battute (`docs/app/features/maestro-voice.md` §3.1, task F1R-24a). Ogni fatto si ricava
 * dalla sola vista del giocatore e dalle sue mosse legali: nessuna carta nascosta.
 */
public sealed interface Fact {

    /** Il giocatore può fare scopa con [move] (mai nell'ultima giocata della smazzata, spec 5.2). */
    public data class SweepAvailable(val move: ScopaMove) : Fact

    /** Il 7 di Denari è nella mano del giocatore. */
    public data object SettebelloInHand : Fact

    /** Il 7 di Denari è a terra; [capture] è la presa che lo prende, `null` se il giocatore non può prenderlo. */
    public data class SettebelloOnTable(val capture: ScopaMove?) : Fact

    /** Il Settebello è già stato preso da [owner] (mai [SeatLead.TIE]). */
    public data class SettebelloTaken(val owner: SeatLead) : Fact

    /** Il banco ha somma [sum] ≤ 10: chi gioca dopo può fare scopa con una sola carta (lezione "soglia dieci"). */
    public data class TableSumLow(val sum: Int) : Fact

    /** Nessuna mossa prende: ogni giocata è una calata. */
    public data object MustDiscard : Fact

    /** Esiste una calata che lascia il banco oltre la soglia dei dieci: [move] è la prima in ordine. */
    public data class SafeDiscardExists(val move: ScopaMove) : Fact

    /** Il giocatore ha [legalCount] mosse tra cui scegliere: scelta difficile. */
    public data class ManyChoices(val legalCount: Int) : Fact

    /** Il punto Carte è già sicuro (più di 20 carte). */
    public data class CardsSecured(val owner: SeatLead) : Fact

    /** Il punto Denari è già sicuro (più di 5 carte di Denari). */
    public data class DenariSecured(val owner: SeatLead) : Fact

    /** Ultima distribuzione della smazzata: il mazzo è finito. */
    public data object LastDeal : Fact

    /** È l'ultima giocata della smazzata. */
    public data object LastPlay : Fact

    /** Il giocatore è avanti ([ahead] > 0) o indietro ([ahead] < 0) di [ahead] punti. */
    public data class ScoreGap(val ahead: Int) : Fact

    /** [owner] è a un punto dall'obiettivo della partita. */
    public data class MatchPoint(val owner: SeatLead) : Fact

    // Fatti sull'ultima mossa (richiedono la battuta).

    /** [seat] ha fatto scopa con l'ultima mossa. */
    public data class SweepMade(val seat: Seat) : Fact

    /** [seat] ha preso il Settebello con l'ultima mossa. */
    public data class SettebelloCaptured(val seat: Seat) : Fact

    /** L'avversario ha calato senza prendere e ora il giocatore può fare scopa: un regalo. */
    public data object OpponentGift : Fact
}

/** Calcolo puro dei [Fact] da vista, mosse legali e ultima battuta. */
public object MatchFacts {

    private val settebello = Card(Rank.SETTE, Suit.DENARI)
    private const val LOW_TABLE_SUM = 10
    private const val MANY_CHOICES = 6
    private const val CARDS_SECURE = 20
    private const val DENARI_SECURE = 5

    /**
     * Fatti veri per [view] (giocatore umano, `view.seat`) quando può muovere [legal]; [lastBeat] è l'ultima mossa
     * messa a terra, se c'è. L'ordine è stabile: prima la situazione, poi la partita, poi l'ultima mossa.
     */
    public fun of(view: ScopaView, legal: List<ScopaMove>, lastBeat: Beat? = null): List<Fact> = buildList {
        val me = view.seat
        val lastPlay = view.deckSize == 0 && view.hand.size == 1 && view.opponentHandSize == 0
        val sweep = if (lastPlay) null else legal.firstOrNull { sweeps(view, it) }
        if (sweep != null) add(Fact.SweepAvailable(sweep))

        if (settebello in view.hand) add(Fact.SettebelloInHand)
        if (settebello in view.table) {
            add(Fact.SettebelloOnTable(legal.firstOrNull { settebello in it.capture }))
        }
        val owner = view.capturedBy.entries.firstOrNull { settebello in it.value }?.key
        if (owner != null) add(Fact.SettebelloTaken(if (owner == me) SeatLead.HUMAN else SeatLead.OPPONENT))

        val sum = view.table.sumOf { it.rank.value }
        if (view.table.isNotEmpty() && sum <= LOW_TABLE_SUM) add(Fact.TableSumLow(sum))
        if (legal.isNotEmpty() && legal.all { it.capture.isEmpty() }) {
            add(Fact.MustDiscard)
            legal.firstOrNull { view.table.sumOf { c -> c.rank.value } + it.card.rank.value > LOW_TABLE_SUM }
                ?.let { add(Fact.SafeDiscardExists(it)) }
        }
        if (legal.size >= MANY_CHOICES) add(Fact.ManyChoices(legal.size))

        val mine = view.capturedBy[me].orEmpty()
        val theirs = view.capturedBy[me.other()].orEmpty()
        secured(mine.size, theirs.size, CARDS_SECURE)?.let { add(Fact.CardsSecured(it)) }
        secured(mine.count { it.suit == Suit.DENARI }, theirs.count { it.suit == Suit.DENARI }, DENARI_SECURE)
            ?.let { add(Fact.DenariSecured(it)) }

        if (view.deckSize == 0 && view.hand.isNotEmpty()) add(Fact.LastDeal)
        if (lastPlay) add(Fact.LastPlay)

        val gap = (view.totals[me] ?: 0) - (view.totals[me.other()] ?: 0)
        if (gap != 0) add(Fact.ScoreGap(gap))
        val target = view.config.targetScore
        if ((view.totals[me] ?: 0) == target - 1) add(Fact.MatchPoint(SeatLead.HUMAN))
        if ((view.totals[me.other()] ?: 0) == target - 1) add(Fact.MatchPoint(SeatLead.OPPONENT))

        if (lastBeat != null) addAll(beatFacts(view, lastBeat, sweep != null))
    }

    private fun beatFacts(view: ScopaView, beat: Beat, sweepNow: Boolean): List<Fact> = buildList {
        beat.events.filterIsInstance<GameEvent.Sweep>().forEach { add(Fact.SweepMade(it.seat)) }
        beat.events.filterIsInstance<GameEvent.Captured>()
            .filter { settebello in it.cards || (settebello == beat.move.card && it.cards.isNotEmpty()) }
            .forEach { add(Fact.SettebelloCaptured(it.seat)) }
        if (beat.actor != view.seat && beat.move.capture.isEmpty() && sweepNow) add(Fact.OpponentGift)
    }

    private fun sweeps(view: ScopaView, move: ScopaMove): Boolean =
        view.table.isNotEmpty() && move.capture.isNotEmpty() && move.capture.size == view.table.size

    /** Chi ha già il punto sicuro (più di [threshold]), se qualcuno lo ha. */
    private fun secured(mine: Int, theirs: Int, threshold: Int): SeatLead? = when {
        mine > threshold -> SeatLead.HUMAN
        theirs > threshold -> SeatLead.OPPONENT
        else -> null
    }
}
