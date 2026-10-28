package it.maestro.app.feature.table.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import it.maestro.app.designsystem.motion.MotionSpec
import it.maestro.app.designsystem.motion.MotionToken
import it.maestro.app.domain.settings.GameSpeed
import it.maestro.game.model.Card

/** Spazio tra due carte della fila. */
private val CardGap = 8.dp

/** Proporzione altezza/larghezza di una carta. */
private const val CARD_RATIO = 92f / 62f

/**
 * Fila di carte scoperte che arrivano una alla volta dall'alto come alla distribuzione del tavolo (task F1R-27d):
 * stessa carta in movimento ([MovingCard]), tempi di [MotionSpec] alla velocità [speed], misure dallo spazio disponibile
 * (le carte si stringono, mai oltre [maxCardWidth]). Si rianima da capo quando cambia [key].
 */
@Composable
internal fun DealtCardRow(
    cards: List<Card>,
    key: Any,
    speed: GameSpeed,
    maxCardWidth: Dp,
    modifier: Modifier = Modifier,
    cardModifier: (Card) -> Modifier = { Modifier },
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val count = cards.size.coerceAtLeast(1)
        val cardWidth = minOf(maxCardWidth, (maxWidth - CardGap * (count - 1)) / count)
        val cardHeight = cardWidth * CARD_RATIO
        val startX = (maxWidth - (cardWidth * count + CardGap * (count - 1))) / 2
        val millis = MotionSpec.millis(MotionToken.DEAL_CARD, speed)
        var revealed by remember(key) { mutableIntStateOf(0) }
        LaunchedEffect(key) {
            cards.indices.forEach { index ->
                revealed = index + 1
                MotionSpec.pause(millis)
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(cardHeight)) {
            cards.take(revealed).forEachIndexed { index, card ->
                val x = startX + (cardWidth + CardGap) * index
                MovingCard(
                    card = card,
                    target = DpOffset(x, 0.dp),
                    origin = DpOffset(x, -cardHeight),
                    millis = millis.toInt(),
                    flip = false,
                    shrink = false,
                    width = cardWidth,
                    height = cardHeight,
                    highlighted = false,
                    modifier = cardModifier(card),
                )
            }
        }
    }
}
