package it.maestro.app.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * Chiede (o fa cambiare) il nome del giocatore (`docs/app/features/home.md` §7). «Più tardi» non cambia il nome:
 * il giocatore può sempre toccarlo nella Home. Tutto centrato nel riquadro: titolo, campo a larghezza piena,
 * spiegazione e due pulsanti alla pari. Testi dalle risorse (ADR-0007).
 */
@Composable
internal fun PlayerNameDialog(
    currentName: String?,
    maxLength: Int,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(currentName.orEmpty()) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        ParchmentPanel(
            modifier = Modifier.fillMaxWidth(0.92f).testTag("player_name_dialog"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 34.dp, vertical = 34.dp),
        ) {
            Text(
                text = stringResource(R.string.home_name_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(maxLength) },
                singleLine = true,
                label = { Text(stringResource(R.string.home_name_dialog_label)) },
                modifier = Modifier.fillMaxWidth().testTag("player_name_field"),
            )
            Text(
                text = stringResource(R.string.home_name_dialog_message),
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 16.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MaestroPillButton(
                    text = stringResource(R.string.home_name_dialog_skip),
                    onClick = onDismiss,
                    variant = PillButtonVariant.CREAM,
                    height = 48.dp,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                    testTag = "player_name_skip",
                )
                MaestroPillButton(
                    text = stringResource(R.string.home_name_dialog_confirm),
                    onClick = { onConfirm(name) },
                    enabled = name.isNotBlank(),
                    variant = PillButtonVariant.PRIMARY_GREEN,
                    height = 48.dp,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                    testTag = "player_name_confirm",
                )
            }
        }
    }
}
