package it.maestro.app.feature.missions.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.maestro.app.R
import it.maestro.app.designsystem.components.WoodPanel
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.ButtonGoldBorder
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import it.maestro.app.designsystem.theme.WoodSurfaceDark
import it.maestro.app.domain.progress.Experience
import it.maestro.app.domain.retention.DailyMission
import it.maestro.app.feature.missions.presentation.DailyMissionsUiState
import it.maestro.app.feature.missions.presentation.DailyMissionsViewModel
import it.maestro.app.feature.missions.presentation.ProgressUiState
import it.maestro.app.feature.missions.presentation.ProgressViewModel
import org.koin.androidx.compose.koinViewModel

/** Obiettivi mostrati nella schermata: gli altri stanno nel Profilo (F1R-26). */
private const val ACHIEVEMENTS_SHOWN = 5

/**
 * Schermata «Oggi» (F1R-29f): le tre missioni del giorno, la serie con i Gettoni Riposo, l'esperienza
 * con il suo livello e gli obiettivi sbloccati. Sostituisce il dialogo delle missioni della Home.
 */
@Composable
public fun TodayScreen(
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
    missionsViewModel: DailyMissionsViewModel = koinViewModel(),
    progressViewModel: ProgressViewModel = koinViewModel(),
) {
    val missions by missionsViewModel.uiState.collectAsStateWithLifecycle()
    val progress by progressViewModel.uiState.collectAsStateWithLifecycle()
    TodayScreenContent(
        missions = missions,
        progress = progress,
        onClaimReward = missionsViewModel::claimReward,
        onNavigateHome = onNavigateHome,
        modifier = modifier,
    )
}

/** Versione senza stato di [TodayScreen], usata anche dai test di screenshot. */
@Composable
public fun TodayScreenContent(
    missions: DailyMissionsUiState,
    progress: ProgressUiState,
    onClaimReward: (DailyMission) -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(WoodSurfaceDark).testTag("today_screen")) {
        Image(
            painter = painterResource(R.drawable.texture_wood),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
        ) {
            TodayHeader(onNavigateHome)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StreakBanner(
                    currentStreak = missions.streak.currentStreak,
                    bestStreak = missions.streak.bestStreak,
                    freezeTokens = missions.streak.freezeTokens,
                )
                missions.missionsState.missions.forEachIndexed { index, mission ->
                    MissionItemCard(
                        mission = mission,
                        onClaimReward = onClaimReward,
                        modifier = Modifier.testTag("daily_mission_item_$index"),
                    )
                }
                ExperienceCard(progress)
                AchievementsCard(progress)
            }
        }
    }
}

@Composable
private fun TodayHeader(onNavigateHome: () -> Unit) {
    WoodPanel(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.button_close),
                contentDescription = stringResource(R.string.pre_game_back_button),
                modifier = Modifier.size(34.dp).clickable(onClick = onNavigateHome).testTag("today_back_button"),
            )
            Text(
                text = stringResource(R.string.today_title).uppercase(),
                color = BrassRivetGold,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MaestroDisplayFont,
            )
        }
    }
}

@Composable
private fun TodayCard(title: String, tag: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF26150B))
            .border(1.dp, ButtonGoldBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(tag),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title.uppercase(),
            color = BrassRivetGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MaestroDisplayFont,
        )
        content()
    }
}

@Composable
private fun ExperienceCard(progress: ProgressUiState) {
    TodayCard(stringResource(R.string.today_experience_title), "today_experience_card") {
        Text(
            text = stringResource(
                R.string.today_level,
                progress.level,
                stringResource(experienceLevelNameRes(progress.level)),
            ),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("today_level_text"),
        )
        if (progress.isMaxLevel) {
            Text(
                text = stringResource(R.string.today_level_max),
                color = Color(0xFFC7B198),
                fontSize = 11.sp,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { progress.pointsIntoLevel.toFloat() / Experience.PX_PER_LEVEL },
                    modifier = Modifier.weight(1f).size(width = 0.dp, height = 6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFD4AF37),
                    trackColor = Color(0xFF150A05),
                )
                Text(
                    text = stringResource(R.string.today_xp_progress, progress.pointsIntoLevel, Experience.PX_PER_LEVEL),
                    color = Color(0xFFC7B198),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun AchievementsCard(progress: ProgressUiState) {
    TodayCard(stringResource(R.string.today_achievements_title), "today_achievements_card") {
        val shown = progress.unlockedInOrder.takeLast(ACHIEVEMENTS_SHOWN)
        if (shown.isEmpty()) {
            Text(
                text = stringResource(R.string.today_achievements_empty),
                color = Color(0xFFE2D6C6),
                fontSize = 11.sp,
                lineHeight = 14.sp,
            )
        } else {
            shown.forEach { id ->
                Column(modifier = Modifier.testTag("today_achievement_${id.id}")) {
                    Text(
                        text = stringResource(id.nameRes),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(id.descriptionRes),
                        color = Color(0xFFC7B198),
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                    )
                }
            }
        }
    }
}
