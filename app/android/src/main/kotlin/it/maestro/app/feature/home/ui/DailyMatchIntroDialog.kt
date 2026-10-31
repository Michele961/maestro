package it.maestro.app.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import it.maestro.app.R
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.ParchmentPanel
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.domain.daily.DailyMatch
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.feature.pregame.ui.style

private val Ink = Color(0xFF2B1A10)

/**
 * Spiega la partita del giorno prima di iniziarla (task F1G-15): contro chi si gioca, in quale modalità, fino a
 * quanti punti e con quali regole. Evita di trovarsi al tavolo senza sapere cosa aspettarsi. Testi dalle risorse
 * (ADR-0007).
 */
@Composable
internal fun DailyMatchIntroDialog(opponent: Opponent, onPlay: () -> Unit, onDismiss: () -> Unit) {
    val style = opponent.style()
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        ParchmentPanel(
            modifier = Modifier.fillMaxWidth(0.92f).testTag("daily_match_intro_dialog"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 30.dp, vertical = 30.dp),
        ) {
            Text(
                text = stringResource(R.string.daily_match_intro_title),
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(painterResource(style.portrait), contentDescription = null, modifier = Modifier.size(64.dp))
                Text(
                    text = stringResource(R.string.daily_match_intro_opponent, stringResource(style.name), stringResource(style.title)),
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            Text(
                text = stringResource(R.string.daily_match_intro_rules, DailyMatch.TARGET_SCORE),
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
            Text(
                text = stringResource(R.string.daily_match_intro_same_hand),
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MaestroPillButton(
                    text = stringResource(R.string.daily_match_intro_cancel),
                    onClick = onDismiss,
                    variant = PillButtonVariant.CREAM,
                    height = 48.dp,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                    testTag = "daily_match_intro_cancel",
                )
                MaestroPillButton(
                    text = stringResource(R.string.daily_match_intro_play),
                    onClick = onPlay,
                    variant = PillButtonVariant.PRIMARY_GREEN,
                    height = 48.dp,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                    testTag = "daily_match_intro_play",
                )
            }
        }
    }
}
