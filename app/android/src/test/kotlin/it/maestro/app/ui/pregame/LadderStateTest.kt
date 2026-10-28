package it.maestro.app.ui.pregame

import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.opponents.OpponentProgress
import it.maestro.app.feature.pregame.presentation.LadderState
import it.maestro.app.feature.pregame.presentation.LadderStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LadderStateTest {

    @Test
    fun atTheStartOnlyTheFirstOpponentIsNextAndTheOthersNameWhoUnlocksThem() {
        val state = LadderState.of(OpponentProgress())

        assertEquals(
            listOf(LadderStatus.NEXT, LadderStatus.LOCKED, LadderStatus.LOCKED, LadderStatus.LOCKED, LadderStatus.LOCKED),
            state.entries.map { it.status },
        )
        assertEquals(Opponent.GIGI, state.next)
        assertEquals(Opponent.GIGI, state.entries[1].unlockedBy)
        assertEquals(Opponent.DON_ALFONSO, state.entries[4].unlockedBy)
        assertNull(state.entries[0].unlockedBy)
    }

    @Test
    fun beatenOpponentsStayChoosableAndTheNextMovesForward() {
        val state = LadderState.of(OpponentProgress(setOf(Opponent.GIGI, Opponent.NONNA_ROSA)))

        assertEquals(
            listOf(LadderStatus.BEATEN, LadderStatus.BEATEN, LadderStatus.NEXT, LadderStatus.LOCKED, LadderStatus.LOCKED),
            state.entries.map { it.status },
        )
        assertEquals(Opponent.TONINO, state.next)
    }

    @Test
    fun aCompleteLadderHasNoNextOpponent() {
        assertNull(LadderState.of(OpponentProgress(Opponent.entries.toSet())).next)
    }
}
