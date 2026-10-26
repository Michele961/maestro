package it.maestro.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import it.maestro.app.ui.components.BubbleTail
import it.maestro.app.ui.components.MaestroSpeechBubble
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.ui.components.BrassIconBadge
import it.maestro.app.ui.components.ClubBackground
import it.maestro.app.ui.components.CurrencyPill
import it.maestro.app.ui.components.FramedPortrait
import it.maestro.app.ui.components.MaestroDialogueBox
import it.maestro.app.ui.components.MaestroPillButton
import it.maestro.app.ui.components.ParchmentPanel
import it.maestro.app.ui.components.PillButtonVariant
import it.maestro.app.ui.components.StreakPill
import it.maestro.app.ui.components.WoodIconButton
import it.maestro.app.ui.components.WoodPanel
import it.maestro.app.ui.components.WoodRowTile
import it.maestro.app.ui.theme.MaestroDisplayFont
import it.maestro.app.ui.theme.BrassRivetGold
import it.maestro.app.ui.theme.ButtonAmberBottom
import it.maestro.app.ui.theme.ButtonGoldBorder
import it.maestro.app.ui.theme.WoodSurfaceDark

/**
 * Menu iniziale di Maestro (F1-18), costruito sugli asset di F1-13 e su `reference_menu_screen.jpg`.
 * Tutti i testi vengono dalle risorse stringhe (ADR-0007).
 *
 * Funzionano: Gioca ora / Scopa (nuova partita o ripresa), impostazioni.
 * Briscola è "Presto disponibile"; monete, gemme, notizie, premi, amici e barra in basso
 * sono decorazioni con valori fissi finché non esistono i task dedicati.
 */
@Composable
public fun HomeScreen(
    modifier: Modifier = Modifier,
    hasActiveMatch: Boolean = false,
    streakCount: Int = 0,
    onResumeGameClicked: () -> Unit = {},
    onStartGameClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
    onDailyPuzzleClicked: () -> Unit = {},
    onProfileClicked: () -> Unit = {},
    onMissionsClicked: () -> Unit = {},
) {
    ClubBackground(modifier = modifier, feltFraction = 0.5f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HomeHeader(
                    onSettingsClicked = onSettingsClicked,
                    onProfileClicked = onProfileClicked,
                )
                // Il pannello del Maestro assorbe lo spazio: la Home sta in una schermata, senza scroll.
                MaestroWelcome(modifier = Modifier.weight(1f))
                // "Gioca ora" / "Riprendi partita" sul piedistallo di panno ovale, come nel mockup
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(R.drawable.felt_oval_base),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxWidth().height(if (hasActiveMatch) 82.dp else 96.dp),
                    )
                    MaestroPillButton(
                        text = stringResource(
                            if (hasActiveMatch) R.string.home_resume_game else R.string.home_play_now,
                        ).uppercase(),
                        onClick = if (hasActiveMatch) onResumeGameClicked else onStartGameClicked,
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
                        onClick = onStartGameClicked,
                        variant = PillButtonVariant.SECONDARY_AMBER,
                        height = 42.dp,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(0.55f),
                        testTag = "new_game_button",
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                WoodRowTile(
                    icon = R.drawable.icon_book_quill,
                    title = stringResource(R.string.home_daily_puzzle_button),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("home_daily_puzzle_tile"),
                    onClick = onDailyPuzzleClicked,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WoodRowTile(
                        icon = R.drawable.icon_missions,
                        title = stringResource(R.string.home_daily_missions),
                        iconSize = 52.dp,
                        modifier = Modifier.weight(1f).height(72.dp).testTag("home_missions_tile"),
                        onClick = onMissionsClicked,
                        trailingBadge = if (streakCount > 0) {
                            {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF2A170C))
                                        .border(1.dp, ButtonGoldBorder, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                        .testTag("home_missions_streak_badge"),
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.icon_flame),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = stringResource(R.string.home_missions_streak_badge, streakCount),
                                        color = BrassRivetGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = MaestroDisplayFont,
                                    )
                                }
                            }
                        } else null,
                    )
                    WoodPanel(modifier = Modifier.weight(1f).height(72.dp)) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.avatar_friends),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                            )
                            Text(
                                text = stringResource(R.string.home_friends).uppercase(),
                                color = BrassRivetGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MaestroDisplayFont,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            }
            BottomNavBar(
                onDailyPuzzleClicked = onDailyPuzzleClicked,
                onProfileClicked = onProfileClicked,
            )
        }
    }
}

@Composable
private fun HomeHeader(
    onSettingsClicked: () -> Unit,
    onProfileClicked: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .testTag("home_player_profile")
                .clickable(onClick = onProfileClicked),
        ) {
            FramedPortrait(portrait = R.drawable.avatar_player_default, size = 56.dp)
            Image(
                painter = painterResource(R.drawable.icon_shield_fleur),
                contentDescription = null,
                modifier = Modifier.size(22.dp).align(Alignment.BottomEnd),
            )
        }
        Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
            Text(
                text = stringResource(R.string.home_player_name),
                color = BrassRivetGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MaestroDisplayFont,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CurrencyPill(icon = R.drawable.icon_coin, value = stringResource(R.string.home_coins_placeholder))
                CurrencyPill(icon = R.drawable.icon_gem, value = stringResource(R.string.home_gems_placeholder))
            }
        }
        WoodIconButton(
            icon = R.drawable.icon_envelope,
            contentDescription = stringResource(R.string.home_messages),
        )
        Spacer(modifier = Modifier.width(8.dp))
        WoodIconButton(
            icon = R.drawable.icon_gear,
            contentDescription = stringResource(R.string.home_settings_button),
            testTag = "home_settings_button",
            onClick = onSettingsClicked,
        )
    }
}

@Composable
private fun MaestroWelcome(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth().heightIn(min = 130.dp)) {
        val compact = maxHeight < 210.dp
        val ultraCompact = maxHeight < 170.dp
        val portrait = when {
            ultraCompact -> 68.dp
            compact -> 80.dp
            else -> (maxHeight - 90.dp).coerceIn(84.dp, 120.dp)
        }
        WoodPanel(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    FramedPortrait(
                        portrait = R.drawable.maestro_neutral,
                        size = portrait,
                        background = ButtonAmberBottom,
                        contentDescription = stringResource(R.string.maestro_avatar_description),
                    )
                    MaestroPillButton(
                        text = stringResource(R.string.home_maestro_title).uppercase(),
                        onClick = {},
                        variant = PillButtonVariant.EDUCATIONAL_BLUE,
                        height = if (compact) 24.dp else 30.dp,
                        fontSize = if (compact) 10.sp else 12.sp,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = if (compact) 10.dp else 16.dp),
                        modifier = Modifier.offset(y = (-6).dp),
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    MaestroSpeechBubble(
                        message = stringResource(R.string.home_coach_welcome),
                        tail = BubbleTail.START,
                        fontSize = if (compact) 11.sp else 13.sp,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun GameTile(
    title: String,
    @DrawableRes card: Int,
    available: Boolean,
    onPlay: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    ParchmentPanel(modifier = modifier.testTag(testTag)) {
        Text(
            text = title.uppercase(),
            color = WoodSurfaceDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MaestroDisplayFont,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Image(
            painter = painterResource(card),
            contentDescription = null,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .height(70.dp)
                .rotate(-6f)
                .alpha(if (available) 1f else 0.55f)
                .align(Alignment.CenterHorizontally),
        )
        MaestroPillButton(
            text = stringResource(if (available) R.string.home_play else R.string.home_coming_soon),
            onClick = onPlay,
            enabled = available,
            variant = if (available) PillButtonVariant.PRIMARY_GREEN else PillButtonVariant.WOOD,
            height = 46.dp,
            fontSize = if (available) 18.sp else 12.sp,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
            modifier = Modifier.fillMaxWidth(),
            testTag = "${testTag}_button",
        )
    }
}

@Composable
private fun BottomNavBar(
    onDailyPuzzleClicked: () -> Unit = {},
    onProfileClicked: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF3B2213), Color(0xFF1E1109))))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BrassIconBadge(
            size = 76.dp,
            icon = R.drawable.icon_trophy,
            label = stringResource(R.string.home_nav_leaderboard),
            onClick = onProfileClicked,
            modifier = Modifier.testTag("home_nav_profile_button"),
        )
        BrassIconBadge(
            size = 76.dp,
            icon = R.drawable.icon_book_quill,
            label = stringResource(R.string.home_daily_puzzle_button),
            onClick = onDailyPuzzleClicked,
            modifier = Modifier.testTag("home_nav_puzzle_button"),
        )
        BrassIconBadge(size = 76.dp, icon = R.drawable.icon_pouch, label = stringResource(R.string.home_nav_shop))
    }
}
