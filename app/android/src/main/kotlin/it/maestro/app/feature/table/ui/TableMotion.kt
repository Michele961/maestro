package it.maestro.app.feature.table.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import it.maestro.app.designsystem.components.CardBackView
import it.maestro.app.designsystem.components.PlaceholderCardView
import it.maestro.game.model.Card
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Scala di una carta che vola verso una pila. */
private const val SHRUNK_SCALE = 0.55f

/** Rotazione a metà della quale la carta girata mostra la faccia. */
private const val FLIP_HALF = 90f

/**
 * Carta a terra che si muove tra posti (`docs/app/game-feel.md` §2): parte da [origin] se è appena comparsa,
 * va a [target] in [millis] (la durata del passo, da `MotionSpec`), e si gira se arriva dalla mano avversaria.
 * Con [millis] = 0 si sposta di colpo.
 */
@Composable
internal fun MovingCard(
    card: Card,
    target: DpOffset,
    origin: DpOffset?,
    millis: Int,
    flip: Boolean,
    shrink: Boolean,
    width: Dp,
    height: Dp,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    val position = remember { Animatable(origin ?: target, DpOffset.VectorConverter) }
    val scale = remember { Animatable(1f) }
    val turn = remember { Animatable(if (flip) 180f else 0f) }
    LaunchedEffect(target, shrink, millis) {
        launch { position.animateTo(target, tween(millis, easing = FastOutSlowInEasing)) }
        launch { scale.animateTo(if (shrink) SHRUNK_SCALE else 1f, tween(millis, easing = FastOutSlowInEasing)) }
    }
    LaunchedEffect(Unit) {
        if (flip) turn.animateTo(0f, tween(millis))
    }
    val density = LocalDensity.current
    Box(
        // Il bordo ([modifier]) segue la carta: va applicato dopo lo spostamento e la scala.
        modifier = Modifier
            .offset { IntOffset(with(density) { position.value.x.roundToPx() }, with(density) { position.value.y.roundToPx() }) }
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                rotationY = turn.value
                cameraDistance = 12f * density.density
            }
            .then(modifier),
    ) {
        if (turn.value > FLIP_HALF) {
            CardBackView(width = width, height = height)
        } else {
            PlaceholderCardView(
                card = card,
                isPlayable = false,
                showGoldFrame = highlighted,
                width = width,
                height = height,
            )
        }
    }
}

/** Carta coperta che vola dal [from] al [to] durante la distribuzione, in [millis]. */
@Composable
internal fun FlightCard(from: DpOffset, to: DpOffset, millis: Int, width: Dp, height: Dp) {
    val position = remember { Animatable(from, DpOffset.VectorConverter) }
    LaunchedEffect(Unit) { position.animateTo(to, tween(millis, easing = FastOutSlowInEasing)) }
    val density = LocalDensity.current
    Box(
        modifier = Modifier.offset {
            IntOffset(with(density) { position.value.x.roundToPx() }, with(density) { position.value.y.roundToPx() })
        },
    ) {
        CardBackView(width = width, height = height)
    }
}

/** Angolo in alto a sinistra di una carta [width] × [height] centrata in [zone]. */
internal fun centeredIn(zone: ZoneRect, width: Dp, height: Dp): DpOffset =
    DpOffset(zone.x + (zone.width - width) / 2, zone.y + (zone.height - height) / 2)

internal operator fun DpOffset.minus(other: DpOffset): DpOffset = DpOffset(x - other.x, y - other.y)

internal val NoOffset: DpOffset = DpOffset(0.dp, 0.dp)
