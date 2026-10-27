package it.maestro.app.feature.table.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.domain.match.PointsRace
import it.maestro.app.domain.match.RaceLine
import it.maestro.game.training.SeatLead
import it.maestro.game.training.SettebelloLocation

/** Colore di chi è in vantaggio: oro per te, rosso per l'avversario, neutro in parità. */
private fun leadColor(lead: SeatLead): Color = when (lead) {
    SeatLead.HUMAN -> Color(0xFFD4AF37)
    SeatLead.OPPONENT -> Color(0xFFE57373)
    SeatLead.TIE -> Color(0xFFB0BEC5)
}

/**
 * Corsa ai punti della smazzata (`docs/app/game-feel.md` P4): quattro targhette, una per categoria.
 * In Sparring ([showNumbers]) con i numeri tuoi–avversario; in Classica solo il colore di chi è in vantaggio.
 */
@Composable
internal fun PointsRaceRow(race: PointsRace, showNumbers: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.testTag("points_race"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RaceChip("race_cards", stringResource(R.string.score_category_carte), race.cards, showNumbers)
        RaceChip("race_denari", stringResource(R.string.score_category_denari), race.denari, showNumbers)
        val settebelloLead = when (race.settebello) {
            SettebelloLocation.CAPTURED_BY_ME -> SeatLead.HUMAN
            SettebelloLocation.CAPTURED_BY_OPPONENT -> SeatLead.OPPONENT
            SettebelloLocation.IN_PLAY_OR_UNKNOWN -> SeatLead.TIE
        }
        RaceChip("race_settebello", stringResource(R.string.race_settebello_short), null, showNumbers, leadOverride = settebelloLead)
        RaceChip("race_primiera", stringResource(R.string.score_category_primiera), race.primiera, showNumbers)
    }
}

@Composable
private fun RowScope.RaceChip(
    tag: String,
    label: String,
    line: RaceLine?,
    showNumbers: Boolean,
    leadOverride: SeatLead? = null,
) {
    val lead = leadOverride ?: line?.lead ?: SeatLead.TIE
    val color = leadColor(lead)
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(Color(0x992A170C), RoundedCornerShape(10.dp))
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .semantics(mergeDescendants = true) {}
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = label, color = color, fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        if (showNumbers && line != null) {
            Text(text = stringResource(R.string.race_values, line.human, line.opponent), color = Color.White, fontSize = 13.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}
