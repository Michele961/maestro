package it.maestro.app.feature.review.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import it.maestro.app.designsystem.components.ClubBackground
import it.maestro.app.designsystem.components.ConfettiCelebration
import it.maestro.app.designsystem.components.MaestroAlertDialog
import it.maestro.app.designsystem.components.MaestroAvatarView
import it.maestro.app.designsystem.components.MaestroMood
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.ParchmentPanel
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.theme.ButtonGoldBorder
import it.maestro.app.designsystem.theme.GradeErroreGraveRed
import it.maestro.app.designsystem.theme.GradeImprecisioneYellow
import it.maestro.app.designsystem.theme.GradeMagistraleBlue
import it.maestro.app.domain.match.Beat
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
import it.maestro.app.feature.review.presentation.GameReviewViewModel
import it.maestro.app.feature.review.presentation.RetryFeedback
import it.maestro.app.feature.review.presentation.ReviewVerdict
import it.maestro.app.feature.review.presentation.isMistake
import it.maestro.app.feature.review.presentation.verdict
import it.maestro.app.domain.coach.ReviewCoach
import it.maestro.app.feature.table.ui.phrasesText

private val InkColor = Color(0xFF2B1A10)
private val GoldAccent = ButtonGoldBorder

private val BestBadgeColor = GradeMagistraleBlue
private val GoodBadgeColor = androidx.compose.ui.graphics.Color(0xFF4F8F5A)
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
 * @property beat La mossa giocata, da mettere in scena con la regia del tavolo.
 * @property bestBeat La mossa migliore al suo posto, solo per un errore del giocatore umano.
 */
public data class ReviewStep(
    val moveIndex: Int,
    val mover: Seat,
    val playerView: ScopaView,
    val analysis: MoveAnalysis,
    val beat: Beat,
    val bestBeat: Beat? = null,
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

        val result = play(state, move) as? PlayResult.Applied ?: break
        val bestBeat = if (mover == humanSeat && moveAnalysis.isMistake()) {
            (play(state, moveAnalysis.bestMove) as? PlayResult.Applied)?.let { beatOf(index, mover, moveAnalysis.bestMove, playerView, it, humanSeat) }
        } else {
            null
        }
        steps.add(
            ReviewStep(
                moveIndex = index,
                mover = mover,
                playerView = playerView,
                analysis = moveAnalysis,
                beat = beatOf(index, mover, move, playerView, result, humanSeat),
                bestBeat = bestBeat,
            ),
        )
        state = result.state
    }
    return steps
}

private fun beatOf(
    index: Int,
    mover: Seat,
    move: ScopaMove,
    viewBefore: ScopaView,
    result: PlayResult.Applied,
    humanSeat: Seat,
): Beat = Beat(
    id = index + 1L,
    actor = mover,
    move = move,
    events = result.events,
    viewBefore = viewBefore,
    viewAfter = viewFor(result.state, humanSeat),
)

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
    // Subito, prima del primo disegno: la schermata appare intera e non a pezzi.
    remember(matchLog, analysis, humanSeat) {
        viewModel.initialize(matchLog, analysis, humanSeat)
        true
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
        val isCurrentRevealed = stepIndex in uiState.revealedStepIndices
        val isSolutionVisible = !isHumanMove || !moveAnalysis.isMistake() || isCurrentSolved || isCurrentRevealed ||
            !ReviewCoach.isWorthRetrying(moveAnalysis.alternatives.size)
        val isRetryMode = uiState.isRetryMode

        // La regia mette in scena la mossa giocata; la migliore, quando la soluzione è svelata o trovata.
        val isFound = uiState.retryFeedback is RetryFeedback.Correct
        val bestShown = isFound || isCurrentRevealed || isCurrentSolved
        LaunchedEffect(stepIndex, isRetryMode, bestShown, isFound) {
            val stage = viewModel.stage ?: return@LaunchedEffect
            when {
                isFound || (!isRetryMode && bestShown && currentStep.bestBeat != null) -> stage.showBest(currentStep)
                isRetryMode -> stage.rest(currentStep)
                else -> stage.rest(currentStep)
            }
        }

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
                    onRevealSolution = {
                        viewModel.revealSolution()
                        viewModel.exitRetry()
                    },
                )
            } else {
                MoveEvaluationBanner(
                    analysis = moveAnalysis,
                    playerView = currentStep.playerView,
                    isHumanMove = isHumanMove,
                    isSolved = isCurrentSolved,
                    isRevealed = isCurrentRevealed,
                    onStartRetry = viewModel::startRetry,
                    onRevealSolution = viewModel::revealSolution,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                viewModel.stage?.let { stage ->
                    ReviewBoard(
                        stage = stage,
                        step = currentStep,
                        humanSeat = humanSeat,
                        isRetryMode = isRetryMode,
                        isSolutionVisible = isSolutionVisible,
                        retryFeedback = uiState.retryFeedback,
                        onPlayCard = viewModel::playRetryCard,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                if (isRetryMode && uiState.retryFeedback is RetryFeedback.Correct) {
                    ConfettiCelebration(modifier = Modifier.fillMaxSize())
                }
            }

            if (!isRetryMode) {
                viewModel.stage?.let { stage ->
                    ReviewReplayControls(
                        stage = stage,
                        step = currentStep,
                        showBest = isSolutionVisible && isHumanMove && moveAnalysis.isMistake() && currentStep.bestBeat != null,
                    )
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
    isRevealed: Boolean,
    onStartRetry: () -> Unit,
    onRevealSolution: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (badgeColor, gradeStringRes) = when (analysis.verdict()) {
        ReviewVerdict.BEST -> Pair(BestBadgeColor, R.string.review_grade_best)
        ReviewVerdict.GOOD -> Pair(GoodBadgeColor, R.string.review_grade_good)
        ReviewVerdict.INACCURACY -> Pair(InaccuracyBadgeColor, R.string.review_grade_inaccuracy)
        ReviewVerdict.BLUNDER -> Pair(BlunderBadgeColor, R.string.review_grade_blunder)
    }

    val badge = when {
        isSolved -> R.drawable.badge_best
        analysis.verdict() == ReviewVerdict.BEST -> R.drawable.badge_best
        analysis.verdict() == ReviewVerdict.GOOD -> R.drawable.badge_correct
        analysis.verdict() == ReviewVerdict.INACCURACY -> R.drawable.badge_warning
        else -> R.drawable.badge_wrong
    }

    val isMistake = isHumanMove && analysis.isMistake()
    // Con una o due scelte non ha senso far riprovare: si vede subito la soluzione (Michele, 2026-10-27).
    val canRetry = ReviewCoach.isWorthRetrying(analysis.alternatives.size)
    val isSolutionVisible = !isMistake || isSolved || isRevealed || !canRetry

    val explanation = remember(analysis, playerView, isSolutionVisible) {
        if (isSolutionVisible && playerView != null && analysis.isMistake()) {
            ReviewCoach.explain(
                played = analysis.playedMove,
                best = analysis.bestMove,
                view = playerView,
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
                        if (isHumanMove && analysis.isMistake() && canRetry) {
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
                    }
                    MoveLine(
                        label = stringResource(id = R.string.review_played_label),
                        move = formatMoveDescription(analysis.playedMove),
                        color = InkColor,
                        tag = "review_played_move",
                    )
                    if (isSolutionVisible && analysis.isMistake()) {
                        MoveLine(
                            label = stringResource(id = R.string.review_best_label),
                            move = formatMoveDescription(analysis.bestMove),
                            color = Color(0xFF1F683D),
                            tag = "review_best_move",
                        )
                    }
                }
            }

            if (!isSolutionVisible) {
                Surface(
                    color = Color(0xFFF6F0E2),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ButtonGoldBorder.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_socratic_prompt_card"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MaestroAvatarView(
                            mood = MaestroMood.THINKING,
                            size = 32.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.review_find_better_prompt),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = InkColor,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        MaestroPillButton(
                            text = stringResource(id = R.string.review_show_solution_button),
                            onClick = onRevealSolution,
                            variant = PillButtonVariant.WOOD,
                            height = 30.dp,
                            fontSize = 11.sp,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            testTag = "review_show_solution_button",
                        )
                    }
                }
            }

            if (explanation != null) {
                var showWhy by remember(analysis) { mutableStateOf(false) }
                MaestroPillButton(
                    text = stringResource(id = R.string.review_why_button),
                    onClick = { showWhy = true },
                    variant = PillButtonVariant.EDUCATIONAL_BLUE,
                    height = 36.dp,
                    fontSize = 14.sp,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("review_why_button"),
                )
                if (showWhy) {
                    MaestroAlertDialog(
                        modifier = Modifier.testTag("review_explanation_card"),
                        onDismissRequest = { showWhy = false },
                        title = { Text(phrasesText(listOf(explanation.title)), fontWeight = FontWeight.Bold) },
                        text = {
                            Row(verticalAlignment = Alignment.Top) {
                                MaestroAvatarView(
                                    mood = if (analysis.grade == MoveGrade.BLUNDER) MaestroMood.WARNING else MaestroMood.THINKING,
                                    size = 48.dp,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = phrasesText(explanation.explanation), style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = stringResource(id = R.string.review_golden_rule, phrasesText(listOf(explanation.rule))),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF8B4513),
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showWhy = false }) { Text(stringResource(id = R.string.review_why_close)) }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RetryEvaluationBanner(
    retryFeedback: RetryFeedback?,
    onExitRetry: () -> Unit,
    onRevealSolution: () -> Unit = {},
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
                    if (retryFeedback !is RetryFeedback.Correct) {
                        MaestroPillButton(
                            text = stringResource(id = R.string.review_show_solution_button),
                            onClick = onRevealSolution,
                            variant = PillButtonVariant.WOOD,
                            height = 30.dp,
                            fontSize = 11.sp,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            testTag = "review_retry_reveal_button",
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
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
                        if (retryFeedback.showsHint) {
                            Text(
                                text = stringResource(id = retryFeedback.hintRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = InkColor.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.testTag("review_retry_hint"),
                            )
                        }
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
            variant = PillButtonVariant.WOOD,
            height = 42.dp,
            fontSize = 15.sp,
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
        height = 38.dp,
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
                step.analysis.verdict() == ReviewVerdict.BEST -> BestBadgeColor
                step.analysis.verdict() == ReviewVerdict.GOOD -> GoodBadgeColor
                step.analysis.verdict() == ReviewVerdict.INACCURACY -> InaccuracyBadgeColor
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
        if (step.mover == seat && step.analysis.isMistake()) {
            return i
        }
    }
    return currentIndex
}

internal fun findNextMistake(currentIndex: Int, steps: List<ReviewStep>, seat: Seat): Int {
    for (i in (currentIndex + 1) until steps.size) {
        val step = steps[i]
        if (step.mover == seat && step.analysis.isMistake()) {
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
