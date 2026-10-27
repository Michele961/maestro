package it.maestro.app.feature.home.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import it.maestro.app.R

/**
 * Chiede (o fa cambiare) il nome del giocatore (`docs/app/features/home.md` §7). «Più tardi» non cambia il nome:
 * il giocatore può sempre toccarlo nella Home. Testi dalle risorse (ADR-0007).
 */
@Composable
internal fun PlayerNameDialog(
    currentName: String?,
    maxLength: Int,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(currentName.orEmpty()) }
    AlertDialog(
        modifier = Modifier.testTag("player_name_dialog"),
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_name_dialog_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(maxLength) },
                singleLine = true,
                label = { Text(stringResource(R.string.home_name_dialog_label)) },
                supportingText = { Text(stringResource(R.string.home_name_dialog_message)) },
                modifier = Modifier.testTag("player_name_field"),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("player_name_confirm"),
            ) { Text(stringResource(R.string.home_name_dialog_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("player_name_skip")) {
                Text(stringResource(R.string.home_name_dialog_skip))
            }
        },
    )
}
