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
import it.maestro.app.designsystem.components.MaestroAvatarView
import it.maestro.app.designsystem.components.MaestroMood
import it.maestro.app.designsystem.components.NineSliceBox
import it.maestro.app.designsystem.components.ScorePlaque
import it.maestro.app.feature.table.presentation.GameUiState
import it.maestro.game.model.Seat

/** Barra in cima: chi si ha di fronte, punteggio della partita e pausa (`docs/app/features/table.md` §3). */
@Composable
internal fun OpponentBar(
    uiState: GameUiState,
    model: StageModel,
    zone: ZoneRect,
    sweepBy: Seat?,
    onPause: () -> Unit,
) {
    val mood = when {
        sweepBy == uiState.humanSeat -> MaestroMood.APPLAUDING
        sweepBy != null -> MaestroMood.DESPAIR
        uiState.isBotThinking || uiState.isHintLoading -> MaestroMood.THINKING
        uiState.trainingQuestionFeedback?.isCorrect == true -> MaestroMood.PLEASED
        uiState.trainingQuestionFeedback?.isCorrect == false -> MaestroMood.WARNING
        else -> MaestroMood.NEUTRAL
    }
    Row(
        modifier = Modifier
            .offset(zone.x, zone.y)
            .size(zone.width, zone.height)
            .testTag("table_top_bar"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MaestroAvatarView(mood = mood, size = zone.height.coerceAtMost(56.dp))
        NineSliceBox(
            skin = R.drawable.skin_name_plaque,
            slice = 0.35f,
            corner = 12.dp,
            modifier = Modifier.padding(start = 4.dp),
        ) {
            Text(
                text = stringResource(R.string.game_player_maestro),
                style = MaterialTheme.typography.titleSmall,
                color = GoldAccentColor,
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
