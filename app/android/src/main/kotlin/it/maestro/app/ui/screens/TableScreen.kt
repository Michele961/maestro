package it.maestro.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import it.maestro.app.ui.components.ParchmentPanel
import it.maestro.app.ui.components.FramedPortrait
import it.maestro.app.ui.components.MaestroDialogueBox
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.shadow
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
    onViewAnalysis: (MatchLog) -> Unit = {},
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
        onViewAnalysis = { onViewAnalysis(viewModel.currentMatchLog) },
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

        // Dialog fine partita (mostrato a partita finita se non c'è il riepilogo smazzata aperto)
        if (uiState.phase == DealPhase.MATCH_OVER && uiState.pendingDealScore == null) {
            MatchOverDialog(
                uiState = uiState,
                onRematch = onRematch,
                onViewAnalysis = onViewAnalysis,
                onNavigateHome = onNavigateHome,
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
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("deal_summary_continue_button"),
            ) {
                Text(text = stringResource(id = R.string.deal_summary_continue))
            }
        },
    )
}

@Composable
private fun MatchOverDialog(
    uiState: GameUiState,
    onRematch: () -> Unit,
    onViewAnalysis: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
) {
    val winnerTitle = when {
        uiState.humanScore > uiState.opponentScore -> stringResource(id = R.string.game_winner_you)
        uiState.opponentScore > uiState.humanScore -> stringResource(id = R.string.game_winner_maestro)
        else -> stringResource(id = R.string.game_tie)
    }

    MaestroAlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = winnerTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "${stringResource(id = R.string.game_score_label)}: ${uiState.humanScore} — ${uiState.opponentScore}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (uiState.trainingResults.isNotEmpty()) {
                    val correct = uiState.trainingResults.count { it.isCorrect }
                    val total = uiState.trainingResults.size
                    val pct = (correct * 100) / total
                    Text(
                        text = stringResource(id = R.string.training_score_label, pct, correct, total),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DialogGold,
                        modifier = Modifier.testTag("match_over_attention_score"),
                    )
                }
                Button(
                    onClick = onViewAnalysis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("view_analysis_button"),
                ) {
                    Text(text = stringResource(id = R.string.post_match_view_analysis))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRematch,
                modifier = Modifier.testTag("rematch_button"),
            ) {
                Text(text = stringResource(id = R.string.game_rematch_button))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onNavigateHome,
                modifier = Modifier.testTag("navigate_home_button"),
            ) {
                Text(text = stringResource(id = R.string.game_menu_home))
            }
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

                        val borderCol = when {
                            !isAnswered -> Color.DarkGray
                            isCorrectOption -> Color(0xFF4CAF50)
                            isSelected -> Color(0xFFE57373)
                            else -> Color.DarkGray
                        }
                        val bgCol = when {
                            !isAnswered -> TableFeltDarkColor
                            isCorrectOption -> Color(0xFF1B5E20)
                            isSelected -> Color(0xFFB71C1C)
                            else -> TableFeltDarkColor
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("training_option_$index")
                                .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                                .clickable(enabled = feedback == null) {
                                    soundManager.play(SoundEffect.BUTTON_CLICK)
                                    onAnswer(index)
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = bgCol,
                        ) {
                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected || isCorrectOption) FontWeight.Bold else FontWeight.Normal,
                                color = DialogInk,
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
                            color = DialogInk,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (feedback != null) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier.testTag("training_continue_button"),
                ) {
                    Text(text = stringResource(id = R.string.training_feedback_continue))
                }
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

/**
 * Disposizione del tavolo come `reference_table_screen.jpg` (F1-21): mano dell'avversario in alto,
 * carte in tavola al centro, Maestro con fumetto a sinistra, mazzo e prese a destra, mano a ventaglio in basso.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeltLayout(
    uiState: GameUiState,
    onCardClicked: (Card) -> Unit,
    onRequestHint: () -> Unit,
) {
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CapturePile(count = uiState.opponentCapturedCount, sweeps = uiState.opponentSweeps, tag = "opponent_captures")
            Box(modifier = Modifier.testTag("deck_pile"), contentAlignment = Alignment.BottomCenter) {
                if (uiState.deckCardCount > 0) DeckStack(cards = uiState.deckCardCount)
                CountBadge(uiState.deckCardCount.toString())
            }
            CapturePile(count = uiState.humanCapturedCount, sweeps = uiState.humanSweeps, tag = "human_captures")
        }

        // Carte in tavola, al centro in griglia
        FlowRow(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = if (uiState.suggestedMove != null) 128.dp else 30.dp, end = 86.dp, bottom = 40.dp)
                .testTag("table_cards_zone"),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = if (uiState.tableCards.size <= 4) 2 else 3,
        ) {
            val suggested = uiState.suggestedMove?.capture
            for (card in uiState.tableCards) {
                val big = uiState.tableCards.size <= 4 && uiState.suggestedMove == null
                PlaceholderCardView(
                    card = card,
                    isPlayable = false,
                    width = if (big) 92.dp else 72.dp,
                    height = if (big) 138.dp else 108.dp,
                    modifier = if (suggested != null && card in suggested) {
                        Modifier.border(3.dp, Color(0xFF42A5F5), RoundedCornerShape(8.dp))
                    } else {
                        Modifier
                    },
                )
            }
        }

        // Il Maestro a sinistra, con il fumetto quando ha qualcosa da dire
        Column(
            modifier = Modifier.align(Alignment.CenterStart).padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(visible = uiState.suggestedMove != null, enter = fadeIn(), exit = fadeOut()) {
                val move = uiState.suggestedMove
                if (move != null) {
                    MaestroDialogueBox(
                        message = formatHintMove(move),
                        title = stringResource(id = R.string.game_player_maestro),
                        modifier = Modifier.width(120.dp).testTag("game_hint_banner"),
                    )
                }
            }
            AnimatedVisibility(visible = uiState.lastEvent is UiGameEvent.Sweep, enter = fadeIn(), exit = fadeOut()) {
                MaestroPillButton(
                    text = stringResource(id = R.string.game_event_sweep).uppercase(),
                    onClick = {},
                    variant = PillButtonVariant.HERO_RED,
                    height = 44.dp,
                    fontSize = 20.sp,
                    modifier = Modifier.width(150.dp),
                )
            }
            if (uiState.suggestedMove != null || uiState.lastEvent is UiGameEvent.Sweep) {
                FramedPortrait(
                    portrait = if (uiState.lastEvent is UiGameEvent.Sweep) R.drawable.maestro_celebrating else R.drawable.maestro_pleased,
                    size = 84.dp,
                    background = Color(0xFF7D3F19),
                )
            }
        }

        // Mano del giocatore a ventaglio
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).testTag("player_zone"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HandFan(uiState = uiState, onCardClicked = onCardClicked)
            if (uiState.isSparring) {
                MaestroPillButton(
                    text = if (uiState.isHintLoading) {
                        stringResource(id = R.string.game_hint_loading)
                    } else {
                        stringResource(id = R.string.game_hint_button)
                    },
                    onClick = onRequestHint,
                    enabled = uiState.isHumanTurn && !uiState.isBotThinking && !uiState.isHintLoading,
                    variant = PillButtonVariant.EDUCATIONAL_BLUE,
                    height = 42.dp,
                    fontSize = 16.sp,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.padding(top = 6.dp),
                    testTag = "game_hint_button",
                )
            }
        }
    }
}

/** Mano a ventaglio: carte sovrapposte e ruotate; quando tocca a te sono più alte e brillano. */
@Composable
private fun HandFan(uiState: GameUiState, onCardClicked: (Card) -> Unit) {
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
            Box(
                modifier = Modifier
                    .offset(y = drop.dp)
                    .rotate(angle)
                    .then(
                        if (isSuggested || uiState.isHumanTurn) {
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
                    isPlayable = uiState.isHumanTurn,
                    width = 110.dp,
                    height = 165.dp,
                    onClick = { onCardClicked(card) },
                )
            }
        }
    }
}

/** Pila delle prese, con il conteggio delle carte e delle scope. */
@Composable
private fun CapturePile(count: Int, sweeps: Int, tag: String) {
    Box(modifier = Modifier.testTag(tag), contentAlignment = Alignment.BottomCenter) {
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
        CountBadge(if (sweeps > 0) "$count · ${stringResource(id = R.string.game_sweeps_short, sweeps)}" else "$count")
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

// Colori dei testi nei dialoghi su pergamena (F1-23)
private val DialogInk = Color(0xFF2B1A10)
private val DialogInkSoft = Color(0xFF6D5A48)
private val DialogGold = Color(0xFF9C6B00)
