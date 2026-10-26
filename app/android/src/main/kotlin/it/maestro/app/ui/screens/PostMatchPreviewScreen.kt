package it.maestro.app.ui.screens

import it.maestro.game.scopa.replay
import it.maestro.game.scopa.ReplayResult
import it.maestro.app.ui.components.PillButtonVariant
import it.maestro.app.ui.components.ParchmentPanel
import it.maestro.app.ui.components.NineSliceBox
import it.maestro.app.ui.components.MaestroPillButton
import it.maestro.app.ui.components.MaestroDialogueBox
import it.maestro.app.ui.components.FramedPortrait
import it.maestro.app.ui.components.ClubBackground
import it.maestro.app.ui.components.ConfettiCelebration
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.Image
import androidx.annotation.StringRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MatchAnalyzer
import it.maestro.ai.analysis.MoveGrade
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.R
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val CardBgColor = Color(0xFF1E293B)
private val GoldAccent = Color(0xFFE9C46A)
private val BlunderColor = Color(0xFFE76F51)
private val InaccuracyColor = Color(0xFFF4A261)
private val BestColor = Color(0xFF2A9D8F)

/**
 * Schermata di anteprima post-partita con MatchAnalyzer in background (ADR-0005, task F1-09).
 * Calcola e visualizza la precisione % e il conteggio degli errori critici del giocatore.
 * Tutti i testi provengono rigorosamente dalle risorse stringhe (ADR-0007).
 */
@Composable
public fun PostMatchPreviewScreen(
    matchLog: MatchLog,
    level: DifficultyLevel,
    modifier: Modifier = Modifier,
    humanSeat: Seat = Seat.P1,
    trainingScore: Pair<Int, Int>? = null,
    trainingFundamental: TrainingFundamental = TrainingFundamental.NONE,
    analyzer: MatchAnalyzer = remember { MatchAnalyzer() },
    ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
    onReviewClicked: (MatchAnalysis) -> Unit = {},
    onRematchClicked: () -> Unit = {},
    onHomeClicked: () -> Unit = {},
    onProfileClicked: () -> Unit = {},
    onMatchAnalyzed: ((MatchAnalysis) -> Unit)? = null,
) {
    var isAnalyzing by remember { mutableStateOf(true) }
    var analysisResult by remember { mutableStateOf<MatchAnalysis?>(null) }
    var hasNotifiedAnalysis by remember(matchLog) { mutableStateOf(false) }

    LaunchedEffect(matchLog) {
        if (!hasNotifiedAnalysis) {
            isAnalyzing = true
            val analysis = withContext(ioDispatcher) {
                analyzer.analyze(matchLog)
            }
            analysisResult = analysis
            isAnalyzing = false
            hasNotifiedAnalysis = true
            onMatchAnalyzed?.invoke(analysis)
        }
    }

    // Punteggio finale dal replay della partita (ADR-0003)
    val finalState = remember(matchLog) { (replay(matchLog) as? ReplayResult.Ok)?.state }
    val opponentSeat = humanSeat.other()
    val humanTotal = finalState?.totals?.get(humanSeat) ?: 0
    val opponentTotal = finalState?.totals?.get(opponentSeat) ?: 0
    val won = finalState?.winner?.let { it == humanSeat } ?: (humanTotal >= opponentTotal)

    ClubBackground(modifier = modifier.testTag("post_match_preview_screen"), feltFraction = 0.55f) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            // Targa del risultato, come "VITTORIA! TU 11 - 8 AVVERSARIO"
            NineSliceBox(
                skin = R.drawable.skin_score_plaque,
                slice = 0.4f,
                corner = 24.dp,
                modifier = Modifier.fillMaxWidth(0.92f).testTag("match_result_plaque"),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(id = if (won) R.string.post_match_victory else R.string.post_match_defeat).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = CreamText,
                    )
                    Text(
                        text = stringResource(
                            id = R.string.post_match_final_score,
                            humanTotal,
                            opponentTotal,
                        ).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = CreamText,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isAnalyzing || analysisResult == null) {
                ParchmentPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator(
                            color = GoldAccent,
                            modifier = Modifier.testTag("analysis_loading_indicator"),
                        )
                        Text(
                            text = stringResource(id = R.string.post_match_analyzing),
                            style = MaterialTheme.typography.bodyLarge,
                            color = InkText,
                        )
                    }
                }
            } else {
                val analysis = analysisResult!!
                ParchmentPanel(modifier = Modifier.fillMaxWidth().testTag("player_summary_card")) {
                    // Precisione in targhetta, come "PRECISIONE 88% • 2 ERRORI CHIAVE"
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
                                analysis.accuracy(humanSeat),
                                analysis.count(humanSeat, MoveGrade.BLUNDER),
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
                        GradeStat(R.drawable.badge_best, analysis.count(humanSeat, MoveGrade.BEST), R.string.post_match_best_moves_label, "player_best_moves")
                        GradeStat(R.drawable.badge_warning, analysis.count(humanSeat, MoveGrade.INACCURACY), R.string.post_match_inaccuracies_label, "player_inaccuracies")
                        GradeStat(R.drawable.badge_wrong, analysis.count(humanSeat, MoveGrade.BLUNDER), R.string.post_match_blunders_label, "player_blunders")
                    }
                    Text(
                        text = stringResource(
                            id = R.string.post_match_opponent_line,
                            analysis.accuracy(opponentSeat),
                            analysis.count(opponentSeat, MoveGrade.BLUNDER),
                        ),
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

                Spacer(modifier = Modifier.height(8.dp))

                // Il Maestro commenta la partita
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    FramedPortrait(
                        portrait = if (won) R.drawable.maestro_triumphant else R.drawable.maestro_encouraging,
                        size = 68.dp,
                        background = Color(0xFF7D3F19),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    MaestroDialogueBox(
                        title = stringResource(id = R.string.game_player_maestro),
                        message = stringResource(
                            id = if (analysis.count(humanSeat, MoveGrade.BLUNDER) == 0) {
                                R.string.post_match_maestro_clean
                            } else {
                                R.string.post_match_maestro_review
                            },
                        ),
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                MaestroPillButton(
                    text = stringResource(id = R.string.post_match_review_button).uppercase(),
                    onClick = { analysisResult?.let { onReviewClicked(it) } },
                    variant = PillButtonVariant.EDUCATIONAL_BLUE,
                    height = 50.dp,
                    fontSize = 20.sp,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "post_match_review_button",
                )
                MaestroPillButton(
                    text = stringResource(id = R.string.post_match_rematch).uppercase(),
                    onClick = onRematchClicked,
                    variant = PillButtonVariant.PRIMARY_GREEN,
                    height = 58.dp,
                    fontSize = 24.sp,
                    modifier = Modifier.fillMaxWidth(0.85f).padding(top = 8.dp),
                    testTag = "post_match_rematch_button",
                )
                MaestroPillButton(
                    text = stringResource(id = R.string.post_match_view_profile).uppercase(),
                    onClick = onProfileClicked,
                    variant = PillButtonVariant.WOOD,
                    height = 44.dp,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(0.85f).padding(top = 6.dp),
                    testTag = "post_match_profile_button",
                )
                MaestroPillButton(
                    text = stringResource(id = R.string.post_match_back_to_menu).uppercase(),
                    onClick = onHomeClicked,
                    variant = PillButtonVariant.SECONDARY_AMBER,
                    height = 46.dp,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth(0.85f).padding(top = 6.dp),
                    testTag = "post_match_home_button",
                )
            }
            if (won) {
                ConfettiCelebration()
            }
        }
    }
}
}

private val CreamText = Color(0xFFFFF4D6)
private val InkText = Color(0xFF2B1A10)

/** Bollino della review (asset F1-13) con il numero di mosse di quel tipo. */
@Composable
private fun GradeStat(@DrawableRes badge: Int, count: Int, @StringRes label: Int, tag: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Image(painter = painterResource(badge), contentDescription = null, modifier = Modifier.size(46.dp))
        }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = InkText,
            modifier = Modifier.testTag(tag),
        )
        Text(
            text = stringResource(id = label),
            style = MaterialTheme.typography.labelSmall,
            color = InkText,
            textAlign = TextAlign.Center,
        )
    }
}
