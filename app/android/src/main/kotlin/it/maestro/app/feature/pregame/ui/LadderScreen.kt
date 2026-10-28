package it.maestro.app.feature.pregame.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.designsystem.components.ClubBackground
import it.maestro.app.designsystem.components.NineSliceBox
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.feature.pregame.presentation.LadderEntry
import it.maestro.app.feature.pregame.presentation.LadderState
import it.maestro.app.feature.pregame.presentation.LadderStatus

private val Ink = Color(0xFF2B1A10)
private val Cream = Color(0xFFFFF4D6)

/**
 * La scala degli avversari (`docs/app/features/pre-game.md` §3): i cinque avversari dal più debole al più forte,
 * con il prossimo da battere in evidenza. Un tocco su uno sbloccato porta alla preparazione; i bloccati dicono
 * chi battere. Tutti i testi dalle risorse (ADR-0007).
 */
@Composable
public fun LadderScreen(
    state: LadderState,
    onOpponentClick: (Opponent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ClubBackground(modifier = modifier.testTag("ladder_screen"), feltFraction = 0.5f) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0x99000000)))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Horizontal))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.ladder_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Cream,
                    modifier = Modifier.weight(1f),
                )
                Image(
                    painter = painterResource(R.drawable.button_close),
                    contentDescription = stringResource(R.string.pre_game_back_button),
                    modifier = Modifier.size(44.dp).clickable(onClick = onNavigateBack).testTag("ladder_back_button"),
                )
            }
            // Le cinque righe stanno nello schermo; lo scorrimento serve solo con font molto grandi.
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (entry in state.entries) {
                    LadderRow(entry = entry, onClick = { onOpponentClick(entry.opponent) })
                }
            }
        }
    }
}

@Composable
private fun LadderRow(entry: LadderEntry, onClick: () -> Unit) {
    val style = entry.opponent.style()
    val locked = entry.status == LadderStatus.LOCKED
    val next = entry.status == LadderStatus.NEXT
    val name = stringResource(style.name)
    NineSliceBox(
        skin = if (next) R.drawable.skin_button_green else R.drawable.skin_button_cream,
        slice = 0.42f,
        corner = 18.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .alpha(if (locked) 0.7f else 1f)
            .clickable(enabled = !locked, onClick = onClick)
            .testTag("ladder_row_${entry.opponent.name}"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Image(
                painter = painterResource(style.portrait),
                contentDescription = null,
                colorFilter = if (locked) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                modifier = Modifier.size(52.dp).clip(CircleShape),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val textColor = if (next) Cream else Ink
                Text(text = name, color = textColor, fontSize = 17.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (locked) {
                    // Un avversario bloccato dice solo chi battere: una riga in meno, le cinque righe stanno nello schermo.
                    val by = entry.unlockedBy?.let { stringResource(it.style().name) }.orEmpty()
                    Text(
                        text = stringResource(R.string.ladder_locked_hint, by),
                        color = Ink,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("ladder_locked_hint_${entry.opponent.name}"),
                    )
                } else {
                    val subtitle = if (entry.status == LadderStatus.BEATEN) style.earnedTitle else style.title
                    Text(text = stringResource(subtitle), color = textColor, fontSize = 12.sp, lineHeight = 14.sp, maxLines = 2)
                }
            }
            when (entry.status) {
                LadderStatus.LOCKED -> Image(
                    painter = painterResource(R.drawable.lock_closed),
                    contentDescription = stringResource(R.string.ladder_locked),
                    modifier = Modifier.size(36.dp),
                )
                LadderStatus.NEXT -> Text(text = stringResource(R.string.ladder_next), color = Cream, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LadderStatus.BEATEN -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(style.crest), contentDescription = null, modifier = Modifier.size(36.dp))
                    Text(text = stringResource(R.string.ladder_beaten), color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
