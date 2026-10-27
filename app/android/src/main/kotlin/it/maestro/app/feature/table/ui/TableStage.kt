package it.maestro.app.feature.table.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.designsystem.components.BubbleTail
import it.maestro.app.designsystem.components.CardBackView
import it.maestro.app.designsystem.components.FramedPortrait
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.MaestroSpeechBubble
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.PlaceholderCardView
import it.maestro.app.feature.table.presentation.GameUiState
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit

/** Disegna lo stadio del tavolo (`docs/app/features/table.md` §3): ogni elemento sta nella sua zona di [geometry]. */
@Composable
internal fun TableStage(
    model: StageModel,
    uiState: GameUiState,
    geometry: TableGeometry,
    canPlay: Boolean,
    atRest: Boolean,
    sweepBy: Seat?,
    onPlay: (Card) -> Unit,
    onRequestHint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zones = geometry.zones
    Box(modifier = modifier.testTag("felt_layout")) {
        // Avversario: mano coperta e le sue prese davanti a lui.
        Zone(zones.opponentHand, Alignment.Center, Modifier.testTag("opponent_zone")) {
            Row(horizontalArrangement = Arrangement.spacedBy(-(geometry.opponentCardWidth * 0.45f))) {
                repeat(model.opponentHand) {
                    CardBackView(width = geometry.opponentCardWidth, height = geometry.opponentCardHeight)
                }
            }
        }
        Zone(zones.opponentPile, Alignment.Center) {
            CapturePile(
                count = model.opponentCaptured,
                sweepCards = if (atRest) uiState.opponentSweepCards else emptyList(),
                sweeps = model.opponentSweeps,
                width = pileCardWidth(zones.opponentPile),
                tag = "opponent_captures",
            )
        }
        Zone(zones.deck, Alignment.Center, Modifier.testTag("deck_pile")) {
            CardPile(count = model.deck, width = pileCardWidth(zones.deck))
        }
        PlayArea(model = model, suggested = uiState.suggestedMove?.capture.orEmpty(), geometry = geometry)
        Zone(zones.humanPile, Alignment.Center) {
            CapturePile(
                count = model.humanCaptured,
                sweepCards = if (atRest) uiState.humanSweepCards else emptyList(),
                sweeps = model.humanSweeps,
                width = pileCardWidth(zones.humanPile),
                tag = "human_captures",
            )
        }
        Zone(zones.hand, Alignment.BottomCenter, Modifier.testTag("player_zone")) {
            PlayerHand(
                cards = model.hand,
                cardWidth = geometry.handCardWidth,
                canPlay = canPlay,
                suggestedCard = uiState.suggestedMove?.card,
                onPlay = onPlay,
            )
        }
        MaestroCorner(uiState, geometry, canAskHint = canPlay && atRest, sweepBy = sweepBy, onRequestHint = onRequestHint)
    }
}

/** Carte a terra e carta in gioco, in due file al massimo (vedi [PlayZoneLayout]). */
@Composable
private fun PlayArea(model: StageModel, suggested: Set<Card>, geometry: TableGeometry) {
    val zone = geometry.zones.playZone
    val cards = model.table + listOfNotNull(model.played)
    val positions = PlayZoneLayout.positions(
        count = cards.size,
        zoneWidth = zone.width,
        zoneHeight = zone.height,
        cardWidth = geometry.cardWidth,
        cardHeight = geometry.cardHeight,
    )
    Zone(zone, Alignment.TopStart, Modifier.testTag("table_cards_zone")) {
        cards.forEachIndexed { i, card ->
            val isPlayed = card == model.played
            val highlighted = card in model.highlighted
            val border = when {
                highlighted -> Modifier.graphicsLayer { scaleX = 1.06f; scaleY = 1.06f }.border(3.dp, GoldAccentColor, RoundedCornerShape(8.dp))
                isPlayed -> Modifier.border(2.dp, Color.White, RoundedCornerShape(8.dp))
                card in suggested -> Modifier.border(3.dp, Color(0xFF42A5F5), RoundedCornerShape(8.dp))
                else -> Modifier
            }
            PlaceholderCardView(
                card = card,
                isPlayable = false,
                showGoldFrame = highlighted || (card.suit == Suit.DENARI && card.rank == Rank.SETTE),
                width = geometry.cardWidth,
                height = geometry.cardHeight,
                modifier = Modifier.offset(positions[i].x, positions[i].y).then(border),
            )
        }
    }
}

/** Maestro a posto fisso: tocco per il consiglio, fumetto verso l'alto sulla zona di gioco. */
@Composable
private fun MaestroCorner(
    uiState: GameUiState,
    geometry: TableGeometry,
    canAskHint: Boolean,
    sweepBy: Seat?,
    onRequestHint: () -> Unit,
) {
    if (!uiState.isSparring) return
    val zone = geometry.zones.maestro
    val hint = uiState.suggestedMove
    var dismissed by remember(hint) { mutableStateOf(false) }
    val showHint = hint != null && !dismissed
    val portrait = when {
        sweepBy == uiState.humanSeat -> R.drawable.maestro_applauding
        sweepBy != null -> R.drawable.maestro_despair
        uiState.isBotThinking -> R.drawable.maestro_thinking
        showHint -> R.drawable.maestro_pleased
        else -> R.drawable.maestro_neutral
    }
    Zone(zone, Alignment.Center) {
        Box(
            modifier = Modifier
                .alpha(if (canAskHint || showHint) 1f else 0.7f)
                .clickable(enabled = canAskHint && uiState.isHumanTurn && !uiState.isHintLoading, onClick = onRequestHint)
                .testTag("game_hint_button"),
        ) {
            FramedPortrait(
                portrait = portrait,
                size = geometry.maestroSize,
                background = Color(0xFF7D3F19),
                contentDescription = stringResource(R.string.game_hint_button),
            )
        }
    }
    if (showHint || uiState.isHintLoading) {
        val play = geometry.zones.playZone
        Box(
            modifier = Modifier
                .offset(play.x, play.bottom - geometry.cardHeight)
                .size(play.width, geometry.cardHeight),
            contentAlignment = Alignment.BottomEnd,
        ) {
            MaestroSpeechBubble(
                message = if (hint != null) {
                    stringResource(determineLogicalHintRes(hint, uiState.tableCards))
                } else {
                    stringResource(R.string.game_hint_loading)
                },
                tail = BubbleTail.BOTTOM_END,
                fontSize = 12.sp,
                maxLines = 6,
                modifier = Modifier.clickable { dismissed = true }.testTag("game_hint_banner"),
            )
        }
    }
    if (sweepBy != null) {
        Zone(zone, Alignment.TopCenter) {
            MaestroPillButton(
                text = stringResource(R.string.game_event_sweep).uppercase(),
                onClick = {},
                variant = PillButtonVariant.HERO_RED,
                height = 28.dp,
                fontSize = 13.sp,
            )
        }
    }
}

/** Larghezza della carta di una pila che sta nella [zone] lasciando posto allo spessore e al numero. */
private fun pileCardWidth(zone: ZoneRect): Dp =
    minOf(zone.width - PILE_MARGIN, (zone.height - PILE_MARGIN - BADGE_HEIGHT) / TableGeometry.CARD_RATIO).coerceAtLeast(24.dp)

private val PILE_MARGIN = 14.dp
private val BADGE_HEIGHT = 12.dp

/** Contenitore posizionato sulla [rect] di una zona del tavolo. */
@Composable
private fun Zone(rect: ZoneRect, alignment: Alignment, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.offset(rect.x, rect.y).size(rect.width, rect.height), contentAlignment = alignment) { content() }
}
