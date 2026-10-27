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
import it.maestro.app.domain.coach.Chattiness
import it.maestro.app.domain.settings.GameSpeed

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
    gameSpeed: GameSpeed = GameSpeed.NORMAL,
    onGameSpeedChanged: (GameSpeed) -> Unit = {},
    maestroChattiness: Chattiness = Chattiness.NORMAL,
    onMaestroChattinessChanged: (Chattiness) -> Unit = {},
    showPointsRace: Boolean = false,
    onShowPointsRaceToggled: (Boolean) -> Unit = {},
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

                // Velocità di gioco: divide tutte le durate di MotionSpec (task F1R-09).
                Column(
                    modifier = Modifier.fillMaxWidth().testTag("game_speed_row"),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_game_speed),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        GameSpeed.USER_CHOICES.forEach { speed ->
                            MaestroPillButton(
                                text = stringResource(id = speed.labelRes()),
                                onClick = { onGameSpeedChanged(speed) },
                                variant = if (speed == gameSpeed) PillButtonVariant.PRIMARY_GREEN else PillButtonVariant.CREAM,
                                height = 40.dp,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f),
                                testTag = "game_speed_${speed.name.lowercase()}",
                            )
                        }
                    }
                }

                // Corsa ai punti al tavolo: un aiuto, spento di default (feedback di Michele, 2026-10-27).
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.settings_points_race),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(id = R.string.settings_points_race_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6D5A48),
                        )
                    }
                    Switch(
                        checked = showPointsRace,
                        onCheckedChange = onShowPointsRaceToggled,
                        modifier = Modifier.testTag("points_race_switch"),
                    )
                }

                // Chiacchiere del Maestro: quanto parla spontaneamente durante la partita (task F1R-24d).
                Column(
                    modifier = Modifier.fillMaxWidth().testTag("maestro_chat_row"),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_maestro_chat),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Chattiness.entries.forEach { level ->
                            MaestroPillButton(
                                text = stringResource(id = level.labelRes()),
                                onClick = { onMaestroChattinessChanged(level) },
                                variant = if (level == maestroChattiness) PillButtonVariant.PRIMARY_GREEN else PillButtonVariant.CREAM,
                                height = 40.dp,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f),
                                testTag = "maestro_chat_${level.name.lowercase()}",
                            )
                        }
                    }
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

private fun GameSpeed.labelRes(): Int = when (this) {
    GameSpeed.NORMAL -> R.string.settings_game_speed_normal
    GameSpeed.FAST -> R.string.settings_game_speed_fast
    GameSpeed.FASTER -> R.string.settings_game_speed_faster
    GameSpeed.INSTANT -> R.string.settings_game_speed_faster
}

private fun Chattiness.labelRes(): Int = when (this) {
    Chattiness.TALKATIVE -> R.string.settings_maestro_chat_talkative
    Chattiness.NORMAL -> R.string.settings_maestro_chat_normal
    Chattiness.QUIET -> R.string.settings_maestro_chat_quiet
    Chattiness.OFF -> R.string.settings_maestro_chat_off
}
