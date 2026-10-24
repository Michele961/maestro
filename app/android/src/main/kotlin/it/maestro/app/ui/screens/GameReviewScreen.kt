package it.maestro.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MoveAnalysis
import it.maestro.ai.analysis.MoveGrade
import it.maestro.app.R
import it.maestro.app.ui.components.CardBackView
import it.maestro.app.ui.components.PlaceholderCardView
import it.maestro.app.ui.theme.ButtonGoldBorder
import it.maestro.app.ui.theme.GradeErroreGraveRed
import it.maestro.app.ui.theme.GradeImprecisioneYellow
import it.maestro.app.ui.theme.GradeMagistraleBlue
import it.maestro.app.ui.theme.TableFeltGreenDark
import it.maestro.app.ui.theme.WoodSurfaceDark
import it.maestro.app.ui.theme.WoodSurfaceLight
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import it.maestro.game.scopa.viewFor

private val TableSurfaceColor = WoodSurfaceDark
private val BoardFeltColor = TableFeltGreenDark
private val CardAreaBg = WoodSurfaceLight
private val GoldAccent = ButtonGoldBorder

private val BestBadgeColor = GradeMagistraleBlue
private val InaccuracyBadgeColor = GradeImprecisioneYellow
private val BlunderBadgeColor = GradeErroreGraveRed

/**
 * Singolo stato visualizzato durante la Game Review mossa per mossa (F1-10, ADR-0005).
 *
 * Rispetta rigorosamente il criterio di sicurezza:
 * [playerView] contiene esclusivamente le carte note al giocatore [humanSeat] in quel momento.
 * La mano avversaria del momento non è presente in alcuna proprietà, garantendo che
 * non possa essere mostrata a schermo.
 *
 * @property moveIndex Indice della mossa a base 0 nel log.
 * @property mover Giocatore che ha effettuato la mossa ([Seat.P1] o [Seat.P2]).
 * @property playerView Vista del giocatore umano nello stato immediatamente precedente la mossa.
 * @property analysis Analisi dettagliata della mossa prodotta da `MatchAnalyzer`.
 */
public data class ReviewStep(
    val moveIndex: Int,
    val mover: Seat,
    val playerView: ScopaView,
    val analysis: MoveAnalysis,
)

/**
 * Ricostruisce la sequenza di passi per la Game Review a partire da [log] e [analysis].
 */
public fun buildReviewSteps(
    log: MatchLog,
    analysis: MatchAnalysis,
    humanSeat: Seat = Seat.P1,
): List<ReviewStep> {
    var state = newMatch(config = log.config, seed = log.seed)
    val steps = ArrayList<ReviewStep>(log.moves.size)

    for ((index, move) in log.moves.withIndex()) {
        val mover = state.toMove
        val playerView = viewFor(state, humanSeat)
        val moveAnalysis = if (index < analysis.moves.size) {
            analysis.moves[index]
        } else {
            break
        }

        steps.add(
            ReviewStep(
                moveIndex = index,
                mover = mover,
                playerView = playerView,
                analysis = moveAnalysis,
            ),
        )

        when (val result = play(state, move)) {
            is PlayResult.Applied -> state = result.state
            is PlayResult.Rejected -> break
        }
    }
    return steps
}

/**
 * Schermata di Game Review mossa per mossa (F1-10, `docs/ai/maestro.md`, `docs/vision.md` §5.B).
 *
 * - Navigazione avanti/indietro e salto ai momenti chiave (errori e imprecisioni).
 * - Distinzione visiva delle classi di mossa con colori dedicati (Blu = Best, Giallo = Inaccuracy, Rosso = Blunder).
 * - Evidenziazione sul tavolo della mossa migliore per gli errori.
 * - Mostra solo ciò che il giocatore sapeva in quel momento; mai la mano avversaria nascosta.
 * - Calcolo interamente locale e gratuito (ADR-0005).
 * - Zero stringhe letterali (ADR-0007).
 */
@Composable
public fun GameReviewScreen(
    matchLog: MatchLog,
    analysis: MatchAnalysis,
    modifier: Modifier = Modifier,
    humanSeat: Seat = Seat.P1,
    onNavigateBack: () -> Unit = {},
) {
    val steps = remember(matchLog, analysis, humanSeat) {
        buildReviewSteps(matchLog, analysis, humanSeat)
    }

    var currentStepIndex by rememberSaveable(matchLog.seed) { mutableIntStateOf(0) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_review_screen"),
        color = TableSurfaceColor,
    ) {
        if (steps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.review_no_moves),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
            return@Surface
        }

        val stepIndex = currentStepIndex.coerceIn(0, steps.size - 1)
        val currentStep = steps[stepIndex]
        val moveAnalysis = currentStep.analysis
        val playerView = currentStep.playerView
        val isHumanMove = currentStep.mover == humanSeat

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header con titolo e indicatore mossa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.review_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                    )
                    Text(
                        text = stringResource(
                            id = R.string.review_move_indicator,
                            stepIndex + 1,
                            steps.size,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        modifier = Modifier.testTag("review_move_indicator"),
                    )
                }

                Text(
                    text = if (isHumanMove) {
                        stringResource(id = R.string.review_turn_player)
                    } else {
                        stringResource(id = R.string.review_turn_opponent)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isHumanMove) Color(0xFF64B5F6) else Color(0xFFFFB74D),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("review_turn_label"),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Banner di valutazione della mossa
            MoveEvaluationBanner(
                analysis = moveAnalysis,
                isHumanMove = isHumanMove,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Vista del tavolo al momento della mossa
            ReviewBoardView(
                step = currentStep,
                humanSeat = humanSeat,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Controlli di navigazione (Momenti chiave e passo-passo)
            ReviewNavigationControls(
                currentIndex = stepIndex,
                totalSteps = steps.size,
                steps = steps,
                humanSeat = humanSeat,
                onStepChanged = { currentStepIndex = it },
                onNavigateBack = onNavigateBack,
            )
        }
    }
}

@Composable
private fun MoveEvaluationBanner(
    analysis: MoveAnalysis,
    isHumanMove: Boolean,
    modifier: Modifier = Modifier,
) {
    val (badgeColor, gradeStringRes) = when (analysis.grade) {
        MoveGrade.BEST -> Pair(BestBadgeColor, R.string.review_grade_best)
        MoveGrade.INACCURACY -> Pair(InaccuracyBadgeColor, R.string.review_grade_inaccuracy)
        MoveGrade.BLUNDER -> Pair(BlunderBadgeColor, R.string.review_grade_blunder)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("review_evaluation_banner"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardAreaBg),
        border = BorderStroke(2.dp, badgeColor),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Badge di classe
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("review_grade_badge"),
                ) {
                    Text(
                        text = stringResource(id = gradeStringRes),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }

                if (analysis.grade != MoveGrade.BEST && analysis.loss > 0.0) {
                    Text(
                        text = stringResource(id = R.string.review_value_diff, analysis.loss),
                        style = MaterialTheme.typography.labelMedium,
                        color = BlunderBadgeColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("review_loss_label"),
                    )
                }
            }

            // Mossa giocata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.review_played_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                )
                Text(
                    text = formatMoveDescription(analysis.playedMove),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.testTag("review_played_move"),
                )
            }

            // Per errori o imprecisioni, mostra la mossa migliore alternativa
            if (analysis.grade != MoveGrade.BEST) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.review_best_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldAccent,
                    )
                    Text(
                        text = formatMoveDescription(analysis.bestMove),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        modifier = Modifier.testTag("review_best_move"),
                    )
                }

                Text(
                    text = stringResource(id = R.string.review_expected_value, analysis.bestValue),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                    modifier = Modifier.testTag("review_best_value"),
                )
            }
        }
    }
}

@Composable
private fun ReviewBoardView(
    step: ReviewStep,
    humanSeat: Seat,
    modifier: Modifier = Modifier,
) {
    val playerView = step.playerView
    val isMistake = step.analysis.grade != MoveGrade.BEST
    val bestMoveCard = if (isMistake && step.mover == humanSeat) step.analysis.bestMove.card else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("review_board"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BoardFeltColor),
        border = BorderStroke(1.dp, Color(0xFF2D6A4F)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Mano avversaria: visualizza ESCLUSIVAMENTE dorsi coperti (mai le carte vere!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("review_opponent_hand"),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(playerView.opponentHandSize) {
                    CardBackView(
                        width = 44.dp,
                        height = 66.dp,
                        modifier = Modifier.padding(horizontal = 2.dp),
                    )
                }
            }

            // Tavolo centrale: carte scoperte sul tavolo in quel momento
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("review_table_cards"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (playerView.table.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .height(66.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(id = R.string.review_table_empty),
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                } else {
                    for (card in playerView.table) {
                        val isCapturedByBest = isMistake && card in step.analysis.bestMove.capture
                        PlaceholderCardView(
                            card = card,
                            isPlayable = false,
                            width = 44.dp,
                            height = 66.dp,
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .then(
                                    if (isCapturedByBest) {
                                        Modifier.background(
                                            color = GoldAccent.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(6.dp),
                                        )
                                    } else {
                                        Modifier
                                    },
                                ),
                        )
                    }
                }
            }

            // Mano del giocatore umano in quel momento
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("review_player_hand"),
                horizontalArrangement = Arrangement.Center,
            ) {
                for (card in playerView.hand) {
                    val isRecommendedBest = card == bestMoveCard
                    Box(modifier = Modifier.padding(horizontal = 2.dp)) {
                        PlaceholderCardView(
                            card = card,
                            isPlayable = false,
                            width = 48.dp,
                            height = 72.dp,
                            modifier = if (isRecommendedBest) {
                                Modifier.background(
                                    color = GoldAccent.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(6.dp),
                                )
                            } else {
                                Modifier
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewNavigationControls(
    currentIndex: Int,
    totalSteps: TotalStepsInt,
    steps: List<ReviewStep>,
    humanSeat: Seat,
    onStepChanged: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val prevMistakeIndex = remember(currentIndex, steps, humanSeat) {
        findPreviousMistake(currentIndex, steps, humanSeat)
    }
    val nextMistakeIndex = remember(currentIndex, steps, humanSeat) {
        findNextMistake(currentIndex, steps, humanSeat)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Slider mossa
        if (totalSteps > 1) {
            Slider(
                value = currentIndex.toFloat(),
                onValueChange = { onStepChanged(it.toInt()) },
                valueRange = 0f..(totalSteps - 1).toFloat(),
                steps = if (totalSteps > 2) totalSteps - 2 else 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("review_slider"),
            )
        }

        // Fila 1: Passo-passo (Precedente / Successiva)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { onStepChanged((currentIndex - 1).coerceAtLeast(0)) },
                enabled = currentIndex > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("review_prev_button"),
            ) {
                Text(text = stringResource(id = R.string.review_prev_button))
            }

            Button(
                onClick = { onStepChanged((currentIndex + 1).coerceAtMost(totalSteps - 1)) },
                enabled = currentIndex < totalSteps - 1,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("review_next_button"),
            ) {
                Text(text = stringResource(id = R.string.review_next_button))
            }
        }

        // Fila 2: Salto ai momenti chiave (Errori precedenti e successivi)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { onStepChanged(prevMistakeIndex) },
                enabled = prevMistakeIndex != currentIndex,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("review_prev_mistake"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = InaccuracyBadgeColor,
                ),
            ) {
                Text(text = stringResource(id = R.string.review_prev_mistake))
            }

            OutlinedButton(
                onClick = { onStepChanged(nextMistakeIndex) },
                enabled = nextMistakeIndex != currentIndex,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("review_next_mistake"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = InaccuracyBadgeColor,
                ),
            ) {
                Text(text = stringResource(id = R.string.review_next_mistake))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Torna indietro
        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("review_back_button"),
        ) {
            Text(text = stringResource(id = R.string.review_back_button))
        }
    }
}

private typealias TotalStepsInt = Int

internal fun findPreviousMistake(currentIndex: Int, steps: List<ReviewStep>, seat: Seat): Int {
    for (i in (currentIndex - 1) downTo 0) {
        val step = steps[i]
        if (step.mover == seat && step.analysis.grade != MoveGrade.BEST) {
            return i
        }
    }
    return currentIndex
}

internal fun findNextMistake(currentIndex: Int, steps: List<ReviewStep>, seat: Seat): Int {
    for (i in (currentIndex + 1) until steps.size) {
        val step = steps[i]
        if (step.mover == seat && step.analysis.grade != MoveGrade.BEST) {
            return i
        }
    }
    return currentIndex
}

@Composable
private fun formatMoveDescription(move: ScopaMove): String {
    val assoStr = stringResource(id = R.string.game_card_rank_asso).take(1)
    val fanteStr = stringResource(id = R.string.game_card_rank_fante).take(1)
    val cavalloStr = stringResource(id = R.string.game_card_rank_cavallo).take(1)
    val reStr = stringResource(id = R.string.game_card_rank_re).take(1)
    val noCaptureStr = stringResource(id = R.string.review_capture_none)

    fun cardLabel(card: Card): String {
        val rankText = when (card.rank) {
            Rank.ASSO -> assoStr
            Rank.FANTE -> fanteStr
            Rank.CAVALLO -> cavalloStr
            Rank.RE -> reStr
            else -> card.rank.value.toString()
        }
        val suitSymbol = when (card.suit) {
            Suit.DENARI -> "◆"
            Suit.SPADE -> "♠"
            Suit.COPPE -> "♥"
            Suit.BASTONI -> "♣"
        }
        return "$rankText$suitSymbol"
    }

    val cardStr = cardLabel(move.card)
    return if (move.capture.isEmpty()) {
        "$cardStr ($noCaptureStr)"
    } else {
        val captureStr = move.capture.joinToString(", ") { cardLabel(it) }
        "$cardStr → $captureStr"
    }
}
