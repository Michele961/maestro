package it.maestro.app.feature.review.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MoveGrade
import it.maestro.app.R
import it.maestro.app.designsystem.components.ClubBackground
import it.maestro.app.designsystem.components.ConfettiCelebration
import it.maestro.app.designsystem.components.FramedPortrait
import it.maestro.app.designsystem.components.MaestroDialogueBox
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.domain.retention.DailyMission
import it.maestro.app.feature.review.presentation.PostMatchStep
import it.maestro.app.feature.review.presentation.PostMatchUiState

/**
 * Fine partita (task F1R-15b, `docs/app/features/post-match.md` §3): finché l'analisi non è pronta c'è solo il
 * caricamento del Maestro; poi targa, precisione, ricompensa e azioni compaiono insieme. Disegna lo stato di
 * [PostMatchUiState] e non decide nulla.
 */
@Composable
public fun PostMatchScreen(
    state: PostMatchUiState,
    modifier: Modifier = Modifier,
    trainingScore: Pair<Int, Int>? = null,
    onClaimMission: (DailyMission) -> Unit = {},
    onReviewClicked: (MatchAnalysis) -> Unit = {},
    onRematchClicked: () -> Unit = {},
    onHomeClicked: () -> Unit = {},
) {
    ClubBackground(modifier = modifier.testTag("post_match_preview_screen"), feltFraction = 0.55f) {
        Box(modifier = Modifier.fillMaxSize()) {
            val analysis = state.analysis
            if (state.step == PostMatchStep.LOADING || analysis == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) { MaestroLine(state, analysis = null) }
                return@Box
            }
            AnimatedVisibility(visible = true, enter = fadeIn(), modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ResultPlaque(state.won, state.humanTotal, state.opponentTotal)
                    Spacer(modifier = Modifier.height(10.dp))
                    // Colonna che scorre solo se i contenuti non stanno nello schermo (azioni comprese): niente è coperto.
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            AccuracyCard(analysis, state.humanSeat, trainingScore)
                            val reward = state.reward
                            if (reward != null && !state.fromHistory && reward.hasContent()) {
                                RewardCard(reward, state.claimedMissions, onClaimMission)
                            }
                            MaestroLine(state, analysis)
                            Actions(state, analysis, onReviewClicked, onRematchClicked, onHomeClicked)
                        }
                    }
                }
            }
            if (state.won && !state.fromHistory) ConfettiCelebration()
        }
    }
}

/** Il Maestro segna i momenti chiave mentre l'analisi non è pronta, poi commenta la partita. */
@Composable
private fun MaestroLine(state: PostMatchUiState, analysis: MatchAnalysis?) {
    val waiting = analysis == null
    val blunders = analysis?.count(state.humanSeat, MoveGrade.BLUNDER) ?: 0
    val portrait = when {
        waiting -> R.drawable.maestro_thinking
        state.won -> R.drawable.maestro_triumphant
        else -> R.drawable.maestro_encouraging
    }
    val message = when {
        waiting -> R.string.post_match_analyzing
        blunders == 0 -> R.string.post_match_maestro_clean
        else -> R.string.post_match_maestro_review
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().testTag(if (waiting) "analysis_loading_indicator" else "post_match_maestro_line")) {
        FramedPortrait(portrait = portrait, size = 68.dp, background = Color(0xFF7D3F19))
        Spacer(modifier = Modifier.width(8.dp))
        MaestroDialogueBox(title = stringResource(id = R.string.game_player_maestro), message = stringResource(id = message), modifier = Modifier.weight(1f))
    }
}

/** Rivedi partita (primaria), Rivincita, Menu. */
@Composable
private fun Actions(state: PostMatchUiState, analysis: MatchAnalysis, onReview: (MatchAnalysis) -> Unit, onRematch: () -> Unit, onHome: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        MaestroPillButton(
            text = stringResource(id = R.string.post_match_review_button).uppercase(),
            onClick = { onReview(analysis) },
            variant = PillButtonVariant.PRIMARY_GREEN,
            height = 58.dp,
            fontSize = 22.sp,
            modifier = Modifier.fillMaxWidth(),
            testTag = "post_match_review_button",
        )
        MaestroPillButton(
            text = stringResource(id = R.string.post_match_rematch).uppercase(),
            onClick = onRematch,
            variant = PillButtonVariant.SECONDARY_AMBER,
            height = 46.dp,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth(0.85f).padding(top = 8.dp),
            testTag = "post_match_rematch_button",
        )
        MaestroPillButton(
            text = stringResource(id = if (state.fromHistory) R.string.post_match_back_to_history else R.string.post_match_back_to_menu).uppercase(),
            onClick = onHome,
            variant = PillButtonVariant.WOOD,
            height = 40.dp,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth(0.85f).padding(top = 6.dp),
            testTag = "post_match_home_button",
        )
    }
}

/** `true` se la ricompensa ha qualcosa da mostrare: altrimenti non si disegna un riquadro vuoto. */
private fun it.maestro.app.domain.rewards.RewardSummary.hasContent(): Boolean =
    missions.isNotEmpty() || masteryDelta != null || (streakAfter ?: 0) > 0
