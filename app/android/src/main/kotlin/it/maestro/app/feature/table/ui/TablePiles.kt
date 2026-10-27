package it.maestro.app.feature.table.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.designsystem.components.CardBackView
import it.maestro.app.designsystem.components.PlaceholderCardView
import it.maestro.game.model.Card

/** Altezza di una carta larga [width]. */
private fun cardHeight(width: Dp): Dp = width * TableGeometry.CARD_RATIO

/**
 * Pila di carte coperte, una copia sfalsata ogni ~5 carte (max 6), con il numero sotto.
 * Stessa pila per il mazzo e per le prese: lo spessore cresce con le carte (`docs/app/features/table.md` §3).
 */
@Composable
internal fun CardPile(count: Int, width: Dp, modifier: Modifier = Modifier, label: String = count.toString()) {
    val height = cardHeight(width)
    val layers = ((count + 4) / 5).coerceIn(1, MAX_LAYERS)
    Box(modifier = modifier.size(width + LAYER_SHIFT * MAX_LAYERS, height + LAYER_SHIFT * MAX_LAYERS)) {
        if (count > 0) {
            repeat(layers) { i ->
                CardBackView(
                    width = width,
                    height = height,
                    modifier = Modifier.offset(x = LAYER_SHIFT * i, y = LAYER_SHIFT * (layers - 1 - i)),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(width, height)
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(8.dp)),
            )
        }
        CountBadge(text = label, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** Pila delle prese di un giocatore, con le carte di scopa scoperte e di traverso sotto (F1-26). */
@Composable
internal fun CapturePile(
    count: Int,
    sweepCards: List<Card>,
    sweeps: Int,
    width: Dp,
    tag: String,
    modifier: Modifier = Modifier,
) {
    val sweepWidth = width * 0.8f
    Box(modifier = modifier.testTag(tag)) {
        sweepCards.forEachIndexed { i, card ->
            PlaceholderCardView(
                card = card,
                isPlayable = false,
                width = sweepWidth,
                height = cardHeight(sweepWidth),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = -(width * 0.2f) - 4.dp * i, y = 6.dp - 4.dp * i)
                    .rotate(90f)
                    .testTag("${tag}_sweep_$i"),
            )
        }
        val label = if (sweeps > sweepCards.size) "$count · ${stringResource(R.string.game_sweeps_short, sweeps)}" else "$count"
        CardPile(count = count, width = width, label = label)
    }
}

@Composable
private fun CountBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .offset(y = 8.dp)
            .background(Color(0xCC2A170C), RoundedCornerShape(10.dp))
            .border(1.dp, GoldAccentColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 1.dp),
    ) {
        Text(text = text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

private const val MAX_LAYERS = 6
private val LAYER_SHIFT = 2.dp
