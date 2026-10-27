package it.maestro.app.feature.table.presentation.director

import it.maestro.app.domain.match.Beat
import it.maestro.game.model.Seat
import it.maestro.game.scopa.GameEvent
import it.maestro.game.scopa.ScoreCategory

/**
 * Copione della regia (`docs/app/table-director.md` §3): traduce gli eventi di una [Beat] nella sequenza
 * fissa di [SceneStep]. È una funzione pura: le sequenze si leggono e si testano come dati.
 */
public object BeatScript {

    /** Carte a terra a inizio smazzata (spec §3.2). */
    private const val TABLE_CARDS: Int = 4

    /** Carte distribuite a ogni giro, tre a testa (spec §3.4). */
    private const val HAND_CARDS: Int = 6

    /** Passi per mettere in scena [beat], visti da [humanSeat]. */
    public fun steps(beat: Beat, humanSeat: Seat): List<SceneStep> = buildList {
        val card = beat.move.card
        if (beat.actor == humanSeat) {
            add(SceneStep.CardToPlayZone(card))
        } else {
            add(SceneStep.OpponentReveal(card))
            add(SceneStep.OpponentReadPause(card))
        }

        val captured = beat.events.filterIsInstance<GameEvent.Captured>().firstOrNull { it.seat == beat.actor }
        if (captured != null) {
            add(SceneStep.CaptureHighlight(beat.actor, card, captured.cards))
            add(SceneStep.CaptureFlight(beat.actor, listOf(card) + captured.cards))
        } else {
            add(SceneStep.DropToTable(beat.actor, card))
        }
        if (beat.events.any { it is GameEvent.Sweep && it.seat == beat.actor }) {
            add(SceneStep.Sweep(beat.actor, humanSeat))
        }

        val dealEnded = beat.events.filterIsInstance<GameEvent.DealEnded>().firstOrNull()
        val matchEnded = beat.events.any { it is GameEvent.MatchEnded }
        if (dealEnded != null) {
            val rows = ScoreCategory.entries.size
            repeat(rows) { add(SceneStep.DealSummaryRow(dealEnded.score, row = it, rows = rows)) }
            add(SceneStep.AwaitDealSummary(dealEnded.score))
            if (!matchEnded) {
                add(SceneStep.Shuffle)
                repeat(TABLE_CARDS) { add(SceneStep.DealCard(index = it, toSeat = null)) }
                addHandDeal(firstToReceive = beat.viewAfter.dealer.other())
            }
        } else if (beat.events.any { it is GameEvent.CardsDealt }) {
            addHandDeal(firstToReceive = beat.viewAfter.dealer.other())
        }
    }

    /** Distribuzione che [beat] provoca, o `null` se non ne provoca. */
    public fun dealPlan(beat: Beat): DealPlan? {
        val dealEnded = beat.events.any { it is GameEvent.DealEnded }
        val matchEnded = beat.events.any { it is GameEvent.MatchEnded }
        return when {
            dealEnded && !matchEnded -> DealPlan(tableCards = TABLE_CARDS, handCards = HAND_CARDS)
            beat.events.any { it is GameEvent.CardsDealt } -> DealPlan(tableCards = 0, handCards = HAND_CARDS)
            else -> null
        }
    }

    /** Apertura della partita: mescolata, 4 carte a terra e tre a testa, iniziando da chi non fa il mazziere. */
    public fun openingDeal(dealer: Seat): List<SceneStep> = buildList {
        add(SceneStep.Shuffle)
        repeat(TABLE_CARDS) { add(SceneStep.DealCard(index = it, toSeat = null)) }
        addHandDeal(firstToReceive = dealer.other())
    }

    /** Spec §3.2/§3.4: una carta alla volta, alternando, iniziando dall'avversario del mazziere. */
    private fun MutableList<SceneStep>.addHandDeal(firstToReceive: Seat) {
        repeat(HAND_CARDS) { index ->
            add(SceneStep.DealCard(index = index, toSeat = if (index % 2 == 0) firstToReceive else firstToReceive.other()))
        }
    }
}
