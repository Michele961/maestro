package it.maestro.app.feature.table.ui

import it.maestro.app.feature.table.presentation.director.SceneStep
import it.maestro.app.feature.table.presentation.director.TableScene
import it.maestro.game.model.Card
import it.maestro.game.model.Seat

/** Punti fissi del tavolo da cui le carte partono o a cui arrivano (`docs/app/game-feel.md` §4). */
public enum class Anchor { DECK, HAND, OPPONENT_HAND, HUMAN_PILE, OPPONENT_PILE }

/**
 * Una carta nella zona di gioco.
 *
 * @property slot posto nella disposizione di [StageModel.layoutCount] posti, o `null` se la carta sta volando
 * verso [flyTo].
 * @property origin da dove la carta arriva se compare in questo passo.
 */
public data class TableEntry(
    val card: Card,
    val slot: Int?,
    val kind: EntryKind = EntryKind.NORMAL,
    val origin: Anchor? = null,
    val flyTo: Anchor? = null,
)

public enum class EntryKind { NORMAL, PLAYED, HIGHLIGHTED }

/** Carta coperta che viaggia tra due punti del tavolo (distribuzione); [card] è `null` per l'avversario. */
public data class Flight(val card: Card?, val from: Anchor, val to: Anchor)

/**
 * Ciò che il tavolo mostra in un istante, ricavato da una [TableScene] (`docs/app/table-director.md` §2).
 * Funzione pura: la UI disegna questo modello e non decide cosa mostrare.
 *
 * @property table carte nella zona di gioco, con posto e provenienza.
 * @property layoutCount numero di posti con cui disporre le carte a terra.
 * @property hand mano del giocatore umano.
 * @property opponentHand carte coperte dell'avversario.
 * @property flights carte in viaggio dal mazzo alle mani.
 */
public data class StageModel(
    val table: List<TableEntry>,
    val layoutCount: Int,
    val hand: List<Card>,
    val opponentHand: Int,
    val deck: Int,
    val humanCaptured: Int,
    val opponentCaptured: Int,
    val humanSweeps: Int,
    val opponentSweeps: Int,
    val humanScore: Int,
    val opponentScore: Int,
    val flights: List<Flight> = emptyList(),
) {
    public companion object {
        /** Modello per [scene]: a riposo è la vista; durante una battuta dipende dal passo in corso. */
        public fun from(scene: TableScene): StageModel {
            val step = scene.step
            val after = scene.after
            if (step == null || after == null) return rest(scene)

            val human = scene.humanSeat
            val before = scene.view
            val plan = scene.deal
            val newDeal = (plan?.tableCards ?: 0) > 0
            val dealTotal = (plan?.tableCards ?: 0) + (plan?.handCards ?: 0)
            // Fino alla fine del volo la mossa non è risolta: pile e punteggi sono quelli di prima.
            val resolved = when (step) {
                is SceneStep.CardToPlayZone, is SceneStep.OpponentReveal, is SceneStep.OpponentReadPause,
                is SceneStep.CaptureHighlight, is SceneStep.CaptureFlight -> false
                else -> true
            }
            val counts = if (resolved) after else before

            // Le mani sono quelle di prima meno la carta giocata; le carte nuove arrivano con la distribuzione.
            val played = scene.played
            var hand = when {
                scene.actor == null -> emptyList()
                scene.actor == human && played != null -> before.hand - played
                else -> before.hand
            }
            var opponentHand = if (scene.actor == null) 0 else before.opponentHandSize - if (scene.actor != human) 1 else 0
            var deck = before.deckSize
            var flights: List<Flight> = emptyList()
            val table: List<TableEntry>
            val layoutCount: Int

            when (step) {
                is SceneStep.CardToPlayZone -> {
                    table = withPlayed(before.table, step.card, EntryKind.PLAYED, Anchor.HAND)
                    layoutCount = table.size
                }
                is SceneStep.OpponentReveal -> {
                    table = withPlayed(before.table, step.card, EntryKind.PLAYED, Anchor.OPPONENT_HAND)
                    layoutCount = table.size
                }
                is SceneStep.OpponentReadPause -> {
                    table = withPlayed(before.table, step.card, EntryKind.PLAYED, null)
                    layoutCount = table.size
                }
                is SceneStep.CaptureHighlight -> {
                    table = withPlayed(before.table, step.card, EntryKind.PLAYED, null)
                        .map { if (it.card in step.captured) it.copy(kind = EntryKind.HIGHLIGHTED) else it }
                    layoutCount = table.size
                }
                is SceneStep.CaptureFlight -> {
                    // Le carte prese volano alla pila di chi prende; le altre prendono il posto che avranno dopo.
                    val pile = if (step.actor == human) Anchor.HUMAN_PILE else Anchor.OPPONENT_PILE
                    val staying = before.table.filter { it !in step.cards }
                    table = (before.table + step.cards.first()).distinct().map { card ->
                        val slot = staying.indexOf(card).takeIf { it >= 0 }
                        TableEntry(card, slot, flyTo = if (slot == null) pile else null)
                    }
                    layoutCount = staying.size
                }
                is SceneStep.DropToTable -> {
                    table = (before.table + step.card).mapIndexed { i, c -> TableEntry(c, slot = i) }
                    layoutCount = table.size
                }
                is SceneStep.Sweep, is SceneStep.DealSummaryRow, is SceneStep.AwaitDealSummary -> {
                    val cards = if (newDeal) emptyList() else after.table
                    table = cards.mapIndexed { i, c -> TableEntry(c, slot = i) }
                    layoutCount = cards.size
                }
                is SceneStep.Shuffle -> {
                    table = emptyList()
                    layoutCount = after.table.size
                    deck = after.deckSize + dealTotal
                }
                is SceneStep.DealCard -> {
                    val seat = step.toSeat
                    if (seat == null) {
                        table = after.table.take(step.index + 1).mapIndexed { i, c ->
                            TableEntry(c, slot = i, origin = if (i == step.index) Anchor.DECK else null)
                        }
                        deck = after.deckSize + dealTotal - (step.index + 1)
                    } else {
                        table = after.table.mapIndexed { i, c -> TableEntry(c, slot = i) }
                        // Si alterna: dalla posizione nella sequenza si risale a chi aveva ricevuto cosa prima.
                        val first = if (step.index % 2 == 0) seat else seat.other()
                        val receivers = (0 until step.index).map { if (it % 2 == 0) first else first.other() }
                        val humanDealt = receivers.count { it == human }
                        hand = after.hand.take(humanDealt)
                        opponentHand = receivers.size - humanDealt
                        deck = after.deckSize + (plan?.handCards ?: 0) - (step.index + 1)
                        val card = if (seat == human) after.hand.getOrNull(humanDealt) else null
                        flights = listOf(Flight(card, Anchor.DECK, if (seat == human) Anchor.HAND else Anchor.OPPONENT_HAND))
                    }
                    layoutCount = after.table.size
                }
            }
            return StageModel(
                table = table,
                layoutCount = layoutCount,
                hand = hand,
                opponentHand = opponentHand,
                deck = deck,
                humanCaptured = counts.capturedBy[human]?.size ?: 0,
                opponentCaptured = counts.capturedBy[human.other()]?.size ?: 0,
                humanSweeps = counts.sweeps[human] ?: 0,
                opponentSweeps = counts.sweeps[human.other()] ?: 0,
                humanScore = counts.totals[human] ?: 0,
                opponentScore = counts.totals[human.other()] ?: 0,
                flights = flights,
            )
        }

        private fun withPlayed(table: List<Card>, card: Card, kind: EntryKind, origin: Anchor?): List<TableEntry> =
            table.mapIndexed { i, c -> TableEntry(c, slot = i) } + TableEntry(card, slot = table.size, kind = kind, origin = origin)

        private fun rest(scene: TableScene): StageModel {
            val view = scene.view
            val human = scene.humanSeat
            return StageModel(
                table = view.table.mapIndexed { i, c -> TableEntry(c, slot = i) },
                layoutCount = view.table.size,
                hand = view.hand,
                opponentHand = view.opponentHandSize,
                deck = view.deckSize,
                humanCaptured = view.capturedBy[human]?.size ?: 0,
                opponentCaptured = view.capturedBy[human.other()]?.size ?: 0,
                humanSweeps = view.sweeps[human] ?: 0,
                opponentSweeps = view.sweeps[human.other()] ?: 0,
                humanScore = view.totals[human] ?: 0,
                opponentScore = view.totals[human.other()] ?: 0,
            )
        }
    }
}
