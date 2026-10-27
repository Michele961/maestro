package it.maestro.app.feature.table.ui

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
import it.maestro.app.designsystem.components.LocalCardBackTheme
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import it.maestro.app.feature.table.presentation.OpponentMoveSummary
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import it.maestro.app.designsystem.components.ParchmentPanel
import it.maestro.app.designsystem.components.FramedPortrait
import it.maestro.app.designsystem.components.BubbleTail
import it.maestro.app.designsystem.components.MaestroSpeechBubble
import it.maestro.app.feature.table.ui.SweepCelebrationOverlay
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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import it.maestro.app.designsystem.audio.NoOpSoundManager
import it.maestro.app.designsystem.audio.SoundEffect
import it.maestro.app.designsystem.audio.SoundManager
import it.maestro.app.feature.table.presentation.GameIntent
import it.maestro.app.feature.table.presentation.GameUiState
import it.maestro.app.feature.table.presentation.GameViewModel
import it.maestro.app.feature.table.presentation.PendingCaptureChoice
import it.maestro.app.feature.table.presentation.TrainingAnswerFeedback
import it.maestro.app.feature.table.presentation.UiGameEvent
import it.maestro.app.feature.table.presentation.director.SceneStep
import it.maestro.app.feature.table.presentation.director.TableScene
import it.maestro.app.designsystem.components.CardBackView
import it.maestro.app.designsystem.components.MaestroAlertDialog
import it.maestro.app.designsystem.components.MaestroAvatarView
import it.maestro.app.designsystem.components.MaestroMood
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.NineSliceBox
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.PlaceholderCardView
import it.maestro.app.designsystem.components.ScorePlaque
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.DealScore
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScoreCategory
import it.maestro.game.training.SeatLead
import it.maestro.game.training.SettebelloLocation
import it.maestro.game.training.TrainingFundamental
import it.maestro.game.training.TrainingOption
import it.maestro.game.training.TrainingQuestion
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import it.maestro.app.domain.time.Clock
import androidx.compose.runtime.saveable.rememberSaveable

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
    resume: Boolean = false,
    tableThemeId: String = "felt_classic_green",
    cardBackThemeId: String = "back_classic_trevigiano",
    viewModel: GameViewModel = koinViewModel(),
    soundManager: SoundManager = koinInject(),
    clock: Clock = koinInject(),
    onNavigateHome: () -> Unit = {},
    onViewAnalysis: (MatchLog, Pair<Int, Int>?) -> Unit = { _, _ -> },
) {
    // La partita parte una volta sola: dopo una rotazione il ViewModel la conserva; dopo la chiusura del
    // processo il ViewModel è nuovo ma `started` sopravvive, quindi si riprende quella salvata (F1R-04).
    var started by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (viewModel.hasMatch) return@LaunchedEffect
        if (resume || started) {
            viewModel.resumeActiveMatch(trainingFundamental, onUnavailable = onNavigateHome)
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
        started = true
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scene by viewModel.scene.collectAsStateWithLifecycle(initialValue = viewModel.currentScene)
    TableContent(
        uiState = uiState,
        scene = scene,
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
        onSkip = viewModel::skipScene,
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

@Composable
public fun TableContent(
    uiState: GameUiState,
    scene: TableScene,
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier = Modifier,
    soundManager: SoundManager = NoOpSoundManager(),
    tableThemeId: String = "felt_classic_green",
    cardBackThemeId: String = "back_classic_trevigiano",
    onNavigateHome: () -> Unit = {},
    onViewAnalysis: () -> Unit = {},
    onRematch: () -> Unit = { onIntent(GameIntent.StartMatch()) },
    onSkip: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showPause by remember { mutableStateOf(false) }
    val step = scene.step

    // Suoni dai passi della regia, non dai composable (`table-director.md` §4.7).
    LaunchedEffect(scene.beatId, step) {
        when (step) {
            is SceneStep.CardToPlayZone, is SceneStep.OpponentReveal, is SceneStep.DropToTable,
            is SceneStep.Shuffle, is SceneStep.DealCard -> soundManager.play(SoundEffect.CARD_PLAY)
            is SceneStep.CaptureFlight -> soundManager.play(SoundEffect.CARD_CAPTURE)
            is SceneStep.Sweep -> soundManager.play(SoundEffect.SWEEP)
            is SceneStep.DealSummaryRow -> if (step.row == 0) soundManager.play(SoundEffect.CARD_CAPTURE)
            else -> {}
        }
    }

    // A fine partita, a scena ferma e riepilogo chiuso, si passa alla schermata di fine partita.
    val matchOver = uiState.phase == DealPhase.MATCH_OVER && uiState.pendingDealScore == null && !scene.busy
    LaunchedEffect(matchOver) {
        if (matchOver) onViewAnalysis()
    }

    LaunchedEffect(uiState.errorMessage) {
        val error = uiState.errorMessage
        if (error != null) {
            snackbarHostState.showSnackbar(message = error, duration = SnackbarDuration.Short)
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
    val feltBrush = remember(tableThemeId) { feltBrushFor(tableThemeId) }
    val model = remember(scene) { StageModel.from(scene) }

    CompositionLocalProvider(LocalCardBackTheme provides cardBackThemeId) {
        Box(modifier = modifier.fillMaxSize().background(feltBrush).testTag("table_screen")) {
            // Cornice in legno attorno al panno, come in reference_table_screen.jpg (F1-21)
            Image(
                painter = painterResource(R.drawable.texture_wood),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(8.dp)
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
                val geometry = remember(maxWidth, maxHeight) { TableGeometry.from(maxWidth - 16.dp, maxHeight - 16.dp) }
                Box(modifier = Modifier.padding(8.dp)) {
                    OpponentBar(
                        uiState = uiState,
                        model = model,
                        zone = geometry.zones.opponentBar,
                        sweepBy = (step as? SceneStep.Sweep)?.actor,
                        onPause = { showPause = true },
                    )
                    TableStage(
                        model = model,
                        uiState = uiState,
                        geometry = geometry,
                        canPlay = scene.inputEnabled && uiState.isHumanTurn,
                        atRest = !scene.busy,
                        sweepBy = (step as? SceneStep.Sweep)?.actor,
                        onPlay = { card ->
                            soundManager.play(SoundEffect.CARD_PLAY)
                            onIntent(GameIntent.PlayCard(card))
                        },
                        onRequestHint = {
                            soundManager.play(SoundEffect.BUTTON_CLICK)
                            onIntent(GameIntent.RequestHint)
                        },
                    )
                }
                if (scene.busy) {
                    // Un tocco sul tavolo salta la scena (`table-director.md` §4.3).
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onSkip)
                            .testTag("skip_scene_layer"),
                    )
                }
            }

            if (showPause) {
                PauseDialog(onResume = { showPause = false }, onNavigateHome = onNavigateHome)
            }

            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))

            // Dialog Check di allenamento del Maestro (task F1-12): a scena ferma, mai sopra una giocata.
            val question = uiState.activeTrainingQuestion
            if (question != null && !scene.busy) {
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

            if (uiState.pendingCaptureChoice != null) {
                CaptureSelectionDialog(
                    choice = uiState.pendingCaptureChoice,
                    onConfirm = { card, capture -> onIntent(GameIntent.SelectCapture(card, capture)) },
                    onCancel = { onIntent(GameIntent.CancelCaptureChoice) },
                )
            }

            // Dialog riepilogo smazzata (task F1-07), dopo i passi della regia.
            val dealScore = uiState.pendingDealScore
            if (dealScore != null && !scene.busy) {
                DealSummaryDialog(
                    score = dealScore,
                    uiState = uiState,
                    onDismiss = { onIntent(GameIntent.DismissDealSummary) },
                )
            }

            // Overlay celebrativo Scopa (task F1-25): dura quanto il passo che lo mostra.
            if (step is SceneStep.Sweep) {
                SweepCelebrationOverlay(
                    event = UiGameEvent.Sweep(step.actor, scene.after?.sweeps?.get(step.actor) ?: 1),
                    isHuman = step.actor == step.humanSeat,
                    onDismiss = onSkip,
                )
            }
        }
    }
}

private fun feltBrushFor(tableThemeId: String): Brush = when (tableThemeId) {
    "felt_royal_red" -> Brush.radialGradient(listOf(Color(0xFF8B1E2B), Color(0xFF5E121C), Color(0xFF38080F), Color(0xFF1E0307)))
    "felt_noble_blue" -> Brush.radialGradient(listOf(Color(0xFF1F487E), Color(0xFF14325C), Color(0xFF0C1F3B), Color(0xFF06101F)))
    "felt_walnut_wood" -> Brush.radialGradient(listOf(Color(0xFF4A2810), Color(0xFF331808), Color(0xFF220E04), Color(0xFF120702)))
    else -> Brush.radialGradient(listOf(Color(0xFF225740), Color(0xFF1B4332), Color(0xFF0D281E), Color(0xFF081C15)))
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

internal fun determineLogicalHintRes(
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


// Colori dei testi nei dialoghi su pergamena (F1-23)
private val DialogInk = Color(0xFF2B1A10)
private val DialogInkSoft = Color(0xFF6D5A48)
private val DialogGold = Color(0xFF9C6B00)
private val OptionPaper = Color(0xFFF3E6C8)
