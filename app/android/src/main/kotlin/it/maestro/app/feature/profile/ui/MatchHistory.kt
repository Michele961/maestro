package it.maestro.app.feature.profile.ui

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.designsystem.components.MaestroAvatarView
import it.maestro.app.designsystem.components.MaestroMood
import it.maestro.app.designsystem.components.ParchmentPanel
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.ButtonGoldBorder
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import it.maestro.app.designsystem.theme.WoodSurfaceDark
import it.maestro.app.domain.mastery.MatchHistoryEntry
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.feature.pregame.ui.objectiveNameRes
import it.maestro.app.feature.pregame.ui.style
import it.maestro.game.scopa.MatchLog
import it.maestro.game.training.TrainingFundamental

/** Tab «Partite» (F1R-26e): una scheda per partita, con avversario, modalità, esito, precisione e «Rivedi analisi». */
@Composable
internal fun MatchHistorySection(
    entries: List<MatchHistoryEntry>,
    onReviewMatch: (MatchLog) -> Unit = {},
) {
    if (entries.isEmpty()) {
        ParchmentPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .testTag("profile_history_empty_card"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MaestroAvatarView(mood = MaestroMood.THINKING, size = 52.dp)
                Text(
                    text = stringResource(R.string.profile_history_empty),
                    color = WoodSurfaceDark,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_history_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            entries.forEachIndexed { index, entry ->
                MatchHistoryCard(
                    entry = entry,
                    onReviewMatch = onReviewMatch,
                    modifier = Modifier.testTag("profile_history_card_$index"),
                )
            }
        }
    }
}

@Composable
private fun MatchHistoryCard(
    entry: MatchHistoryEntry,
    modifier: Modifier = Modifier,
    onReviewMatch: (MatchLog) -> Unit = {},
) {
    val parsedLog = androidx.compose.runtime.remember(entry.matchLogText) {
        if (!entry.matchLogText.isNullOrBlank()) {
            try {
                MatchLog.parse(entry.matchLogText)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    val cardModifier = if (parsedLog != null) {
        modifier
            .fillMaxWidth()
            .clickable { onReviewMatch(parsedLog) }
    } else {
        modifier.fillMaxWidth()
    }

    val opponent = entry.opponent?.let { name -> Opponent.entries.firstOrNull { it.name == name } }
    val opponentName = opponent?.let { stringResource(it.style().name) }
        ?: stringResource(R.string.profile_history_unknown_opponent)
    val objective = entry.sparringObjective
        ?.let { name -> TrainingFundamental.entries.firstOrNull { it.name == name } }
        ?.objectiveNameRes()
    val modeText = if (objective != null) {
        stringResource(R.string.profile_history_sparring, stringResource(objective))
    } else {
        stringResource(R.string.profile_history_classic)
    }

    Surface(
        modifier = cardModifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF24150B),
        border = BorderStroke(1.dp, if (entry.isWin) Color(0xFF4CAF50).copy(alpha = 0.6f) else ButtonGoldBorder.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Risultato e Punteggio
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        color = if (entry.isWin) Color(0xFF1E3A24) else Color(0xFF4A1818),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (entry.isWin) Color(0xFF81C784) else Color(0xFFE57373)),
                    ) {
                        Text(
                            text = stringResource(if (entry.isWin) R.string.profile_history_win else R.string.profile_history_loss).uppercase(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MaestroDisplayFont,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    if (opponent != null) {
                        Image(
                            painter = painterResource(opponent.style().portrait),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(CircleShape),
                        )
                    }
                    Text(
                        text = opponentName,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.testTag("profile_history_opponent"),
                    )
                }
                Text(
                    text = modeText,
                    color = Color(0xFFC7B198),
                    fontSize = 11.sp,
                    modifier = Modifier.testTag("profile_history_mode"),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "${entry.humanScore} - ${entry.botScore}",
                        color = BrassRivetGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaestroDisplayFont,
                    )
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                    )
                    Text(
                        text = stringResource(R.string.profile_history_accuracy, entry.accuracy),
                        color = Color(0xFFE2D6C6),
                        fontSize = 12.sp,
                    )
                }
            }

            // Impatto Elo (+X / -Y) e pulsante Rivedi
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Surface(
                    color = if (entry.eloDelta >= 0) Color(0xFF1B3D22) else Color(0xFF3E1E1E),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (entry.eloDelta >= 0) Color(0xFF4CAF50) else Color(0xFFE57373)),
                ) {
                    Text(
                        text = if (entry.eloDelta >= 0) {
                            stringResource(R.string.profile_history_elo_delta_plus, entry.eloDelta)
                        } else {
                            stringResource(R.string.profile_history_elo_delta_minus, entry.eloDelta)
                        },
                        color = if (entry.eloDelta >= 0) Color(0xFF81C784) else Color(0xFFFF8A80),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaestroDisplayFont,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    )
                }
                if (parsedLog != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.profile_history_review_button),
                            color = BrassRivetGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "→",
                            color = BrassRivetGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
