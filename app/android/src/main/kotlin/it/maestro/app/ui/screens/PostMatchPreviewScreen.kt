package it.maestro.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
    analyzer: MatchAnalyzer = remember { MatchAnalyzer() },
    ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
    onReviewClicked: (MatchAnalysis) -> Unit = {},
    onRematchClicked: () -> Unit = {},
    onHomeClicked: () -> Unit = {},
) {
    var isAnalyzing by remember { mutableStateOf(true) }
    var analysisResult by remember { mutableStateOf<MatchAnalysis?>(null) }

    LaunchedEffect(matchLog) {
        isAnalyzing = true
        val analysis = withContext(ioDispatcher) {
            analyzer.analyze(matchLog)
        }
        analysisResult = analysis
        isAnalyzing = false
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("post_match_preview_screen"),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.post_match_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.post_match_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isAnalyzing || analysisResult == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
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
                            color = Color.White,
                        )
                    }
                }
            } else {
                val analysis = analysisResult!!
                val opponentSeat = humanSeat.other()

                // Sezione Prestazione Giocatore
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GoldAccent, RoundedCornerShape(12.dp))
                        .testTag("player_summary_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBgColor),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.game_player_you),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(id = R.string.post_match_accuracy_label),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "${analysis.accuracy(humanSeat)}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldAccent,
                                modifier = Modifier.testTag("player_accuracy"),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(id = R.string.post_match_blunders_label),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "${analysis.count(humanSeat, MoveGrade.BLUNDER)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = BlunderColor,
                                modifier = Modifier.testTag("player_blunders"),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(id = R.string.post_match_inaccuracies_label),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "${analysis.count(humanSeat, MoveGrade.INACCURACY)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = InaccuracyColor,
                                modifier = Modifier.testTag("player_inaccuracies"),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(id = R.string.post_match_best_moves_label),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "${analysis.count(humanSeat, MoveGrade.BEST)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = BestColor,
                                modifier = Modifier.testTag("player_best_moves"),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sezione Prestazione Avversario (Maestro)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("opponent_summary_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBgColor),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.game_player_maestro),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(id = R.string.post_match_accuracy_label),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "${analysis.accuracy(opponentSeat)}%",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.testTag("opponent_accuracy"),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(id = R.string.post_match_blunders_label),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "${analysis.count(opponentSeat, MoveGrade.BLUNDER)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray,
                                modifier = Modifier.testTag("opponent_blunders"),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Pulsanti di navigazione
                Button(
                    onClick = { analysisResult?.let { onReviewClicked(it) } },
                    enabled = analysisResult != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("post_match_review_button"),
                ) {
                    Text(
                        text = stringResource(id = R.string.post_match_review_button),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onRematchClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("post_match_rematch_button"),
                ) {
                    Text(
                        text = stringResource(id = R.string.game_rematch_button),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onHomeClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("post_match_home_button"),
                ) {
                    Text(
                        text = stringResource(id = R.string.game_menu_home),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
