package it.maestro.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MoveAnalysis
import it.maestro.ai.analysis.MoveGrade
import it.maestro.app.R
import it.maestro.app.ui.components.CardBackView
import it.maestro.app.ui.components.ClubBackground
import it.maestro.app.ui.components.ConfettiCelebration
import it.maestro.app.ui.components.MaestroAvatarView
import it.maestro.app.ui.components.MaestroMood
import it.maestro.app.ui.components.MaestroPillButton
import it.maestro.app.ui.components.ParchmentPanel
import it.maestro.app.ui.components.PillButtonVariant
import it.maestro.app.ui.components.PlaceholderCardView
import it.maestro.app.ui.theme.ButtonGoldBorder
import it.maestro.app.ui.theme.GradeErroreGraveRed
import it.maestro.app.ui.theme.GradeImprecisioneYellow
import it.maestro.app.ui.theme.GradeMagistraleBlue
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
 * Schermata di Game Review mossa per mossa e funzione didattica "Rigioca l'errore"
 * (F1-10, F2-01, `docs/ai/maestro.md`, `docs/vision.md` §5.B).
 *
 * - Navigazione avanti/indietro e salto ai momenti chiave (errori e imprecisioni).
 * - Distinzione visiva delle classi di mossa con colori dedicati (Blu = Best, Giallo = Inaccuracy, Rosso = Blunder).
 * - Modalità interattiva "Riprova la mano": riproduce la disposizione delle carte, permette di giocare
 *   la carta alternativa e restituisce feedback immediato con pose del Maestro ed effetti visivi.
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
    viewModel: GameReviewViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
) {
    LaunchedEffect(matchLog, analysis, humanSeat) {
        viewModel.initialize(matchLog, analysis, humanSeat)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val steps = uiState.steps

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

        val stepIndex = uiState.currentStepIndex.coerceIn(0, steps.size - 1)
        val currentStep = steps[stepIndex]
        val moveAnalysis = currentStep.analysis
        val isHumanMove = currentStep.mover == humanSeat
        val isCurrentSolved = uiState.isCurrentStepSolved
        val isRetryMode = uiState.isRetryMode

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .then(if (isRetryMode) Modifier.testTag("review_retry_mode") else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.review_title).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFFFE082),
                        maxLines = 1,
                    )
                    Text(
                        text = stringResource(
                            id = R.string.review_move_indicator,
                            stepIndex + 1,
                            steps.size,
                        ),
                        style = MaterialTheme.typography.bodySmall,
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

            if (isRetryMode) {
                RetryEvaluationBanner(
                    retryFeedback = uiState.retryFeedback,
                    onExitRetry = viewModel::exitRetry,
                )
            } else {
                MoveEvaluationBanner(
                    analysis = moveAnalysis,
                    playerView = currentStep.playerView,
                    isHumanMove = isHumanMove,
                    isSolved = isCurrentSolved,
                    onStartRetry = viewModel::startRetry,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                ReviewBoardView(
                    step = currentStep,
                    humanSeat = humanSeat,
                    isRetryMode = isRetryMode,
                    retryFeedback = uiState.retryFeedback,
                    selectedRetryCard = uiState.selectedRetryCard,
                    onPlayCard = viewModel::playRetryCard,
                    modifier = Modifier.fillMaxSize(),
                )

                if (isRetryMode && uiState.retryFeedback is RetryFeedback.Correct) {
                    ConfettiCelebration(modifier = Modifier.fillMaxSize())
                }
            }

            ReviewNavigationControls(
                currentIndex = stepIndex,
                totalSteps = steps.size,
                steps = steps,
                humanSeat = humanSeat,
                solvedStepIndices = uiState.solvedStepIndices,
                onStepChanged = viewModel::selectStep,
                onPreviousStep = viewModel::previousStep,
                onNextStep = viewModel::nextStep,
                onPreviousMistake = { viewModel.previousMistake(humanSeat) },
                onNextMistake = { viewModel.nextMistake(humanSeat) },
                onNavigateBack = onNavigateBack,
            )
        }
    }
}

@Composable
private fun MoveEvaluationBanner(
    analysis: MoveAnalysis,
    playerView: ScopaView?,
    isHumanMove: Boolean,
    isSolved: Boolean,
    onStartRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (badgeColor, gradeStringRes) = when (analysis.grade) {
        MoveGrade.BEST -> Pair(BestBadgeColor, R.string.review_grade_best)
        MoveGrade.INACCURACY -> Pair(InaccuracyBadgeColor, R.string.review_grade_inaccuracy)
        MoveGrade.BLUNDER -> Pair(BlunderBadgeColor, R.string.review_grade_blunder)
    }

    val badge = when {
        isSolved -> R.drawable.badge_best
        analysis.grade == MoveGrade.BEST -> R.drawable.badge_best
        analysis.grade == MoveGrade.INACCURACY -> R.drawable.badge_warning
        else -> R.drawable.badge_wrong
    }

    val explanation = remember(analysis, playerView) {
        if (playerView != null && analysis.grade != MoveGrade.BEST) {
            ReviewExplanationEngine.explain(
                playedMove = analysis.playedMove,
                bestMove = analysis.bestMove,
                playerView = playerView,
            )
        } else {
            null
        }
    }

    ParchmentPanel(modifier = modifier.fillMaxWidth().testTag("review_evaluation_banner")) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(badge), contentDescription = null, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            color = if (isSolved) BestBadgeColor else badgeColor,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("review_grade_badge"),
                        ) {
                            Text(
                                text = if (isSolved) {
                                    stringResource(id = R.string.review_retry_solved_badge)
                                } else {
                                    stringResource(id = gradeStringRes)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (isHumanMove && analysis.grade != MoveGrade.BEST) {
                            MaestroPillButton(
                                text = stringResource(id = R.string.review_retry_button),
                                onClick = onStartRetry,
                                variant = if (isSolved) PillButtonVariant.PRIMARY_GREEN else PillButtonVariant.SECONDARY_AMBER,
                                height = 32.dp,
                                fontSize = 11.sp,
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                testTag = "review_retry_button",
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        if (analysis.grade != MoveGrade.BEST && analysis.loss > 0.0) {
                            Text(
                                text = stringResource(id = R.string.review_value_diff, analysis.loss),
                                style = MaterialTheme.typography.labelLarge,
                                color = BlunderBadgeColor,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.testTag("review_loss_label"),
                            )
                        }
                    }
                    MoveLine(
                        label = stringResource(id = R.string.review_played_label),
                        move = formatMoveDescription(analysis.playedMove),
                        color = InkColor,
                        tag = "review_played_move",
                    )
                    if (analysis.grade != MoveGrade.BEST) {
                        MoveLine(
                            label = stringResource(id = R.string.review_best_label),
                            move = formatMoveDescription(analysis.bestMove),
                            color = Color(0xFF1F683D),
                            tag = "review_best_move",
                        )
                        Text(
                            text = stringResource(id = R.string.review_expected_value, analysis.bestValue),
                            style = MaterialTheme.typography.labelSmall,
                            color = InkColor.copy(alpha = 0.7f),
                            modifier = Modifier.testTag("review_best_value"),
                        )
                    }
                }
            }

            if (explanation != null) {
                Surface(
                    color = Color(0xFFF6F0E2),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ButtonGoldBorder.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_explanation_card"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        MaestroAvatarView(
                            mood = if (analysis.grade == MoveGrade.BLUNDER) MaestroMood.WARNING else MaestroMood.THINKING,
                            size = 36.dp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                text = "${stringResource(id = R.string.review_explanation_header)}: ${explanation.title}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5C2D16),
                            )
                            Text(
                                text = explanation.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkColor.copy(alpha = 0.95f),
                                lineHeight = 16.sp,
                            )
                            Text(
                                text = "Regola d'oro: ${explanation.keyConcept}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF8B4513),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RetryEvaluationBanner(
    retryFeedback: RetryFeedback?,
    onExitRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mood = when (retryFeedback) {
        is RetryFeedback.Correct -> MaestroMood.TRIUMPHANT
        is RetryFeedback.Imperfect -> MaestroMood.ENCOURAGING
        null -> MaestroMood.THINKING
    }

    ParchmentPanel(modifier = modifier.fillMaxWidth().testTag("review_retry_banner")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MaestroAvatarView(
                mood = mood,
                size = 46.dp,
                modifier = Modifier.testTag("review_retry_maestro_avatar"),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.review_retry_title).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = InkColor,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    MaestroPillButton(
                        text = stringResource(id = R.string.review_retry_exit),
                        onClick = onExitRetry,
                        variant = PillButtonVariant.WOOD,
                        height = 30.dp,
                        fontSize = 11.sp,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        testTag = "review_retry_exit_button",
                    )
                }

                when (retryFeedback) {
                    is RetryFeedback.Correct -> {
                        Text(
                            text = stringResource(id = retryFeedback.messageRes),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20),
                            modifier = Modifier.testTag("review_retry_feedback"),
                        )
                    }
                    is RetryFeedback.Imperfect -> {
                        Text(
                            text = stringResource(id = retryFeedback.messageRes),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = BlunderBadgeColor,
                            modifier = Modifier.testTag("review_retry_feedback"),
                        )
                        Text(
                            text = stringResource(id = retryFeedback.hintRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkColor.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.testTag("review_retry_hint"),
                        )
                    }
                    null -> {
                        Text(
                            text = stringResource(id = R.string.review_retry_prompt),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkColor,
                            modifier = Modifier.testTag("review_retry_prompt"),
                        )
                    }
                }
            }
        }
    }
}

/** Riga compatta "Etichetta: mossa" su una o due righe al massimo. */
@Composable
private fun MoveLine(label: String, move: String, color: Color, tag: String) {
    Text(
        text = buildAnnotatedString {
            append("$label ")
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append(move)
            pop()
        },
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        maxLines = 2,
        modifier = Modifier.testTag(tag),
    )
}

@Composable
private fun ReviewBoardView(
    step: ReviewStep,
    humanSeat: Seat,
    isRetryMode: Boolean,
    retryFeedback: RetryFeedback?,
    selectedRetryCard: Card?,
    onPlayCard: (Card) -> Unit,
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
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val unit = (maxHeight / 330f).value.coerceIn(0.55f, 1.15f)
            val backW = (44 * unit).dp
            val tableW = (64 * unit).dp
            val handW = (84 * unit).dp

            Column(
                modifier = Modifier.fillMaxSize().padding(10.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
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
                            width = backW,
                            height = backW * 1.5f,
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
                            val isCapturedByBest = if (isRetryMode) {
                                retryFeedback is RetryFeedback.Correct && card in retryFeedback.bestMove.capture
                            } else {
                                isMistake && card in step.analysis.bestMove.capture
                            }

                            PlaceholderCardView(
                                card = card,
                                isPlayable = false,
                                width = tableW,
                                height = tableW * 1.5f,
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
                        if (isRetryMode) {
                            val isCorrectCard = retryFeedback is RetryFeedback.Correct && card == retryFeedback.bestMove.card
                            val isWrongCard = retryFeedback is RetryFeedback.Imperfect && card == selectedRetryCard
                            val badge = when {
                                isCorrectCard -> R.drawable.badge_best
                                isWrongCard -> R.drawable.badge_wrong
                                else -> null
                            }
                            val borderModifier = when {
                                isCorrectCard -> Modifier.border(3.dp, Color(0xFF42A5F5), RoundedCornerShape(8.dp))
                                isWrongCard -> Modifier.border(3.dp, GradeErroreGraveRed, RoundedCornerShape(8.dp))
                                else -> Modifier
                            }

                            Box(
                                modifier = Modifier.padding(horizontal = 3.dp),
                            ) {
                                PlaceholderCardView(
                                    card = card,
                                    isPlayable = true,
                                    width = handW,
                                    height = handW * 1.5f,
                                    modifier = borderModifier.testTag("review_retry_card_${card.suit.name}_${card.rank.name}"),
                                    onClick = { onPlayCard(card) },
                                )
                                if (badge != null) {
                                    Image(
                                        painter = painterResource(badge),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 10.dp, y = (-10).dp)
                                            .size(34.dp),
                                    )
                                }
                            }
                        } else {
                            val isRecommendedBest = card == bestMoveCard
                            val isPlayed = step.mover == humanSeat && card == step.analysis.playedMove.card
                            Box(modifier = Modifier.padding(horizontal = 3.dp)) {
                                PlaceholderCardView(
                                    card = card,
                                    isPlayable = false,
                                    width = handW,
                                    height = handW * 1.5f,
                                    modifier = if (isRecommendedBest) {
                                        Modifier.border(3.dp, Color(0xFF42A5F5), RoundedCornerShape(8.dp))
                                    } else {
                                        Modifier
                                    },
                                )
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
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 10.dp, y = (-10).dp)
                                            .size(34.dp),
                                    )
                                }
                            }
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
    totalSteps: Int,
    steps: List<ReviewStep>,
    humanSeat: Seat,
    solvedStepIndices: Set<Int>,
    onStepChanged: (Int) -> Unit,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit,
    onPreviousMistake: () -> Unit,
    onNextMistake: () -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ReviewTimeline(
            currentIndex = currentIndex,
            steps = steps,
            humanSeat = humanSeat,
            solvedStepIndices = solvedStepIndices,
            onStepChanged = onStepChanged,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            NavPill(R.string.review_prev_button, PillButtonVariant.WOOD, currentIndex > 0, "review_prev_button", onPreviousStep)
            NavPill(R.string.review_prev_mistake, PillButtonVariant.SECONDARY_AMBER, prevMistakeIndex != currentIndex, "review_prev_mistake", onPreviousMistake)
            NavPill(R.string.review_next_mistake, PillButtonVariant.SECONDARY_AMBER, nextMistakeIndex != currentIndex, "review_next_mistake", onNextMistake)
            NavPill(R.string.review_next_button, PillButtonVariant.WOOD, currentIndex < totalSteps - 1, "review_next_button", onNextStep)
        }

        MaestroPillButton(
            text = stringResource(id = R.string.review_back_button),
            onClick = onNavigateBack,
            variant = PillButtonVariant.PRIMARY_GREEN,
            height = 46.dp,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth(),
            testTag = "review_back_button",
        )
    }
}

@Composable
private fun RowScope.NavPill(
    text: Int,
    variant: PillButtonVariant,
    enabled: Boolean,
    tag: String,
    onClick: () -> Unit,
) {
    MaestroPillButton(
        text = stringResource(id = text),
        onClick = onClick,
        enabled = enabled,
        variant = variant,
        height = 42.dp,
        fontSize = 13.sp,
        contentPadding = PaddingValues(horizontal = 4.dp),
        modifier = Modifier.weight(1f),
        testTag = tag,
    )
}

/** Timeline delle tue mosse: un pallino per mossa, centrato automaticamente sulla mossa corrente. */
@Composable
private fun ReviewTimeline(
    currentIndex: Int,
    steps: List<ReviewStep>,
    humanSeat: Seat,
    solvedStepIndices: Set<Int>,
    onStepChanged: (Int) -> Unit,
) {
    val humanSteps = remember(steps, humanSeat) {
        steps.mapIndexedNotNull { index, step ->
            if (step.mover == humanSeat) Pair(index, step) else null
        }
    }
    val selectedHumanIndex = humanSteps.indexOfFirst { it.first == currentIndex }
    val listState = rememberLazyListState()

    LaunchedEffect(selectedHumanIndex) {
        if (selectedHumanIndex >= 0) {
            listState.animateScrollToItem(
                index = (selectedHumanIndex - 4).coerceAtLeast(0),
            )
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("review_slider"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        itemsIndexed(humanSteps) { _, (stepIndex, step) ->
            val isSolved = stepIndex in solvedStepIndices
            val color = when {
                isSolved -> BestBadgeColor
                step.analysis.grade == MoveGrade.BEST -> BestBadgeColor
                step.analysis.grade == MoveGrade.INACCURACY -> InaccuracyBadgeColor
                else -> BlunderBadgeColor
            }
            val selected = stepIndex == currentIndex
            val dotTag = if (isSolved) "review_timeline_dot_${stepIndex}_solved" else "review_timeline_dot_$stepIndex"

            Box(
                modifier = Modifier
                    .size(if (selected) 24.dp else 14.dp)
                    .background(color, CircleShape)
                    .border(if (selected) 2.5.dp else 1.dp, GoldAccent, CircleShape)
                    .testTag(dotTag)
                    .clickable { onStepChanged(stepIndex) },
            )
        }
    }
}

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
