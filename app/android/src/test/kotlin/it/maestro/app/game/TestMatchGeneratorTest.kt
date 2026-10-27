package it.maestro.app.game

import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.play
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import it.maestro.app.feature.table.presentation.TestMatchGenerator

class TestMatchGeneratorTest {

    @Test
    fun generateEndgameMatch_producesPlayableStateWithOneCardEach() {
        val (state, log) = TestMatchGenerator.generateEndgameMatch(seed = 101L)

        assertNotNull(state)
        assertNotNull(log)
        assertEquals(DealPhase.PLAYING, state.phase)
        assertTrue("Il mazzo deve essere vuoto all'ultima mano", state.deck.isEmpty())
        assertEquals("P1 deve avere 1 carta", 1, state.hands.getValue(Seat.P1).size)
        assertEquals("P2 deve avere 1 carta", 1, state.hands.getValue(Seat.P2).size)

        // Giocando le ultime 2 mosse la partita si deve concludere
        val move1 = legalMoves(state).first()
        val res1 = play(state, move1) as PlayResult.Applied
        val move2 = legalMoves(res1.state).first()
        val res2 = play(res1.state, move2) as PlayResult.Applied

        assertEquals(DealPhase.MATCH_OVER, res2.state.phase)
        assertNotNull("Deve esserci un vincitore", res2.state.winner)
    }
}
