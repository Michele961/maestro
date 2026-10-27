package it.maestro.app.feature.table.ui

import it.maestro.app.feature.table.presentation.director.SceneStep
import it.maestro.app.feature.table.presentation.director.TableScene
import it.maestro.game.model.Card
import it.maestro.game.scopa.ScopaView

/**
 * Ciò che il tavolo mostra in un istante, ricavato da una [TableScene] (`docs/app/table-director.md` §2).
 * Funzione pura: la UI disegna questo modello e non decide cosa mostrare.
 *
 * @property table carte a terra.
 * @property played carta in gioco in questo passo, sempre visibile nella zona di gioco.
 * @property highlighted carte a terra che stanno per essere prese.
 * @property hand mano del giocatore umano.
 * @property opponentHand carte coperte dell'avversario.
 */
public data class StageModel(
    val table: List<Card>,
    val played: Card?,
    val highlighted: Set<Card>,
    val hand: List<Card>,
    val opponentHand: Int,
    val deck: Int,
    val humanCaptured: Int,
    val opponentCaptured: Int,
    val humanSweeps: Int,
    val opponentSweeps: Int,
    val humanScore: Int,
    val opponentScore: Int,
) {
    public companion object {
        /** Modello per [scene]: a riposo è la vista; durante una battuta dipende dal passo in corso. */
        public fun from(scene: TableScene): StageModel {
            val human = scene.humanSeat
            val before = scene.view
            val step = scene.step
            // Fino alla presa la mossa è ancora "in aria": il tavolo è quello di prima, la carta è in gioco.
            val inAir = when (step) {
                is SceneStep.CardToPlayZone, is SceneStep.OpponentReveal, is SceneStep.OpponentReadPause,
                is SceneStep.CaptureHighlight -> true
                else -> false
            }
            val playedCard = when (step) {
                is SceneStep.CardToPlayZone -> step.card
                is SceneStep.OpponentReveal -> step.card
                is SceneStep.OpponentReadPause -> step.card
                is SceneStep.CaptureHighlight -> step.card
                else -> null
            }
            val actor = when (step) {
                is SceneStep.CardToPlayZone -> human
                is SceneStep.OpponentReveal, is SceneStep.OpponentReadPause -> human.other()
                is SceneStep.CaptureHighlight -> step.actor
                else -> null
            }
            val shown: ScopaView = if (inAir || scene.after == null) before else scene.after
            val hand = if (inAir && actor == human) shown.hand - playedCard!! else shown.hand
            val opponentHand = shown.opponentHandSize - if (inAir && actor == human.other()) 1 else 0
            val highlighted = (step as? SceneStep.CaptureHighlight)?.captured.orEmpty()
            return StageModel(
                table = shown.table,
                played = playedCard,
                highlighted = highlighted,
                hand = hand,
                opponentHand = opponentHand,
                deck = shown.deckSize,
                humanCaptured = shown.capturedBy[human]?.size ?: 0,
                opponentCaptured = shown.capturedBy[human.other()]?.size ?: 0,
                humanSweeps = shown.sweeps[human] ?: 0,
                opponentSweeps = shown.sweeps[human.other()] ?: 0,
                humanScore = shown.totals[human] ?: 0,
                opponentScore = shown.totals[human.other()] ?: 0,
            )
        }
    }
}
