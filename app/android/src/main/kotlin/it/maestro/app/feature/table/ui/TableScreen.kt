package it.maestro.app.feature.table.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.R
import it.maestro.app.domain.audio.Backdrops
import androidx.compose.runtime.DisposableEffect
import it.maestro.app.domain.coach.Chattiness
import it.maestro.app.designsystem.cosmetics.CosmeticStyle
import it.maestro.app.designsystem.audio.NoOpSoundManager
import it.maestro.app.designsystem.audio.SoundEffect
import it.maestro.app.designsystem.audio.SoundManager
import it.maestro.app.designsystem.components.LocalCardBackTheme
import it.maestro.app.domain.time.Clock
import it.maestro.app.feature.table.presentation.GameIntent
import it.maestro.app.feature.table.presentation.GameUiState
import it.maestro.app.feature.table.presentation.GameViewModel
import it.maestro.app.feature.table.presentation.UiGameEvent
import it.maestro.app.feature.table.presentation.director.SceneStep
import it.maestro.app.feature.table.presentation.director.TableScene
import it.maestro.app.feature.table.ui.SweepCelebrationOverlay
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.training.TrainingFundamental
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

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
    showPointsRace: Boolean = false,
    chattiness: Chattiness = Chattiness.NORMAL,
    onChattinessChange: (Chattiness) -> Unit = {},
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
        localHour = clock.localHour(),
        tableThemeId = tableThemeId,
        cardBackThemeId = cardBackThemeId,
        showPointsRace = showPointsRace,
        chattiness = chattiness,
        onChattinessChange = onChattinessChange,
        onNavigateHome = onNavigateHome,
        onViewAnalysis = {
            val results = uiState.trainingResults
            val training = if (results.isEmpty()) null else results.count { it.isCorrect } to results.size
            onViewAnalysis(viewModel.currentMatchLog, training)
        },
        onSkip = viewModel::skipScene,
        onSummaryContinue = viewModel::continueAfterSummary,
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
    localHour: Int = 12,
    tableThemeId: String = "felt_classic_green",
    cardBackThemeId: String = "back_classic_trevigiano",
    showPointsRace: Boolean = false,
    chattiness: Chattiness = Chattiness.NORMAL,
    onChattinessChange: (Chattiness) -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onViewAnalysis: () -> Unit = {},
    onRematch: () -> Unit = { onIntent(GameIntent.StartMatch()) },
    onSkip: () -> Unit = {},
    onSummaryContinue: () -> Unit = { onIntent(GameIntent.DismissDealSummary) },
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

    // Sotto il tavolo suonano il rumore del tavolo e la musica; all'ultima smazzata la musica si fa tesa,
    // e mentre il Maestro parla si abbassa (F1R-20).
    val lastDeal = uiState.deckCardCount == 0 && uiState.humanHand.isNotEmpty()
    LaunchedEffect(lastDeal, localHour) { soundManager.setBackdrop(Backdrops.table(localHour, lastDeal)) }
    val maestroSpeaking = uiState.maestroLine != null
    LaunchedEffect(maestroSpeaking) { soundManager.maestroSpeaking = maestroSpeaking }
    DisposableEffect(Unit) { onDispose { soundManager.maestroSpeaking = false } }

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
    val tableStyle = remember(tableThemeId) { CosmeticStyle.tableStyle(tableThemeId) }
    val tableFeltResId = remember(tableThemeId) {
        tableStyle.feltDrawableRes ?: run {
            val id = context.resources.getIdentifier("table_felt_bg", "drawable", context.packageName)
            if (id != 0) id else null
        }
    }
    val feltTint = tableStyle.feltTint
    val feltBrush = tableStyle.background
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
                        stepKey = step,
                        stepMillis = scene.stepMillis.toInt(),
                        showPointsRace = showPointsRace,
                        onPlay = { card ->
                            soundManager.play(SoundEffect.CARD_PLAY)
                            onIntent(GameIntent.PlayCard(card))
                        },
                        onRequestHint = {
                            soundManager.play(SoundEffect.BUTTON_CLICK)
                            onIntent(GameIntent.RequestHint)
                        },
                        onDismissLine = { onIntent(GameIntent.DismissMaestroLine) },
                        soundManager = soundManager,
                        onAnswerCheck = { index -> onIntent(GameIntent.AnswerTrainingQuestion(index)) },
                        onContinueCheck = { onIntent(GameIntent.DismissTrainingQuestion) },
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
                PauseDialog(
                    onResume = { showPause = false },
                    onNavigateHome = onNavigateHome,
                    chattiness = chattiness,
                    onChattinessChange = onChattinessChange,
                )
            }

            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))

            if (uiState.pendingCaptureChoice != null) {
                CaptureSelectionDialog(
                    choice = uiState.pendingCaptureChoice,
                    onConfirm = { card, capture -> onIntent(GameIntent.SelectCapture(card, capture)) },
                    onCancel = { onIntent(GameIntent.CancelCaptureChoice) },
                )
            }

            // Riepilogo di smazzata (task F1-07): le voci si accendono coi passi della regia, poi si aspetta il giocatore.
            val summary = when (step) {
                is SceneStep.DealSummaryRow -> step.score to (step.row + 1)
                is SceneStep.AwaitDealSummary -> step.score to Int.MAX_VALUE
                else -> null
            }
            if (summary != null) {
                DealSummaryDialog(
                    score = summary.first,
                    uiState = uiState,
                    revealedCount = summary.second,
                    onDismiss = if (step is SceneStep.AwaitDealSummary) onSummaryContinue else onSkip,
                    onSkip = onSkip,
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

