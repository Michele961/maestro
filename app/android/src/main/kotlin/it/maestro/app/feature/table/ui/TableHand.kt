package it.maestro.app.feature.table.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import it.maestro.app.R
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.PlaceholderCardView
import it.maestro.game.model.Card
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/** Inclinazione massima di una carta della mano, in gradi: ventaglio leggero (`docs/app/features/table.md` §6). */
private const val FAN_STEP_DEGREES = 3f

/**
 * Mano del giocatore: ventaglio leggero con le carte sempre interamente visibili, larghe [cardWidth].
 * Si gioca trascinando verso il tavolo o toccando due volte (tocco = seleziona, conferma o secondo tocco = gioca).
 */
@Composable
internal fun PlayerHand(
    cards: List<Card>,
    cardWidth: Dp,
    canPlay: Boolean,
    suggestedCard: Card?,
    onPlay: (Card) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardHeight = cardWidth * TableGeometry.CARD_RATIO
    var selected by remember(cards) { mutableStateOf<Card?>(null) }
    var dragging by remember { mutableStateOf<Card?>(null) }
    val dragOffsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val playThreshold = with(LocalDensity.current) { -(cardHeight * 0.45f).toPx() }
    val activeSelected = selected?.takeIf { it in cards }
    // Le carte non si sovrappongono: se non stanno affiancate si accostano quanto serve.
    val overlap = -(cardWidth * 0.08f)

    Column(modifier = modifier.testTag("player_zone_fan"), horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(visible = activeSelected != null && canPlay, enter = fadeIn(), exit = fadeOut()) {
            if (activeSelected != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    // La carta scelta si alza di 0.18 della sua altezza: i comandi stanno sopra, mai coperti da lei.
                    modifier = Modifier.padding(bottom = cardHeight * SELECTED_LIFT + 4.dp),
                ) {
                    MaestroPillButton(
                        text = stringResource(R.string.game_card_play_confirm),
                        onClick = {
                            selected = null
                            onPlay(activeSelected)
                        },
                        variant = PillButtonVariant.PRIMARY_GREEN,
                        height = 36.dp,
                        fontSize = 14.sp,
                        testTag = "confirm_play_button",
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp),
                    )
                    Text(
                        text = stringResource(R.string.game_card_drag_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldAccentColor,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        Row(
            modifier = Modifier.testTag("human_hand_row"),
            horizontalArrangement = Arrangement.spacedBy(overlap),
            verticalAlignment = Alignment.Bottom,
        ) {
            cards.forEachIndexed { i, card ->
                val mid = (cards.size - 1) / 2f
                val isSelected = activeSelected == card
                val isDragging = dragging == card
                val overThreshold = isDragging && dragOffsetY.value < playThreshold
                val glow = if (overThreshold || isSelected) GoldAccentColor else Color(0xFFFFD54F)
                Box(
                    modifier = Modifier
                        .zIndex(if (isDragging) 10f else if (isSelected) 5f else i.toFloat())
                        .offset {
                            val lift = when {
                                isDragging -> dragOffsetY.value
                                isSelected -> -(cardHeight * SELECTED_LIFT).toPx()
                                else -> 0f
                            }
                            IntOffset(0, (abs(i - mid) * 3.dp.toPx() + lift).roundToInt())
                        }
                        .rotate(if (isDragging || isSelected) 0f else (i - mid) * FAN_STEP_DEGREES)
                        .graphicsLayer {
                            val scale = if (isDragging) 1.06f else if (isSelected) 1.03f else 1f
                            scaleX = scale
                            scaleY = scale
                        }
                        .draggable(
                            state = rememberDraggableState { delta -> scope.launch { dragOffsetY.snapTo(dragOffsetY.value + delta) } },
                            orientation = Orientation.Vertical,
                            enabled = canPlay,
                            onDragStarted = {
                                dragging = card
                                selected = card
                            },
                            onDragStopped = {
                                val shouldPlay = dragOffsetY.value < playThreshold
                                scope.launch { dragOffsetY.animateTo(0f, tween(200)) }
                                dragging = null
                                if (shouldPlay) {
                                    selected = null
                                    onPlay(card)
                                }
                            },
                        )
                        .then(
                            if (canPlay || suggestedCard == card || isSelected) {
                                Modifier.shadow(if (isSelected || overThreshold) 14.dp else 6.dp, RoundedCornerShape(10.dp), ambientColor = glow, spotColor = glow)
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    PlaceholderCardView(
                        card = card,
                        isPlayable = canPlay,
                        width = cardWidth,
                        height = cardHeight,
                        modifier = if (isSelected || overThreshold) Modifier.border(3.dp, GoldAccentColor, RoundedCornerShape(6.dp)) else Modifier,
                        onClick = {
                            if (canPlay) {
                                if (isSelected) {
                                    selected = null
                                    onPlay(card)
                                } else {
                                    selected = card
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

/** Quanto si alza la carta selezionata, in frazione della sua altezza. */
private const val SELECTED_LIFT = 0.18f
