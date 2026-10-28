package it.maestro.app.feature.profile.ui

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
import it.maestro.game.training.TrainingFundamental
import it.maestro.app.feature.home.ui.PlayerNameDialog
import it.maestro.game.scopa.MatchLog
import it.maestro.app.designsystem.components.ClubBackground
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import org.koin.androidx.compose.koinViewModel
import it.maestro.app.feature.profile.presentation.ProfileTab
import it.maestro.app.feature.profile.presentation.ProfileUiState
import it.maestro.app.feature.profile.presentation.ProfileViewModel

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


