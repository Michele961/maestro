package it.maestro.app.feature.table.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.maestro.app.R
import it.maestro.app.feature.pregame.ui.style
import it.maestro.app.domain.opponents.Opponent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import it.maestro.app.designsystem.components.NineSliceBox
import it.maestro.app.designsystem.components.ScorePlaque
import it.maestro.app.feature.table.presentation.GameUiState
import it.maestro.game.model.Seat

/** Barra in cima: chi si ha di fronte (l'avversario scelto, con il suo ritratto), punteggio della partita e pausa (`docs/app/features/table.md` §3). */
@Composable
internal fun OpponentBar(
    uiState: GameUiState,
    model: StageModel,
    zone: ZoneRect,
    sweepBy: Seat?,
    onPause: () -> Unit,
    opponent: Opponent = Opponent.MAESTRO,
    soundEnabled: Boolean = true,
    onToggleSound: () -> Unit = {},
) {
    val portrait = when {
        sweepBy == uiState.humanSeat -> opponent.losingPortrait()
        sweepBy != null -> opponent.winningPortrait()
        else -> opponent.style().portrait
    }
    Row(
        modifier = Modifier
            .offset(zone.x, zone.y)
            .size(zone.width, zone.height)
            .testTag("table_top_bar"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(portrait),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(zone.height.coerceAtMost(56.dp))
                .clip(CircleShape)
                .border(2.dp, GoldAccentColor, CircleShape)
                .testTag("opponent_portrait"),
        )
        NineSliceBox(
            skin = R.drawable.skin_name_plaque,
            slice = 0.35f,
            corner = 12.dp,
            modifier = Modifier.padding(start = 4.dp).weight(1f, fill = false),
        ) {
            Text(
                text = stringResource(opponent.style().name),
                style = MaterialTheme.typography.titleSmall,
                color = GoldAccentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
        Spacer(modifier = Modifier.widthIn(min = 4.dp).weight(1f, fill = false))
        ScorePlaque(
            p1Score = model.humanScore,
            p2Score = model.opponentScore,
            p1Label = stringResource(R.string.game_player_you),
            p2Label = stringResource(R.string.game_opponent_short),
        )
        val soundLabel = stringResource(R.string.game_sound_toggle)
        Text(
            text = stringResource(if (soundEnabled) R.string.game_sound_on else R.string.game_sound_off),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(start = 6.dp)
                .size(zone.height.coerceAtMost(40.dp))
                .clip(CircleShape)
                .clickable(onClick = onToggleSound)
                .semantics { contentDescription = soundLabel }
                .testTag("sound_toggle_button"),
            textAlign = TextAlign.Center,
        )
        Image(
            painter = painterResource(R.drawable.button_pause),
            contentDescription = stringResource(R.string.game_pause),
            modifier = Modifier
                .padding(start = 6.dp)
                .size(zone.height.coerceAtMost(46.dp))
                .clickable(onClick = onPause)
                .testTag("pause_button"),
        )
    }
}

/** Ritratto dell'avversario quando il giocatore fa Scopa: perde (`docs/app/features/table.md` §3). */
private fun Opponent.losingPortrait(): Int = when (this) {
    Opponent.GIGI -> R.drawable.opponent_1_lose
    Opponent.NONNA_ROSA -> R.drawable.opponent_2_lose
    Opponent.TONINO -> R.drawable.opponent_3_lose
    Opponent.DON_ALFONSO -> R.drawable.opponent_4_lose
    Opponent.MAESTRO -> R.drawable.opponent_5_lose
}

/** Ritratto dell'avversario quando fa Scopa lui. */
private fun Opponent.winningPortrait(): Int = when (this) {
    Opponent.GIGI -> R.drawable.opponent_1_win
    Opponent.NONNA_ROSA -> R.drawable.opponent_2_win
    Opponent.TONINO -> R.drawable.opponent_3_win
    Opponent.DON_ALFONSO -> R.drawable.opponent_4_win
    Opponent.MAESTRO -> R.drawable.opponent_5_win
}
