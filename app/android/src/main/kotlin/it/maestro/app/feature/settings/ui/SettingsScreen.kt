package it.maestro.app.feature.settings.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.maestro.app.R
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.ParchmentPanel
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import it.maestro.app.designsystem.theme.WoodSurfaceDark
import it.maestro.app.domain.coach.Chattiness
import it.maestro.app.domain.settings.AppLanguage
import it.maestro.app.domain.settings.GameSpeed
import it.maestro.app.feature.settings.presentation.SettingsUiState
import it.maestro.app.feature.settings.presentation.SettingsViewModel
import it.maestro.app.shell.DeveloperTools
import org.koin.androidx.compose.koinViewModel

/** Colore dei sottotitoli su pergamena. */
private val SubtitleColor = Color(0xFF6D5A48)

/** Poco margine interno: le etichette delle scelte (Normale, Rapidissima…) devono leggersi per intero. */
private val ChoicePadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)

/**
 * Schermata delle impostazioni, a sezioni (task F1R-31a): Audio, Gioco e, nelle sole build di sviluppo, Sviluppo.
 * Le scelte si applicano subito. Tutti i testi vengono dalle risorse (ADR-0007).
 */
@Composable
public fun SettingsScreen(
    onTriggerTestCrash: () -> Unit,
    onStartTestEndgameMatch: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreenContent(
        state = state,
        onSoundToggled = viewModel::setSoundEnabled,
        onGameSpeedChanged = viewModel::setGameSpeed,
        onMaestroChattinessChanged = viewModel::setMaestroChattiness,
        onShowPointsRaceToggled = viewModel::setShowPointsRace,
        onLanguageChanged = viewModel::setLanguage,
        onEffectsVolumeChange = viewModel::previewEffectsVolume,
        onEffectsVolumeCommitted = viewModel::commitEffectsVolume,
        onMusicVolumeChange = viewModel::previewMusicVolume,
        onMusicVolumeCommitted = viewModel::commitMusicVolume,
        onAmbientVolumeChange = viewModel::previewAmbientVolume,
        onAmbientVolumeCommitted = viewModel::commitAmbientVolume,
        onHapticsToggled = viewModel::setHapticsEnabled,
        onTriggerTestCrash = onTriggerTestCrash,
        onStartTestEndgameMatch = onStartTestEndgameMatch,
        onNavigateHome = onNavigateHome,
        modifier = modifier,
        showDeveloperTools = DeveloperTools.isEnabled(LocalContext.current),
    )
}

@Composable
public fun SettingsScreenContent(
    state: SettingsUiState,
    onSoundToggled: (Boolean) -> Unit,
    onGameSpeedChanged: (GameSpeed) -> Unit,
    onMaestroChattinessChanged: (Chattiness) -> Unit,
    onShowPointsRaceToggled: (Boolean) -> Unit,
    onTriggerTestCrash: () -> Unit,
    onStartTestEndgameMatch: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
    showDeveloperTools: Boolean = false,
    onEffectsVolumeChange: (Int) -> Unit = {},
    onEffectsVolumeCommitted: (Int) -> Unit = {},
    onMusicVolumeChange: (Int) -> Unit = {},
    onMusicVolumeCommitted: (Int) -> Unit = {},
    onAmbientVolumeChange: (Int) -> Unit = {},
    onAmbientVolumeCommitted: (Int) -> Unit = {},
    onHapticsToggled: (Boolean) -> Unit = {},
    onLanguageChanged: (AppLanguage) -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WoodSurfaceDark)
            .testTag("settings_screen"),
    ) {
        Image(
            painter = painterResource(R.drawable.texture_wood),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.settings_title).uppercase(),
                    color = BrassRivetGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MaestroDisplayFont,
                    modifier = Modifier.weight(1f),
                )
                MaestroPillButton(
                    text = stringResource(R.string.settings_close),
                    onClick = onNavigateHome,
                    variant = PillButtonVariant.PRIMARY_GREEN,
                    height = 40.dp,
                    fontSize = 14.sp,
                    testTag = "close_settings_button",
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SettingsSection(title = stringResource(R.string.settings_section_audio), testTag = "settings_section_audio") {
                    SwitchRow(
                        title = stringResource(R.string.settings_sound_effects),
                        subtitle = stringResource(R.string.settings_sound_subtitle),
                        checked = state.soundEnabled,
                        onCheckedChange = onSoundToggled,
                        rowTag = "sound_toggle_row",
                        switchTag = "sound_enabled_switch",
                    )
                    VolumeRow(
                        title = stringResource(R.string.settings_effects_volume),
                        volume = state.effectsVolume,
                        enabled = state.soundEnabled,
                        onChange = onEffectsVolumeChange,
                        onCommitted = onEffectsVolumeCommitted,
                        tag = "effects_volume_slider",
                    )
                    VolumeRow(
                        title = stringResource(R.string.settings_music_volume),
                        volume = state.musicVolume,
                        enabled = state.soundEnabled,
                        onChange = onMusicVolumeChange,
                        onCommitted = onMusicVolumeCommitted,
                        tag = "music_volume_slider",
                    )
                    VolumeRow(
                        title = stringResource(R.string.settings_ambient_volume),
                        volume = state.ambientVolume,
                        enabled = state.soundEnabled,
                        onChange = onAmbientVolumeChange,
                        onCommitted = onAmbientVolumeCommitted,
                        tag = "ambient_volume_slider",
                    )
                    SwitchRow(
                        title = stringResource(R.string.settings_haptics),
                        subtitle = stringResource(R.string.settings_haptics_subtitle),
                        checked = state.hapticsEnabled,
                        onCheckedChange = onHapticsToggled,
                        rowTag = "haptics_row",
                        switchTag = "haptics_switch",
                    )
                }
                SettingsSection(title = stringResource(R.string.settings_section_game), testTag = "settings_section_game") {
                    ChoiceRow(
                        title = stringResource(R.string.settings_game_speed),
                        subtitle = stringResource(R.string.settings_game_speed_subtitle),
                        choices = GameSpeed.USER_CHOICES,
                        selected = state.gameSpeed,
                        label = { stringResource(it.labelRes()) },
                        onSelected = onGameSpeedChanged,
                        rowTag = "game_speed_row",
                        choiceTag = { "game_speed_${it.name.lowercase()}" },
                    )
                    SwitchRow(
                        title = stringResource(R.string.settings_points_race),
                        subtitle = stringResource(R.string.settings_points_race_subtitle),
                        checked = state.showPointsRace,
                        onCheckedChange = onShowPointsRaceToggled,
                        rowTag = "points_race_row",
                        switchTag = "points_race_switch",
                    )
                    ChoiceRow(
                        title = stringResource(R.string.settings_maestro_chat),
                        subtitle = stringResource(R.string.settings_maestro_chat_subtitle),
                        choices = Chattiness.entries,
                        selected = state.maestroChattiness,
                        label = { stringResource(it.labelRes()) },
                        onSelected = onMaestroChattinessChanged,
                        rowTag = "maestro_chat_row",
                        choiceTag = { "maestro_chat_${it.name.lowercase()}" },
                    )
                    ChoiceRow(
                        title = stringResource(R.string.settings_language),
                        subtitle = stringResource(R.string.settings_language_subtitle),
                        choices = AppLanguage.entries,
                        selected = state.language,
                        label = { stringResource(it.labelRes()) },
                        onSelected = onLanguageChanged,
                        rowTag = "language_row",
                        choiceTag = { "language_${it.name.lowercase()}" },
                    )
                }
                // Strumenti di sviluppo solo nelle build debuggabili (task F1R-05).
                if (showDeveloperTools) {
                    SettingsSection(title = stringResource(R.string.settings_section_developer), testTag = "settings_section_developer") {
                        DeveloperAction(
                            title = stringResource(R.string.settings_crash_title),
                            subtitle = stringResource(R.string.settings_crash_warning),
                            button = stringResource(R.string.settings_crash_button),
                            variant = PillButtonVariant.HERO_RED,
                            onClick = onTriggerTestCrash,
                            testTag = "trigger_test_crash_button",
                        )
                        HorizontalDivider(color = Color(0xFF8C6D1F))
                        DeveloperAction(
                            title = stringResource(R.string.settings_test_match_title),
                            subtitle = stringResource(R.string.settings_test_match_subtitle),
                            button = stringResource(R.string.settings_test_match_button),
                            variant = PillButtonVariant.WOOD,
                            onClick = onStartTestEndgameMatch,
                            testTag = "start_test_match_button",
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, testTag: String, content: @Composable ColumnScope.() -> Unit) {
    ParchmentPanel(
        modifier = Modifier.fillMaxWidth().testTag(testTag),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 20.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = title.uppercase(),
                color = Color(0xFF8C6D1F),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MaestroDisplayFont,
            )
            content()
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    rowTag: String,
    switchTag: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().testTag(rowTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = SubtitleColor)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.testTag(switchTag))
    }
}

/** Cursore del volume: mentre si trascina vale subito, al rilascio si salva e si fa sentire un suono. */
@Composable
private fun VolumeRow(title: String, volume: Int, enabled: Boolean, onChange: (Int) -> Unit, onCommitted: (Int) -> Unit, tag: String) {
    var dragging by remember(volume) { mutableFloatStateOf(volume.toFloat()) }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = stringResource(R.string.settings_volume_percent, dragging.toInt()), style = MaterialTheme.typography.bodyMedium, color = SubtitleColor)
        }
        Slider(
            value = dragging,
            onValueChange = {
                dragging = it
                onChange(it.toInt())
            },
            onValueChangeFinished = { onCommitted(dragging.toInt()) },
            valueRange = 0f..100f,
            enabled = enabled,
            modifier = Modifier.testTag(tag),
        )
    }
}

@Composable
private fun <T> ChoiceRow(
    title: String,
    subtitle: String,
    choices: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
    rowTag: String,
    choiceTag: (T) -> String,
) {
    Column(modifier = Modifier.fillMaxWidth().testTag(rowTag), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = SubtitleColor)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            choices.forEach { choice ->
                MaestroPillButton(
                    text = label(choice),
                    onClick = { onSelected(choice) },
                    variant = if (choice == selected) PillButtonVariant.PRIMARY_GREEN else PillButtonVariant.CREAM,
                    height = 40.dp,
                    fontSize = 13.sp,
                    contentPadding = ChoicePadding,
                    modifier = Modifier.weight(1f),
                    testTag = choiceTag(choice),
                )
            }
        }
    }
}

@Composable
private fun DeveloperAction(
    title: String,
    subtitle: String,
    button: String,
    variant: PillButtonVariant,
    onClick: () -> Unit,
    testTag: String,
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = SubtitleColor)
        MaestroPillButton(
            text = button,
            onClick = onClick,
            variant = variant,
            height = 46.dp,
            fontSize = 15.sp,
            modifier = Modifier.fillMaxWidth(),
            testTag = testTag,
        )
    }
}

private fun GameSpeed.labelRes(): Int = when (this) {
    GameSpeed.SLOW, GameSpeed.NORMAL -> R.string.settings_game_speed_normal
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

private fun AppLanguage.labelRes(): Int = when (this) {
    AppLanguage.SYSTEM -> R.string.settings_language_system
    AppLanguage.ITALIAN -> R.string.settings_language_italian
    AppLanguage.ENGLISH -> R.string.settings_language_english
}
