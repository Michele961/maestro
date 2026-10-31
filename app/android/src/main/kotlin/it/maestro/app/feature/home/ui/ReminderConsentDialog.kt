package it.maestro.app.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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

private val Ink = Color(0xFF2B1A10)

/**
 * Spiega il promemoria e chiede il consenso (task F1G-13): compare una volta sola, dopo la prima vittoria. Due scelte
 * alla pari; chiudere il riquadro equivale a «No, grazie». Testi dalle risorse (ADR-0007).
 */
@Composable
internal fun ReminderConsentDialog(onAccept: () -> Unit, onDecline: () -> Unit) {
    Dialog(onDismissRequest = onDecline, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        ParchmentPanel(
            modifier = Modifier.fillMaxWidth(0.92f).testTag("reminder_consent_dialog"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 30.dp, vertical = 30.dp),
        ) {
            Text(
                text = stringResource(R.string.reminder_prompt_title),
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            )
            Text(
                text = stringResource(R.string.reminder_prompt_message),
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MaestroPillButton(
                    text = stringResource(R.string.reminder_prompt_decline),
                    onClick = onDecline,
                    variant = PillButtonVariant.CREAM,
                    height = 48.dp,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                    testTag = "reminder_decline",
                )
                MaestroPillButton(
                    text = stringResource(R.string.reminder_prompt_accept),
                    onClick = onAccept,
                    variant = PillButtonVariant.PRIMARY_GREEN,
                    height = 48.dp,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                    testTag = "reminder_accept",
                )
            }
        }
    }
}
