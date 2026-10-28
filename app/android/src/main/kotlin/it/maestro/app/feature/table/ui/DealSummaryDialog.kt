package it.maestro.app.feature.table.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import it.maestro.app.designsystem.haptics.rememberGameHaptics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.maestro.app.R
import it.maestro.app.designsystem.components.MaestroAlertDialog
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.feature.table.presentation.GameUiState
import it.maestro.game.scopa.DealScore
import it.maestro.game.scopa.ScoreCategory

@Composable
internal fun DealSummaryDialog(
    score: DealScore,
    uiState: GameUiState,
    revealedCount: Int,
    onDismiss: () -> Unit,
    onSkip: () -> Unit,
) {
    val humanSeat = uiState.humanSeat
    val oppSeat = humanSeat.other()

    val categories = listOfNotNull(
        ScoreCategory.CARTE to R.string.score_category_carte,
        ScoreCategory.DENARI to R.string.score_category_denari,
        ScoreCategory.SETTEBELLO to R.string.score_category_settebello,
        ScoreCategory.PRIMIERA to R.string.score_category_primiera,
        ScoreCategory.SCOPE to R.string.score_category_scope,
        if (score.pointsBySeatAndCategory.getValue(humanSeat).containsKey(ScoreCategory.RE_BELLO)) {
            ScoreCategory.RE_BELLO to R.string.score_category_re_bello
        } else {
            null
        },
    )

    // Le voci si accendono una alla volta al ritmo della regia (`DealSummaryRow`), non con un timer del dialog.
    val haptic = rememberGameHaptics()
    LaunchedEffect(revealedCount) {
        if (revealedCount > 0) haptic(HapticFeedbackType.TextHandleMove)
    }

    MaestroAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.deal_summary_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSkip,
                    )
                    .testTag("deal_summary_dialog"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Intestazione colonne: Categoria | Tu | Maestro
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(id = R.string.game_score_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = DialogInkSoft,
                        modifier = Modifier.weight(2f),
                    )
                    Text(
                        text = stringResource(id = R.string.game_player_you),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DialogGold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(id = R.string.game_player_maestro),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DialogInk,
                        modifier = Modifier.weight(1f),
                    )
                }

                HorizontalDivider(color = Color.DarkGray)

                categories.forEachIndexed { i, (category, labelResId) ->
                    val isRevealed = revealedCount > i
                    val tag = "deal_summary_${category.name.lowercase()}"
                    val p1Pts = score.points(humanSeat, category)
                    val p2Pts = score.points(oppSeat, category)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = if (isRevealed) 1f else 0.2f
                                translationX = if (isRevealed) 0f else -12f
                            }
                            .testTag(tag),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(id = labelResId),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(2f),
                        )
                        Text(
                            text = "$p1Pts",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isRevealed && p1Pts > 0) DialogGold else DialogInkSoft,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "$p2Pts",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isRevealed && p2Pts > 0) DialogInk else DialogInkSoft,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                HorizontalDivider(color = Color.DarkGray)

                val totalsRevealed = revealedCount >= categories.size
                // Totale smazzata
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = if (totalsRevealed) 1f else 0.3f
                        }
                        .testTag("deal_summary_deal_total"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.deal_summary_deal_total),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2f),
                    )
                    Text(
                        text = "${score.totalPoints(humanSeat)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = DialogGold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${score.totalPoints(oppSeat)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = DialogInk,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Punteggio complessivo partita
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = if (totalsRevealed) 1f else 0.3f
                        }
                        .testTag("deal_summary_match_total"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.deal_summary_match_total),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DialogInkSoft,
                        modifier = Modifier.weight(2f),
                    )
                    Text(
                        text = "${uiState.humanScore}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DialogGold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${uiState.opponentScore}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DialogInk,
                        modifier = Modifier.weight(1f),
                    )
                }

                if (uiState.trainingResults.isNotEmpty()) {
                    val correct = uiState.trainingResults.count { it.isCorrect }
                    val total = uiState.trainingResults.size
                    val pct = (correct * 100) / total
                    HorizontalDivider(color = Color.DarkGray)
                    Text(
                        text = stringResource(id = R.string.training_score_label, pct, correct, total),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DialogGold,
                        modifier = Modifier.testTag("deal_summary_attention_score"),
                    )
                }
            }
        },
        confirmButton = {
            MaestroPillButton(
                text = stringResource(id = R.string.deal_summary_continue),
                onClick = onDismiss,
                variant = PillButtonVariant.PRIMARY_GREEN,
                height = 46.dp,
                testTag = "deal_summary_continue_button",
            )
        },
    )
}
