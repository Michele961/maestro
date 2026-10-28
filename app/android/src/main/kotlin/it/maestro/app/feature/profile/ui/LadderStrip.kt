package it.maestro.app.feature.profile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import it.maestro.app.designsystem.components.WoodPanel
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import it.maestro.app.feature.pregame.presentation.LadderEntry
import it.maestro.app.feature.pregame.presentation.LadderState
import it.maestro.app.feature.pregame.presentation.LadderStatus
import it.maestro.app.feature.pregame.ui.style

/**
 * La scala nel Profilo (F1R-26c): i cinque stemmi in fila, battuto / prossimo da battere / bloccato.
 * Un tocco su uno stemma dice chi è e che titolo ha dato (o chi battere per sbloccarlo).
 */
@Composable
internal fun LadderStrip(ladder: LadderState) {
    var shown by remember { mutableStateOf<LadderEntry?>(null) }
    WoodPanel(modifier = Modifier.fillMaxWidth().testTag("profile_ladder_panel")) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_ladder_title).uppercase(),
                color = BrassRivetGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MaestroDisplayFont,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ladder.entries.forEach { entry -> Crest(entry, entry == shown) { shown = entry } }
            }
            shown?.let { entry -> LadderDetail(entry) }
        }
    }
}

@Composable
private fun Crest(entry: LadderEntry, selected: Boolean, onClick: () -> Unit) {
    val style = entry.opponent.style()
    val name = stringResource(style.name)
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (entry.status == LadderStatus.NEXT) Color(0xFF3E2313) else Color(0xFF221610),
        border = BorderStroke(if (selected || entry.status == LadderStatus.NEXT) 1.5.dp else 1.dp, if (selected || entry.status == LadderStatus.NEXT) BrassRivetGold else Color(0xFF4E3629)),
        modifier = Modifier.clickable(onClick = onClick).testTag("profile_ladder_${entry.opponent.name}"),
    ) {
        Image(
            painter = painterResource(if (entry.status == LadderStatus.BEATEN) style.crest else R.drawable.lock_closed),
            contentDescription = name,
            modifier = Modifier.padding(6.dp).size(40.dp),
        )
    }
}

@Composable
private fun LadderDetail(entry: LadderEntry) {
    val style = entry.opponent.style()
    val line = when (entry.status) {
        LadderStatus.BEATEN -> stringResource(style.earnedTitle)
        LadderStatus.NEXT -> stringResource(R.string.ladder_next)
        LadderStatus.LOCKED -> stringResource(R.string.ladder_locked_hint, entry.unlockedBy?.let { stringResource(it.style().name) }.orEmpty())
    }
    Column(modifier = Modifier.fillMaxWidth().testTag("profile_ladder_detail"), horizontalAlignment = Alignment.Start) {
        Text(text = stringResource(style.name), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = line, color = Color(0xFFD4C8B8), fontSize = 12.sp)
    }
}
