package it.maestro.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import it.maestro.app.ui.components.LocalCardBackTheme
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import it.maestro.app.game.OpponentMoveSummary
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import it.maestro.app.ui.components.ParchmentPanel
import it.maestro.app.ui.components.FramedPortrait
import it.maestro.app.ui.components.BubbleTail
import it.maestro.app.ui.components.MaestroSpeechBubble
import it.maestro.app.ui.components.SweepCelebrationOverlay
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.R
import it.maestro.app.audio.NoOpSoundManager
import it.maestro.app.audio.SoundEffect
import it.maestro.app.audio.SoundManager
import it.maestro.app.game.GameIntent
import it.maestro.app.game.GameUiState
import it.maestro.app.game.GameViewModel
import it.maestro.app.game.PendingCaptureChoice
import it.maestro.app.game.TrainingAnswerFeedback
import it.maestro.app.game.UiGameEvent
import it.maestro.app.ui.components.CardBackView
import it.maestro.app.ui.components.MaestroAlertDialog
import it.maestro.app.ui.components.MaestroAvatarView
import it.maestro.app.ui.components.MaestroMood
import it.maestro.app.ui.components.MaestroPillButton
import it.maestro.app.ui.components.NineSliceBox
import it.maestro.app.ui.components.PillButtonVariant
import it.maestro.app.ui.components.PlaceholderCardView
import it.maestro.app.ui.components.ScorePlaque
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.DealScore
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaState
import it.maestro.game.scopa.ScoreCategory
import it.maestro.game.training.SeatLead
import it.maestro.game.training.SettebelloLocation
import it.maestro.game.training.TrainingFundamental
import it.maestro.game.training.TrainingOption
import it.maestro.game.training.TrainingQuestion
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import it.maestro.app.domain.time.Clock

public val TableFeltColor: Color = Color(0xFF1B4332)
public val TableFeltDarkColor: Color = Color(0xFF081C15)
public val GoldAccentColor: Color = Color(0xFFD4AF37)

/**
 * Schermata di gioco del tavolo di Scopa (ADR-0012, task F1-06, F1-07).
 */
@Composable
public fun TableScreen(
    modifier: Modifier = Modifier,
    level: DifficultyLevel = DifficultyLevel.INTERMEDIO,
    targetScore: Int = 11,
    trainingFundamental: TrainingFundamental = TrainingFundamental.NONE,
    seed: Long = 42L,
    restoreState: ScopaState? = null,
    restoreLog: MatchLog? = null,
    tableThemeId: String = "felt_classic_green",
    cardBackThemeId: String = "back_classic_trevigiano",
    viewModel: GameViewModel = koinViewModel(),
    soundManager: SoundManager = koinInject(),
    clock: Clock = koinInject(),
    onNavigateHome: () -> Unit = {},
    onViewAnalysis: (MatchLog, Pair<Int, Int>?) -> Unit = { _, _ -> },
) {
    LaunchedEffect(level, targetScore, seed, restoreState, restoreLog, trainingFundamental) {
        if (restoreState != null && restoreLog != null) {
            viewModel.onIntent(
                GameIntent.RestoreMatch(
                    state = restoreState,
                    matchLog = restoreLog,
                    level = level,
                    trainingFundamental = trainingFundamental,
                ),
            )
        } else {
            viewModel.onIntent(
                GameIntent.StartMatch(
                    config = ScopaConfig(targetScore = targetScore),
                    level = level,
                    seed = seed,
                    trainingFundamental = trainingFundamental,
                ),
            )
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TableContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        soundManager = soundManager,
        tableThemeId = tableThemeId,
        cardBackThemeId = cardBackThemeId,
        onNavigateHome = onNavigateHome,
        onViewAnalysis = {
            val results = uiState.trainingResults
            val training = if (results.isEmpty()) null else results.count { it.isCorrect } to results.size
            onViewAnalysis(viewModel.currentMatchLog, training)
        },
        onRematch = {
            viewModel.onIntent(
                GameIntent.StartMatch(
                    config = ScopaConfig(targetScore = targetScore),
                    level = level,
                    seed = clock.newSeed(),
                    trainingFundamental = trainingFundamental,
                ),
            )
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun TableContent(
    uiState: GameUiState,
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier = Modifier,
    soundManager: SoundManager = NoOpSoundManager(),
    tableThemeId: String = "felt_classic_green",
    cardBackThemeId: String = "back_classic_trevigiano",
    onNavigateHome: () -> Unit = {},
    onViewAnalysis: () -> Unit = {},
    onRematch: () -> Unit = { onIntent(GameIntent.StartMatch()) },
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showPause by remember { mutableStateOf(false) }
    var isTableAnimating by remember { mutableStateOf(false) }

    // Dialog Check di allenamento del Maestro (task F1-12)
    // Non andiamo a coprire la giocata dell'avversario a metà animazione: attendiamo che
    // l'animazione di calata/presa sia terminata e lasciamo un breve respiro per registrare la mossa.
    val question = uiState.activeTrainingQuestion
    var canShowQuestion by remember { mutableStateOf(question != null && uiState.lastMoveId == 0L) }
    LaunchedEffect(question, isTableAnimating) {
        if (question == null) {
            canShowQuestion = false
        } else if (!isTableAnimating) {
            if (uiState.lastMoveId > 0L) {
                delay(800L)
            }
            canShowQuestion = true
        } else {
            canShowQuestion = false
        }
    }

    LaunchedEffect(uiState.lastEvent) {
        when (uiState.lastEvent) {
            is UiGameEvent.Sweep -> soundManager.play(SoundEffect.SWEEP)
            is UiGameEvent.DealEnded -> soundManager.play(SoundEffect.CARD_CAPTURE)
            else -> {}
        }
    }

    // F1-26: "SCOPA!" e il Maestro festante restano a schermo solo per un momento.
    LaunchedEffect(uiState.lastEvent) {
        if (uiState.lastEvent is UiGameEvent.Sweep) {
            kotlinx.coroutines.delay(SWEEP_BANNER_MILLIS)
            onIntent(GameIntent.DismissEvent)
        }
    }

    // F1-26: a fine partita si passa subito alla schermata di fine partita in stile (niente dialog Material).
    val matchOver = uiState.phase == DealPhase.MATCH_OVER && uiState.pendingDealScore == null
    LaunchedEffect(matchOver) {
        if (matchOver) {
            kotlinx.coroutines.delay(MATCH_OVER_PAUSE_MILLIS)
            onViewAnalysis()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val error = uiState.errorMessage
        if (error != null) {
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short,
            )
            onIntent(GameIntent.DismissError)
        }
    }

    val context = LocalContext.current
    val tableFeltResId = remember(tableThemeId) {
        if (tableThemeId == "felt_walnut_wood") {
            R.drawable.texture_wood
        } else {
            val id = context.resources.getIdentifier("table_felt_bg", "drawable", context.packageName)
            if (id != 0) id else null
        }
    }

    val feltTint = remember(tableThemeId) {
        when (tableThemeId) {
            "felt_royal_red" -> Color(0xFFE57373)
            "felt_noble_blue" -> Color(0xFF64B5F6)
            else -> null
        }
    }

    val feltBrush = remember(tableThemeId) {
        when (tableThemeId) {
            "felt_royal_red" -> Brush.radialGradient(
                colors = listOf(
                    Color(0xFF8B1E2B),
                    Color(0xFF5E121C),
                    Color(0xFF38080F),
                    Color(0xFF1E0307),
                ),
            )
            "felt_noble_blue" -> Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1F487E),
                    Color(0xFF14325C),
                    Color(0xFF0C1F3B),
                    Color(0xFF06101F),
                ),
            )
            "felt_walnut_wood" -> Brush.radialGradient(
                colors = listOf(
                    Color(0xFF4A2810),
                    Color(0xFF331808),
                    Color(0xFF220E04),
                    Color(0xFF120702),
                ),
            )
            else -> Brush.radialGradient(
                colors = listOf(
                    Color(0xFF225740),
                    Color(0xFF1B4332),
                    Color(0xFF0D281E),
                    Color(0xFF081C15),
                ),
            )
        }
    }

    CompositionLocalProvider(LocalCardBackTheme provides cardBackThemeId) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(feltBrush)
                .testTag("table_screen"),
        ) {
            // Cornice in legno attorno al panno, come in reference_table_screen.jpg (F1-21)
            Image(
                painter = painterResource(R.drawable.texture_wood),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Column(modifier = Modifier.fillMaxSize()) {
                TableTopBar(uiState = uiState, onPause = { showPause = true })
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(3.dp, GoldAccentColor, RoundedCornerShape(14.dp)),
                ) {
                    if (tableFeltResId != null) {
                        Image(
                            painter = painterResource(id = tableFeltResId),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            colorFilter = feltTint?.let { ColorFilter.tint(it, BlendMode.Modulate) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(feltBrush))
                    }
                    FeltLayout(
                        uiState = uiState,
                        soundManager = soundManager,
                        onCardClicked = { card ->
                            soundManager.play(SoundEffect.CARD_PLAY)
                            onIntent(GameIntent.PlayCard(card))
                        },
                        onRequestHint = {
                            soundManager.play(SoundEffect.BUTTON_CLICK)
                            onIntent(GameIntent.RequestHint)
                        },
                        onAnimationRunningChanged = { isTableAnimating = it },
                    )
            }
        }

        if (showPause) {
            PauseDialog(onResume = { showPause = false }, onNavigateHome = onNavigateHome)
        }

        // Host per notifiche di errore
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        // Dialog Check di allenamento del Maestro (task F1-12)
        if (question != null && canShowQuestion) {
            TrainingQuestionDialog(
                question = question,
                feedback = uiState.trainingQuestionFeedback,
                opponentLastMove = uiState.opponentLastMove,
                soundManager = soundManager,
                onAnswer = { index -> onIntent(GameIntent.AnswerTrainingQuestion(index)) },
                onContinue = {
                    soundManager.play(SoundEffect.BUTTON_CLICK)
                    onIntent(GameIntent.DismissTrainingQuestion)
                },
            )
        }

        // Dialog per scelta di presa multipla
        if (uiState.pendingCaptureChoice != null) {
            CaptureSelectionDialog(
                choice = uiState.pendingCaptureChoice,
                onConfirm = { card, capture -> onIntent(GameIntent.SelectCapture(card, capture)) },
                onCancel = { onIntent(GameIntent.CancelCaptureChoice) },
            )
        }

        // Dialog riepilogo smazzata (task F1-07)
        val dealScore = uiState.pendingDealScore
        if (dealScore != null) {
            DealSummaryDialog(
                score = dealScore,
                uiState = uiState,
                onDismiss = { onIntent(GameIntent.DismissDealSummary) },
            )
        }

        // Overlay celebrativo Scopa (task F1-25)
        val sweepEvent = uiState.lastEvent as? UiGameEvent.Sweep
        if (sweepEvent != null) {
            SweepCelebrationOverlay(
                event = sweepEvent,
                isHuman = sweepEvent.seat == uiState.humanSeat,
                onDismiss = { onIntent(GameIntent.DismissEvent) },
            )
        }

    }
    }
}

/** Barra superiore su legno: avversario con espressione e punteggio della partita (F1-21, F1-25). */
@Composable
private fun TableTopBar(uiState: GameUiState, onPause: () -> Unit) {
    val lastEvent = uiState.lastEvent
    val mood = when {
        lastEvent is UiGameEvent.Sweep && lastEvent.seat == uiState.humanSeat -> MaestroMood.APPLAUDING
        lastEvent is UiGameEvent.Sweep -> MaestroMood.DESPAIR
        uiState.isBotThinking || uiState.isHintLoading -> MaestroMood.THINKING
        uiState.trainingQuestionFeedback?.isCorrect == true -> MaestroMood.PLEASED
        uiState.trainingQuestionFeedback?.isCorrect == false -> MaestroMood.WARNING
        else -> MaestroMood.NEUTRAL
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("table_top_bar"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MaestroAvatarView(mood = mood, size = 48.dp)
        NineSliceBox(
            skin = R.drawable.skin_name_plaque,
            slice = 0.35f,
            corner = 12.dp,
            modifier = Modifier.padding(start = 4.dp),
        ) {
            Text(
                text = stringResource(id = R.string.game_player_maestro),
                style = MaterialTheme.typography.titleSmall,
                color = GoldAccentColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
        Spacer(modifier = Modifier.widthIn(min = 4.dp).weight(1f, fill = false))
        ScorePlaque(
            p1Score = uiState.humanScore,
            p2Score = uiState.opponentScore,
            p1Label = stringResource(id = R.string.game_player_you),
            p2Label = stringResource(id = R.string.game_opponent_short),
        )
        Image(
            painter = painterResource(R.drawable.button_pause),
            contentDescription = stringResource(id = R.string.game_pause),
            modifier = Modifier
                .padding(start = 6.dp)
                .size(46.dp)
                .clickable(onClick = onPause)
                .testTag("pause_button"),
        )
    }
}





@Composable
private fun rememberCardFormatter(): (Card) -> String {
    val assoStr = stringResource(id = R.string.game_card_rank_asso)
    val fanteStr = stringResource(id = R.string.game_card_rank_fante)
    val cavalloStr = stringResource(id = R.string.game_card_rank_cavallo)
    val reStr = stringResource(id = R.string.game_card_rank_re)
    val denariStr = stringResource(id = R.string.game_suit_denari)
    val coppeStr = stringResource(id = R.string.game_suit_coppe)
    val spadeStr = stringResource(id = R.string.game_suit_spade)
    val bastoniStr = stringResource(id = R.string.game_suit_bastoni)

    return remember(assoStr, fanteStr, cavalloStr, reStr, denariStr, coppeStr, spadeStr, bastoniStr) {
        { card: Card ->
            val rankText = when (card.rank) {
                Rank.ASSO -> assoStr
                Rank.FANTE -> fanteStr
                Rank.CAVALLO -> cavalloStr
                Rank.RE -> reStr
                else -> card.rank.value.toString()
            }
            val suitText = when (card.suit) {
                Suit.DENARI -> denariStr
                Suit.COPPE -> coppeStr
                Suit.SPADE -> spadeStr
                Suit.BASTONI -> bastoniStr
            }
            "$rankText $suitText"
        }
    }
}

private fun determineLogicalHintRes(
    move: ScopaMove,
    tableCards: List<Card>,
): Int {
    val captures = move.capture
    val isSweep = captures.isNotEmpty() && captures.size == tableCards.size
    val involvesSettebello = (move.card.suit == Suit.DENARI && move.card.rank == Rank.SETTE) ||
        captures.any { it.suit == Suit.DENARI && it.rank == Rank.SETTE }
    val capturesDenari = captures.any { it.suit == Suit.DENARI } || move.card.suit == Suit.DENARI
    val capturesPrimiera = captures.any { it.rank == Rank.SETTE || it.rank == Rank.SEI }

    return when {
        isSweep -> R.string.hint_sweep_opportunity
        involvesSettebello -> R.string.hint_settebello_priority
        capturesDenari -> R.string.hint_denari_priority
        capturesPrimiera -> R.string.hint_primiera_priority
        captures.isEmpty() -> R.string.hint_safe_discard
        else -> R.string.review_hint_generic
    }
}

@Composable
private fun formatHintMove(move: ScopaMove): String {
    val assoStr = stringResource(id = R.string.game_card_rank_asso).take(1)
    val fanteStr = stringResource(id = R.string.game_card_rank_fante).take(1)
    val cavalloStr = stringResource(id = R.string.game_card_rank_cavallo).take(1)
    val reStr = stringResource(id = R.string.game_card_rank_re).take(1)

    // Semi napoletani per nome (niente simboli francesi), es. "7 di denari"
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
        cardStr
    } else {
        val captureStr = move.capture.joinToString(", ") { cardLabel(it) }
        "$cardStr → $captureStr"
    }
}

@Composable
private fun CaptureSelectionDialog(
    choice: PendingCaptureChoice,
    onConfirm: (Card, Set<Card>) -> Unit,
    onCancel: () -> Unit,
) {
    MaestroAlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = stringResource(id = R.string.game_dialog_capture_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("capture_selection_dialog"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for ((index, option) in choice.options.withIndex()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfirm(choice.card, option) }
                            .border(1.dp, DialogGold, RoundedCornerShape(6.dp))
                            .testTag("capture_option_$index"),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF264653),
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            for (takenCard in option) {
                                PlaceholderCardView(
                                    card = takenCard,
                                    isPlayable = false,
                                    width = 38.dp,
                                    height = 56.dp,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.testTag("cancel_capture_button"),
            ) {
                Text(text = stringResource(id = R.string.game_dialog_cancel))
            }
        },
    )
}

@Composable
private fun DealSummaryDialog(
    score: DealScore,
    uiState: GameUiState,
    onDismiss: () -> Unit,
) {
    val humanSeat = uiState.humanSeat
    val oppSeat = humanSeat.other()

    val categories = listOfNotNull(
        ScoreCategory.CARTE to R.string.score_category_carte,
        ScoreCategory.DENARI to R.string.score_category_denari,
        ScoreCategory.SETTEBELLO to R.string.score_category_settebello,
        ScoreCategory.PRIMIERA to R.string.score_category_primiera,
        ScoreCategory.SCOPE to R.string.score_category_scope,
        if (score.pointsBySeatAndCategory.getValue(humanSeat).containsKey(ScoreCategory.RE_BELLO)) {
            ScoreCategory.RE_BELLO to R.string.score_category_re_bello
        } else {
            null
        },
    )

    var revealedCount by remember { mutableStateOf(0) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        for (i in 1..categories.size) {
            delay(150L)
            revealedCount = i
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    MaestroAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.deal_summary_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { revealedCount = categories.size },
                    )
                    .testTag("deal_summary_dialog"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Intestazione colonne: Categoria | Tu | Maestro
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(id = R.string.game_score_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = DialogInkSoft,
                        modifier = Modifier.weight(2f),
                    )
                    Text(
                        text = stringResource(id = R.string.game_player_you),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DialogGold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(id = R.string.game_player_maestro),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DialogInk,
                        modifier = Modifier.weight(1f),
                    )
                }

                HorizontalDivider(color = Color.DarkGray)

                categories.forEachIndexed { i, (category, labelResId) ->
                    val isRevealed = revealedCount > i
                    val tag = "deal_summary_${category.name.lowercase()}"
                    val p1Pts = score.points(humanSeat, category)
                    val p2Pts = score.points(oppSeat, category)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = if (isRevealed) 1f else 0.2f
                                translationX = if (isRevealed) 0f else -12f
                            }
                            .testTag(tag),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(id = labelResId),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(2f),
                        )
                        Text(
                            text = "$p1Pts",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isRevealed && p1Pts > 0) DialogGold else DialogInkSoft,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "$p2Pts",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isRevealed && p2Pts > 0) DialogInk else DialogInkSoft,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                HorizontalDivider(color = Color.DarkGray)

                val totalsRevealed = revealedCount >= categories.size
                // Totale smazzata
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = if (totalsRevealed) 1f else 0.3f
                        }
                        .testTag("deal_summary_deal_total"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.deal_summary_deal_total),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2f),
                    )
                    Text(
                        text = "${score.totalPoints(humanSeat)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = DialogGold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${score.totalPoints(oppSeat)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = DialogInk,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Punteggio complessivo partita
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = if (totalsRevealed) 1f else 0.3f
                        }
                        .testTag("deal_summary_match_total"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.deal_summary_match_total),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DialogInkSoft,
                        modifier = Modifier.weight(2f),
                    )
                    Text(
                        text = "${uiState.humanScore}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DialogGold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${uiState.opponentScore}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DialogInk,
                        modifier = Modifier.weight(1f),
                    )
                }

                if (uiState.trainingResults.isNotEmpty()) {
                    val correct = uiState.trainingResults.count { it.isCorrect }
                    val total = uiState.trainingResults.size
                    val pct = (correct * 100) / total
                    HorizontalDivider(color = Color.DarkGray)
                    Text(
                        text = stringResource(id = R.string.training_score_label, pct, correct, total),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DialogGold,
                        modifier = Modifier.testTag("deal_summary_attention_score"),
                    )
                }
            }
        },
        confirmButton = {
            MaestroPillButton(
                text = stringResource(id = R.string.deal_summary_continue),
                onClick = onDismiss,
                variant = PillButtonVariant.PRIMARY_GREEN,
                height = 46.dp,
                testTag = "deal_summary_continue_button",
            )
        },
    )
}

@Composable
private fun TrainingQuestionDialog(
    question: TrainingQuestion,
    feedback: TrainingAnswerFeedback?,
    opponentLastMove: OpponentMoveSummary? = null,
    soundManager: SoundManager = NoOpSoundManager(),
    onAnswer: (Int) -> Unit,
    onContinue: () -> Unit,
) {
    LaunchedEffect(feedback) {
        if (feedback != null) {
            if (feedback.isCorrect) {
                soundManager.play(SoundEffect.CHECK_CORRECT)
            } else {
                soundManager.play(SoundEffect.CHECK_WRONG)
            }
        }
    }

    val questionTitleResId = when (question.fundamental) {
        TrainingFundamental.SEVENS_COUNT -> R.string.training_question_sevens
        TrainingFundamental.DENARI_COUNT -> R.string.training_question_denari
        TrainingFundamental.PRIMIERA_LEAD -> R.string.training_question_primiera
        TrainingFundamental.SETTEBELLO_LOCATION -> R.string.training_question_settebello
        TrainingFundamental.CARDS_LEAD -> R.string.training_question_cards
        TrainingFundamental.NONE -> R.string.training_dialog_subtitle
    }

    MaestroAlertDialog(
        onDismissRequest = { /* Modal: interazione richiesta prima di chiudere */ },
        modifier = Modifier.testTag("training_question_dialog"),
        title = {
            Column {
                Text(
                    text = stringResource(id = R.string.training_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DialogGold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.training_dialog_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = DialogInkSoft,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (opponentLastMove != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GoldAccentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x123B2213),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(id = R.string.game_opponent_last_move) + ":",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DialogInk,
                                fontSize = 11.sp,
                            )
                            PlaceholderCardView(
                                card = opponentLastMove.card,
                                isPlayable = false,
                                width = 24.dp,
                                height = 36.dp,
                            )
                            if (opponentLastMove.capture.isNotEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.game_arrow_symbol),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DialogInkSoft,
                                    fontSize = 11.sp,
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    for (taken in opponentLastMove.capture) {
                                        PlaceholderCardView(
                                            card = taken,
                                            isPlayable = false,
                                            width = 24.dp,
                                            height = 36.dp,
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "(${stringResource(id = R.string.game_opponent_drop_table)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DialogInkSoft,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                }

                Text(
                    text = stringResource(id = questionTitleResId),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = DialogInk,
                    modifier = Modifier.testTag("training_question_text"),
                )

                // Opzioni di risposta
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    question.options.forEachIndexed { index, option ->
                        val optionText = when (option) {
                            is TrainingOption.IntCount -> stringResource(id = R.string.training_option_count_format, option.count)
                            is TrainingOption.Lead -> when (option.lead) {
                                SeatLead.HUMAN -> stringResource(id = R.string.training_option_lead_human)
                                SeatLead.OPPONENT -> stringResource(id = R.string.training_option_lead_opponent)
                                SeatLead.TIE -> stringResource(id = R.string.training_option_lead_tie)
                            }
                            is TrainingOption.Location -> when (option.location) {
                                SettebelloLocation.CAPTURED_BY_ME -> stringResource(id = R.string.training_option_settebello_by_me)
                                SettebelloLocation.CAPTURED_BY_OPPONENT -> stringResource(id = R.string.training_option_settebello_by_opp)
                                SettebelloLocation.IN_PLAY_OR_UNKNOWN -> stringResource(id = R.string.training_option_settebello_in_play)
                            }
                        }

                        val isAnswered = feedback != null
                        val isSelected = feedback?.selectedIndex == index
                        val isCorrectOption = index == question.correctIndex

                        // F1-26: opzioni su cartoncino chiaro con inchiostro scuro; colori pieni solo dopo la risposta.
                        val borderCol = when {
                            !isAnswered -> GoldAccentColor
                            isCorrectOption -> Color(0xFF2E7D32)
                            isSelected -> Color(0xFFC62828)
                            else -> Color(0x553B2213)
                        }
                        val bgCol = when {
                            !isAnswered -> OptionPaper
                            isCorrectOption -> Color(0xFF2E7D32)
                            isSelected -> Color(0xFFC62828)
                            else -> OptionPaper.copy(alpha = 0.5f)
                        }
                        val textCol = if (isAnswered && (isCorrectOption || isSelected)) Color.White else DialogInk

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("training_option_$index")
                                .border(2.dp, borderCol, RoundedCornerShape(10.dp))
                                .clickable(enabled = feedback == null) {
                                    soundManager.play(SoundEffect.BUTTON_CLICK)
                                    onAnswer(index)
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = bgCol,
                        ) {
                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textCol,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }

                // Banner di feedback dopo la risposta
                if (feedback != null) {
                    val feedbackText = if (feedback.isCorrect) {
                        stringResource(id = R.string.training_feedback_correct)
                    } else {
                        stringResource(id = R.string.training_feedback_wrong)
                    }
                    val feedbackBg = if (feedback.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("training_feedback_banner"),
                        shape = RoundedCornerShape(8.dp),
                        color = feedbackBg,
                    ) {
                        Text(
                            text = feedbackText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (feedback != null) {
                MaestroPillButton(
                    text = stringResource(id = R.string.training_feedback_continue),
                    onClick = onContinue,
                    variant = PillButtonVariant.PRIMARY_GREEN,
                    height = 46.dp,
                    testTag = "training_continue_button",
                )
            }
        },
    )
}

/**
 * Targhetta compatta con il riepilogo dell'ultima giocata dell'avversario (carta giocata, prese effettuate, eventuale scopa).
 */
@Composable
private fun OpponentLastMovePlaque(
    summary: OpponentMoveSummary,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .testTag("opponent_last_move_banner")
            .shadow(6.dp, RoundedCornerShape(10.dp))
            .border(1.5.dp, GoldAccentColor, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = OptionPaper,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = stringResource(id = R.string.game_opponent_last_move) + ":",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DialogInk,
                fontSize = 11.sp,
                maxLines = 1,
                softWrap = false,
            )
            PlaceholderCardView(
                card = summary.card,
                isPlayable = false,
                width = 24.dp,
                height = 36.dp,
            )
            Text(
                text = stringResource(id = R.string.game_arrow_symbol),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DialogInkSoft,
                fontSize = 11.sp,
            )
            if (summary.capture.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.game_opponent_drop_table),
                    style = MaterialTheme.typography.labelSmall,
                    color = DialogInkSoft,
                    fontSize = 11.sp,
                    maxLines = 1,
                    softWrap = false,
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    for (taken in summary.capture) {
                        PlaceholderCardView(
                            card = taken,
                            isPlayable = false,
                            width = 24.dp,
                            height = 36.dp,
                        )
                    }
                }
            }
            if (summary.isSweep) {
                Surface(
                    color = Color(0xFFC62828),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(start = 3.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.game_event_sweep).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 10.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

/** Pila del mazzo disegnata con il dorso del mazzo in uso, una copia sfalsata ogni ~5 carte (max 6). */
@Composable
private fun DeckStack(cards: Int) {
    val layers = ((cards + 4) / 5).coerceIn(1, 6)
    Box(modifier = Modifier.width(64.dp).height((96 + (layers - 1) * 3).dp)) {
        repeat(layers) { i ->
            CardBackView(
                width = 64.dp,
                height = 96.dp,
                modifier = Modifier.offset(x = (i * 2).dp, y = ((layers - 1 - i) * 3).dp),
            )
        }
    }
}

public enum class MoveAnimStage {
    NONE,
    PLAYING_TO_TABLE,
    SHOWING_CAPTURE,
    FLYING_TO_PILE,
    GLIDING_TO_TABLE,
}

public data class ActiveMoveAnimation(
    val moveId: Long,
    val mover: Seat,
    val card: Card,
    val capture: Set<Card>,
    val isSweep: Boolean,
    val stage: MoveAnimStage,
)

@Composable
private fun MoveFlightOverlay(
    anim: ActiveMoveAnimation,
    humanSeat: Seat,
) {
    val isHuman = anim.mover == humanSeat

    val startOffsetY = if (isHuman) 220f else -200f
    // Posizione di sosta (staging): sopra le carte per l'avversario (-130f), sotto per il giocatore (+130f).
    // Non copre mai le carte in tavola al centro (0f, 0f)!
    val stagingY = if (isHuman) 130f else -130f
    val pileX = 135f
    val pileY = if (isHuman) 120f else -120f

    val animOffsetX = remember(anim.moveId) { Animatable(0f) }
    val animOffsetY = remember(anim.moveId) { Animatable(startOffsetY) }
    val animScale = remember(anim.moveId) { Animatable(0.9f) }
    val animAlpha = remember(anim.moveId) { Animatable(0.85f) }

    LaunchedEffect(anim.stage) {
        when (anim.stage) {
            MoveAnimStage.PLAYING_TO_TABLE -> {
                // Arriva nello spazio aperto di staging (sopra o sotto le carte in tavola)
                launch { animOffsetY.animateTo(stagingY, tween(320, easing = FastOutSlowInEasing)) }
                launch { animScale.animateTo(1.08f, tween(320, easing = FastOutSlowInEasing)) }
                launch { animAlpha.animateTo(1f, tween(200)) }
            }
            MoveAnimStage.SHOWING_CAPTURE -> {
                animOffsetY.snapTo(stagingY)
                animScale.snapTo(1.08f)
                animAlpha.snapTo(1f)
            }
            MoveAnimStage.GLIDING_TO_TABLE -> {
                // Calata semplice: scivola dalla posizione di staging al centro del tavolo
                launch { animOffsetY.animateTo(0f, tween(240, easing = FastOutSlowInEasing)) }
                launch { animScale.animateTo(1.0f, tween(240, easing = FastOutSlowInEasing)) }
            }
            MoveAnimStage.FLYING_TO_PILE -> {
                launch { animOffsetX.animateTo(pileX, tween(360, easing = FastOutSlowInEasing)) }
                launch { animOffsetY.animateTo(pileY, tween(360, easing = FastOutSlowInEasing)) }
                launch { animScale.animateTo(0.5f, tween(360, easing = FastOutSlowInEasing)) }
                launch { animAlpha.animateTo(0.15f, tween(360)) }
            }
            MoveAnimStage.NONE -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Se siamo nella fase FLYING_TO_PILE, anche le carte catturate volano verso la pila
        if (anim.stage == MoveAnimStage.FLYING_TO_PILE) {
            anim.capture.forEachIndexed { idx, capturedCard ->
                val staggered = (idx * 8).dp
                Box(
                    modifier = Modifier
                        .offset(x = staggered, y = staggered)
                        .graphicsLayer {
                            translationX = animOffsetX.value.dp.toPx()
                            translationY = animOffsetY.value.dp.toPx()
                            scaleX = (animScale.value * 0.9f).coerceAtLeast(0.01f)
                            scaleY = (animScale.value * 0.9f).coerceAtLeast(0.01f)
                            alpha = animAlpha.value.coerceIn(0f, 1f)
                        }
                        .shadow(6.dp, RoundedCornerShape(8.dp)),
                ) {
                    PlaceholderCardView(
                        card = capturedCard,
                        isPlayable = false,
                        showGoldFrame = capturedCard.suit == Suit.DENARI && capturedCard.rank == Rank.SETTE,
                        width = 72.dp,
                        height = 108.dp,
                    )
                }
            }
        }

        // Carta giocata
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = animOffsetX.value.dp.toPx()
                    translationY = animOffsetY.value.dp.toPx()
                    scaleX = animScale.value.coerceAtLeast(0.01f)
                    scaleY = animScale.value.coerceAtLeast(0.01f)
                    alpha = animAlpha.value.coerceIn(0f, 1f)
                }
                .shadow(12.dp, RoundedCornerShape(8.dp)),
        ) {
            PlaceholderCardView(
                card = anim.card,
                isPlayable = false,
                showGoldFrame = anim.card.suit == Suit.DENARI && anim.card.rank == Rank.SETTE,
                width = 80.dp,
                height = 120.dp,
                modifier = if (anim.capture.isNotEmpty() && anim.stage == MoveAnimStage.SHOWING_CAPTURE) {
                    Modifier.border(3.dp, GoldAccentColor, RoundedCornerShape(8.dp))
                } else Modifier,
            )
        }
    }
}

public enum class DealAnimStage {
    SHUFFLE,
    DEAL_TABLE_0,
    DEAL_TABLE_1,
    DEAL_TABLE_2,
    DEAL_TABLE_3,
    DEAL_HAND_OPP_0,
    DEAL_HAND_HUMAN_0,
    DEAL_HAND_OPP_1,
    DEAL_HAND_HUMAN_1,
    DEAL_HAND_OPP_2,
    DEAL_HAND_HUMAN_2,
}

public data class ActiveDealAnimation(
    val dealId: Long,
    val isInitial: Boolean,
    val stage: DealAnimStage,
)

@Composable
private fun DealAnimationOverlay(
    anim: ActiveDealAnimation,
    tableCards: List<Card> = emptyList(),
) {
    // Coordinate del mazzo (destra centro)
    val deckX = 135f
    val deckY = 0f

    val shuffleOffsetX = remember(anim.dealId) { Animatable(0f) }
    val flyProgress = remember(anim.dealId, anim.stage) { Animatable(0f) }

    LaunchedEffect(anim.stage) {
        if (anim.stage == DealAnimStage.SHUFFLE) {
            shuffleOffsetX.animateTo(-26f, tween(110, easing = FastOutSlowInEasing))
            shuffleOffsetX.animateTo(0f, tween(110, easing = FastOutSlowInEasing))
        } else {
            flyProgress.animateTo(1f, tween(150, easing = FastOutSlowInEasing))
        }
    }

    val tableTargets = listOf(
        Pair(-40f, -58f),
        Pair(40f, -58f),
        Pair(-40f, 58f),
        Pair(40f, 58f),
    )
    val oppTargets = listOf(
        Pair(-40f, -240f),
        Pair(0f, -240f),
        Pair(40f, -240f),
    )
    val humanTargets = listOf(
        Pair(-48f, 240f),
        Pair(0f, 240f),
        Pair(48f, 240f),
    )

    val oppLandedCount = when (anim.stage) {
        DealAnimStage.DEAL_HAND_HUMAN_0, DealAnimStage.DEAL_HAND_OPP_1 -> 1
        DealAnimStage.DEAL_HAND_HUMAN_1, DealAnimStage.DEAL_HAND_OPP_2 -> 2
        DealAnimStage.DEAL_HAND_HUMAN_2 -> 3
        else -> 0
    }

    val humanLandedCount = when (anim.stage) {
        DealAnimStage.DEAL_HAND_OPP_1, DealAnimStage.DEAL_HAND_HUMAN_1 -> 1
        DealAnimStage.DEAL_HAND_OPP_2, DealAnimStage.DEAL_HAND_HUMAN_2 -> 2
        else -> 0
    }

    // Bersaglio della carta tavolo in volo
    val currentFlyingTableIndex = when (anim.stage) {
        DealAnimStage.DEAL_TABLE_0 -> 0
        DealAnimStage.DEAL_TABLE_1 -> 1
        DealAnimStage.DEAL_TABLE_2 -> 2
        DealAnimStage.DEAL_TABLE_3 -> 3
        else -> null
    }

    // Bersaglio della carta mano in volo
    val currentFlyingHandTarget: Pair<Float, Float>? = when (anim.stage) {
        DealAnimStage.DEAL_HAND_OPP_0 -> oppTargets[0]
        DealAnimStage.DEAL_HAND_HUMAN_0 -> humanTargets[0]
        DealAnimStage.DEAL_HAND_OPP_1 -> oppTargets[1]
        DealAnimStage.DEAL_HAND_HUMAN_1 -> humanTargets[1]
        DealAnimStage.DEAL_HAND_OPP_2 -> oppTargets[2]
        DealAnimStage.DEAL_HAND_HUMAN_2 -> humanTargets[2]
        else -> null
    }

    val isFlyingToHuman = anim.stage in listOf(
        DealAnimStage.DEAL_HAND_HUMAN_0,
        DealAnimStage.DEAL_HAND_HUMAN_1,
        DealAnimStage.DEAL_HAND_HUMAN_2,
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Taglio dello shuffle sul mazzo
        if (anim.stage == DealAnimStage.SHUFFLE) {
            CardBackView(
                width = 64.dp,
                height = 96.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = (deckX + shuffleOffsetX.value).dp.toPx()
                        translationY = deckY.dp.toPx()
                        rotationZ = shuffleOffsetX.value * 0.4f
                    }
                    .shadow(10.dp, RoundedCornerShape(6.dp)),
            )
        }

        // NOTA BENE: Nessun dorso statico disegnato sul tavolo!
        // Le carte a terra che sono già atterrate vengono disegnate direttamente da FlowRow
        // a faccia in su in modo stabile, naturale e perfettamente allineato.

        // Carte già atterrate all'avversario (dorso)
        for (i in 0 until oppLandedCount) {
            val (tx, ty) = oppTargets[i]
            CardBackView(
                width = 64.dp,
                height = 96.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = tx.dp.toPx()
                        translationY = ty.dp.toPx()
                    }
                    .shadow(6.dp, RoundedCornerShape(6.dp)),
            )
        }

        // Carte già atterrate al giocatore umano (dorso)
        for (i in 0 until humanLandedCount) {
            val (tx, ty) = humanTargets[i]
            CardBackView(
                width = 68.dp,
                height = 102.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = tx.dp.toPx()
                        translationY = ty.dp.toPx()
                    }
                    .shadow(8.dp, RoundedCornerShape(6.dp)),
            )
        }

        // Carta attualmente in volo verso il tavolo:
        // Parte di dorso dal mazzo e si gira a faccia in su in volo, atterrando scoperta sul tavolo!
        if (currentFlyingTableIndex != null) {
            val p = flyProgress.value
            val (targetX, targetY) = tableTargets[currentFlyingTableIndex]
            val cardX = deckX + (targetX - deckX) * p
            val cardY = deckY + (targetY - deckY) * p
            val card = tableCards.getOrNull(currentFlyingTableIndex)

            val isFaceUp = p >= 0.45f
            val rotY = if (p < 0.45f) (1f - p) * 180f else (1f - p) * 180f - 180f

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = cardX.dp.toPx()
                        translationY = cardY.dp.toPx()
                        rotationY = rotY
                        alpha = p.coerceIn(0.3f, 1f)
                    }
                    .shadow(8.dp, RoundedCornerShape(6.dp)),
            ) {
                if (card != null && isFaceUp) {
                    PlaceholderCardView(
                        card = card,
                        isPlayable = false,
                        width = 72.dp,
                        height = 108.dp,
                    )
                } else {
                    CardBackView(
                        width = 72.dp,
                        height = 108.dp,
                    )
                }
            }
        }

        // Carta attualmente in volo verso le mani (dorso coperto)
        if (currentFlyingHandTarget != null) {
            val p = flyProgress.value
            val (targetX, targetY) = currentFlyingHandTarget
            val cardX = deckX + (targetX - deckX) * p
            val cardY = deckY + (targetY - deckY) * p
            val cardScale = if (isFlyingToHuman) 0.85f + 0.25f * p else 0.85f + 0.15f * p

            CardBackView(
                width = if (isFlyingToHuman) 68.dp else 64.dp,
                height = if (isFlyingToHuman) 102.dp else 96.dp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = cardX.dp.toPx()
                        translationY = cardY.dp.toPx()
                        scaleX = cardScale
                        scaleY = cardScale
                        alpha = p.coerceIn(0.3f, 1f)
                    }
                    .shadow(8.dp, RoundedCornerShape(6.dp)),
            )
        }
    }
}

/**
 * Disposizione del tavolo come `reference_table_screen.jpg` (F1-21): mano dell'avversario in alto,
 * carte in tavola al centro, Maestro con fumetto a sinistra, mazzo e prese a destra, mano a ventaglio in basso.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeltLayout(
    uiState: GameUiState,
    soundManager: SoundManager,
    onCardClicked: (Card) -> Unit,
    onRequestHint: () -> Unit,
    onAnimationRunningChanged: (Boolean) -> Unit = {},
) {
    var activeMoveAnim by remember { mutableStateOf<ActiveMoveAnimation?>(null) }
    var activeDealAnim by remember { mutableStateOf<ActiveDealAnimation?>(null) }
    var lastCompletedDealId by remember { mutableStateOf(0L) }
    var lastHandledMoveId by remember { mutableStateOf(uiState.lastMoveId) }

    val isAnimating = activeMoveAnim != null || lastHandledMoveId < uiState.lastMoveId || activeDealAnim != null
    LaunchedEffect(isAnimating) {
        onAnimationRunningChanged(isAnimating)
    }

    LaunchedEffect(uiState.dealAnimationId) {
        if (uiState.dealAnimationId == 0L) return@LaunchedEffect
        // Se non è la smazzata iniziale, la nuova distribuzione delle mani è scaturita dalla giocata
        // della terza carta (uiState.lastMoveId): attendiamo che tale mossa sia stata
        // completamente animata e risolta sul tavolo prima di distribuire le nuove carte dal mazzo!
        if (!uiState.isInitialDeal) {
            while (lastHandledMoveId < uiState.lastMoveId || activeMoveAnim != null) {
                delay(40L)
            }
        } else {
            while (activeMoveAnim != null) {
                delay(40L)
            }
        }
        try {
            if (uiState.isInitialDeal) {
                // Smazzata iniziale: taglio del mazzo
                soundManager.play(SoundEffect.CARD_PLAY)
                activeDealAnim = ActiveDealAnimation(
                    dealId = uiState.dealAnimationId,
                    isInitial = true,
                    stage = DealAnimStage.SHUFFLE,
                )
                delay(220L)

                // 4 Carte a terra una ad una
                val tableStages = listOf(
                    DealAnimStage.DEAL_TABLE_0,
                    DealAnimStage.DEAL_TABLE_1,
                    DealAnimStage.DEAL_TABLE_2,
                    DealAnimStage.DEAL_TABLE_3,
                )
                for (s in tableStages) {
                    soundManager.play(SoundEffect.CARD_PLAY)
                    activeDealAnim = activeDealAnim?.copy(stage = s)
                    delay(160L)
                }
            }

            // Distribuzione alternata 1 a 1 ad ogni giocatore:
            // Avversario -> Giocatore -> Avversario -> Giocatore -> Avversario -> Giocatore
            val handStages = listOf(
                DealAnimStage.DEAL_HAND_OPP_0,
                DealAnimStage.DEAL_HAND_HUMAN_0,
                DealAnimStage.DEAL_HAND_OPP_1,
                DealAnimStage.DEAL_HAND_HUMAN_1,
                DealAnimStage.DEAL_HAND_OPP_2,
                DealAnimStage.DEAL_HAND_HUMAN_2,
            )
            for (s in handStages) {
                soundManager.play(SoundEffect.CARD_PLAY)
                activeDealAnim = ActiveDealAnimation(
                    dealId = uiState.dealAnimationId,
                    isInitial = uiState.isInitialDeal,
                    stage = s,
                )
                delay(160L)
            }
            delay(80L)
        } finally {
            if (activeDealAnim?.dealId == uiState.dealAnimationId) {
                activeDealAnim = null
            }
            lastCompletedDealId = uiState.dealAnimationId
        }
    }

    LaunchedEffect(uiState.lastMoveId) {
        val move = uiState.lastMove ?: return@LaunchedEffect
        val mover = uiState.lastMover ?: return@LaunchedEffect
        if (uiState.lastMoveId == 0L) return@LaunchedEffect
        // Attende che l'eventuale animazione di smazzata o distribuzione carte sia completata
        while (activeDealAnim != null) {
            delay(40L)
        }

        val isSweep = uiState.lastEvent is UiGameEvent.Sweep
        val isOpponent = mover != uiState.humanSeat
        try {
            // 1. Carta scivola nello spazio aperto di staging (sopra o sotto le carte in tavola)
            // così le carte sul tavolo restano sempre visibili e mai coperte
            activeMoveAnim = ActiveMoveAnimation(
                moveId = uiState.lastMoveId,
                mover = mover,
                card = move.card,
                capture = move.capture,
                isSweep = isSweep,
                stage = MoveAnimStage.PLAYING_TO_TABLE,
            )
            delay(if (isOpponent) 350L else 280L)
            if (isOpponent) {
                // Pausa per mostrare la carta giocata dall'avversario in chiaro
                delay(600L)
            }

            if (move.capture.isNotEmpty()) {
                // 2. Le carte catturate sul tavolo si evidenziano mentre la carta giocata resta visibile in staging
                activeMoveAnim = activeMoveAnim?.copy(stage = MoveAnimStage.SHOWING_CAPTURE)
                delay(if (isOpponent) 900L else 450L)

                // 3. Volo congiunto verso la pila delle prese
                activeMoveAnim = activeMoveAnim?.copy(stage = MoveAnimStage.FLYING_TO_PILE)
                soundManager.play(SoundEffect.CARD_CAPTURE)
                delay(if (isOpponent) 450L else 350L)
            } else {
                // Calata semplice: tempo di sosta in staging, poi scivola al centro del tavolo
                delay(if (isOpponent) 500L else 180L)
                activeMoveAnim = activeMoveAnim?.copy(stage = MoveAnimStage.GLIDING_TO_TABLE)
                soundManager.play(SoundEffect.CARD_PLAY)
                delay(240L)
            }
        } finally {
            if (activeMoveAnim?.moveId == uiState.lastMoveId) {
                activeMoveAnim = null
            }
            lastHandledMoveId = uiState.lastMoveId
        }
    }

    val currentAnim = activeMoveAnim
    val currentDealAnim = activeDealAnim
    val isDealPending = uiState.dealAnimationId > 0L && lastCompletedDealId < uiState.dealAnimationId
    val hideHands = isDealPending || currentDealAnim != null

    // Conteggio visibile delle carte nel mazzo:
    // Durante la smazzata/distribuzione (sia iniziale che mid-game), il mazzo
    // decrementa gradualmente di una carta man mano che ciascuna carta viene distribuita,
    // invece di passare immediatamente al conteggio finale.
    val visibleDeckCount = if (isDealPending || currentDealAnim != null) {
        val totalToDeal = if (uiState.isInitialDeal) 10 else 6
        val dealt = when (currentDealAnim?.stage) {
            null, DealAnimStage.SHUFFLE -> 0
            DealAnimStage.DEAL_TABLE_0 -> 1
            DealAnimStage.DEAL_TABLE_1 -> 2
            DealAnimStage.DEAL_TABLE_2 -> 3
            DealAnimStage.DEAL_TABLE_3 -> 4
            DealAnimStage.DEAL_HAND_OPP_0 -> if (uiState.isInitialDeal) 5 else 1
            DealAnimStage.DEAL_HAND_HUMAN_0 -> if (uiState.isInitialDeal) 6 else 2
            DealAnimStage.DEAL_HAND_OPP_1 -> if (uiState.isInitialDeal) 7 else 3
            DealAnimStage.DEAL_HAND_HUMAN_1 -> if (uiState.isInitialDeal) 8 else 4
            DealAnimStage.DEAL_HAND_OPP_2 -> if (uiState.isInitialDeal) 9 else 5
            DealAnimStage.DEAL_HAND_HUMAN_2 -> if (uiState.isInitialDeal) 10 else 6
        }
        (uiState.deckCardCount + totalToDeal - dealt).coerceAtLeast(0)
    } else {
        uiState.deckCardCount
    }

    // Le carte visibili sul tavolo:
    // - Durante la smazzata iniziale, le carte atterrate compaiono a faccia in su mano a mano che atterrano
    // - Durante una calata semplice (capture.isEmpty()), la carta vola nell'overlay e NON
    //   deve comparire nel tavolo finché l'animazione di volo non finisce (elimina il duplicato)
    // - Durante una presa (capture.isNotEmpty()), le carte catturate restano evidenziate sul tavolo
    //   finché non volano alla pila delle prese
    val visibleTableCards = remember(uiState.tableCards, currentAnim, currentDealAnim, isDealPending) {
        val dealAnim = currentDealAnim
        if ((isDealPending || dealAnim != null) && uiState.isInitialDeal) {
            if (dealAnim == null || dealAnim.stage in listOf(DealAnimStage.SHUFFLE, DealAnimStage.DEAL_TABLE_0)) {
                emptyList()
            } else {
                when (dealAnim.stage) {
                    DealAnimStage.DEAL_TABLE_1 -> uiState.tableCards.take(1)
                    DealAnimStage.DEAL_TABLE_2 -> uiState.tableCards.take(2)
                    DealAnimStage.DEAL_TABLE_3 -> uiState.tableCards.take(3)
                    else -> uiState.tableCards
                }
            }
        } else {
            val anim = currentAnim
            if (anim != null) {
                if (anim.capture.isNotEmpty()) {
                    if (anim.stage != MoveAnimStage.FLYING_TO_PILE) {
                        (uiState.tableCards + anim.capture).distinct()
                    } else {
                        uiState.tableCards
                    }
                } else {
                    // Calata semplice: esclude la carta in volo dal tavolo finché non atterra
                    uiState.tableCards.filter { it != anim.card }
                }
            } else {
                uiState.tableCards
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(10.dp).testTag("felt_layout")) {
        // Mano dell'avversario, coperta e sovrapposta (invisibile durante la smazzata animata)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .alpha(if (hideHands) 0f else 1f)
                .testTag("opponent_zone"),
            horizontalArrangement = Arrangement.spacedBy((-26).dp),
        ) {
            repeat(uiState.opponentCardCount) { CardBackView(width = 72.dp, height = 108.dp) }
        }

        // Piccolo registro / HUD dell'ultima giocata effettuata dall'avversario
        val opponentLastMove = uiState.opponentLastMove
        if (opponentLastMove != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 114.dp),
            ) {
                OpponentLastMovePlaque(summary = opponentLastMove)
            }
        }

        // Colonna destra: mazzo e prese
        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            CapturePile(count = uiState.opponentCapturedCount, sweepCards = uiState.opponentSweepCards, sweeps = uiState.opponentSweeps, tag = "opponent_captures")
            Box(modifier = Modifier.testTag("deck_pile"), contentAlignment = Alignment.BottomCenter) {
                if (visibleDeckCount > 0) DeckStack(cards = visibleDeckCount)
                CountBadge(visibleDeckCount.toString())
            }
            CapturePile(count = uiState.humanCapturedCount, sweepCards = uiState.humanSweepCards, sweeps = uiState.humanSweeps, tag = "human_captures")
        }

        // Il Maestro a sinistra: fumetto solo quando richiesto dall'utente (consiglio) o per scopa.
        // Posizionato nello spazio dedicato a sinistra sotto l'HUD dell'ultima giocata, evitando qualsiasi overlap.
        val hint = uiState.suggestedMove
        var hintDismissed by remember(hint) { mutableStateOf(false) }
        LaunchedEffect(hint) {
            if (hint != null) {
                kotlinx.coroutines.delay(HINT_BUBBLE_MILLIS)
                hintDismissed = true
            }
        }
        val showHint = hint != null && !hintDismissed
        val sweeping = uiState.lastEvent is UiGameEvent.Sweep
        val canAskHint = uiState.isSparring && uiState.isHumanTurn && !uiState.isBotThinking && !uiState.isHintLoading && currentAnim == null && currentDealAnim == null && !isDealPending

        // Carte in tavola, al centro in griglia stabile con margini fissi
        FlowRow(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(
                    start = 76.dp,
                    end = 76.dp,
                    top = 28.dp,
                    bottom = 28.dp,
                )
                .testTag("table_cards_zone"),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3,
        ) {
            val suggested = uiState.suggestedMove?.capture
            for (card in visibleTableCards) {
                val isBeingCaptured = currentAnim != null && card in currentAnim.capture && currentAnim.stage == MoveAnimStage.SHOWING_CAPTURE
                val isSettebello = card.suit == Suit.DENARI && card.rank == Rank.SETTE
                PlaceholderCardView(
                    card = card,
                    isPlayable = false,
                    showGoldFrame = isBeingCaptured || isSettebello,
                    width = 72.dp,
                    height = 108.dp,
                    modifier = when {
                        isBeingCaptured -> Modifier
                            .graphicsLayer { scaleX = 1.08f; scaleY = 1.08f }
                            .border(3.dp, GoldAccentColor, RoundedCornerShape(8.dp))
                        suggested != null && card in suggested -> Modifier.border(3.dp, Color(0xFF42A5F5), RoundedCornerShape(8.dp))
                        else -> Modifier
                    },
                )
            }
        }

        // Ritratto del Maestro: posizionato nello spazio a sinistra tra avversario e carte in tavola
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 152.dp)
                .width(150.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
            ) {
                AnimatedVisibility(
                    visible = uiState.isSparring,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Box(
                        modifier = Modifier
                            .alpha(if (canAskHint || showHint) 1f else 0.7f)
                            .clickable(enabled = canAskHint, onClick = onRequestHint)
                            .testTag("game_hint_button"),
                    ) {
                        val sweepEvent = uiState.lastEvent as? UiGameEvent.Sweep
                        FramedPortrait(
                            portrait = when {
                                sweepEvent?.seat == uiState.humanSeat -> R.drawable.maestro_applauding
                                sweepEvent?.seat != null -> R.drawable.maestro_despair
                                uiState.isBotThinking -> R.drawable.maestro_thinking
                                showHint -> R.drawable.maestro_pleased
                                else -> R.drawable.maestro_neutral
                            },
                            size = 50.dp,
                            background = Color(0xFF7D3F19),
                            contentDescription = stringResource(id = R.string.game_hint_button),
                        )
                    }
                }
                AnimatedVisibility(
                    visible = sweeping,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    MaestroPillButton(
                        text = stringResource(id = R.string.game_event_sweep).uppercase(),
                        onClick = {},
                        variant = PillButtonVariant.HERO_RED,
                        height = 34.dp,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp),
                    )
                }
                AnimatedVisibility(
                    visible = showHint || uiState.isHintLoading,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    MaestroSpeechBubble(
                        message = when {
                            hint != null -> stringResource(id = determineLogicalHintRes(hint, uiState.tableCards))
                            else -> stringResource(id = R.string.game_hint_loading)
                        },
                        tail = BubbleTail.TOP_START,
                        fontSize = 11.5.sp,
                        maxLines = 8,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { hintDismissed = true }
                            .testTag("game_hint_banner"),
                    )
                }
            }
        }

        // Overlay per l'animazione di smazzata e distribuzione carte dal mazzo
        if (currentDealAnim != null) {
            DealAnimationOverlay(
                anim = currentDealAnim,
                tableCards = uiState.tableCards,
            )
        }

        // Overlay per l'animazione di volo della carta giocata e presa
        if (currentAnim != null) {
            MoveFlightOverlay(
                anim = currentAnim,
                humanSeat = uiState.humanSeat,
            )
        }

        // Mano del giocatore a ventaglio con supporto a trascinamento e conferma (invisibile durante la smazzata animata)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(if (hideHands) 0f else 1f)
                .testTag("player_zone"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HandFan(
                uiState = uiState,
                enabled = !hideHands && currentAnim == null,
                onCardClicked = onCardClicked,
            )
        }
    }
}

/**
 * Mano a ventaglio: carte sovrapposte e ruotate.
 * Supporta sia il trascinamento verso il tavolo (drag-to-play) sia la selezione con conferma (tap-to-confirm).
 */
@Composable
private fun HandFan(
    uiState: GameUiState,
    enabled: Boolean = true,
    onCardClicked: (Card) -> Unit,
) {
    val cards = uiState.humanHand
    var selectedCard by remember(cards) { mutableStateOf<Card?>(null) }
    var draggingCard by remember { mutableStateOf<Card?>(null) }
    val dragOffsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val dragPlayThreshold = with(LocalDensity.current) { -75.dp.toPx() }

    val activeSelected = selectedCard?.takeIf { it in cards }

    Column(
        modifier = Modifier.testTag("player_zone_fan"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(
            visible = activeSelected != null && enabled && uiState.isHumanTurn,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (activeSelected != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    MaestroPillButton(
                        text = stringResource(id = R.string.game_card_play_confirm),
                        onClick = {
                            selectedCard = null
                            onCardClicked(activeSelected)
                        },
                        variant = PillButtonVariant.PRIMARY_GREEN,
                        height = 36.dp,
                        fontSize = 14.sp,
                        testTag = "confirm_play_button",
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp),
                    )
                    Text(
                        text = stringResource(id = R.string.game_card_drag_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldAccentColor,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.testTag("human_hand_row"),
            horizontalArrangement = Arrangement.spacedBy((-22).dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            cards.forEachIndexed { i, card ->
                val mid = (cards.size - 1) / 2f
                val angle = (i - mid) * 8f
                val baseDrop = kotlin.math.abs(i - mid) * 12f
                val isSuggested = uiState.suggestedMove?.card == card
                val canPlay = enabled && uiState.isHumanTurn
                val isSelected = activeSelected == card
                val isDragging = draggingCard == card

                val visualOffset = when {
                    isDragging -> dragOffsetY.value
                    isSelected -> -36f
                    else -> 0f
                }
                val visualAngle = if (isDragging || isSelected) 0f else angle
                val visualScale = if (isDragging) 1.08f else if (isSelected) 1.04f else 1f
                val isOverThreshold = isDragging && dragOffsetY.value < dragPlayThreshold

                Box(
                    modifier = Modifier
                        .zIndex(if (isDragging) 10f else if (isSelected) 5f else i.toFloat())
                        .offset { IntOffset(0, (baseDrop.dp.toPx() + visualOffset).roundToInt()) }
                        .rotate(visualAngle)
                        .graphicsLayer {
                            scaleX = visualScale
                            scaleY = visualScale
                        }
                        .draggable(
                            state = rememberDraggableState { delta ->
                                coroutineScope.launch {
                                    dragOffsetY.snapTo(dragOffsetY.value + delta)
                                }
                            },
                            orientation = Orientation.Vertical,
                            enabled = canPlay,
                            onDragStarted = {
                                draggingCard = card
                                selectedCard = card
                            },
                            onDragStopped = {
                                val shouldPlay = dragOffsetY.value < dragPlayThreshold
                                coroutineScope.launch {
                                    dragOffsetY.animateTo(0f, tween(200))
                                }
                                draggingCard = null
                                if (shouldPlay) {
                                    selectedCard = null
                                    onCardClicked(card)
                                }
                            },
                        )
                        .then(
                            if (isOverThreshold) {
                                Modifier.shadow(
                                    elevation = 20.dp,
                                    shape = RoundedCornerShape(10.dp),
                                    ambientColor = GoldAccentColor,
                                    spotColor = GoldAccentColor,
                                )
                            } else if (isSuggested || isSelected || canPlay) {
                                Modifier.shadow(
                                    elevation = if (isSelected) 16.dp else if (isSuggested) 14.dp else 6.dp,
                                    shape = RoundedCornerShape(10.dp),
                                    ambientColor = if (isSelected) GoldAccentColor else Color(0xFFFFD54F),
                                    spotColor = if (isSelected) GoldAccentColor else Color(0xFFFFD54F),
                                )
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    val isSettebello = card.suit == Suit.DENARI && card.rank == Rank.SETTE
                    PlaceholderCardView(
                        card = card,
                        isPlayable = canPlay,
                        showGoldFrame = isSettebello,
                        width = 110.dp,
                        height = 165.dp,
                        modifier = if (isSelected || isOverThreshold) {
                            Modifier.border(3.dp, GoldAccentColor, RoundedCornerShape(6.dp))
                        } else Modifier,
                        onClick = {
                            if (canPlay) {
                                if (isSelected) {
                                    selectedCard = null
                                    onCardClicked(card)
                                } else {
                                    selectedCard = card
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

/**
 * Pila delle prese con il conteggio. Le carte che hanno fatto scopa stanno scoperte e di traverso
 * sotto la pila, sporgendo in basso, come in `reference_table_screen.jpg` (F1-26).
 */
@Composable
private fun CapturePile(count: Int, sweepCards: List<Card>, sweeps: Int, tag: String) {
    Box(
        modifier = Modifier
            .width(86.dp)
            .height(if (sweepCards.isNotEmpty()) 128.dp else 96.dp)
            .testTag(tag),
        contentAlignment = Alignment.TopEnd,
    ) {
        sweepCards.forEachIndexed { i, card ->
            PlaceholderCardView(
                card = card,
                isPlayable = false,
                width = 56.dp,
                height = 84.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-14 - i * 4).dp, y = (6 - i * 4).dp)
                    .rotate(90f)
                    .testTag("${tag}_sweep_$i"),
            )
        }
        Box(modifier = Modifier.align(Alignment.TopEnd), contentAlignment = Alignment.BottomCenter) {
            if (count > 0) {
                DeckStack(cards = count)
            } else {
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(96.dp)
                        .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(8.dp)),
                )
            }
            CountBadge(if (sweeps > sweepCards.size) "$count · ${stringResource(id = R.string.game_sweeps_short, sweeps)}" else "$count")
        }
    }
}

@Composable
private fun CountBadge(text: String) {
    Box(
        modifier = Modifier
            .offset(y = 10.dp)
            .background(Color(0xCC2A170C), RoundedCornerShape(10.dp))
            .border(1.dp, GoldAccentColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 1.dp),
    ) {
        Text(text = text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

/** Pausa: riprendi o torna al menu (la partita in corso resta salvata, F1-08). */
@Composable
private fun PauseDialog(onResume: () -> Unit, onNavigateHome: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onResume) {
        ParchmentPanel(modifier = Modifier.width(300.dp).testTag("pause_dialog")) {
            Text(
                text = stringResource(id = R.string.game_pause).uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF2B1A10),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 10.dp),
            )
            MaestroPillButton(
                text = stringResource(id = R.string.game_resume),
                onClick = onResume,
                variant = PillButtonVariant.PRIMARY_GREEN,
                modifier = Modifier.fillMaxWidth(),
                testTag = "pause_resume_button",
            )
            MaestroPillButton(
                text = stringResource(id = R.string.game_menu_home),
                onClick = onNavigateHome,
                variant = PillButtonVariant.WOOD,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp),
                testTag = "pause_home_button",
            )
        }
    }
}

private const val SWEEP_BANNER_MILLIS = 2_000L
private const val HINT_BUBBLE_MILLIS = 6_000L
private const val MATCH_OVER_PAUSE_MILLIS = 1_200L

// Colori dei testi nei dialoghi su pergamena (F1-23)
private val DialogInk = Color(0xFF2B1A10)
private val DialogInkSoft = Color(0xFF6D5A48)
private val DialogGold = Color(0xFF9C6B00)
private val OptionPaper = Color(0xFFF3E6C8)
