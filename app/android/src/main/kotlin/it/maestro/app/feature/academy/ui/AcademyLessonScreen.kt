package it.maestro.app.feature.academy.ui

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpOffset
import it.maestro.app.designsystem.motion.MotionSpec
import it.maestro.app.designsystem.motion.MotionToken
import it.maestro.app.domain.settings.GameSpeed
import it.maestro.app.feature.table.ui.MovingCard
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.domain.academy.AcademyLesson
import it.maestro.app.domain.academy.AcademyStep
import it.maestro.app.domain.academy.AcademyStepType
import it.maestro.app.designsystem.components.BubbleTail
import it.maestro.app.designsystem.components.CardView
import it.maestro.app.designsystem.components.ConfettiCelebration
import it.maestro.app.designsystem.components.FramedPortrait
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.MaestroSpeechBubble
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.WoodPanel
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.ButtonAmberBottom
import it.maestro.app.designsystem.theme.ButtonGoldBorder
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import it.maestro.game.model.Card
import it.maestro.app.feature.academy.presentation.AcademyViewMode

/**
 * Schermata interattiva di una lezione al tavolo del Maestro (100% feltro e legno, zero grigio!).
 */
@Composable
public fun AcademyLessonContent(
    lesson: AcademyViewMode.Lesson,
    onSelectOption: (String) -> Unit,
    onPlayCard: (Card) -> Unit,
    onNextStep: () -> Unit,
    onBackToMap: () -> Unit,
    modifier: Modifier = Modifier,
    speed: GameSpeed = GameSpeed.NORMAL,
) {
    val step = lesson.currentStep
    val isSummary = step.type == AcademyStepType.SUMMARY_AND_BADGE

    // Sfondo a tutto schermo in panno verde lussuoso del circolo
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF13361E))
            .testTag("academy_lesson_content"),
    ) {
        Image(
            painter = painterResource(R.drawable.table_felt_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .padding(bottom = 12.dp),
        ) {
            // Intestazione lezione
            LessonHeader(
                lesson = lesson.lesson,
                currentStep = step,
                totalSteps = lesson.lesson.steps.size,
                onBackToMap = onBackToMap,
            )

            // Area del Maestro con avatar e fumetto didattico
            LessonCoachArea(
                step = step,
                mood = lesson.effectiveMaestroMood,
                feedbackRes = lesson.feedbackRes,
                isAnswerCorrect = lesson.isAnswerCorrect,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            )

            // Contenuto centrale: Tavolo e interazione
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!isSummary) {
                    // Tavolo con feltro e carte a terra
                    LessonTableArea(step = step, speed = speed)

                    // Area di interazione specifica per tipo di step
                    when (step.type) {
                        AcademyStepType.INTERACTIVE_QUESTION -> {
                            InteractiveQuestionSection(
                                step = step,
                                selectedOptionId = lesson.selectedOptionId,
                                isAnswerCorrect = lesson.isAnswerCorrect,
                                feedbackRes = lesson.feedbackRes,
                                onSelectOption = onSelectOption,
                            )
                        }
                        AcademyStepType.CARD_ACTION -> {
                            CardActionSection(
                                step = step,
                                isCompleted = lesson.isCardActionCompleted,
                                feedbackRes = lesson.feedbackRes,
                                onPlayCard = onPlayCard,
                            )
                        }
                        else -> Unit
                    }
                } else {
                    // Schermata di riepilogo con distintivo d'oro e regola ferrea
                    LessonSummarySection(
                        lesson = lesson.lesson,
                        step = step,
                    )
                }
            }

            // Pulsante d'azione inferiore
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            ) {
                if (lesson.isLastStep) {
                    MaestroPillButton(
                        text = stringResource(R.string.academy_button_complete),
                        onClick = onNextStep,
                        enabled = lesson.isStepCompleted,
                        variant = PillButtonVariant.HERO_RED,
                        height = 54.dp,
                        fontSize = 18.sp,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "academy_complete_module_button",
                    )
                } else {
                    MaestroPillButton(
                        text = stringResource(R.string.academy_button_next_step),
                        onClick = onNextStep,
                        enabled = lesson.isStepCompleted,
                        variant = PillButtonVariant.PRIMARY_GREEN,
                        height = 50.dp,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "academy_next_step_button",
                    )
                }
            }
        }

        // Coriandoli celebrativi all'ultimo step
        if (isSummary) {
            ConfettiCelebration(modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * Intestazione della lezione con titolo e contatore step.
 */
@Composable
private fun LessonHeader(
    lesson: AcademyLesson,
    currentStep: AcademyStep,
    totalSteps: Int,
    onBackToMap: () -> Unit,
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
                contentDescription = stringResource(R.string.academy_button_back_map),
                modifier = Modifier
                    .size(38.dp)
                    .clickable(onClick = onBackToMap)
                    .testTag("academy_lesson_back_button"),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = stringResource(lesson.titleRes),
                    color = BrassRivetGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MaestroDisplayFont,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(currentStep.titleRes),
                    color = Color(0xFFD4AF37),
                    fontSize = 11.sp,
                    maxLines = 1,
                )
            }
            Text(
                text = "${currentStep.stepNumber}/$totalSteps",
                color = BrassRivetGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MaestroDisplayFont,
                modifier = Modifier.testTag("academy_step_indicator"),
            )
        }
    }
}

/**
 * Area del Maestro con avatar e fumetto maieutico parlante.
 */
@Composable
private fun LessonCoachArea(
    step: AcademyStep,
    mood: it.maestro.app.designsystem.components.MaestroMood,
    @StringRes feedbackRes: Int? = null,
    isAnswerCorrect: Boolean? = null,
    modifier: Modifier = Modifier,
) {
    val messageText = if (feedbackRes != null) {
        stringResource(feedbackRes)
    } else {
        stringResource(step.maestroSpeechRes)
    }

    WoodPanel(modifier = modifier.heightIn(min = 112.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FramedPortrait(
                portrait = when (mood) {
                    it.maestro.app.designsystem.components.MaestroMood.TRIUMPHANT,
                    it.maestro.app.designsystem.components.MaestroMood.CELEBRATING,
                    it.maestro.app.designsystem.components.MaestroMood.APPLAUDING -> R.drawable.maestro_pleased
                    it.maestro.app.designsystem.components.MaestroMood.WARNING,
                    it.maestro.app.designsystem.components.MaestroMood.DESPAIR -> R.drawable.maestro_warning
                    else -> R.drawable.maestro_thinking
                },
                size = 72.dp,
                background = ButtonAmberBottom,
                contentDescription = stringResource(R.string.maestro_avatar_description),
                modifier = Modifier.testTag("academy_coach_avatar"),
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                MaestroSpeechBubble(
                    message = messageText,
                    tail = BubbleTail.START,
                    fontSize = 11.5.sp,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth().testTag("academy_coach_bubble"),
                )
            }
        }
    }
}

/**
 * Il tavolo da gioco con carte a terra e cartigli esplicativi.
 */
@Composable
private fun LessonTableArea(
    step: AcademyStep,
    speed: GameSpeed,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D2514))
            .border(2.dp, Color(0xFF5D4037), RoundedCornerShape(12.dp))
            .padding(10.dp)
            .testTag("academy_table_area"),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Cartiglio esplicativo in testa al tavolo
            step.tableBadgeRes?.let { badgeRes ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2A170C))
                        .border(1.dp, ButtonGoldBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                        .testTag("academy_table_badge"),
                ) {
                    Text(
                        text = stringResource(badgeRes),
                        color = BrassRivetGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaestroDisplayFont,
                    )
                }
            }

            // Carte disposte sul tavolo: arrivano una alla volta con i tempi di MotionSpec (come al tavolo vero)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val count = step.tableCards.size.coerceAtLeast(1)
                val cardWidth = minOf(62.dp, (maxWidth - CARD_GAP * (count - 1)) / count)
                val cardHeight = cardWidth * 92 / 62
                val rowWidth = cardWidth * count + CARD_GAP * (count - 1)
                val startX = (maxWidth - rowWidth) / 2
                val millis = MotionSpec.millis(MotionToken.DEAL_CARD, speed)
                var revealed by remember(step.id) { mutableIntStateOf(0) }
                LaunchedEffect(step.id) {
                    step.tableCards.indices.forEach { index ->
                        revealed = index + 1
                        MotionSpec.pause(millis)
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(BADGE_ROOM + cardHeight)) {
                    step.tableCards.take(revealed).forEachIndexed { index, card ->
                        val x = startX + (cardWidth + CARD_GAP) * index
                        step.cardBadges[card]?.let { badgeResId ->
                            Box(
                                modifier = Modifier
                                    .offset(x = x, y = 0.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF2A170C))
                                    .border(1.dp, BrassRivetGold, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                            ) {
                                Text(
                                    text = stringResource(badgeResId),
                                    color = BrassRivetGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        MovingCard(
                            card = card,
                            target = DpOffset(x, BADGE_ROOM),
                            origin = DpOffset(x, -(cardHeight + BADGE_ROOM)),
                            millis = millis.toInt(),
                            flip = false,
                            shrink = false,
                            width = cardWidth,
                            height = cardHeight,
                            highlighted = false,
                        )
                    }
                }
            }
        }
    }
}

private val CARD_GAP = 8.dp
private val BADGE_ROOM = 20.dp

/**
 * Sezione didattica a scelta multipla con risposte interattive.
 */
@Composable
private fun InteractiveQuestionSection(
    step: AcademyStep,
    selectedOptionId: String?,
    isAnswerCorrect: Boolean?,
    feedbackRes: Int?,
    onSelectOption: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("academy_question_section"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.academy_question_prompt),
            color = BrassRivetGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MaestroDisplayFont,
        )

        step.options.forEach { option ->
            val isSelected = selectedOptionId == option.id
            val borderColor = when {
                isSelected && option.isCorrect -> Color(0xFF48BB78)
                isSelected && !option.isCorrect -> Color(0xFFE53E3E)
                else -> ButtonGoldBorder
            }
            val bgColor = when {
                isSelected && option.isCorrect -> Color(0xFF1E3A1E)
                isSelected && !option.isCorrect -> Color(0xFF3A1E1E)
                else -> Color(0xFF2B1810)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable { onSelectOption(option.id) }
                    .padding(12.dp)
                    .testTag("academy_option_${option.id}"),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(option.textRes),
                        color = Color(0xFFFFF8DC),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        feedbackRes?.let { fbRes ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A170C))
                    .border(1.dp, if (isAnswerCorrect == true) Color(0xFF48BB78) else Color(0xFFE57373), RoundedCornerShape(8.dp))
                    .padding(10.dp)
                    .testTag("academy_question_feedback"),
            ) {
                Text(
                    text = stringResource(fbRes),
                    color = if (isAnswerCorrect == true) Color(0xFFC6F6D5) else Color(0xFFFED7D7),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

/**
 * Sezione didattica con azione pratica sulla carta corretta.
 */
@Composable
private fun CardActionSection(
    step: AcademyStep,
    isCompleted: Boolean,
    feedbackRes: Int?,
    onPlayCard: (Card) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("academy_card_action_section"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.academy_card_action_hint),
            color = BrassRivetGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MaestroDisplayFont,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            step.handCards.forEach { card ->
                val isTarget = card == step.targetCard
                val showFrame = isCompleted && isTarget

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    step.cardBadges[card]?.let { badgeResId ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF2A170C))
                                .border(1.dp, BrassRivetGold, RoundedCornerShape(6.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                        ) {
                            Text(
                                text = stringResource(badgeResId),
                                color = BrassRivetGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    CardView(
                        card = card,
                        width = 72.dp,
                        height = 106.dp,
                        isPlayable = !isCompleted,
                        showGoldFrame = showFrame,
                        onClick = { onPlayCard(card) },
                        modifier = Modifier.testTag("academy_hand_card_${card.rank.name}_${card.suit.name}"),
                    )
                }
            }
        }

        feedbackRes?.let { fbRes ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF3A1E1E))
                    .border(1.dp, Color(0xFFE57373), RoundedCornerShape(8.dp))
                    .padding(10.dp)
                    .testTag("academy_card_action_feedback"),
            ) {
                Text(
                    text = stringResource(fbRes),
                    color = Color(0xFFFED7D7),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

/**
 * Sezione conclusiva con distintivo d'oro e proclamazione della regola ferrea del circolo.
 */
@Composable
private fun LessonSummarySection(
    lesson: AcademyLesson,
    step: AcademyStep,
) {
    WoodPanel(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("academy_summary_section"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.badge_best),
                contentDescription = null,
                modifier = Modifier.size(76.dp),
            )

            Text(
                text = stringResource(step.titleRes),
                color = BrassRivetGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MaestroDisplayFont,
                textAlign = TextAlign.Center,
            )

            // Il consiglio del Maestro
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF2A170C),
                border = BorderStroke(1.dp, BrassRivetGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.academy_advice_header),
                        color = BrassRivetGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaestroDisplayFont,
                    )
                    Text(
                        text = stringResource(step.maestroSpeechRes),
                        color = Color(0xFFFFF8DC),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2A170C))
                    .border(1.5.dp, ButtonGoldBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.academy_golden_rule_header),
                        color = Color(0xFFD4AF37),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaestroDisplayFont,
                    )
                    Text(
                        text = stringResource(step.goldenRuleRes ?: lesson.goldenRuleRes),
                        color = Color(0xFFFFF8DC),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                    )
                }
            }
        }
    }
}
