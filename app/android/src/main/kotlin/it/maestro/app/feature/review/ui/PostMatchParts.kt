package it.maestro.app.feature.review.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MoveGrade
import it.maestro.app.R
import it.maestro.app.designsystem.components.NineSliceBox
import it.maestro.app.designsystem.components.ParchmentPanel
import it.maestro.app.domain.retention.DailyMission
import it.maestro.app.domain.rewards.MissionAdvance
import it.maestro.app.domain.rewards.RewardSummary
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.feature.missions.ui.titleRes
import it.maestro.game.model.Seat

internal val CreamText = Color(0xFFFFF4D6)
internal val InkText = Color(0xFF2B1A10)

/** Targa del risultato: «VITTORIA! TU 11 - 8 AVVERSARIO». */
@Composable
internal fun ResultPlaque(won: Boolean, humanTotal: Int, opponentTotal: Int, modifier: Modifier = Modifier) {
    NineSliceBox(
        skin = R.drawable.skin_score_plaque,
        slice = 0.4f,
        corner = 24.dp,
        modifier = modifier.fillMaxWidth(0.92f).testTag("match_result_plaque"),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = if (won) R.string.post_match_victory else R.string.post_match_defeat).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = CreamText,
            )
            Text(
                text = stringResource(id = R.string.post_match_final_score, humanTotal, opponentTotal).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = CreamText,
            )
        }
    }
}

/** Precisione e conteggi degli errori del giocatore, con la riga dell'avversario. */
@Composable
internal fun AccuracyCard(analysis: MatchAnalysis, human: Seat, trainingScore: Pair<Int, Int>?, modifier: Modifier = Modifier) {
    val opponent = human.other()
    ParchmentPanel(modifier = modifier.fillMaxWidth().testTag("player_summary_card")) {
        NineSliceBox(
            skin = R.drawable.skin_plaque_wood,
            slice = 0.3f,
            corner = 16.dp,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
        ) {
            Text(
                text = stringResource(
                    id = R.string.post_match_accuracy_plaque,
                    analysis.accuracy(human),
                    analysis.count(human, MoveGrade.BLUNDER),
                ).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 15.sp,
                color = Color(0xFFFFE082),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("player_accuracy"),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            GradeStat(R.drawable.badge_best, analysis.count(human, MoveGrade.BEST), R.string.post_match_best_moves_label, "player_best_moves")
            GradeStat(R.drawable.badge_warning, analysis.count(human, MoveGrade.INACCURACY), R.string.post_match_inaccuracies_label, "player_inaccuracies")
            GradeStat(R.drawable.badge_wrong, analysis.count(human, MoveGrade.BLUNDER), R.string.post_match_blunders_label, "player_blunders")
        }
        Text(
            text = stringResource(id = R.string.post_match_opponent_line, analysis.accuracy(opponent), analysis.count(opponent, MoveGrade.BLUNDER)),
            style = MaterialTheme.typography.bodySmall,
            color = InkText.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("opponent_summary_card"),
        )
        if (trainingScore != null && trainingScore.second > 0) {
            val (correct, total) = trainingScore
            Text(
                text = stringResource(id = R.string.training_score_label, correct * 100 / total, correct, total),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C6B00),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp).testTag("match_over_attention_score"),
            )
        }
    }
}

/** Cosa si è guadagnato: l'Indice di Maestria con la variazione (sempre, vittoria o sconfitta) e la serie di giorni. */
@Composable
internal fun RewardCard(
    reward: RewardSummary,
    claimed: Set<String>,
    onClaim: (DailyMission) -> Unit,
    modifier: Modifier = Modifier,
) {
    ParchmentPanel(modifier = modifier.fillMaxWidth().testTag("reward_card")) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            val after = reward.masteryAfter
            val delta = reward.masteryDelta
            if (after != null && delta != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.post_match_mastery_line, after),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkText,
                        modifier = Modifier.testTag("reward_mastery"),
                    )
                    Text(
                        text = stringResource(id = if (delta >= 0) R.string.post_match_delta_up else R.string.post_match_delta_down, kotlin.math.abs(delta)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (delta >= 0) Color(0xFF2E7D32) else Color(0xFFB23A2B),
                        modifier = Modifier.testTag("reward_mastery_delta"),
                    )
                }
            }
            reward.missions.forEach { advance -> MissionRow(advance, advance.mission.id in claimed, onClaim) }
            val streak = reward.streakAfter
            if (streak != null && streak > 0) {
                Text(
                    text = stringResource(id = R.string.retention_streak_days, streak),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkText,
                    modifier = Modifier.testTag("reward_streak"),
                )
            }
        }
    }
}

/** Una missione avanzata: titolo, avanzamento e, se completata, il pulsante per riscuotere la ricompensa (un tocco). */
@Composable
private fun MissionRow(advance: MissionAdvance, claimed: Boolean, onClaim: (DailyMission) -> Unit) {
    val mission = advance.mission
    Row(
        modifier = Modifier.fillMaxWidth().testTag("reward_mission_${mission.type.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(id = mission.type.titleRes), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = InkText)
            androidx.compose.material3.LinearProgressIndicator(
                progress = { mission.progressFraction },
                modifier = Modifier.fillMaxWidth().padding(top = 3.dp).height(6.dp),
                color = Color(0xFF2E7D32),
                trackColor = InkText.copy(alpha = 0.15f),
            )
        }
        if (mission.isCompleted && !mission.isClaimed || claimed) {
            MaestroPillButton(
                text = stringResource(id = if (claimed) R.string.retention_missions_claimed else R.string.retention_missions_claim, mission.type.rewardCredits),
                onClick = { if (!claimed) onClaim(mission) },
                variant = if (claimed) PillButtonVariant.WOOD else PillButtonVariant.PRIMARY_GREEN,
                height = 46.dp,
                fontSize = 14.sp,
                testTag = "reward_claim_${mission.type.name.lowercase()}",
            )
        }
    }
}

/** Bollino della review (asset F1-13) con il numero di mosse di quel tipo. */
@Composable
private fun GradeStat(@DrawableRes badge: Int, count: Int, @StringRes label: Int, tag: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Image(painter = painterResource(badge), contentDescription = null, modifier = Modifier.size(46.dp))
        }
        Text(text = count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = InkText, modifier = Modifier.testTag(tag))
        Text(text = stringResource(id = label), style = MaterialTheme.typography.labelSmall, color = InkText, textAlign = TextAlign.Center)
    }
}
