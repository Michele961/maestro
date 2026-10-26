package it.maestro.app.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import it.maestro.app.R
import it.maestro.app.data.local.missions.DailyMission
import it.maestro.app.ui.components.BubbleTail
import it.maestro.app.ui.components.FramedPortrait
import it.maestro.app.ui.components.MaestroSpeechBubble
import it.maestro.app.ui.components.NineSliceBox
import it.maestro.app.ui.components.ParchmentPanel
import it.maestro.app.ui.components.WoodPanel
import it.maestro.app.ui.screens.DailyMissionsUiState
import it.maestro.app.ui.theme.BrassRivetGold
import it.maestro.app.ui.theme.ButtonGoldBorder
import it.maestro.app.ui.theme.MaestroDisplayFont
import it.maestro.app.ui.theme.WoodSurfaceDark

private val CompletedGreen = Color(0xFF2E7D32)

/**
 * Dialog/Pannello delle Missioni Didattiche quotidiane e della Streak ("Fiamma") (task F2-05).
 */
@Composable
public fun DailyMissionsDialog(
    uiState: DailyMissionsUiState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 660.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("daily_missions_dialog"),
            color = Color.Transparent,
        ) {
            WoodPanel(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Header con Titolo e tasto di chiusura
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.retention_missions_title).uppercase(),
                            color = BrassRivetGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MaestroDisplayFont,
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                        )
                        Image(
                            painter = painterResource(R.drawable.button_close),
                            contentDescription = stringResource(R.string.pre_game_back_button),
                            modifier = Modifier
                                .size(34.dp)
                                .clickable(onClick = onDismissRequest)
                                .testTag("missions_close_button"),
                        )
                    }

                    // 1. Fiamma della Streak e Gettone Riposo
                    StreakBanner(
                        currentStreak = uiState.streak.currentStreak,
                        bestStreak = uiState.streak.bestStreak,
                        freezeTokens = uiState.streak.freezeTokens,
                    )

                    // 2. Le 3 Missioni Didattiche Giornaliere
                    uiState.missionsState.missions.forEachIndexed { index, mission ->
                        MissionItemCard(
                            mission = mission,
                            modifier = Modifier.testTag("daily_mission_item_$index"),
                        )
                    }

                    // 3. Commento Pedagogico del Maestro
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FramedPortrait(
                            portrait = R.drawable.maestro_pleased,
                            size = 50.dp,
                            contentDescription = stringResource(R.string.maestro_avatar_description),
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            MaestroSpeechBubble(
                                message = stringResource(R.string.retention_maestro_encouragement),
                                tail = BubbleTail.START,
                                fontSize = 11.sp,
                                maxLines = 4,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakBanner(
    currentStreak: Int,
    bestStreak: Int,
    freezeTokens: Int,
) {
    NineSliceBox(
        skin = R.drawable.skin_score_plaque,
        slice = 0.4f,
        corner = 16.dp,
        modifier = Modifier.fillMaxWidth().testTag("missions_streak_banner"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_flame),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.retention_streak_title).uppercase(),
                        color = BrassRivetGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaestroDisplayFont,
                        letterSpacing = 0.5.sp,
                    )
                    Text(
                        text = stringResource(R.string.retention_streak_days, currentStreak),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaestroDisplayFont,
                        modifier = Modifier.testTag("missions_streak_text"),
                    )
                    if (bestStreak > 0) {
                        Text(
                            text = stringResource(R.string.retention_streak_record, bestStreak),
                            color = BrassRivetGold.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF24150B))
                    .border(1.dp, ButtonGoldBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon_freeze),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(
                                if (freezeTokens > 0) {
                                    R.string.retention_streak_freeze_available
                                } else {
                                    R.string.retention_streak_freeze_none
                                },
                            ),
                            color = BrassRivetGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("missions_freeze_text"),
                        )
                        Text(
                            text = stringResource(
                                if (freezeTokens > 0) {
                                    R.string.retention_streak_freeze_desc
                                } else {
                                    R.string.retention_streak_freeze_none_desc
                                },
                            ),
                            color = Color(0xFFC7B198),
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionItemCard(
    mission: DailyMission,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF26150B),
        border = BorderStroke(1.dp, if (mission.isCompleted) Color(0xFF4CAF50).copy(alpha = 0.6f) else ButtonGoldBorder.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = stringResource(mission.type.titleRes).uppercase(),
                        color = BrassRivetGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaestroDisplayFont,
                    )
                    Text(
                        text = stringResource(mission.type.descriptionRes),
                        color = Color(0xFFE2D6C6),
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                    )
                }

                if (mission.isCompleted) {
                    Image(
                        painter = painterResource(R.drawable.badge_correct),
                        contentDescription = stringResource(R.string.retention_missions_completed_badge),
                        modifier = Modifier.size(28.dp),
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF3E2313))
                            .border(1.dp, ButtonGoldBorder.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.icon_coin),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(R.string.retention_missions_reward, mission.type.rewardCredits),
                            color = BrassRivetGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // Barra di avanzamento
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LinearProgressIndicator(
                    progress = { mission.progressFraction },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (mission.isCompleted) CompletedGreen else Color(0xFFD4AF37),
                    trackColor = Color(0xFF150A05),
                )
                Text(
                    text = "${mission.progress} / ${mission.target}",
                    color = if (mission.isCompleted) Color(0xFF81C784) else Color(0xFFC7B198),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
