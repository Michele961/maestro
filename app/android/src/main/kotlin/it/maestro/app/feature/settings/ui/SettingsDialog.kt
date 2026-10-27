package it.maestro.app.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import it.maestro.app.shell.DeveloperTools
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.designsystem.components.MaestroAlertDialog
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.R

/**
 * Dialog delle impostazioni applicative (task F1-14).
 * Permette la disattivazione degli effetti sonori e il trigger del crash di test per Crashlytics.
 * Tutti i testi provengono rigorosamente dalle risorse stringhe (ADR-0007).
 */
@Composable
public fun SettingsDialog(
    soundEnabled: Boolean,
    onSoundToggled: (Boolean) -> Unit,
    onTriggerTestCrash: () -> Unit,
    onDismiss: () -> Unit,
    onStartTestEndgameMatch: () -> Unit = {},
    showDeveloperTools: Boolean = DeveloperTools.isEnabled(LocalContext.current),
) {
    MaestroAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("settings_dialog"),
        title = {
            Text(
                text = stringResource(id = R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Sezione Effetti Sonori
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sound_toggle_row"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.settings_sound_effects),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(id = R.string.settings_sound_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6D5A48),
                        )
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onSoundToggled,
                        modifier = Modifier.testTag("sound_enabled_switch"),
                    )
                }

                // Strumenti di sviluppo solo nelle build debuggabili (task F1R-05).
                if (showDeveloperTools) {
                HorizontalDivider(color = Color(0xFF8C6D1F))

                // Sezione Crashlytics / Diagnostica
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_crash_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(id = R.string.settings_crash_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6D5A48),
                    )
                    MaestroPillButton(
                        text = stringResource(id = R.string.settings_crash_button),
                        onClick = onTriggerTestCrash,
                        variant = PillButtonVariant.HERO_RED,
                        height = 46.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "trigger_test_crash_button",
                    )
                }

                HorizontalDivider(color = Color(0xFF8C6D1F))

                // Sezione Sviluppo e Collaudo rapido
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_test_match_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(id = R.string.settings_test_match_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6D5A48),
                    )
                    MaestroPillButton(
                        text = stringResource(id = R.string.settings_test_match_button),
                        onClick = onStartTestEndgameMatch,
                        variant = PillButtonVariant.WOOD,
                        height = 46.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "start_test_match_button",
                    )
                }
                }
            }
        },
        confirmButton = {
            MaestroPillButton(
                text = stringResource(id = R.string.settings_close),
                onClick = onDismiss,
                variant = PillButtonVariant.PRIMARY_GREEN,
                height = 44.dp,
                fontSize = 16.sp,
                testTag = "close_settings_button",
            )
        },
    )
}
