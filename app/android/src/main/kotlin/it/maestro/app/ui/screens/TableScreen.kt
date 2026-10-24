package it.maestro.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.R
import it.maestro.app.game.GameIntent
import it.maestro.app.game.GameUiState
import it.maestro.app.game.GameViewModel
import it.maestro.app.game.PendingCaptureChoice
import it.maestro.app.game.UiGameEvent
import it.maestro.app.ui.components.CardBackView
import it.maestro.app.ui.components.PlaceholderCardView
import it.maestro.game.model.Card
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.DealScore
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaState
import it.maestro.game.scopa.ScoreCategory
import org.koin.androidx.compose.koinViewModel

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
    seed: Long = 42L,
    restoreState: ScopaState? = null,
    restoreLog: MatchLog? = null,
    viewModel: GameViewModel = koinViewModel(),
    onNavigateHome: () -> Unit = {},
    onViewAnalysis: (MatchLog) -> Unit = {},
) {
    LaunchedEffect(level, targetScore, seed, restoreState) {
        if (restoreState != null && restoreLog != null) {
            viewModel.onIntent(
                GameIntent.RestoreMatch(
                    state = restoreState,
                    matchLog = restoreLog,
                    level = level,
                ),
            )
        } else {
            viewModel.onIntent(
                GameIntent.StartMatch(
                    config = ScopaConfig(targetScore = targetScore),
                    level = level,
                    seed = seed,
                ),
            )
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TableContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        onNavigateHome = onNavigateHome,
        onViewAnalysis = { onViewAnalysis(viewModel.currentMatchLog) },
        onRematch = {
            viewModel.onIntent(
                GameIntent.StartMatch(
                    config = ScopaConfig(targetScore = targetScore),
                    level = level,
                    seed = System.currentTimeMillis(),
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
    onNavigateHome: () -> Unit = {},
    onViewAnalysis: () -> Unit = {},
    onRematch: () -> Unit = { onIntent(GameIntent.StartMatch()) },
) {
    val snackbarHostState = remember { SnackbarHostState() }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TableFeltColor)
            .testTag("table_screen"),
    ) {
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

            // 2. TABELLONE PUNTEGGIO E STATO TURNO
            ScoreAndStatusHeader(uiState = uiState)

            Spacer(modifier = Modifier.height(12.dp))

            // 3. TAVOLO CENTRALE E MAZZO
            TableCardsAndDeckZone(
                tableCards = uiState.tableCards,
                deckCount = uiState.deckCardCount,
                lastEvent = uiState.lastEvent,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. ZONA GIOCATORE UMANO
            PlayerZone(
                uiState = uiState,
                onCardClicked = { card -> onIntent(GameIntent.PlayCard(card)) },
            )
        }

        // Host per notifiche di errore
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

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

@Composable
private fun OpponentZone(uiState: GameUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("opponent_zone"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TableFeltDarkColor),
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
                    text = stringResource(id = R.string.game_player_maestro),
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldAccentColor,
                    fontWeight = FontWeight.Bold,
                )
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
        // Punteggio complessivo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${stringResource(id = R.string.game_player_you)}: ${uiState.humanScore}",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "—",
                style = MaterialTheme.typography.titleLarge,
                color = GoldAccentColor,
            )
            Text(
                text = "${stringResource(id = R.string.game_player_maestro)}: ${uiState.opponentScore}",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

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
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("table_cards_zone"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14382A)),
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
                        CardBackView(width = 48.dp, height = 72.dp)
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
                    maxItemsInEachRow = 5,
                ) {
                    for (card in tableCards) {
                        PlaceholderCardView(
                            card = card,
                            isPlayable = false,
                            width = 50.dp,
                            height = 75.dp,
                            modifier = Modifier.padding(3.dp),
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
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_zone"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TableFeltDarkColor),
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

            Spacer(modifier = Modifier.height(8.dp))

            // Carte in mano al giocatore umano
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("human_hand_row"),
            ) {
                for (card in uiState.humanHand) {
                    PlaceholderCardView(
                        card = card,
                        isPlayable = uiState.isHumanTurn,
                        width = 64.dp,
                        height = 96.dp,
                        onClick = { onCardClicked(card) },
                    )
                }
            }
        }
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
