package it.maestro.app.feature.profile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.maestro.app.R
import it.maestro.app.domain.mastery.MatchHistoryEntry
import it.maestro.game.scopa.MatchLog
import it.maestro.app.feature.profile.ui.AbilityRadarChart
import it.maestro.app.designsystem.components.BubbleTail
import it.maestro.app.designsystem.components.ClubBackground
import it.maestro.app.designsystem.components.FramedPortrait
import it.maestro.app.designsystem.components.MaestroAvatarView
import it.maestro.app.designsystem.components.MaestroMood
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.MaestroSpeechBubble
import it.maestro.app.designsystem.components.NineSliceBox
import it.maestro.app.designsystem.components.ParchmentPanel
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.WoodIconButton
import it.maestro.app.designsystem.components.WoodPanel
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.ButtonGoldBorder
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import it.maestro.app.designsystem.theme.WoodSurfaceDark
import org.koin.androidx.compose.koinViewModel
import it.maestro.app.feature.profile.presentation.ProfileTab
import it.maestro.app.feature.profile.presentation.ProfileUiState
import it.maestro.app.feature.profile.presentation.ProfileViewModel

private val CreamText = Color(0xFFFFF4D6)

/**
 * Schermata Profilo Utente con Indice di Maestria (Elo Cognitivo) e Radar delle Abilità a 5 assi (task F2-04).
 */
@Composable
public fun ProfileScreen(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onReviewMatch: (MatchLog) -> Unit = {},
    playerName: String? = null,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(
        uiState = uiState,
        onBackClicked = onBackClicked,
        onSelectTab = { viewModel.selectTab(it) },
        onReviewMatch = onReviewMatch,
        playerName = playerName,
        modifier = modifier,
    )
}

@Composable
public fun ProfileContent(
    uiState: ProfileUiState,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onSelectTab: (ProfileTab) -> Unit = {},
    onReviewMatch: (MatchLog) -> Unit = {},
    playerName: String? = null,
) {
    val profile = uiState.profile

    ClubBackground(modifier = modifier.testTag("profile_screen"), feltFraction = 0.5f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                ),
        ) {
            // Header con tasto Indietro e Titolo
            ProfileHeader(onBackClicked = onBackClicked)

            // Switcher Tab [ Maestria & Radar ] / [ Cronologia Partite ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .testTag("profile_tabs"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProfileTabChip(
                    title = stringResource(R.string.profile_tab_mastery),
                    isSelected = uiState.selectedTab == ProfileTab.MASTERY,
                    onClick = { onSelectTab(ProfileTab.MASTERY) },
                    modifier = Modifier.weight(1f).testTag("profile_tab_mastery"),
                )
                ProfileTabChip(
                    title = stringResource(R.string.profile_tab_history),
                    isSelected = uiState.selectedTab == ProfileTab.HISTORY,
                    onClick = { onSelectTab(ProfileTab.HISTORY) },
                    modifier = Modifier.weight(1f).testTag("profile_tab_history"),
                )
            }

            // Contenuto scrollabile del profilo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (uiState.selectedTab == ProfileTab.MASTERY) {
                    // 1. Scheda Giocatore con Avatar, Nome e Livello di Maestria
                    PlayerIdentityCard(
                        rankTitleRes = uiState.rankTitleRes,
                        playerName = playerName,
                    )

                    // 2. Indice di Maestria (Elo Cognitivo)
                    MasteryIndexCard(
                        cognitiveElo = profile.cognitiveElo,
                        accuracyPercentage = profile.accuracyPercentage,
                    )

                    // 3. Radar delle Abilità a 5 assi
                    RadarCard(radar = profile.radar)

                    // 4. Consiglio Socratico del Maestro
                    MaestroAdviceCard(adviceRes = uiState.maestroAdviceRes)

                    // 5. Statistiche complessive
                    PlayerStatsCard(
                        matchesPlayed = profile.matchesPlayed,
                        accuracyPercentage = profile.accuracyPercentage,
                    )
                } else {
                    // Cronologia Partite e Impatto ELO
                    MatchHistorySection(
                        entries = uiState.matchHistory,
                        onReviewMatch = onReviewMatch,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileTabChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (isSelected) Color(0xFF3E2313) else Color(0xFF221610)
    val border = if (isSelected) BrassRivetGold else Color(0xFF4E3629)
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = bg,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, border),
    ) {
        Text(
            text = title.uppercase(),
            color = if (isSelected) BrassRivetGold else Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = MaestroDisplayFont,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun MatchHistorySection(
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
                    Text(
                        text = stringResource(R.string.profile_history_vs_bot),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

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

@Composable
private fun ProfileHeader(onBackClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.button_close),
            contentDescription = stringResource(R.string.pre_game_back_button),
            modifier = Modifier
                .size(42.dp)
                .clickable(onClick = onBackClicked)
                .testTag("profile_back_button"),
        )
        Text(
            text = stringResource(R.string.profile_title).uppercase(),
            color = BrassRivetGold,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MaestroDisplayFont,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f).padding(end = 42.dp),
        )
    }
}

@Composable
private fun PlayerIdentityCard(rankTitleRes: Int, playerName: String?) {
    WoodPanel(modifier = Modifier.fillMaxWidth().testTag("profile_identity_card")) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
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
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E3A5F),
                    border = BorderStroke(1.dp, Color(0xFF64B5F6)),
                    modifier = Modifier.testTag("profile_rank_badge"),
                ) {
                    Text(
                        text = stringResource(rankTitleRes).uppercase(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaestroDisplayFont,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MasteryIndexCard(
    cognitiveElo: Int,
    accuracyPercentage: Int,
) {
    NineSliceBox(
        skin = R.drawable.skin_score_plaque,
        slice = 0.4f,
        corner = 20.dp,
        modifier = Modifier.fillMaxWidth().testTag("profile_elo_card"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.profile_elo_badge, cognitiveElo),
                color = CreamText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                fontFamily = MaestroDisplayFont,
                modifier = Modifier.testTag("profile_elo_text"),
            )
            Text(
                text = stringResource(R.string.profile_accuracy_label, accuracyPercentage).uppercase(),
                color = BrassRivetGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MaestroDisplayFont,
                modifier = Modifier.testTag("profile_accuracy_text"),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.profile_elo_explanation),
                color = Color(0xFFD4C8B8),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}

@Composable
private fun RadarCard(radar: it.maestro.ai.mastery.AbilityRadar) {
    WoodPanel(modifier = Modifier.fillMaxWidth().testTag("profile_radar_panel")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.profile_radar_title).uppercase(),
                color = BrassRivetGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MaestroDisplayFont,
            )
            Text(
                text = stringResource(R.string.profile_radar_subtitle),
                color = Color(0xFFC0A080),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            AbilityRadarChart(
                radar = radar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp),
            )
        }
    }
}

@Composable
private fun MaestroAdviceCard(adviceRes: Int) {
    WoodPanel(modifier = Modifier.fillMaxWidth().testTag("profile_maestro_advice_panel")) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FramedPortrait(
                portrait = R.drawable.maestro_pleased,
                size = 64.dp,
                contentDescription = stringResource(R.string.maestro_avatar_description),
            )
            Box(modifier = Modifier.weight(1f)) {
                MaestroSpeechBubble(
                    message = stringResource(adviceRes),
                    tail = BubbleTail.START,
                    fontSize = 12.sp,
                    maxLines = 4,
                )
            }
        }
    }
}

@Composable
private fun PlayerStatsCard(
    matchesPlayed: Int,
    accuracyPercentage: Int,
) {
    ParchmentPanel(modifier = Modifier.fillMaxWidth().testTag("profile_stats_card")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_stats_title).uppercase(),
                color = WoodSurfaceDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MaestroDisplayFont,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                Text(
                    text = stringResource(R.string.profile_matches_played, matchesPlayed),
                    color = WoodSurfaceDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.profile_accuracy_label, accuracyPercentage),
                    color = WoodSurfaceDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
