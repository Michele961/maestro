package it.maestro.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import it.maestro.app.ui.components.ParchmentPanel
import it.maestro.app.ui.components.FramedPortrait
import it.maestro.app.ui.components.MaestroSpeechBubble
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
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
    viewModel: GameViewModel = koinViewModel(),
    soundManager: SoundManager = koinInject(),
    onNavigateHome: () -> Unit = {},
    onViewAnalysis: (MatchLog, Pair<Int, Int>?) -> Unit = { _, _ -> },
) {
    LaunchedEffect(level, targetScore, seed, restoreState, trainingFundamental) {
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
                    seed = System.currentTimeMillis(),
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
    onNavigateHome: () -> Unit = {},
    onViewAnalysis: () -> Unit = {},
    onRematch: () -> Unit = { onIntent(GameIntent.StartMatch()) },
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showPause by remember { mutableStateOf(false) }

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
    val tableFeltResId = remember {
        val id = context.resources.getIdentifier("table_felt_bg", "drawable", context.packageName)
        if (id != 0) id else null
    }

    val feltBrush = remember {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF225740),
                Color(0xFF1B4332),
                Color(0xFF0D281E),
                Color(0xFF081C15),
            ),
        )
    }

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
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(3.dp, GoldAccentColor, RoundedCornerShape(14.dp)),
            ) {
                if (tableFeltResId != null) {
                    Image(
                        painter = painterResource(id = tableFeltResId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
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
        val question = uiState.activeTrainingQuestion
        if (question != null) {
            TrainingQuestionDialog(
                question = question,
                feedback = uiState.trainingQuestionFeedback,
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

    }
}

/** Barra superiore su legno: avversario con espressione e punteggio della partita (F1-21). */
@Composable
private fun TableTopBar(uiState: GameUiState, onPause: () -> Unit) {
    val mood = when {
        uiState.lastEvent is UiGameEvent.Sweep -> MaestroMood.CELEBRATING
        uiState.isBotThinking || uiState.isHintLoading -> MaestroMood.THINKING
        uiState.trainingQuestionFeedback?.isCorrect == true -> MaestroMood.PLEASED
        uiState.trainingQuestionFeedback?.isCorrect == false -> MaestroMood.WARNING
        else -> MaestroMood.NEUTRAL
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("table_top_bar"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MaestroAvatarView(mood = mood, size = 60.dp)
        NineSliceBox(
            skin = R.drawable.skin_name_plaque,
            slice = 0.35f,
            corner = 14.dp,
            modifier = Modifier.padding(start = 4.dp),
        ) {
            Text(
                text = stringResource(id = R.string.game_player_maestro),
                style = MaterialTheme.typography.titleMedium,
                color = GoldAccentColor,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
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
                .size(52.dp)
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

                for ((category, labelResId) in categories) {
                    val tag = "deal_summary_${category.name.lowercase()}"
                    val p1Pts = score.points(humanSeat, category)
                    val p2Pts = score.points(oppSeat, category)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            color = DialogGold,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "$p2Pts",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = DialogInk,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                HorizontalDivider(color = Color.DarkGray)

                // Totale smazzata
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
    val pileX = 130f
    val pileY = if (isHuman) 130f else -130f

    val offsetX = remember(anim.moveId) { Animatable(0f) }
    val offsetY = remember(anim.moveId) { Animatable(startOffsetY) }
    val scale = remember(anim.moveId) { Animatable(0.9f) }
    val alpha = remember(anim.moveId) { Animatable(0.85f) }

    LaunchedEffect(anim.stage) {
        when (anim.stage) {
            MoveAnimStage.PLAYING_TO_TABLE -> {
                launch { offsetY.animateTo(0f, tween(360, easing = FastOutSlowInEasing)) }
                launch { scale.animateTo(1.15f, tween(360, easing = FastOutSlowInEasing)) }
                launch { alpha.animateTo(1f, tween(200)) }
            }
            MoveAnimStage.SHOWING_CAPTURE -> {
                offsetY.snapTo(0f)
                scale.snapTo(1.15f)
                alpha.snapTo(1f)
            }
            MoveAnimStage.FLYING_TO_PILE -> {
                launch { offsetX.animateTo(pileX, tween(360, easing = FastOutSlowInEasing)) }
                launch { offsetY.animateTo(pileY, tween(360, easing = FastOutSlowInEasing)) }
                launch { scale.animateTo(0.5f, tween(360, easing = FastOutSlowInEasing)) }
                launch { alpha.animateTo(0.15f, tween(360)) }
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
                            translationX = offsetX.value.dp.toPx()
                            translationY = offsetY.value.dp.toPx()
                            scaleX = scale.value * 0.9f
                            scaleY = scale.value * 0.9f
                            this.alpha = alpha.value
                        }
                        .shadow(6.dp, RoundedCornerShape(8.dp)),
                ) {
                    PlaceholderCardView(
                        card = capturedCard,
                        isPlayable = false,
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
                    translationX = offsetX.value.dp.toPx()
                    translationY = offsetY.value.dp.toPx()
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                }
                .shadow(12.dp, RoundedCornerShape(8.dp)),
        ) {
            PlaceholderCardView(
                card = anim.card,
                isPlayable = false,
                width = 82.dp,
                height = 123.dp,
                modifier = if (anim.capture.isNotEmpty() && anim.stage == MoveAnimStage.SHOWING_CAPTURE) {
                    Modifier.border(3.dp, GoldAccentColor, RoundedCornerShape(8.dp))
                } else Modifier,
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
) {
    var activeMoveAnim by remember { mutableStateOf<ActiveMoveAnimation?>(null) }

    LaunchedEffect(uiState.lastMoveId) {
        val move = uiState.lastMove ?: return@LaunchedEffect
        val mover = uiState.lastMover ?: return@LaunchedEffect
        if (uiState.lastMoveId == 0L) return@LaunchedEffect

        val isSweep = uiState.lastEvent is UiGameEvent.Sweep
        // 1. Carta parte dalla mano e vola al centro del tavolo
        activeMoveAnim = ActiveMoveAnimation(
            moveId = uiState.lastMoveId,
            mover = mover,
            card = move.card,
            capture = move.capture,
            isSweep = isSweep,
            stage = MoveAnimStage.PLAYING_TO_TABLE,
        )
        delay(360L)

        if (move.capture.isNotEmpty()) {
            // 2. Le carte catturate sul tavolo si evidenziano e il bot annuncia la presa
            activeMoveAnim = activeMoveAnim?.copy(stage = MoveAnimStage.SHOWING_CAPTURE)
            delay(480L)

            // 3. Volo congiunto verso la pila delle prese
            activeMoveAnim = activeMoveAnim?.copy(stage = MoveAnimStage.FLYING_TO_PILE)
            soundManager.play(SoundEffect.CARD_CAPTURE)
            delay(360L)
        } else {
            delay(160L)
        }
        activeMoveAnim = null
    }

    val formatCard = rememberCardFormatter()
    val botPlaying = activeMoveAnim != null && activeMoveAnim?.mover != uiState.humanSeat
    val botSpeechMessage: String? = if (botPlaying) {
        val anim = activeMoveAnim!!
        val cardStr = formatCard(anim.card)
        if (anim.capture.isEmpty()) {
            stringResource(id = R.string.game_bot_plays_card, cardStr)
        } else {
            val takesStr = anim.capture.joinToString(", ") { formatCard(it) }
            stringResource(id = R.string.game_bot_takes_cards, cardStr, takesStr)
        }
    } else null

    // Le carte catturate restano visibili sul tavolo finché non volano alla pila
    val visibleTableCards = remember(uiState.tableCards, activeMoveAnim) {
        val anim = activeMoveAnim
        if (anim != null && anim.capture.isNotEmpty() && anim.stage != MoveAnimStage.FLYING_TO_PILE) {
            (uiState.tableCards + anim.capture).distinct()
        } else {
            uiState.tableCards
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(10.dp).testTag("felt_layout")) {
        // Mano dell'avversario, coperta e sovrapposta
        Row(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp).testTag("opponent_zone"),
            horizontalArrangement = Arrangement.spacedBy((-26).dp),
        ) {
            repeat(uiState.opponentCardCount) { CardBackView(width = 72.dp, height = 108.dp) }
        }

        // Colonna destra: mazzo e prese
        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            CapturePile(count = uiState.opponentCapturedCount, sweepCards = uiState.opponentSweepCards, sweeps = uiState.opponentSweeps, tag = "opponent_captures")
            Box(modifier = Modifier.testTag("deck_pile"), contentAlignment = Alignment.BottomCenter) {
                if (uiState.deckCardCount > 0) DeckStack(cards = uiState.deckCardCount)
                CountBadge(uiState.deckCardCount.toString())
            }
            CapturePile(count = uiState.humanCapturedCount, sweepCards = uiState.humanSweepCards, sweeps = uiState.humanSweeps, tag = "human_captures")
        }

        // Il Maestro a sinistra, come nel mockup: fumetto compatto che sparisce da solo o con un tap.
        // In Sparring il ritratto resta visibile e toccarlo chiede il consiglio (niente pulsante extra).
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
        val canAskHint = uiState.isSparring && uiState.isHumanTurn && !uiState.isBotThinking && !uiState.isHintLoading && activeMoveAnim == null
        val showLeftBubble = showHint || uiState.isHintLoading || sweeping || botPlaying

        // Carte in tavola, al centro in griglia
        FlowRow(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(
                    start = when {
                        showLeftBubble -> 108.dp
                        uiState.isSparring -> 80.dp
                        else -> 24.dp
                    },
                    end = 86.dp,
                    bottom = 40.dp,
                )
                .testTag("table_cards_zone"),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = if (visibleTableCards.size <= 4) 2 else 3,
        ) {
            val suggested = uiState.suggestedMove?.capture
            for (card in visibleTableCards) {
                val big = visibleTableCards.size <= 4 && !showLeftBubble
                val isBeingCaptured = activeMoveAnim != null && card in activeMoveAnim!!.capture && activeMoveAnim!!.stage == MoveAnimStage.SHOWING_CAPTURE
                PlaceholderCardView(
                    card = card,
                    isPlayable = false,
                    width = if (big) 82.dp else 72.dp,
                    height = if (big) 123.dp else 108.dp,
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

        Column(
            modifier = Modifier.align(Alignment.CenterStart).padding(bottom = 150.dp).width(135.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            AnimatedVisibility(visible = showHint || uiState.isHintLoading || botPlaying, enter = fadeIn(), exit = fadeOut()) {
                MaestroSpeechBubble(
                    message = when {
                        botSpeechMessage != null -> botSpeechMessage
                        hint != null -> formatHintMove(hint)
                        else -> stringResource(id = R.string.game_hint_loading)
                    },
                    fontSize = if (botSpeechMessage != null) 11.sp else 12.sp,
                    modifier = Modifier
                        .clickable { hintDismissed = true }
                        .testTag("game_hint_banner"),
                )
            }
            AnimatedVisibility(visible = sweeping, enter = fadeIn(), exit = fadeOut()) {
                MaestroPillButton(
                    text = stringResource(id = R.string.game_event_sweep).uppercase(),
                    onClick = {},
                    variant = PillButtonVariant.HERO_RED,
                    height = 38.dp,
                    fontSize = 17.sp,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                )
            }
            AnimatedVisibility(visible = uiState.isSparring || showHint || sweeping || botPlaying, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .alpha(if (canAskHint || showHint || sweeping || botPlaying) 1f else 0.7f)
                        .clickable(enabled = canAskHint, onClick = onRequestHint)
                        .testTag("game_hint_button"),
                ) {
                    FramedPortrait(
                        portrait = when {
                            sweeping -> R.drawable.maestro_celebrating
                            uiState.isBotThinking -> R.drawable.maestro_thinking
                            botPlaying -> R.drawable.maestro_pleased
                            showHint -> R.drawable.maestro_pleased
                            else -> R.drawable.maestro_neutral
                        },
                        size = 72.dp,
                        background = Color(0xFF7D3F19),
                        contentDescription = stringResource(id = R.string.game_hint_button),
                    )
                }
            }
        }

        // Overlay per l'animazione di volo della carta giocata e presa
        if (activeMoveAnim != null) {
            MoveFlightOverlay(
                anim = activeMoveAnim!!,
                humanSeat = uiState.humanSeat,
            )
        }

        // Mano del giocatore a ventaglio
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).testTag("player_zone"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HandFan(
                uiState = uiState,
                enabled = activeMoveAnim == null,
                onCardClicked = onCardClicked,
            )
        }
    }
}

/** Mano a ventaglio: carte sovrapposte e ruotate; quando tocca a te sono più alte e brillano. */
@Composable
private fun HandFan(
    uiState: GameUiState,
    enabled: Boolean = true,
    onCardClicked: (Card) -> Unit,
) {
    val cards = uiState.humanHand
    Row(
        modifier = Modifier.testTag("human_hand_row"),
        horizontalArrangement = Arrangement.spacedBy((-22).dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        cards.forEachIndexed { i, card ->
            val mid = (cards.size - 1) / 2f
            val angle = (i - mid) * 8f
            val drop = kotlin.math.abs(i - mid) * 12f
            val isSuggested = uiState.suggestedMove?.card == card
            val canPlay = enabled && uiState.isHumanTurn
            Box(
                modifier = Modifier
                    .offset(y = drop.dp)
                    .rotate(angle)
                    .then(
                        if (isSuggested || canPlay) {
                            Modifier.shadow(
                                elevation = if (isSuggested) 18.dp else 8.dp,
                                shape = RoundedCornerShape(10.dp),
                                ambientColor = Color(0xFFFFD54F),
                                spotColor = Color(0xFFFFD54F),
                            )
                        } else {
                            Modifier
                        },
                    ),
            ) {
                PlaceholderCardView(
                    card = card,
                    isPlayable = canPlay,
                    width = 110.dp,
                    height = 165.dp,
                    onClick = {
                        if (canPlay) {
                            onCardClicked(card)
                        }
                    },
                )
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
