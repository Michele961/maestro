package it.maestro.app.game

import it.maestro.game.model.Seat
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaState
import it.maestro.game.scopa.viewFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

/**
 * Verifica formale del criterio di accettazione cardine di F1-05 e ADR-0012:
 * "La UI non riceve mai ScopaState né carte nascoste: solo dati derivati da viewFor() del giocatore umano".
 */
class GameUiStateSecurityTest {

    @Test
    fun gameUiStateDoesNotExposeScopaStateOrHiddenCards() {
        val declaredFields = GameUiState::class.java.declaredFields

        // 1. Nessun campo di tipo ScopaState
        for (field in declaredFields) {
            assertNotEquals(
                ScopaState::class.java,
                field.type,
                "GameUiState non deve mai esporre ScopaState! Campo violato: ${field.name}",
            )
        }

        // 2. Nessun campo che contenga la mano dell'avversario o le carte del mazzo come collezione di Card
        val disallowedFieldNames = setOf(
            "opponentHand",
            "deck",
            "deckCards",
            "hiddenCards",
            "scopaState",
            "authoritativeState",
        )
        for (field in declaredFields) {
            assertFalse(
                field.name in disallowedFieldNames,
                "GameUiState contiene un campo proibito: ${field.name}",
            )
        }

        // 3. I campi per avversario e mazzo devono essere strettamente di tipo primitivo int (conteggi scalari)
        val opponentCardCountField = declaredFields.first { it.name == "opponentCardCount" }
        val deckCardCountField = declaredFields.first { it.name == "deckCardCount" }
        assertEquals(Int::class.javaPrimitiveType, opponentCardCountField.type)
        assertEquals(Int::class.javaPrimitiveType, deckCardCountField.type)
    }

    @Test
    fun uiStateValuesMatchExactlyHumanViewForProjection() {
        val initial = GameReducer.initial(
            GameIntent.StartMatch(config = ScopaConfig(), seed = 42L, humanSeat = Seat.P1),
        )
        val uiState = initial.state.toUiState()
        val expectedView = viewFor(initial.state.scopaState, Seat.P1)

        assertEquals(expectedView.phase, uiState.phase)
        assertEquals(Seat.P1, uiState.humanSeat)
        assertEquals(expectedView.hand, uiState.humanHand)
        assertEquals(expectedView.table, uiState.tableCards)
        assertEquals(expectedView.opponentHandSize, uiState.opponentCardCount)
        assertEquals(expectedView.deckSize, uiState.deckCardCount)
        assertEquals(expectedView.capturedBy[Seat.P1]?.size ?: 0, uiState.humanCapturedCount)
        assertEquals(expectedView.capturedBy[Seat.P2]?.size ?: 0, uiState.opponentCapturedCount)
        assertEquals(expectedView.sweeps[Seat.P1] ?: 0, uiState.humanSweeps)
        assertEquals(expectedView.sweeps[Seat.P2] ?: 0, uiState.opponentSweeps)
        assertEquals(expectedView.totals[Seat.P1] ?: 0, uiState.humanScore)
        assertEquals(expectedView.totals[Seat.P2] ?: 0, uiState.opponentScore)
    }
}
