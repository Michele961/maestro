package it.maestro.app.feature.table.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Posizione di una carta nella zona di gioco, in dp dall'angolo in alto a sinistra della zona. */
public data class SlotPosition(val x: Dp, val y: Dp)

/**
 * Disposizione delle carte a terra nella zona di gioco (`docs/app/game-feel.md` §4): al massimo due file,
 * centrate. Se le carte non stanno affiancate si sovrappongono quanto serve, senza uscire dalla zona.
 */
public object PlayZoneLayout {

    private const val MAX_ROWS: Int = 2

    /** Posizioni per [count] carte larghe [cardWidth] e alte [cardHeight] in una zona [zoneWidth] × [zoneHeight]. */
    public fun positions(
        count: Int,
        zoneWidth: Dp,
        zoneHeight: Dp,
        cardWidth: Dp,
        cardHeight: Dp,
        gap: Dp = 8.dp,
    ): List<SlotPosition> {
        if (count <= 0) return emptyList()
        val perRowFit = (((zoneWidth + gap) / (cardWidth + gap)).toInt()).coerceAtLeast(1)
        val rows = if (count <= perRowFit) 1 else MAX_ROWS
        val perRow = if (rows == 1) count else (count + 1) / 2
        val step = if (perRow <= 1) 0.dp else minOf(cardWidth + gap, (zoneWidth - cardWidth) / (perRow - 1))
        val rowStep = if (rows == 1) 0.dp else minOf(cardHeight + gap, (zoneHeight - cardHeight).coerceAtLeast(0.dp))
        val top = (zoneHeight - (cardHeight + rowStep * (rows - 1))) / 2
        return List(count) { index ->
            val row = index / perRow
            val inRow = if (row == rows - 1) count - perRow * (rows - 1) else perRow
            val rowWidth = cardWidth + step * (inRow - 1)
            SlotPosition(
                x = (zoneWidth - rowWidth) / 2 + step * (index - row * perRow),
                y = top.coerceAtLeast(0.dp) + rowStep * row,
            )
        }
    }
}
