package it.maestro.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
            TableTopBar(uiState = uiState)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 1. ZONA AVVERSARIO (MAESTRO)
            OpponentZone(uiState = uiState)

            Spacer(modifier = Modifier.height(8.dp))

            // 2. STATO TURNO (il punteggio è nella barra superiore)
            ScoreAndStatusHeader(uiState = uiState)

            Spacer(modifier = Modifier.height(12.dp))

            // 3. TAVOLO CENTRALE E MAZZO
            TableCardsAndDeckZone(
                tableCards = uiState.tableCards,
                deckCount = uiState.deckCardCount,
                lastEvent = uiState.lastEvent,
                suggestedCapture = uiState.suggestedMove?.capture,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. ZONA GIOCATORE UMANO
            PlayerZone(
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
private fun TableTopBar(uiState: GameUiState) {
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
            p2Label = stringResource(id = R.string.game_player_maestro),
        )
    }
}

@Composable
private fun OpponentZone(uiState: GameUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("opponent_zone"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(1.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${stringResource(id = R.string.game_sweeps_label)}: ${uiState.opponentSweeps}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                    )
                    Text(
                        text = "${stringResource(id = R.string.game_captured_label)}: ${uiState.opponentCapturedCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Carte coperte in mano all'avversario
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(uiState.opponentCardCount) {
                    CardBackView(width = 44.dp, height = 66.dp)
                }
            }
        }
    }
}

@Composable
private fun ScoreAndStatusHeader(uiState: GameUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("score_and_status_header"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Indicatore di turno
        val statusText = if (uiState.isHumanTurn) {
            stringResource(id = R.string.game_your_turn)
        } else {
            stringResource(id = R.string.game_bot_thinking)
        }

        Box(
            modifier = Modifier
                .background(
                    color = if (uiState.isHumanTurn) GoldAccentColor else Color(0xFF374151),
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(horizontal = 14.dp, vertical = 4.dp),
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                color = if (uiState.isHumanTurn) Color.Black else Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TableCardsAndDeckZone(
    tableCards: List<Card>,
    deckCount: Int,
    lastEvent: UiGameEvent?,
    suggestedCapture: Set<Card>? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("table_cards_zone"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Notifica evento Scopa se presente
            AnimatedVisibility(
                visible = lastEvent is UiGameEvent.Sweep,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .background(GoldAccentColor, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.game_event_sweep),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Tallone (Mazzo coperto)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.testTag("deck_pile"),
                ) {
                    if (deckCount > 0) {
                        Image(
                            painter = painterResource(R.drawable.card_stack),
                            contentDescription = null,
                            modifier = Modifier.width(64.dp),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(72.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(6.dp)),
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$deckCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Carte sul tavolo disposte in griglia flessibile
                FlowRow(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .testTag("table_cards_flow"),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center,
                    maxItemsInEachRow = 4,
                ) {
                    for (card in tableCards) {
                        val isSuggestedCapture = suggestedCapture != null && card in suggestedCapture
                        PlaceholderCardView(
                            card = card,
                            isPlayable = false,
                            width = 70.dp,
                            height = 105.dp,
                            modifier = Modifier
                                .padding(3.dp)
                                .then(
                                    if (isSuggestedCapture) {
                                        Modifier.background(
                                            color = Color(0xFF2196F3).copy(alpha = 0.35f),
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
        }
    }
}

@Composable
private fun PlayerZone(
    uiState: GameUiState,
    onCardClicked: (Card) -> Unit,
    onRequestHint: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_zone"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.game_player_you),
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldAccentColor,
                    fontWeight = FontWeight.Bold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${stringResource(id = R.string.game_sweeps_label)}: ${uiState.humanSweeps}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                    )
                    Text(
                        text = "${stringResource(id = R.string.game_captured_label)}: ${uiState.humanCapturedCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                    )
                }
            }

            // Banner del suggerimento del Maestro se presente
            val suggested = uiState.suggestedMove
            if (suggested != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2196F3).copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF2196F3), shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("game_hint_banner"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.game_hint_banner, formatHintMove(suggested)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Carte in mano al giocatore umano
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("human_hand_row"),
            ) {
                for (card in uiState.humanHand) {
                    val isSuggested = uiState.suggestedMove?.card == card
                    Box(
                        modifier = Modifier.then(
                            if (isSuggested) {
                                Modifier
                                    .background(Color(0xFF2196F3).copy(alpha = 0.35f), shape = RoundedCornerShape(6.dp))
                                    .border(2.dp, Color(0xFF2196F3), shape = RoundedCornerShape(6.dp))
                            } else {
                                Modifier
                            },
                        ),
                    ) {
                        PlaceholderCardView(
                            card = card,
                            isPlayable = uiState.isHumanTurn,
                            width = 94.dp,
                            height = 141.dp,
                            onClick = { onCardClicked(card) },
                        )
                    }
                }
            }

            // Pulsante "Chiedi al Maestro" (solo nelle partite di Sparring, F1-11, D9)
            if (uiState.isSparring) {
                Spacer(modifier = Modifier.height(10.dp))
                MaestroPillButton(
                    text = if (uiState.isHintLoading) {
                        stringResource(id = R.string.game_hint_loading)
                    } else {
                        stringResource(id = R.string.game_hint_button)
                    },
                    onClick = onRequestHint,
                    enabled = uiState.isHumanTurn && !uiState.isBotThinking && !uiState.isHintLoading,
                    variant = PillButtonVariant.EDUCATIONAL_BLUE,
                    height = 46.dp,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "game_hint_button",
                )
            }
        }
    }
}

@Composable
private fun formatHintMove(move: ScopaMove): String {
    val assoStr = stringResource(id = R.string.game_card_rank_asso).take(1)
    val fanteStr = stringResource(id = R.string.game_card_rank_fante).take(1)
    val cavalloStr = stringResource(id = R.string.game_card_rank_cavallo).take(1)
    val reStr = stringResource(id = R.string.game_card_rank_re).take(1)

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
    AlertDialog(
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
                            .border(1.dp, GoldAccentColor, RoundedCornerShape(6.dp))
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

    AlertDialog(
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
                        color = Color.Gray,
                        modifier = Modifier.weight(2f),
                    )
                    Text(
                        text = stringResource(id = R.string.game_player_you),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccentColor,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(id = R.string.game_player_maestro),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
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
                            color = GoldAccentColor,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "$p2Pts",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
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
                        color = GoldAccentColor,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${score.totalPoints(oppSeat)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
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
                        color = Color.LightGray,
                        modifier = Modifier.weight(2f),
                    )
                    Text(
                        text = "${uiState.humanScore}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldAccentColor,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${uiState.opponentScore}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
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
                        color = GoldAccentColor,
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

    AlertDialog(
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
                        color = GoldAccentColor,
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

    AlertDialog(
        onDismissRequest = { /* Modal: interazione richiesta prima di chiudere */ },
        modifier = Modifier.testTag("training_question_dialog"),
        title = {
            Column {
                Text(
                    text = stringResource(id = R.string.training_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccentColor,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.training_dialog_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
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
                    color = Color.White,
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
                                color = Color.White,
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
