package it.maestro.app.feature.table.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.key
import androidx.compose.ui.unit.DpOffset
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
import it.maestro.app.domain.coach.HintComposer
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
    stepKey: Any?,
    stepMillis: Int,
    showPointsRace: Boolean,
    onPlay: (Card) -> Unit,
    onRequestHint: () -> Unit,
    onDismissLine: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zones = geometry.zones
    // Le scope già in pila restano visibili durante la presa: la nuova compare solo a scena finita.
    val opponentSweepCards = restingValue(uiState.opponentSweepCards, atRest)
    val humanSweepCards = restingValue(uiState.humanSweepCards, atRest)
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
                sweepCards = opponentSweepCards,
                sweeps = model.opponentSweeps,
                width = pileCardWidth(zones.opponentPile),
                tag = "opponent_captures",
            )
        }
        Zone(zones.deck, Alignment.Center, Modifier.testTag("deck_pile")) {
            CardPile(count = model.deck, width = pileCardWidth(zones.deck))
        }
        Zone(zones.humanPile, Alignment.Center) {
            CapturePile(
                count = model.humanCaptured,
                sweepCards = humanSweepCards,
                sweeps = model.humanSweeps,
                width = pileCardWidth(zones.humanPile),
                tag = "human_captures",
            )
        }
        PlayArea(model, suggested = uiState.suggestedMove?.capture.orEmpty(), geometry = geometry, millis = stepMillis)
        Zone(zones.hand, Alignment.BottomCenter, Modifier.testTag("player_zone")) {
            PlayerHand(
                cards = model.hand,
                cardWidth = geometry.handCardWidth,
                canPlay = canPlay,
                suggestedCard = uiState.suggestedMove?.card,
                onPlay = onPlay,
            )
        }
        model.flights.forEach { flight ->
            key(stepKey) {
                val from = anchorOrigin(flight.from, geometry, geometry.opponentCardWidth, geometry.opponentCardHeight)
                val to = anchorOrigin(flight.to, geometry, geometry.opponentCardWidth, geometry.opponentCardHeight)
                FlightCard(from, to, stepMillis, geometry.opponentCardWidth, geometry.opponentCardHeight)
            }
        }
        RaceOrBubble(model, uiState, geometry, showRace = showPointsRace, onDismissLine = onDismissLine)
        MaestroCorner(uiState, geometry, canAskHint = canPlay && atRest, atRest = atRest, sweepBy = sweepBy, onRequestHint = onRequestHint)
    }
}

/** Carte a terra e carta in gioco, in due file al massimo (vedi [PlayZoneLayout]); si muovono al ritmo del passo. */
@Composable
private fun PlayArea(model: StageModel, suggested: Set<Card>, geometry: TableGeometry, millis: Int) {
    val zone = geometry.zones.playZone
    val positions = PlayZoneLayout.positions(
        count = model.layoutCount,
        zoneWidth = zone.width,
        zoneHeight = zone.height,
        cardWidth = geometry.cardWidth,
        cardHeight = geometry.cardHeight,
    )
    Box(modifier = Modifier.testTag("table_cards_zone")) {
        model.table.forEach { entry ->
            key(entry.card) {
                val target = entry.flyTo?.let { anchorOrigin(it, geometry, geometry.cardWidth, geometry.cardHeight) }
                    ?: positions[entry.slot ?: 0].let { DpOffset(zone.x + it.x, zone.y + it.y) }
                val origin = entry.origin?.let { anchorOrigin(it, geometry, geometry.cardWidth, geometry.cardHeight) }
                val highlight = entry.kind == EntryKind.HIGHLIGHTED
                val ring = when {
                    highlight -> Modifier.border(3.dp, GoldAccentColor, RoundedCornerShape(8.dp))
                    entry.kind == EntryKind.PLAYED -> Modifier.border(2.dp, Color.White, RoundedCornerShape(8.dp))
                    entry.card in suggested -> Modifier.border(3.dp, Color(0xFF42A5F5), RoundedCornerShape(8.dp))
                    else -> Modifier
                }
                MovingCard(
                    card = entry.card,
                    target = target,
                    origin = origin,
                    millis = millis,
                    flip = entry.origin == Anchor.OPPONENT_HAND,
                    shrink = entry.flyTo != null,
                    width = geometry.cardWidth,
                    height = geometry.cardHeight,
                    highlighted = highlight,
                    modifier = ring,
                )
            }
        }
    }
}

/** Angolo in alto a sinistra di una carta [width] × [height] posata sul punto [anchor] del tavolo. */
private fun anchorOrigin(anchor: Anchor, geometry: TableGeometry, width: Dp, height: Dp): DpOffset {
    val zones = geometry.zones
    val zone = when (anchor) {
        Anchor.DECK -> zones.deck
        Anchor.HAND -> zones.hand
        Anchor.OPPONENT_HAND -> zones.opponentHand
        Anchor.HUMAN_PILE -> zones.humanPile
        Anchor.OPPONENT_PILE -> zones.opponentPile
    }
    return centeredIn(zone, width, height)
}

/** Maestro a posto fisso: tocco per il consiglio, fumetto verso l'alto sulla zona di gioco. */
@Composable
private fun MaestroCorner(
    uiState: GameUiState,
    geometry: TableGeometry,
    canAskHint: Boolean,
    atRest: Boolean,
    sweepBy: Seat?,
    onRequestHint: () -> Unit,
) {
    if (!uiState.isSparring) return
    val zone = geometry.zones.maestro
    val hint = uiState.suggestedMove
    val showHint = hint != null || uiState.hintNudge != null
    val line = uiState.maestroLine
    val portrait = when {
        line != null && hint == null -> line.pose.portraitRes()
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
}

/**
 * Zona della corsa ai punti: mostra le quattro targhette, e cede il posto al fumetto del Maestro quando c'è un
 * consiglio. Il fumetto sta sempre qui, tra le carte a terra e la mano: non copre mai le carte (`game-feel.md` §5).
 */
@Composable
private fun RaceOrBubble(
    model: StageModel,
    uiState: GameUiState,
    geometry: TableGeometry,
    showRace: Boolean,
    onDismissLine: () -> Unit,
) {
    val zone = geometry.zones.pointsRace
    val hint = uiState.suggestedMove
    var dismissed by remember(hint, uiState.hintNudge) { mutableStateOf(false) }
    val nudge = uiState.hintNudge
    val showBubble = uiState.isSparring && (((hint != null || nudge != null) && !dismissed) || uiState.isHintLoading)
    val line = uiState.maestroLine?.takeIf { uiState.isSparring && !showBubble }
    // Il fumetto si appoggia al lato del Maestro: la coda tocca il ritratto, senza vuoto in mezzo.
    Zone(zone, Alignment.CenterEnd) {
        if (line != null) {
            // Battuta spontanea del Maestro: sparisce quando giochi o al tocco, senza timer.
            MaestroSpeechBubble(
                message = voiceText(line),
                tail = BubbleTail.END,
                fontSize = 12.sp,
                maxLines = 5,
                modifier = Modifier.clickable(onClick = onDismissLine).testTag("maestro_line_bubble"),
            )
        } else if (showBubble) {
            MaestroSpeechBubble(
                message = if (hint != null) {
                    hintText(remember(hint, uiState.tableCards) { HintComposer.compose(hint, uiState.tableCards) })
                } else if (nudge != null) {
                    hintText(nudge)
                } else {
                    stringResource(R.string.game_hint_loading)
                },
                tail = BubbleTail.END,
                fontSize = 12.sp,
                maxLines = 5,
                modifier = Modifier.clickable { dismissed = true }.testTag("game_hint_banner"),
            )
        } else if (showRace) {
            PointsRaceRow(race = model.race, showNumbers = uiState.isSparring, modifier = Modifier.fillMaxSize())
        }
    }
}

/** [value] aggiornato solo a scena ferma: durante una scena resta l'ultimo valore a riposo. */
@Composable
private fun <T> restingValue(value: T, atRest: Boolean): T {
    var last by remember { mutableStateOf(value) }
    if (atRest && last != value) last = value
    return last
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
