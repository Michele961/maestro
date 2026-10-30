package it.maestro.app.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.feature.home.presentation.HomeLines
import it.maestro.app.designsystem.components.ClubBackground
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.WoodRowTile

/**
 * Menu iniziale di Maestro (task F1R-19, `docs/app/features/home.md`): l'azione principale («Riprendi partita» se ce n'è
 * una in corso, altrimenti «Gioca ora»), le tessere dei giochi (Scopa; Briscola arriverà), il Maestro e le
 * destinazioni che esistono davvero: Sfida del giorno, Missioni, Accademia. Il Profilo si apre dall'avatar, la Bottega dal portafoglio. Tutti i testi dalle risorse (ADR-0007).
 */
@Composable
public fun HomeScreen(
    modifier: Modifier = Modifier,
    hasActiveMatch: Boolean = false,
    streakCount: Int = 0,
    credits: Int = 50,
    avatarId: String = "avatar_default",
    playerName: String? = null,
    askPlayerName: Boolean = false,
    playerNameMaxLength: Int = 20,
    /** Giorno corrente (`LocalDate.toEpochDay`): fa cambiare la battuta del Maestro da un giorno all'altro. */
    day: Long = 0L,
    mastery: Int? = null,
    onResumeGameClicked: () -> Unit = {},
    onStartGameClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
    onDailyPuzzleClicked: () -> Unit = {},
    onDailyMatchClicked: () -> Unit = {},
    onProfileClicked: () -> Unit = {},
    onMissionsClicked: () -> Unit = {},
    onAcademyClicked: () -> Unit = {},
    onShopClicked: () -> Unit = {},
    onPlayerNameConfirmed: (String) -> Unit = {},
    onPlayerNameSkipped: () -> Unit = {},
) {
    // Si apre da solo la prima volta; poi tocchi il nome per cambiarlo.
    var editingName by remember { mutableStateOf(false) }
    if (askPlayerName || editingName) {
        PlayerNameDialog(
            currentName = playerName,
            maxLength = playerNameMaxLength,
            onConfirm = { editingName = false; onPlayerNameConfirmed(it) },
            onDismiss = { editingName = false; onPlayerNameSkipped() },
        )
    }
    ClubBackground(modifier = modifier, feltFraction = 0.5f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            HomeHeader(
                credits = credits,
                avatarId = avatarId,
                playerName = playerName,
                mastery = mastery,
                onSettingsClicked = onSettingsClicked,
                onProfileClicked = onProfileClicked,
                onShopClicked = onShopClicked,
                onNameClicked = { editingName = true },
            )
            MaestroWelcome(
                line = remember(hasActiveMatch, streakCount, playerName, day) {
                    HomeLines.choose(hasActiveMatch, streakCount, !playerName.isNullOrBlank(), day)
                },
                streakCount = streakCount,
                playerName = playerName,
                modifier = Modifier.weight(1f, fill = false),
            )
            PrimaryAction(hasActiveMatch, onResumeGameClicked, onStartGameClicked)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GameTile(
                    title = stringResource(R.string.home_game_briscola),
                    card = R.drawable.card_bastoni_asso,
                    available = false,
                    onPlay = {},
                    testTag = "briscola_tile",
                    modifier = Modifier.weight(1f),
                )
                GameTile(
                    title = stringResource(R.string.home_game_scopa),
                    card = R.drawable.card_denari_7,
                    available = true,
                    onPlay = onStartGameClicked,
                    testTag = "scopa_tile",
                    modifier = Modifier.weight(1f),
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WoodRowTile(
                    icon = R.drawable.icon_book_quill,
                    title = stringResource(R.string.home_daily_puzzle_button),
                    modifier = Modifier.weight(1f).height(50.dp).testTag("home_daily_puzzle_tile"),
                    onClick = onDailyPuzzleClicked,
                )
                WoodRowTile(
                    icon = R.drawable.card_denari_7,
                    title = stringResource(R.string.home_daily_match_button),
                    modifier = Modifier.weight(1f).height(50.dp).testTag("home_daily_match_tile"),
                    onClick = onDailyMatchClicked,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MissionsTile(streakCount, onMissionsClicked, Modifier.weight(1f).height(84.dp))
                AcademyTile(onAcademyClicked, Modifier.weight(1f).height(84.dp))
            }
        }
    }
}

/** La chiamata all'azione principale: senza ornamenti dietro, il pulsante basta. */
@Composable
private fun PrimaryAction(hasActiveMatch: Boolean, onResume: () -> Unit, onStart: () -> Unit) {
    MaestroPillButton(
        text = stringResource(if (hasActiveMatch) R.string.home_resume_game else R.string.home_play_now).uppercase(),
        onClick = if (hasActiveMatch) onResume else onStart,
        variant = PillButtonVariant.HERO_RED,
        height = if (hasActiveMatch) 62.dp else 72.dp,
        fontSize = if (hasActiveMatch) 24.sp else 32.sp,
        modifier = Modifier.fillMaxWidth(0.86f),
        testTag = if (hasActiveMatch) "resume_game_button" else "play_now_button",
    )
    if (hasActiveMatch) {
        MaestroPillButton(
            text = stringResource(R.string.home_start_game),
            onClick = onStart,
            variant = PillButtonVariant.SECONDARY_AMBER,
            height = 42.dp,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth(0.55f),
            testTag = "new_game_button",
        )
    }
}
