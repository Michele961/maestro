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
import org.koin.androidx.compose.koinViewModel

public val TableFeltColor: Color = Color(0xFF1B4332)
public val TableFeltDarkColor: Color = Color(0xFF081C15)
public val GoldAccentColor: Color = Color(0xFFD4AF37)

/**
 * Schermata di gioco del tavolo di Scopa (ADR-0012, task F1-06).
 */
@Composable
public fun TableScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = koinViewModel(),
    onNavigateHome: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TableContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        onNavigateHome = onNavigateHome,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun TableContent(
    uiState: GameUiState,
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateHome: () -> Unit = {},
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

        // Dialog fine partita
        if (uiState.phase == DealPhase.MATCH_OVER) {
            MatchOverDialog(
                uiState = uiState,
                onRematch = { onIntent(GameIntent.StartMatch()) },
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
private fun MatchOverDialog(
    uiState: GameUiState,
    onRematch: () -> Unit,
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
            Text(
                text = "${stringResource(id = R.string.game_score_label)}: ${uiState.humanScore} — ${uiState.opponentScore}",
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            Button(
                onClick = onRematch,
                modifier = Modifier.testTag("rematch_button"),
            ) {
                Text(text = stringResource(id = R.string.game_rematch_button))
            }
        },
    )
}
