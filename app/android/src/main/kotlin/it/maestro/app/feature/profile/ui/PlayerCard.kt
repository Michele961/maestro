package it.maestro.app.feature.profile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.designsystem.components.FramedPortrait
import it.maestro.app.designsystem.components.WoodPanel
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.feature.pregame.ui.style

/**
 * Scheda del giocatore (F1R-26b): il nome si cambia toccandolo, il titolo è quello scelto tra i guadagnati
 * (un tocco apre l'elenco). Senza vittorie sulla scala il titolo è «Nuovo al circolo».
 */
@Composable
internal fun PlayerIdentityCard(
    playerName: String?,
    currentTitle: Opponent?,
    earnedTitles: List<Opponent>,
    onSelectTitle: (Opponent) -> Unit,
    onEditName: () -> Unit,
) {
    var picking by remember { mutableStateOf(false) }
    WoodPanel(modifier = Modifier.fillMaxWidth().testTag("profile_identity_card")) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box {
                    FramedPortrait(portrait = R.drawable.avatar_player_default, size = 68.dp)
                    Image(
                        painter = painterResource(R.drawable.icon_shield_fleur),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).align(Alignment.BottomEnd),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playerName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.home_player_default_name),
                        color = BrassRivetGold,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaestroDisplayFont,
                        modifier = Modifier.clickable(onClick = onEditName).testTag("profile_name"),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TitleBadge(
                        title = currentTitle,
                        enabled = earnedTitles.size > 1,
                        onClick = { picking = !picking },
                    )
                }
            }
            if (picking) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.profile_title_pick).uppercase(),
                    color = BrassRivetGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MaestroDisplayFont,
                )
                earnedTitles.forEach { opponent ->
                    Text(
                        text = stringResource(opponent.style().earnedTitle),
                        color = if (opponent == currentTitle) BrassRivetGold else Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (opponent == currentTitle) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTitle(opponent); picking = false }
                            .padding(vertical = 6.dp)
                            .testTag("profile_title_option_${opponent.name}"),
                    )
                }
            }
        }
    }
}

@Composable
private fun TitleBadge(title: Opponent?, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E3A5F),
        border = BorderStroke(1.dp, Color(0xFF64B5F6)),
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick).testTag("profile_rank_badge"),
    ) {
        Text(
            text = stringResource(title?.style()?.earnedTitle ?: R.string.profile_title_none).uppercase(),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MaestroDisplayFont,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
        )
    }
}
