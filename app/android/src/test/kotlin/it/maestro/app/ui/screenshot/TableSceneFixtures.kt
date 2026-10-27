package it.maestro.app.ui.screenshot

import it.maestro.app.feature.table.presentation.GameUiState
import it.maestro.app.feature.table.presentation.director.SceneStep
import it.maestro.app.feature.table.presentation.director.TableScene
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaView

/** Carte di riempimento per le pile: solo il numero conta nel disegno. */
private fun filler(count: Int): List<Card> =
    Suit.entries.flatMap { suit -> Rank.entries.map { rank -> Card(rank, suit) } }.take(count)

/** Scena a riposo con i numeri di [uiState] (per gli screenshot dei dialog e del tavolo con stati inventati). */
fun sceneOf(uiState: GameUiState, step: SceneStep? = null, busy: Boolean = step != null): TableScene {
    val human = uiState.humanSeat
    val view = ScopaView(
        seat = human,
        config = ScopaConfig(),
        dealer = human.other(),
        toMove = if (uiState.isHumanTurn) human else human.other(),
        phase = uiState.phase,
        hand = uiState.humanHand,
        table = uiState.tableCards,
        capturedBy = mapOf(human to filler(uiState.humanCapturedCount), human.other() to filler(uiState.opponentCapturedCount)),
        playedCards = emptyList(),
        opponentHandSize = uiState.opponentCardCount,
        deckSize = uiState.deckCardCount,
        sweeps = mapOf(human to uiState.humanSweeps, human.other() to uiState.opponentSweeps),
        totals = mapOf(human to uiState.humanScore, human.other() to uiState.opponentScore),
    )
    val (actor, played) = when (step) {
        is SceneStep.CardToPlayZone -> human to step.card
        is SceneStep.OpponentReveal -> human.other() to step.card
        is SceneStep.OpponentReadPause -> human.other() to step.card
        is SceneStep.CaptureHighlight -> step.actor to step.card
        is SceneStep.CaptureFlight -> step.actor to step.cards.first()
        is SceneStep.DropToTable -> step.actor to step.card
        is SceneStep.Sweep -> step.actor to null
        else -> null to null
    }
    return TableScene(
        view = view,
        humanSeat = human,
        step = step,
        busy = busy,
        after = view.takeIf { step != null },
        actor = actor,
        played = played,
    )
}
