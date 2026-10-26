package it.maestro.app.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.maestro.app.R
import it.maestro.app.ui.components.BubbleTail
import it.maestro.app.ui.components.CardBackView
import it.maestro.app.ui.components.CardView
import it.maestro.app.ui.components.ClubBackground
import it.maestro.app.ui.components.ConfettiCelebration
import it.maestro.app.ui.components.FramedPortrait
import it.maestro.app.ui.components.MaestroPillButton
import it.maestro.app.ui.components.MaestroSpeechBubble
import it.maestro.app.ui.components.PillButtonVariant
import it.maestro.app.ui.components.WoodIconButton
import it.maestro.app.ui.components.WoodPanel
import it.maestro.app.ui.theme.BrassRivetGold
import it.maestro.app.ui.theme.ButtonAmberBottom
import it.maestro.app.ui.theme.MaestroDisplayFont
import it.maestro.app.ui.theme.WoodSurfaceDark
import it.maestro.game.model.Card
import org.koin.androidx.compose.koinViewModel

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

    DailyPuzzleScreenContent(
        uiState = uiState,
        modifier = modifier,
        onPlayCard = { viewModel.playCard(it) },
        onRetry = { viewModel.retryAttempt() },
        onShare = { PuzzleShareFormatter.shareViaIntent(context, uiState.shareText) },
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
    onShare: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
) {
    val puzzle = uiState.puzzle
    val initialView = puzzle?.initialView
    val isSolved = uiState.status is DailyPuzzleStatus.Solved

    ClubBackground(modifier = modifier.testTag("daily_puzzle_screen"), feltFraction = 0.5f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
        ) {
            // Intestazione schermata
            PuzzleHeader(
                puzzleNumber = uiState.puzzleNumber,
                onNavigateHome = onNavigateHome,
            )

            // Area del Maestro con avatar e fumetto maieutico
            PuzzleCoachArea(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            )

            // Tavolo da gioco: carte dell'avversario coperte e carte a terra
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Carte coperte dell'avversario
                    val opponentCardsCount = initialView?.opponentHandSize ?: 3
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .testTag("opponent_hand_area"),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        repeat(opponentCardsCount) {
                            CardBackView(
                                width = 56.dp,
                                height = 84.dp,
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }
                    }

                    // Carte scoperte sul tavolo
                    val tableCards = initialView?.table.orEmpty()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
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
                            tableCards.forEach { card ->
                                CardView(
                                    card = card,
                                    width = 68.dp,
                                    height = 102.dp,
                                    isPlayable = false,
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .testTag("puzzle_table_card_${card.rank.name}_${card.suit.name}"),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
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

@Composable
private fun PuzzleHeader(
    puzzleNumber: Int,
    onNavigateHome: () -> Unit,
) {
    WoodPanel(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 14.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.button_close),
                contentDescription = stringResource(R.string.puzzle_button_home),
                modifier = Modifier
                    .size(38.dp)
                    .clickable(onClick = onNavigateHome)
                    .testTag("puzzle_back_button"),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = stringResource(R.string.puzzle_screen_title),
                    color = BrassRivetGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MaestroDisplayFont,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(R.string.puzzle_number_format, puzzleNumber),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontFamily = MaestroDisplayFont,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun PuzzleCoachArea(
    uiState: DailyPuzzleUiState,
    modifier: Modifier = Modifier,
) {
    WoodPanel(modifier = modifier.heightIn(min = 106.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FramedPortrait(
                portrait = when (uiState.maestroMood) {
                    it.maestro.app.ui.components.MaestroMood.TRIUMPHANT,
                    it.maestro.app.ui.components.MaestroMood.CELEBRATING -> R.drawable.maestro_pleased
                    it.maestro.app.ui.components.MaestroMood.WARNING,
                    it.maestro.app.ui.components.MaestroMood.DESPAIR -> R.drawable.maestro_warning
                    else -> R.drawable.maestro_thinking
                },
                size = 72.dp,
                background = ButtonAmberBottom,
                contentDescription = stringResource(R.string.maestro_avatar_description),
                modifier = Modifier.testTag("puzzle_maestro_avatar"),
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                MaestroSpeechBubble(
                    message = stringResource(uiState.maestroMessageResId),
                    tail = BubbleTail.START,
                    fontSize = 12.sp,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth().testTag("puzzle_coach_bubble"),
                )
            }
        }
    }
}

@Composable
private fun PlayerHandSection(
    handCards: List<Card>,
    isPlayable: Boolean,
    onPlayCard: (Card) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("puzzle_player_hand_area"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            handCards.forEach { card ->
                CardView(
                    card = card,
                    width = 72.dp,
                    height = 108.dp,
                    isPlayable = isPlayable,
                    onClick = { onPlayCard(card) },
                    modifier = Modifier.testTag("puzzle_card_${card.rank.name}_${card.suit.name}"),
                )
            }
        }
    }
}

@Composable
private fun WrongAttemptBanner(
    attemptNumber: Int,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("puzzle_wrong_attempt_banner"),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF6B1D1D),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE57373)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.puzzle_attempt_failed_banner, attemptNumber),
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = MaestroDisplayFont,
                modifier = Modifier.weight(1f),
            )
            MaestroPillButton(
                text = stringResource(R.string.puzzle_button_retry).uppercase(),
                onClick = onRetry,
                variant = PillButtonVariant.SECONDARY_AMBER,
                height = 36.dp,
                fontSize = 13.sp,
                testTag = "puzzle_retry_button",
            )
        }
    }
}

@Composable
private fun SolvedCelebrationBanner(
    attemptsCount: Int,
    onShare: () -> Unit,
    onNavigateHome: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("puzzle_solved_banner"),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E3A24),
        border = androidx.compose.foundation.BorderStroke(2.dp, BrassRivetGold),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.puzzle_solved_title),
                color = BrassRivetGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MaestroDisplayFont,
            )
            val subtitle = if (attemptsCount == 1) {
                stringResource(R.string.puzzle_solved_subtitle_first_try)
            } else {
                stringResource(R.string.puzzle_solved_subtitle_other_tries, attemptsCount)
            }
            Text(
                text = subtitle,
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = MaestroDisplayFont,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MaestroPillButton(
                    text = stringResource(R.string.puzzle_button_share).uppercase(),
                    onClick = onShare,
                    variant = PillButtonVariant.PRIMARY_GREEN,
                    height = 46.dp,
                    fontSize = 14.sp,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f),
                    testTag = "puzzle_share_button",
                )
                MaestroPillButton(
                    text = stringResource(R.string.puzzle_button_home).uppercase(),
                    onClick = onNavigateHome,
                    variant = PillButtonVariant.SECONDARY_AMBER,
                    height = 46.dp,
                    fontSize = 13.sp,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f),
                    testTag = "puzzle_home_button",
                )
            }
        }
    }
}
