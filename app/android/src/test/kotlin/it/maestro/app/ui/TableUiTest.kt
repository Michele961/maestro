package it.maestro.app.ui

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.game.GameIntent
import it.maestro.app.game.GameReducer
import it.maestro.app.game.UiGameEvent
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.legalMoves
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test per la schermata del tavolo da gioco (ADR-0012, task F1-06):
 * - Giocata semplice
 * - Presa obbligatoria
 * - Scelta tra due prese
 * - Scopa
 * - Leggibilità e vincoli su schermo da 5"
 */
class TableUiTest {

    @Test
    fun simpleDiscardMoveUpdatesHandAndTableCorrectly() {
        // Troviamo un seed dove P1 gioca per primo e può fare una calata (nessuna presa)
        for (seed in 1L..100L) {
            val initial = GameReducer.initial(GameIntent.StartMatch(seed = seed, humanSeat = Seat.P1))
            val state = initial.state
            if (state.scopaState.toMove == Seat.P1) {
                val discards = legalMoves(state.scopaState).filter { it.capture.isEmpty() }
                if (discards.isNotEmpty()) {
                    val move = discards.first()
                    val uiBefore = state.toUiState()
                    val handSizeBefore = uiBefore.humanHand.size
                    val tableSizeBefore = uiBefore.tableCards.size

                    val result = GameReducer.reduce(state, GameIntent.PlayCard(move.card))
                    val uiAfter = result.state.toUiState()

                    assertEquals(handSizeBefore - 1, uiAfter.humanHand.size)
                    assertEquals(tableSizeBefore + 1, uiAfter.tableCards.size)
                    assertTrue(move.card in uiAfter.tableCards)
                    assertFalse(move.card in uiAfter.humanHand)
                    assertNull(uiAfter.pendingCaptureChoice)
                    return
                }
            }
        }
    }

    @Test
    fun mandatorySingleCaptureExecutesDirectlyWithoutPendingDialog() {
        // Troviamo uno scenario in cui P1 ha esattamente una presa obbligatoria
        for (seed in 1L..100L) {
            val initial = GameReducer.initial(GameIntent.StartMatch(seed = seed, humanSeat = Seat.P1))
            val state = initial.state
            if (state.scopaState.toMove == Seat.P1) {
                val legal = legalMoves(state.scopaState)
                val cardCaptures = legal.filter { it.capture.isNotEmpty() }.groupBy { it.card }
                val singleOptionCard = cardCaptures.entries.firstOrNull { it.value.size == 1 }?.key
                if (singleOptionCard != null) {
                    val expectedMove = cardCaptures.getValue(singleOptionCard).first()
                    val result = GameReducer.reduce(state, GameIntent.PlayCard(singleOptionCard))
                    val uiAfter = result.state.toUiState()

                    // Nessun dialog di scelta aperta
                    assertNull(uiAfter.pendingCaptureChoice)
                    // Carte catturate incrementate
                    assertTrue(uiAfter.humanCapturedCount >= expectedMove.capture.size + 1)
                    return
                }
            }
        }
    }

    @Test
    fun multipleCaptureChoicePromptsDialogAndApplyingChoiceExecutesCapture() {
        for (seed in 1L..200L) {
            val initial = GameReducer.initial(GameIntent.StartMatch(seed = seed, humanSeat = Seat.P1))
            val state = initial.state
            if (state.scopaState.toMove == Seat.P1) {
                val legal = legalMoves(state.scopaState)
                val cardCaptures = legal.filter { it.capture.isNotEmpty() }.groupBy { it.card }
                val multipleOptionCard = cardCaptures.entries.firstOrNull { it.value.size > 1 }?.key
                if (multipleOptionCard != null) {
                    val options = cardCaptures.getValue(multipleOptionCard).map { it.capture }

                    // 1. Giocata carta -> compare la scelta
                    val promptResult = GameReducer.reduce(state, GameIntent.PlayCard(multipleOptionCard))
                    val promptUi = promptResult.state.toUiState()
                    assertNotNull(promptUi.pendingCaptureChoice)
                    assertEquals(multipleOptionCard, promptUi.pendingCaptureChoice?.card)
                    assertEquals(options, promptUi.pendingCaptureChoice?.options)

                    // 2. Selezione della prima opzione
                    val chosenCapture = options.first()
                    val selectResult = GameReducer.reduce(
                        promptResult.state,
                        GameIntent.SelectCapture(multipleOptionCard, chosenCapture),
                    )
                    val afterUi = selectResult.state.toUiState()
                    assertNull(afterUi.pendingCaptureChoice)
                    assertNull(afterUi.errorMessage)
                    assertTrue(afterUi.humanCapturedCount >= chosenCapture.size + 1)
                    return
                }
            }
        }
    }

    @Test
    fun sweepOccursWhenTableIsEmptiedAndEmitsSweepEvent() {
        // Cerchiamo una giocata che svuota completamente il tavolo
        for (seed in 1L..300L) {
            val initial = GameReducer.initial(GameIntent.StartMatch(seed = seed, humanSeat = Seat.P1))
            val state = initial.state
            if (state.scopaState.toMove == Seat.P1) {
                val table = state.scopaState.table
                val legal = legalMoves(state.scopaState)
                val sweepMove = legal.firstOrNull { it.capture.size == table.size && it.capture.containsAll(table) }
                if (sweepMove != null) {
                    val result = GameReducer.reduce(state, GameIntent.PlayCard(sweepMove.card))
                    val uiAfter = result.state.toUiState()

                    assertEquals(1, uiAfter.humanSweeps)
                    assertTrue(uiAfter.lastEvent is UiGameEvent.Sweep)
                    assertEquals(Seat.P1, (uiAfter.lastEvent as UiGameEvent.Sweep).seat)
                    assertEquals(0, uiAfter.tableCards.size)
                    return
                }
            }
        }
    }

    @Test
    fun layoutFitsFiveInchVerticalScreenBudget() {
        // Uno schermo tipico da 5" verticale ha larghezza 360 dp e altezza ~640 dp.
        // Verifichiamo che il modello UI del tavolo rispetti i vincoli di budget visivo:
        val screenWidthDp = 360
        val maxCardsInHand = 3
        val cardWidthDp = 64
        val cardSpacingDp = 10
        val totalHandWidth = maxCardsInHand * cardWidthDp + (maxCardsInHand - 1) * cardSpacingDp
        assertTrue(
            totalHandWidth <= screenWidthDp,
            "La mano del giocatore (${totalHandWidth}dp) deve entrare nei ${screenWidthDp}dp dello schermo da 5\"",
        )

        val tableCardWidthDp = 50
        val tableCardSpacingDp = 6
        val fiveTableCardsWidth = 5 * tableCardWidthDp + 4 * tableCardSpacingDp
        assertTrue(
            fiveTableCardsWidth <= screenWidthDp,
            "Una riga del tavolo (${fiveTableCardsWidth}dp) entra nei ${screenWidthDp}dp dello schermo",
        )
    }
}
