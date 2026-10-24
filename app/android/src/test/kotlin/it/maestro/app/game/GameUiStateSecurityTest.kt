package it.maestro.app.game

import it.maestro.game.model.Seat
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaState
import it.maestro.game.scopa.viewFor
import kotlin.reflect.full.memberProperties
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
        val uiStateProperties = GameUiState::class.memberProperties

        // 1. Nessun campo di tipo ScopaState
        for (prop in uiStateProperties) {
            assertNotEquals(
                ScopaState::class,
                prop.returnType.classifier,
                "GameUiState non deve mai esporre ScopaState! Campo violato: ${prop.name}",
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
        for (prop in uiStateProperties) {
            assertFalse(
                prop.name in disallowedFieldNames,
                "GameUiState contiene un campo proibito: ${prop.name}",
            )
        }

        // 3. I campi per avversario e mazzo devono essere strettamente di tipo Int (conteggi scalari)
        val opponentCardCountProp = uiStateProperties.first { it.name == "opponentCardCount" }
        val deckCardCountProp = uiStateProperties.first { it.name == "deckCardCount" }
        assertEquals(Int::class, opponentCardCountProp.returnType.classifier)
        assertEquals(Int::class, deckCardCountProp.returnType.classifier)
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
