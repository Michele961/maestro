package it.maestro.app.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import it.maestro.app.designsystem.components.ParchmentPanel
import it.maestro.app.designsystem.theme.WoodSurfaceDark
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.designsystem.components.BubbleTail
import it.maestro.app.designsystem.components.CurrencyPill
import it.maestro.app.designsystem.components.FramedPortrait
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.MaestroSpeechBubble
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.WoodIconButton
import it.maestro.app.designsystem.components.WoodPanel
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.ButtonAmberBottom
import it.maestro.app.designsystem.theme.ButtonGoldBorder
import it.maestro.app.designsystem.theme.MaestroDisplayFont

/**
 * Intestazione della Home: avatar (apre il Profilo) con nome e Indice di Maestria, portafoglio (apre la Bottega) e
 * impostazioni. Niente voci che non portano da nessuna parte (`docs/app/features/home.md` §3).
 */
@Composable
internal fun HomeHeader(
    credits: Int,
    avatarId: String,
    playerName: String?,
    mastery: Int?,
    onSettingsClicked: () -> Unit,
    onProfileClicked: () -> Unit,
    onShopClicked: () -> Unit,
) {
    val avatarRes = when (avatarId) {
        "avatar_alt" -> R.drawable.avatar_player_alt
        "avatar_expert" -> R.drawable.avatar_expert
        else -> R.drawable.avatar_player_default
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.testTag("home_player_profile").clickable(onClick = onProfileClicked)) {
            FramedPortrait(portrait = avatarRes, size = 56.dp)
            Image(
                painter = painterResource(R.drawable.icon_shield_fleur),
                contentDescription = null,
                modifier = Modifier.size(22.dp).align(Alignment.BottomEnd),
            )
        }
        Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
            Text(
                text = playerName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.home_player_default_name),
                color = BrassRivetGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MaestroDisplayFont,
                maxLines = 1,
                modifier = Modifier.testTag("home_player_name"),
            )
            if (mastery != null) {
                Text(
                    text = stringResource(R.string.home_mastery_label, mastery),
                    color = BrassRivetGold.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontFamily = MaestroDisplayFont,
                    maxLines = 1,
                    modifier = Modifier.testTag("home_mastery"),
                )
            }
            CurrencyPill(
                icon = R.drawable.icon_coin,
                value = credits.toString(),
                modifier = Modifier.testTag("home_credits_pill").clickable(onClick = onShopClicked),
            )
        }
        WoodIconButton(
            icon = R.drawable.icon_gear,
            contentDescription = stringResource(R.string.home_settings_button),
            testTag = "home_settings_button",
            onClick = onSettingsClicked,
        )
    }
}

/** Il Maestro con la sua battuta: occupa lo spazio che resta, così la Home sta in una schermata senza scroll. */
@Composable
internal fun MaestroWelcome(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth().heightIn(min = 130.dp, max = 240.dp)) {
        val compact = maxHeight < 210.dp
        val ultraCompact = maxHeight < 170.dp
        val portrait = when {
            ultraCompact -> 68.dp
            compact -> 80.dp
            else -> (maxHeight - 90.dp).coerceIn(84.dp, 120.dp)
        }
        WoodPanel(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
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
                        contentPadding = PaddingValues(horizontal = if (compact) 10.dp else 16.dp),
                        modifier = Modifier.offset(y = (-6).dp),
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
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

/** Missioni giornaliere, con la fiamma della serie se c'è. */
@Composable
internal fun MissionsTile(streakCount: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    WoodPanel(modifier = modifier.testTag("home_missions_tile").clickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(R.drawable.icon_missions), contentDescription = null, modifier = Modifier.size(42.dp))
            Column(modifier = Modifier.padding(start = 6.dp).weight(1f), verticalArrangement = Arrangement.Center) {
                TileTitle(stringResource(R.string.home_missions_title))
                if (streakCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2A170C))
                            .border(1.dp, ButtonGoldBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                            .testTag("home_missions_streak_badge"),
                    ) {
                        Image(painter = painterResource(R.drawable.icon_flame), contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = stringResource(R.string.home_missions_streak_badge, streakCount),
                            color = BrassRivetGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MaestroDisplayFont,
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.home_missions_subtitle),
                        color = BrassRivetGold.copy(alpha = 0.75f),
                        fontSize = 10.sp,
                        fontFamily = MaestroDisplayFont,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** Accademia del Maestro. */
@Composable
internal fun AcademyTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    WoodPanel(modifier = modifier.testTag("home_academy_tile").clickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(R.drawable.icon_book_closed), contentDescription = null, modifier = Modifier.size(42.dp))
            Column(modifier = Modifier.padding(start = 6.dp).weight(1f), verticalArrangement = Arrangement.Center) {
                TileTitle(stringResource(R.string.academy_nav_title))
            }
        }
    }
}

@Composable
private fun TileTitle(text: String) {
    Text(
        text = text.uppercase(),
        color = BrassRivetGold,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = MaestroDisplayFont,
        maxLines = 1,
    )
}

/** Tessera di un gioco: Scopa si gioca, Briscola arriva più avanti. */
@Composable
internal fun GameTile(
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

