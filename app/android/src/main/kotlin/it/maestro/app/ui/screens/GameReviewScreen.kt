package it.maestro.app.ui.screens

import it.maestro.app.ui.components.PillButtonVariant
import it.maestro.app.ui.components.ParchmentPanel
import it.maestro.app.ui.components.MaestroPillButton
import it.maestro.app.ui.components.ClubBackground
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
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

private val InkColor = Color(0xFF2B1A10)
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

    ClubBackground(modifier = modifier.testTag("game_review_screen"), feltFraction = 0.6f) {
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
            return@ClubBackground
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
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFFFFE082),
                    )
                    Text(
                        text = stringResource(
                            id = R.string.review_move_indicator,
                            stepIndex + 1,
                            steps.size,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFF4D6),
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

    val badge = when (analysis.grade) {
        MoveGrade.BEST -> R.drawable.badge_best
        MoveGrade.INACCURACY -> R.drawable.badge_warning
        MoveGrade.BLUNDER -> R.drawable.badge_wrong
    }
    ParchmentPanel(modifier = modifier.fillMaxWidth().testTag("review_evaluation_banner")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(badge), contentDescription = null, modifier = Modifier.size(52.dp))
        Spacer(modifier = Modifier.width(10.dp))
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
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(id = R.string.review_played_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkColor,
                )
                Text(
                    text = formatMoveDescription(analysis.playedMove),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = InkColor,
                    modifier = Modifier.testTag("review_played_move"),
                )
            }

            // Per errori o imprecisioni, mostra la mossa migliore alternativa
            if (analysis.grade != MoveGrade.BEST) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(id = R.string.review_best_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1F683D),
                    )
                    Text(
                        text = formatMoveDescription(analysis.bestMove),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F683D),
                        modifier = Modifier.testTag("review_best_move"),
                    )
                }

                Text(
                    text = stringResource(id = R.string.review_expected_value, analysis.bestValue),
                    style = MaterialTheme.typography.labelSmall,
                    color = InkColor.copy(alpha = 0.7f),
                    modifier = Modifier.testTag("review_best_value"),
                )
            }
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E4D2B)),
        border = BorderStroke(4.dp, Brush.verticalGradient(listOf(Color(0xFFFFE082), GoldAccent, Color(0xFF8C6D1F)))),
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
                            width = 64.dp,
                            height = 96.dp,
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .then(
                                    if (isCapturedByBest) {
                                        Modifier.border(3.dp, Color(0xFF42A5F5), RoundedCornerShape(8.dp))
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
                    val isPlayed = step.mover == humanSeat && card == step.analysis.playedMove.card
                    Box(modifier = Modifier.padding(horizontal = 3.dp)) {
                        PlaceholderCardView(
                            card = card,
                            isPlayable = false,
                            width = 84.dp,
                            height = 126.dp,
                            modifier = if (isRecommendedBest) {
                                Modifier.border(3.dp, Color(0xFF42A5F5), RoundedCornerShape(8.dp))
                            } else {
                                Modifier
                            },
                        )
                        // Bollini come nel mockup: ✕ sulla carta giocata per errore, ★ sulla mossa migliore
                        val badge = when {
                            isRecommendedBest -> R.drawable.badge_best
                            isPlayed && isMistake -> R.drawable.badge_wrong
                            isPlayed -> R.drawable.badge_correct
                            else -> null
                        }
                        if (badge != null) {
                            Image(
                                painter = painterResource(badge),
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-10).dp).size(34.dp),
                            )
                        }
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
        // Timeline: un pallino per ogni tua mossa, colorato per classe; tocca per saltare
        ReviewTimeline(
            currentIndex = currentIndex,
            steps = steps,
            humanSeat = humanSeat,
            onStepChanged = onStepChanged,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MaestroPillButton(
                text = stringResource(id = R.string.review_prev_button),
                onClick = { onStepChanged((currentIndex - 1).coerceAtLeast(0)) },
                enabled = currentIndex > 0,
                variant = PillButtonVariant.WOOD,
                height = 48.dp,
                fontSize = 16.sp,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.weight(1f),
                testTag = "review_prev_button",
            )
            MaestroPillButton(
                text = stringResource(id = R.string.review_next_button),
                onClick = { onStepChanged((currentIndex + 1).coerceAtMost(totalSteps - 1)) },
                enabled = currentIndex < totalSteps - 1,
                variant = PillButtonVariant.WOOD,
                height = 48.dp,
                fontSize = 16.sp,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.weight(1f),
                testTag = "review_next_button",
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MaestroPillButton(
                text = stringResource(id = R.string.review_prev_mistake),
                onClick = { onStepChanged(prevMistakeIndex) },
                enabled = prevMistakeIndex != currentIndex,
                variant = PillButtonVariant.SECONDARY_AMBER,
                height = 48.dp,
                fontSize = 16.sp,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.weight(1f),
                testTag = "review_prev_mistake",
            )
            MaestroPillButton(
                text = stringResource(id = R.string.review_next_mistake),
                onClick = { onStepChanged(nextMistakeIndex) },
                enabled = nextMistakeIndex != currentIndex,
                variant = PillButtonVariant.SECONDARY_AMBER,
                height = 48.dp,
                fontSize = 16.sp,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.weight(1f),
                testTag = "review_next_mistake",
            )
        }

        MaestroPillButton(
            text = stringResource(id = R.string.review_back_button),
            onClick = onNavigateBack,
            variant = PillButtonVariant.PRIMARY_GREEN,
            height = 54.dp,
            fontSize = 20.sp,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            testTag = "review_back_button",
        )
    }
}

/** Timeline delle tue mosse: blu = magistrale, giallo = imprecisione, rosso = errore grave. */
@Composable
private fun ReviewTimeline(
    currentIndex: Int,
    steps: List<ReviewStep>,
    humanSeat: Seat,
    onStepChanged: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 6.dp)
            .testTag("review_slider"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        steps.forEachIndexed { index, step ->
            if (step.mover != humanSeat) return@forEachIndexed
            val color = when (step.analysis.grade) {
                MoveGrade.BEST -> BestBadgeColor
                MoveGrade.INACCURACY -> InaccuracyBadgeColor
                MoveGrade.BLUNDER -> BlunderBadgeColor
            }
            val selected = index == currentIndex
            Box(
                modifier = Modifier
                    .size(if (selected) 26.dp else 16.dp)
                    .background(color, CircleShape)
                    .border(if (selected) 3.dp else 1.5.dp, GoldAccent, CircleShape)
                    .clickable { onStepChanged(index) },
            )
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

    val suitNames = mapOf(
        Suit.DENARI to stringResource(id = R.string.game_suit_denari),
        Suit.COPPE to stringResource(id = R.string.game_suit_coppe),
        Suit.SPADE to stringResource(id = R.string.game_suit_spade),
        Suit.BASTONI to stringResource(id = R.string.game_suit_bastoni),
    )

    fun cardLabel(card: Card): String {
        val rankText = when (card.rank) {
            Rank.ASSO -> assoStr
            Rank.FANTE -> fanteStr
            Rank.CAVALLO -> cavalloStr
            Rank.RE -> reStr
            else -> card.rank.value.toString()
        }
        return "$rankText ${suitNames.getValue(card.suit)}"
    }

    val cardStr = cardLabel(move.card)
    return if (move.capture.isEmpty()) {
        "$cardStr ($noCaptureStr)"
    } else {
        val captureStr = move.capture.joinToString(", ") { cardLabel(it) }
        "$cardStr → $captureStr"
    }
}
