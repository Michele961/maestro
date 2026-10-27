package it.maestro.app.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Fondo a 9 sezioni da un'immagine "pelle" (pulsanti, pannelli, targhe generati in F1-13).
 * Angoli e bordi restano intatti, si stira solo il centro; [slice] è la frazione dell'immagine
 * occupata da ciascun angolo, [corner] la dimensione con cui l'angolo viene disegnato.
 * Il padding del contenuto va passato in [contentPadding], non in [modifier], altrimenti la pelle si rimpicciolisce.
 */
@Composable
public fun NineSliceBox(
    @DrawableRes skin: Int,
    slice: Float,
    corner: Dp,
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null,
    contentAlignment: Alignment = Alignment.Center,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit = {},
) {
    val image = ImageBitmap.imageResource(skin)
    Box(
        // La pelle va disegnata sull'area intera, prima del padding del contenuto.
        modifier = modifier.drawBehind { drawNineSlice(image, slice, corner.toPx(), colorFilter) }.padding(contentPadding),
        contentAlignment = contentAlignment,
        content = content,
    )
}

private fun DrawScope.drawNineSlice(
    image: ImageBitmap,
    slice: Float,
    cornerPx: Float,
    colorFilter: ColorFilter? = null,
) {
    val sw = image.width
    val sh = image.height
    // Angolo sorgente quadrato, preso sul lato corto dell'immagine.
    val sc = (minOf(sw, sh) * slice).roundToInt()
    val w = size.width.roundToInt()
    val h = size.height.roundToInt()
    val dc = minOf(cornerPx.roundToInt(), w / 2, h / 2)
    val srcX = intArrayOf(0, sc, sw - sc, sw)
    val srcY = intArrayOf(0, sc, sh - sc, sh)
    val dstX = intArrayOf(0, dc, w - dc, w)
    val dstY = intArrayOf(0, dc, h - dc, h)
    for (row in 0..2) {
        for (col in 0..2) {
            val srcW = srcX[col + 1] - srcX[col]
            val srcH = srcY[row + 1] - srcY[row]
            val dstW = dstX[col + 1] - dstX[col]
            val dstH = dstY[row + 1] - dstY[row]
            if (srcW <= 0 || srcH <= 0 || dstW <= 0 || dstH <= 0) continue
            drawImage(
                image = image,
                srcOffset = IntOffset(srcX[col], srcY[row]),
                srcSize = IntSize(srcW, srcH),
                dstOffset = IntOffset(dstX[col], dstY[row]),
                dstSize = IntSize(dstW, dstH),
                colorFilter = colorFilter,
            )
        }
    }
}
