package it.maestro.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import it.maestro.app.ui.components.CurrencyPill
import it.maestro.app.ui.components.FramedPortrait
import it.maestro.app.ui.components.MaestroDialogueBox
import it.maestro.app.ui.components.MaestroPillButton
import it.maestro.app.ui.components.ParchmentPanel
import it.maestro.app.ui.components.PillButtonVariant
import it.maestro.app.ui.components.WoodIconButton
import it.maestro.app.ui.components.WoodPanel
import it.maestro.app.ui.components.WoodRowTile
import it.maestro.app.ui.theme.BrassRivetGold
import it.maestro.app.ui.theme.ButtonAmberBottom
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
    onResumeGameClicked: () -> Unit = {},
    onStartGameClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize().background(WoodSurfaceDark)) {
        Image(
            painter = painterResource(R.drawable.texture_wood),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Image(
            painter = painterResource(R.drawable.table_felt_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f).align(Alignment.BottomCenter),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                HomeHeader(onSettingsClicked = onSettingsClicked)
                MaestroWelcome()
                MaestroPillButton(
                    text = stringResource(
                        if (hasActiveMatch) R.string.home_resume_game else R.string.home_play_now,
                    ).uppercase(),
                    onClick = if (hasActiveMatch) onResumeGameClicked else onStartGameClicked,
                    variant = PillButtonVariant.HERO_RED,
                    height = 64.dp,
                    fontSize = 26.sp,
                    modifier = Modifier.fillMaxWidth(0.75f),
                    testTag = if (hasActiveMatch) "resume_game_button" else "play_now_button",
                )
                if (hasActiveMatch) {
                    MaestroPillButton(
                        text = stringResource(R.string.home_start_game),
                        onClick = onStartGameClicked,
                        variant = PillButtonVariant.SECONDARY_AMBER,
                        modifier = Modifier.fillMaxWidth(0.6f),
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
                    icon = R.drawable.icon_newspaper,
                    title = stringResource(R.string.home_news),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WoodRowTile(
                        icon = R.drawable.icon_gift,
                        title = stringResource(R.string.home_daily_rewards),
                        iconSize = 52.dp,
                        modifier = Modifier.weight(1f).height(72.dp),
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
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            }
            BottomNavBar()
        }
    }
}

@Composable
private fun HomeHeader(onSettingsClicked: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
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
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
private fun MaestroWelcome() {
    ParchmentPanel(
        showTopOrnament = true,
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FramedPortrait(
                portrait = R.drawable.maestro_neutral,
                size = 112.dp,
                background = ButtonAmberBottom,
                contentDescription = stringResource(R.string.maestro_avatar_description),
            )
            Spacer(modifier = Modifier.width(12.dp))
            MaestroDialogueBox(
                title = stringResource(R.string.game_player_maestro),
                message = stringResource(R.string.home_coach_welcome),
                modifier = Modifier.weight(1f),
            )
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
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Image(
            painter = painterResource(card),
            contentDescription = null,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .height(96.dp)
                .rotate(-6f)
                .alpha(if (available) 1f else 0.55f)
                .align(Alignment.CenterHorizontally),
        )
        MaestroPillButton(
            text = stringResource(if (available) R.string.home_play else R.string.home_coming_soon),
            onClick = onPlay,
            enabled = available,
            variant = if (available) PillButtonVariant.PRIMARY_GREEN else PillButtonVariant.WOOD,
            height = 40.dp,
            fontSize = 14.sp,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
            modifier = Modifier.fillMaxWidth(),
            testTag = "${testTag}_button",
        )
    }
}

@Composable
private fun BottomNavBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF3B2213), Color(0xFF1E1109))))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BrassIconBadge(icon = R.drawable.icon_trophy, label = stringResource(R.string.home_nav_leaderboard))
        BrassIconBadge(icon = R.drawable.icon_book_quill, label = stringResource(R.string.home_nav_training))
        BrassIconBadge(icon = R.drawable.icon_pouch, label = stringResource(R.string.home_nav_shop))
    }
}
