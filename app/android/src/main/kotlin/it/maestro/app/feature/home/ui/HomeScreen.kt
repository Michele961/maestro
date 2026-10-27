package it.maestro.app.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.designsystem.components.ClubBackground
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.WoodRowTile

/**
 * Menu iniziale di Maestro (task F1R-19, `docs/app/features/home.md`): una sola azione principale («Riprendi partita» se
 * ce n'è una in corso, altrimenti «Gioca»), il Maestro, e le destinazioni che esistono davvero: Sfida del giorno,
 * Missioni, Accademia. Il Profilo si apre dall'avatar, la Bottega dal portafoglio. Tutti i testi dalle risorse (ADR-0007).
 */
@Composable
public fun HomeScreen(
    modifier: Modifier = Modifier,
    hasActiveMatch: Boolean = false,
    streakCount: Int = 0,
    credits: Int = 50,
    avatarId: String = "avatar_default",
    playerName: String? = null,
    mastery: Int? = null,
    onResumeGameClicked: () -> Unit = {},
    onStartGameClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
    onDailyPuzzleClicked: () -> Unit = {},
    onProfileClicked: () -> Unit = {},
    onMissionsClicked: () -> Unit = {},
    onAcademyClicked: () -> Unit = {},
    onShopClicked: () -> Unit = {},
) {
    ClubBackground(modifier = modifier, feltFraction = 0.5f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HomeHeader(
                credits = credits,
                avatarId = avatarId,
                playerName = playerName,
                mastery = mastery,
                onSettingsClicked = onSettingsClicked,
                onProfileClicked = onProfileClicked,
                onShopClicked = onShopClicked,
            )
            MaestroWelcome(modifier = Modifier.weight(1f))
            PrimaryAction(hasActiveMatch, onResumeGameClicked, onStartGameClicked)
            WoodRowTile(
                icon = R.drawable.icon_book_quill,
                title = stringResource(R.string.home_daily_puzzle_button),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("home_daily_puzzle_tile"),
                onClick = onDailyPuzzleClicked,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MissionsTile(streakCount, onMissionsClicked, Modifier.weight(1f).height(72.dp))
                AcademyTile(onAcademyClicked, Modifier.weight(1f).height(72.dp))
            }
        }
    }
}

/** L'unica chiamata all'azione: sul piedistallo di panno ovale, come nel mockup. */
@Composable
private fun PrimaryAction(hasActiveMatch: Boolean, onResume: () -> Unit, onStart: () -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(R.drawable.felt_oval_base),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxWidth().height(if (hasActiveMatch) 82.dp else 96.dp),
        )
        MaestroPillButton(
            text = stringResource(if (hasActiveMatch) R.string.home_resume_game else R.string.home_play_now).uppercase(),
            onClick = if (hasActiveMatch) onResume else onStart,
            variant = PillButtonVariant.HERO_RED,
            height = if (hasActiveMatch) 66.dp else 80.dp,
            fontSize = if (hasActiveMatch) 24.sp else 34.sp,
            modifier = Modifier.fillMaxWidth(if (hasActiveMatch) 0.82f else 0.72f),
            testTag = if (hasActiveMatch) "resume_game_button" else "play_now_button",
        )
    }
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
