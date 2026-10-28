package it.maestro.app.feature.profile.ui

import it.maestro.app.domain.mastery.MatchHistoryEntry
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.profile.MasteryTrend
import it.maestro.app.domain.retention.StreakState
import it.maestro.app.feature.profile.presentation.RadarAxis
import it.maestro.game.training.TrainingFundamental
import it.maestro.app.feature.home.ui.PlayerNameDialog
import it.maestro.game.scopa.MatchLog
import it.maestro.app.feature.profile.ui.AbilityRadarChart
import it.maestro.app.designsystem.components.BubbleTail
import it.maestro.app.designsystem.components.ClubBackground
import it.maestro.app.designsystem.components.FramedPortrait
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.MaestroSpeechBubble
import it.maestro.app.designsystem.components.NineSliceBox
import it.maestro.app.designsystem.components.ParchmentPanel
import it.maestro.app.designsystem.components.WoodPanel
import it.maestro.app.designsystem.theme.BrassRivetGold
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
    onPlayerNameChanged: (String) -> Unit = {},
    playerNameMaxLength: Int = 20,
    onTrain: (TrainingFundamental) -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(
        uiState = uiState,
        onBackClicked = onBackClicked,
        onSelectTab = { viewModel.selectTab(it) },
        onReviewMatch = onReviewMatch,
        playerName = playerName,
        onSelectTitle = viewModel::selectTitle,
        onPlayerNameChanged = onPlayerNameChanged,
        playerNameMaxLength = playerNameMaxLength,
        onTrain = onTrain,
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
    onSelectTitle: (Opponent) -> Unit = {},
    onPlayerNameChanged: (String) -> Unit = {},
    playerNameMaxLength: Int = 20,
    onTrain: (TrainingFundamental) -> Unit = {},
) {
    val profile = uiState.profile
    var editingName by remember { mutableStateOf(false) }
    if (editingName) {
        PlayerNameDialog(
            currentName = playerName,
            maxLength = playerNameMaxLength,
            onConfirm = { editingName = false; onPlayerNameChanged(it) },
            onDismiss = { editingName = false },
        )
    }

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
                        playerName = playerName,
                        currentTitle = uiState.currentTitle,
                        earnedTitles = uiState.earnedTitles,
                        onSelectTitle = onSelectTitle,
                        onEditName = { editingName = true },
                    )

                    // 2. Indice di Maestria (Elo Cognitivo)
                    MasteryIndexCard(
                        cognitiveElo = profile.cognitiveElo,
                        accuracyPercentage = profile.accuracyPercentage,
                        rankTitleRes = uiState.rankTitleRes,
                        trend = uiState.trend,
                    )

                    // 3. Radar delle Abilità a 5 assi
                    RadarCard(radar = profile.radar, weakest = uiState.weakestAxis)

                    // 4. Consiglio Socratico del Maestro
                    MaestroAdviceCard(
                        adviceRes = uiState.maestroAdviceRes,
                        trainObjective = uiState.trainObjective,
                        onTrain = onTrain,
                    )

                    // 5. La scala degli avversari (F1R-26c)
                    LadderStrip(ladder = uiState.ladder)

                    // 6. Statistiche complessive
                    PlayerStatsCard(
                        matchesPlayed = profile.matchesPlayed,
                        recent = uiState.matchHistory,
                        streak = uiState.streak,
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
private fun MasteryIndexCard(
    cognitiveElo: Int,
    accuracyPercentage: Int,
    rankTitleRes: Int,
    trend: MasteryTrend,
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
                text = stringResource(rankTitleRes).uppercase() + " · " + stringResource(trend.labelRes),
                color = CreamText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("profile_mastery_trend"),
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
private fun RadarCard(radar: it.maestro.ai.mastery.AbilityRadar, weakest: RadarAxis?) {
    var opened by remember { mutableStateOf<RadarAxis?>(null) }
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
                highlighted = weakest,
                onAxisClick = { axis -> opened = if (opened == axis) null else axis },
            )
            opened?.let { axis ->
                Text(
                    text = stringResource(axis.explanationRes),
                    color = Color(0xFFE2D6C6),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).testTag("profile_radar_explanation"),
                )
            }
        }
    }
}

@Composable
private fun MaestroAdviceCard(adviceRes: Int, trainObjective: TrainingFundamental?, onTrain: (TrainingFundamental) -> Unit) {
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
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MaestroSpeechBubble(
                    message = stringResource(adviceRes),
                    tail = BubbleTail.START,
                    fontSize = 12.sp,
                    maxLines = 4,
                )
                if (trainObjective != null) {
                    MaestroPillButton(
                        text = stringResource(R.string.profile_train_button),
                        onClick = { onTrain(trainObjective) },
                        testTag = "profile_train_button",
                        height = 40.dp,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerStatsCard(
    matchesPlayed: Int,
    recent: List<MatchHistoryEntry>,
    streak: StreakState,
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
            val stats = listOf(
                stringResource(R.string.profile_matches_played, matchesPlayed),
                stringResource(
                    R.string.profile_recent_record,
                    recent.size,
                    recent.count { it.isWin },
                    recent.count { !it.isWin },
                ),
                stringResource(R.string.profile_streak_days, streak.currentStreak, streak.bestStreak),
            )
            stats.forEach { line ->
                Text(text = line, color = WoodSurfaceDark, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private val MasteryTrend.labelRes: Int
    get() = when (this) {
        MasteryTrend.UP -> R.string.profile_trend_up
        MasteryTrend.FLAT -> R.string.profile_trend_flat
        MasteryTrend.DOWN -> R.string.profile_trend_down
    }

private val RadarAxis.explanationRes: Int
    get() = when (this) {
        RadarAxis.SEVENS -> R.string.radar_axis_sevens_what
        RadarAxis.DENARI -> R.string.radar_axis_denari_what
        RadarAxis.PRIMIERA -> R.string.radar_axis_primiera_what
        RadarAxis.BOARD -> R.string.radar_axis_board_what
        RadarAxis.ENDGAME -> R.string.radar_axis_endgame_what
    }
