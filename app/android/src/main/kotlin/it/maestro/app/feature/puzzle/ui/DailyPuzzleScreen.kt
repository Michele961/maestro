package it.maestro.app.feature.puzzle.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import it.maestro.app.data.local.DailyPuzzleTier
import it.maestro.app.designsystem.components.BubbleTail
import it.maestro.app.designsystem.components.CardBackView
import it.maestro.app.designsystem.components.CardView
import it.maestro.app.designsystem.components.ClubBackground
import it.maestro.app.designsystem.components.ConfettiCelebration
import it.maestro.app.designsystem.components.FramedPortrait
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.MaestroSpeechBubble
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.WoodIconButton
import it.maestro.app.designsystem.components.WoodPanel
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.ButtonAmberBottom
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import it.maestro.app.designsystem.theme.WoodSurfaceDark
import it.maestro.game.model.Card
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import it.maestro.app.feature.puzzle.presentation.DailyPuzzleStatus
import it.maestro.app.feature.puzzle.presentation.DailyPuzzleUiState
import it.maestro.app.feature.puzzle.presentation.DailyPuzzleViewModel
import it.maestro.app.data.local.PuzzleLesson
import it.maestro.app.feature.table.ui.DealtCardRow
import it.maestro.app.feature.table.ui.phrasesText
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

            // Selettore livelli di difficoltà progressivi
            PuzzleTierSelector(
                tiers = uiState.availableTiers,
                currentTierIndex = uiState.currentTierIndex,
                solvedTiers = uiState.solvedTiers,
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
                    .weight(1f)
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
private fun PuzzleGoalBanner(
    goalDescriptionResId: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.testTag("puzzle_goal_banner"),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF2A1C14),
        border = BorderStroke(1.dp, BrassRivetGold.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(goalDescriptionResId),
                color = BrassRivetGold,
                fontSize = 12.sp,
                fontFamily = MaestroDisplayFont,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun PuzzleCoachArea(
    uiState: DailyPuzzleUiState,
    onRequestHint: () -> Unit = {},
    onShowSolution: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isSolved = uiState.status is DailyPuzzleStatus.Solved

    WoodPanel(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FramedPortrait(
                portrait = when (uiState.maestroMood) {
                    it.maestro.app.designsystem.components.MaestroMood.TRIUMPHANT,
                    it.maestro.app.designsystem.components.MaestroMood.CELEBRATING -> R.drawable.maestro_pleased
                    it.maestro.app.designsystem.components.MaestroMood.WARNING,
                    it.maestro.app.designsystem.components.MaestroMood.DESPAIR -> R.drawable.maestro_warning
                    else -> R.drawable.maestro_thinking
                },
                size = 72.dp,
                background = ButtonAmberBottom,
                contentDescription = stringResource(R.string.maestro_avatar_description),
                modifier = Modifier.testTag("puzzle_maestro_avatar"),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MaestroSpeechBubble(
                    message = if (uiState.maestroPhrases.isEmpty()) {
                        stringResource(uiState.maestroMessageResId)
                    } else {
                        phrasesText(uiState.maestroPhrases)
                    },
                    tail = BubbleTail.START,
                    fontSize = 12.sp,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth().testTag("puzzle_coach_bubble"),
                )
                if (uiState.solutionShown && uiState.lesson != null) {
                    Text(
                        text = stringResource(uiState.lesson.conceptRes),
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.testTag("puzzle_solution_concept"),
                    )
                }
                if (!isSolved) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        MaestroPillButton(
                            text = stringResource(R.string.puzzle_button_hint).uppercase(),
                            onClick = onRequestHint,
                            variant = PillButtonVariant.WOOD,
                            height = 26.dp,
                            fontSize = 11.sp,
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            testTag = "puzzle_coach_hint_button",
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        MaestroPillButton(
                            text = stringResource(R.string.puzzle_button_solution).uppercase(),
                            onClick = onShowSolution,
                            variant = PillButtonVariant.WOOD,
                            height = 26.dp,
                            fontSize = 11.sp,
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            testTag = "puzzle_coach_solution_button",
                        )
                    }
                }
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
private fun PuzzleTierSelector(
    tiers: List<DailyPuzzleTier>,
    currentTierIndex: Int,
    solvedTiers: Set<Int>,
    onSelectTier: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("puzzle_tier_selector"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tiers.forEach { tier ->
            val isSelected = tier.index == currentTierIndex
            val isSolved = solvedTiers.contains(tier.index)
            val backgroundColor = when {
                isSolved && isSelected -> Color(0xFF264A2E)
                isSolved -> Color(0xFF1E3A24)
                isSelected -> Color(0xFF3E2313)
                else -> Color(0xFF221610)
            }
            val borderColor = when {
                isSolved && isSelected -> BrassRivetGold
                isSelected -> BrassRivetGold
                isSolved -> Color(0xFF4CAF50)
                else -> Color(0xFF4E3629)
            }
            val borderWidth = if (isSelected) 2.dp else 1.dp

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectTier(tier.index) }
                    .testTag("puzzle_tier_tab_${tier.index}"),
                shape = RoundedCornerShape(8.dp),
                color = backgroundColor,
                border = BorderStroke(borderWidth, borderColor),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (isSolved) {
                            Text(
                                text = "✓",
                                color = Color(0xFF81C784),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            text = stringResource(tier.titleRes),
                            color = if (isSelected) BrassRivetGold else Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = MaestroDisplayFont,
                            maxLines = 1,
                        )
                    }
                    Text(
                        text = stringResource(R.string.puzzle_tier_reward, tier.rewardCredits),
                        color = BrassRivetGold.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SolvedCelebrationBanner(
    attemptsCount: Int,
    lesson: PuzzleLesson?,
    hasNextTier: Boolean,
    onNextTier: () -> Unit,
    onReplay: () -> Unit,
    onShare: () -> Unit,
    onNavigateHome: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("puzzle_solved_banner"),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E3A24),
        border = BorderStroke(2.dp, BrassRivetGold),
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

            if (lesson != null) {
                Surface(
                    color = Color(0xFF122316),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BrassRivetGold.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("puzzle_logical_concept_box"),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.puzzle_explanation_header),
                            color = BrassRivetGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MaestroDisplayFont,
                        )
                        Text(
                            text = stringResource(lesson.conceptRes),
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.testTag("puzzle_logical_concept"),
                        )
                    }
                }
            }

            if (lesson != null) {
                Surface(
                    color = Color(0xFF2A170C),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFD4AF37)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("puzzle_golden_rule_box"),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.puzzle_golden_rule_header),
                            color = Color(0xFFD4AF37),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MaestroDisplayFont,
                        )
                        Text(
                            text = stringResource(lesson.ruleRes),
                            color = Color(0xFFFFF8DC),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.testTag("puzzle_golden_rule"),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (hasNextTier) {
                MaestroPillButton(
                    text = stringResource(R.string.puzzle_button_next).uppercase(),
                    onClick = onNextTier,
                    variant = PillButtonVariant.PRIMARY_GREEN,
                    height = 44.dp,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "puzzle_next_tier_button",
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MaestroPillButton(
                    text = stringResource(R.string.puzzle_button_replay).uppercase(),
                    onClick = onReplay,
                    variant = PillButtonVariant.WOOD,
                    height = 42.dp,
                    fontSize = 12.sp,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f),
                    testTag = "puzzle_replay_button",
                )
                MaestroPillButton(
                    text = stringResource(R.string.puzzle_button_share).uppercase(),
                    onClick = onShare,
                    variant = if (hasNextTier) PillButtonVariant.WOOD else PillButtonVariant.PRIMARY_GREEN,
                    height = 42.dp,
                    fontSize = 12.sp,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f),
                    testTag = "puzzle_share_button",
                )
                MaestroPillButton(
                    text = stringResource(R.string.puzzle_button_home).uppercase(),
                    onClick = onNavigateHome,
                    variant = PillButtonVariant.SECONDARY_AMBER,
                    height = 42.dp,
                    fontSize = 12.sp,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f),
                    testTag = "puzzle_home_button",
                )
            }
        }
    }
}
