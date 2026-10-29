package it.maestro.app.feature.puzzle.ui

import androidx.compose.runtime.getValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.maestro.app.R
import it.maestro.app.designsystem.components.CardBackView
import it.maestro.app.designsystem.components.ClubBackground
import it.maestro.app.designsystem.components.ConfettiCelebration
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import it.maestro.game.model.Card
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import it.maestro.app.feature.puzzle.presentation.DailyPuzzleStatus
import it.maestro.app.feature.puzzle.presentation.DailyPuzzleUiState
import it.maestro.app.feature.puzzle.presentation.DailyPuzzleViewModel
import it.maestro.app.feature.table.ui.DealtCardRow
import it.maestro.app.feature.puzzle.presentation.PuzzleShareFormatter

/**
 * Schermata dedicata alla "Sfida del Giorno" (task F2-03).
 */
@Composable
public fun DailyPuzzleScreen(
    modifier: Modifier = Modifier,
    viewModel: DailyPuzzleViewModel = koinViewModel(),
    onNavigateHome: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val symbols = PuzzleShareFormatter.tierSymbols(uiState.solvedTiers, uiState.availableTiers.size)
    val shareText = if (uiState.status is DailyPuzzleStatus.Solved) {
        stringResource(R.string.puzzle_share_solved, uiState.puzzleNumber, symbols)
    } else {
        stringResource(R.string.puzzle_share_played, uiState.puzzleNumber)
    }
    // Se l'app torna in primo piano dopo mezzanotte si passa alla sfida del nuovo giorno (F1R-02).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshDay() }

    DailyPuzzleScreenContent(
        uiState = uiState,
        modifier = modifier,
        onPlayCard = { viewModel.playCard(it) },
        onRetry = { viewModel.retryAttempt() },
        onRequestHint = { viewModel.requestHint() },
        onShowSolution = { viewModel.showSolution() },
        onReplay = { viewModel.replayPuzzle() },
        onShare = { PuzzleShareFormatter.shareViaIntent(context, shareText) },
        onSelectTier = { viewModel.selectTier(it) },
        onNextTier = { viewModel.nextTier() },
        onNavigateHome = onNavigateHome,
    )
}

/**
 * Contenuto visuale puro e stateless della schermata del puzzle.
 */
@Composable
public fun DailyPuzzleScreenContent(
    uiState: DailyPuzzleUiState,
    modifier: Modifier = Modifier,
    onPlayCard: (Card) -> Unit = {},
    onRetry: () -> Unit = {},
    onRequestHint: () -> Unit = {},
    onShowSolution: () -> Unit = {},
    onReplay: () -> Unit = {},
    onShare: () -> Unit = {},
    onSelectTier: (Int) -> Unit = {},
    onNextTier: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
) {
    val puzzle = uiState.puzzle
    val initialView = puzzle?.initialView
    val isSolved = uiState.status is DailyPuzzleStatus.Solved

    ClubBackground(modifier = modifier.testTag("daily_puzzle_screen"), feltFraction = 0.72f) {
        // Il tavolo ha un'altezza minima che gli basta a mostrare le carte: se il resto non ci sta (a sfida risolta c'è
        // molto da leggere), la pagina scorre invece di schiacciarlo.
        val tableHeight = (LocalConfiguration.current.screenHeightDp * 0.3f).dp.coerceAtLeast(210.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .verticalScroll(rememberScrollState()),
        ) {
            // Intestazione schermata
            PuzzleHeader(
                puzzleNumber = uiState.puzzleNumber,
                challengeStreak = uiState.challengeStreak,
                onNavigateHome = onNavigateHome,
            )

            // Selettore livelli di difficoltà progressivi
            PuzzleTierSelector(
                tiers = uiState.availableTiers,
                currentTierIndex = uiState.currentTierIndex,
                solvedTiers = uiState.solvedTiers,
                recommendedTierIndex = uiState.recommendedTierIndex,
                onSelectTier = onSelectTier,
            )

            // Banner con spiegazione chiara dell'obiettivo
            PuzzleGoalBanner(
                goalDescriptionResId = uiState.goalDescriptionResId,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
            )

            // Area del Maestro con avatar e fumetto maieutico
            PuzzleCoachArea(
                uiState = uiState,
                onRequestHint = onRequestHint,
                onShowSolution = onShowSolution,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            )

            // Tavolo da gioco: carte dell'avversario coperte e carte a terra su panno verde
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tableHeight)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF13361E),
                border = BorderStroke(2.dp, Color(0xFF5D4037)),
                shadowElevation = 4.dp,
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.table_felt_bg),
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Carte coperte dell'avversario
                        val opponentCardsCount = initialView?.opponentHandSize ?: 3
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .testTag("opponent_hand_area"),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            repeat(opponentCardsCount) {
                                CardBackView(
                                    width = 54.dp,
                                    height = 80.dp,
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                )
                            }
                        }

                        // Carte scoperte sul tavolo
                        val tableCards = initialView?.table.orEmpty()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("puzzle_table_cards"),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            if (tableCards.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.puzzle_table_empty),
                                    color = BrassRivetGold.copy(alpha = 0.6f),
                                    fontFamily = MaestroDisplayFont,
                                    fontSize = 14.sp,
                                )
                            } else {
                                DealtCardRow(
                                    cards = tableCards,
                                    key = puzzle?.id.orEmpty(),
                                    speed = uiState.speed,
                                    maxCardWidth = 66.dp,
                                    cardModifier = { card ->
                                        Modifier.testTag("puzzle_table_card_${card.rank.name}_${card.suit.name}")
                                    },
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            // Banner inferiore: interazione o celebrazione
            when (val status = uiState.status) {
                is DailyPuzzleStatus.Playing -> {
                    PlayerHandSection(
                        handCards = initialView?.hand.orEmpty(),
                        isPlayable = true,
                        onPlayCard = onPlayCard,
                    )
                }

                is DailyPuzzleStatus.WrongAttempt -> {
                    WrongAttemptBanner(
                        attemptNumber = status.attemptNumber,
                        onRetry = onRetry,
                    )
                    PlayerHandSection(
                        handCards = initialView?.hand.orEmpty(),
                        isPlayable = true,
                        onPlayCard = onPlayCard,
                    )
                }

                is DailyPuzzleStatus.Solved -> {
                    SolvedCelebrationBanner(
                        attemptsCount = status.attemptsCount,
                        lesson = uiState.lesson,
                        hasNextTier = uiState.currentTierIndex + 1 < uiState.availableTiers.size,
                        onNextTier = onNextTier,
                        onReplay = onReplay,
                        onShare = onShare,
                        onNavigateHome = onNavigateHome,
                    )
                }
            }
        }

        // Effetto coriandoli se risolto
        if (isSolved) {
            ConfettiCelebration(modifier = Modifier.fillMaxSize())
        }
    }
}
