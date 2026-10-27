package it.maestro.app.feature.table.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.maestro.app.R
import it.maestro.app.designsystem.components.MaestroAlertDialog
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.ParchmentPanel
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.PlaceholderCardView
import it.maestro.app.feature.table.presentation.PendingCaptureChoice
import it.maestro.game.model.Card

@Composable
internal fun CaptureSelectionDialog(
    choice: PendingCaptureChoice,
    onConfirm: (Card, Set<Card>) -> Unit,
    onCancel: () -> Unit,
) {
    MaestroAlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = stringResource(id = R.string.game_dialog_capture_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("capture_selection_dialog"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for ((index, option) in choice.options.withIndex()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfirm(choice.card, option) }
                            .border(1.dp, DialogGold, RoundedCornerShape(6.dp))
                            .testTag("capture_option_$index"),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF264653),
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            for (takenCard in option) {
                                PlaceholderCardView(
                                    card = takenCard,
                                    isPlayable = false,
                                    width = 38.dp,
                                    height = 56.dp,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.testTag("cancel_capture_button"),
            ) {
                Text(text = stringResource(id = R.string.game_dialog_cancel))
            }
        },
    )
}


/** Pausa: riprendi o torna al menu (la partita in corso resta salvata, F1-08). */
@Composable
internal fun PauseDialog(onResume: () -> Unit, onNavigateHome: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onResume) {
        ParchmentPanel(modifier = Modifier.width(300.dp).testTag("pause_dialog")) {
            Text(
                text = stringResource(id = R.string.game_pause).uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF2B1A10),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 10.dp),
            )
            MaestroPillButton(
                text = stringResource(id = R.string.game_resume),
                onClick = onResume,
                variant = PillButtonVariant.PRIMARY_GREEN,
                modifier = Modifier.fillMaxWidth(),
                testTag = "pause_resume_button",
            )
            MaestroPillButton(
                text = stringResource(id = R.string.game_menu_home),
                onClick = onNavigateHome,
                variant = PillButtonVariant.WOOD,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp),
                testTag = "pause_home_button",
            )
        }
    }
}
