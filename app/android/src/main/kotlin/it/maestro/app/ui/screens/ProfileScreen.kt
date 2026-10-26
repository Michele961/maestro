package it.maestro.app.ui.screens

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
import androidx.compose.foundation.verticalScroll
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
import it.maestro.app.ui.components.AbilityRadarChart
import it.maestro.app.ui.components.BubbleTail
import it.maestro.app.ui.components.ClubBackground
import it.maestro.app.ui.components.FramedPortrait
import it.maestro.app.ui.components.MaestroPillButton
import it.maestro.app.ui.components.MaestroSpeechBubble
import it.maestro.app.ui.components.NineSliceBox
import it.maestro.app.ui.components.ParchmentPanel
import it.maestro.app.ui.components.PillButtonVariant
import it.maestro.app.ui.components.WoodIconButton
import it.maestro.app.ui.components.WoodPanel
import it.maestro.app.ui.theme.BrassRivetGold
import it.maestro.app.ui.theme.MaestroDisplayFont
import it.maestro.app.ui.theme.WoodSurfaceDark
import org.koin.androidx.compose.koinViewModel

private val CreamText = Color(0xFFFFF4D6)

/**
 * Schermata Profilo Utente con Indice di Maestria (Elo Cognitivo) e Radar delle Abilità a 5 assi (task F2-04).
 */
@Composable
public fun ProfileScreen(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(
        uiState = uiState,
        onBackClicked = onBackClicked,
        modifier = modifier,
    )
}

@Composable
public fun ProfileContent(
    uiState: ProfileUiState,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
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

            // Contenuto scrollabile del profilo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // 1. Scheda Giocatore con Avatar, Nome e Livello di Maestria
                PlayerIdentityCard(
                    rankTitleRes = uiState.rankTitleRes,
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

                Spacer(modifier = Modifier.height(12.dp))
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
private fun PlayerIdentityCard(rankTitleRes: Int) {
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
                    text = stringResource(R.string.home_player_name),
                    color = BrassRivetGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MaestroDisplayFont,
                )
                Spacer(modifier = Modifier.height(4.dp))
                MaestroPillButton(
                    text = stringResource(rankTitleRes).uppercase(),
                    onClick = {},
                    variant = PillButtonVariant.EDUCATIONAL_BLUE,
                    height = 28.dp,
                    fontSize = 11.sp,
                    modifier = Modifier.testTag("profile_rank_badge"),
                )
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
