package it.maestro.app.feature.table.ui

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Task F1R-12a (`docs/app/game-feel.md` §4): le carte a terra stanno nella zona, in due file al massimo. */
class PlayZoneLayoutTest {

    private val zoneW = 300.dp
    private val zoneH = 260.dp
    private val cardW = 70.dp
    private val cardH = 120.dp

    private fun layout(count: Int) = PlayZoneLayout.positions(count, zoneW, zoneH, cardW, cardH)

    @Test
    fun noCardsNoPositions() {
        assertEquals(emptyList(), layout(0))
    }

    @Test
    fun fewCardsFitInOneCenteredRow() {
        val positions = layout(3)
        assertEquals(1, positions.map { it.y }.distinct().size)
        val left = positions.first().x
        val right = zoneW - (positions.last().x + cardW)
        assertTrue(kotlin.math.abs((left - right).value) < 0.5f, "Riga centrata")
    }

    @Test
    fun manyCardsUseAtMostTwoRowsAndStayInsideTheZone() {
        for (count in 1..14) {
            val positions = layout(count)
            assertEquals(count, positions.size)
            assertTrue(positions.map { it.y }.distinct().size <= 2, "Al massimo due file con $count carte")
            positions.forEach {
                assertTrue(it.x >= 0.dp && it.x + cardW <= zoneW + 0.01.dp, "Carta fuori in orizzontale con $count carte")
                assertTrue(it.y >= 0.dp && it.y + cardH <= zoneH + 0.01.dp, "Carta fuori in verticale con $count carte")
            }
        }
    }
}
