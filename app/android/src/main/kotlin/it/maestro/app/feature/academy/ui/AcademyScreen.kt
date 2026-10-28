package it.maestro.app.feature.academy.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.maestro.app.R
import it.maestro.app.data.local.academy.AcademyChapter
import it.maestro.app.data.local.academy.AcademyChapterId
import it.maestro.app.data.local.academy.AcademyLesson
import it.maestro.app.data.local.academy.AcademyLessonId
import it.maestro.app.data.local.academy.AcademyStep
import it.maestro.app.data.local.academy.AcademyStepType
import it.maestro.app.designsystem.components.BubbleTail
import it.maestro.app.designsystem.components.CardView
import it.maestro.app.designsystem.components.ConfettiCelebration
import it.maestro.app.designsystem.components.FramedPortrait
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.MaestroSpeechBubble
import it.maestro.app.designsystem.components.NineSliceBox
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.WoodPanel
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.ButtonAmberBottom
import it.maestro.app.designsystem.theme.ButtonGoldBorder
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import it.maestro.app.designsystem.theme.WoodSurfaceDark
import it.maestro.game.model.Card
import org.koin.androidx.compose.koinViewModel
import it.maestro.app.feature.academy.presentation.AcademyChapterCardState
import it.maestro.app.feature.academy.presentation.AcademyLessonCardState
import it.maestro.app.feature.academy.presentation.AcademyViewMode
import it.maestro.app.feature.academy.presentation.AcademyViewModel

/**
 * Schermata principale dell'Accademia del Maestro arricchita con Capitoli e Sotto-Moduli (task F2-06).
 */
@Composable
public fun AcademyScreen(
    modifier: Modifier = Modifier,
    viewModel: AcademyViewModel = koinViewModel(),
    onNavigateHome: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val mode = uiState.viewMode) {
        is AcademyViewMode.ChapterMap -> {
            AcademyChapterMapContent(
                chapters = uiState.chapters,
                expandedChapterId = uiState.expandedChapterId,
                onToggleChapter = { viewModel.toggleChapter(it) },
                onOpenLesson = { viewModel.openLesson(it) },
                onNavigateHome = onNavigateHome,
                modifier = modifier,
                recommendedChapterId = uiState.recommendedChapterId,
                onOpenReview = { viewModel.openReview(it) },
            )
        }
        is AcademyViewMode.Lesson -> {
            AcademyLessonContent(
                lesson = mode,
                onSelectOption = { viewModel.selectOption(it) },
                onPlayCard = { viewModel.playCard(it) },
                onNextStep = { viewModel.nextStep() },
                onBackToMap = { viewModel.backToMap() },
                modifier = modifier,
            )
        }
    }
}

/**
 * Mappa formativa dei Capitoli (macro-moduli) e delle relative Lezioni (sotto-moduli).
 */
@Composable
public fun AcademyChapterMapContent(
    chapters: List<AcademyChapterCardState>,
    expandedChapterId: AcademyChapterId?,
    onToggleChapter: (AcademyChapterId) -> Unit,
    onOpenLesson: (AcademyLessonId) -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
    recommendedChapterId: AcademyChapterId? = null,
    onOpenReview: (AcademyLessonId) -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WoodSurfaceDark)
            .testTag("academy_screen"),
    ) {
        Image(
            painter = painterResource(R.drawable.texture_wood),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
        ) {
            // Intestazione con pulsante di chiusura
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
                            .testTag("academy_back_button"),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                        Text(
                            text = stringResource(R.string.academy_screen_title),
                            color = BrassRivetGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MaestroDisplayFont,
                            maxLines = 1,
                        )
                        Text(
                            text = stringResource(R.string.academy_screen_subtitle),
                            color = Color(0xFFD4AF37),
                            fontSize = 11.sp,
                            maxLines = 1,
                        )
                    }
                }
            }

            // Lista scorrevole dei Capitoli e sotto-moduli
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                chapters.forEach { chapterState ->
                    AcademyChapterCard(
                        chapterState = chapterState,
                        isExpanded = expandedChapterId == chapterState.chapter.id,
                        isRecommended = recommendedChapterId == chapterState.chapter.id,
                        onToggle = { onToggleChapter(chapterState.chapter.id) },
                        onOpenLesson = onOpenLesson,
                        onOpenReview = onOpenReview,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Card di un Capitolo (macro-modulo) espandibile con l'elenco dei sotto-moduli.
 */
@Composable
private fun AcademyChapterCard(
    chapterState: AcademyChapterCardState,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOpenLesson: (AcademyLessonId) -> Unit,
    modifier: Modifier = Modifier,
    isRecommended: Boolean = false,
    onOpenReview: (AcademyLessonId) -> Unit = {},
) {
    val chapter = chapterState.chapter
    val isUnlocked = chapterState.isUnlocked
    val isCompleted = chapterState.isCompleted

    WoodPanel(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isUnlocked) 1f else 0.55f)
            .testTag("academy_chapter_card_${chapter.number}"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header del capitolo (cliccabile per espandere/comprimere)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isUnlocked, onClick = onToggle)
                    .testTag("academy_chapter_header_${chapter.number}"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Stemma del capitolo con cerchio dorato
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A170C))
                        .border(
                            width = if (isCompleted) 2.dp else 1.5.dp,
                            color = if (isCompleted) BrassRivetGold else ButtonGoldBorder,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(chapter.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (isRecommended) {
                        Text(
                            text = stringResource(R.string.academy_chapter_recommended),
                            color = Color(0xFFFFF8DC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MaestroDisplayFont,
                            modifier = Modifier.testTag("academy_chapter_recommended_${chapter.number}"),
                        )
                    }
                    Text(
                        text = stringResource(R.string.academy_chapter_number, chapter.number).uppercase(),
                        color = BrassRivetGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaestroDisplayFont,
                        letterSpacing = 0.5.sp,
                    )

                    Text(
                        text = stringResource(chapter.titleRes),
                        color = Color(0xFFFFF8DC),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaestroDisplayFont,
                        lineHeight = 20.sp,
                    )

                    Text(
                        text = stringResource(chapter.subtitleRes),
                        color = Color(0xFFE2D9C8),
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Contatore lezioni o Distintivo Ottone di Completamento Capitolo
                Column(horizontalAlignment = Alignment.End) {
                    if (isCompleted) {
                        NineSliceBox(
                            skin = R.drawable.skin_name_plaque,
                            slice = 0.35f,
                            corner = 8.dp,
                            modifier = Modifier.testTag("academy_chapter_completed_tag_${chapter.number}"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.badge_correct),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = stringResource(R.string.academy_chapter_progress, chapterState.completedCount, chapterState.totalCount),
                                    color = BrassRivetGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MaestroDisplayFont,
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2A170C))
                                .border(1.dp, ButtonGoldBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.academy_chapter_progress, chapterState.completedCount, chapterState.totalCount),
                                color = BrassRivetGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            // Descrizione del capitolo
            Text(
                text = stringResource(chapter.descriptionRes),
                color = Color(0xFFE2D9C8),
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )

            // Se il capitolo è completato, mostriamo una nobile targa di encomio del Circolo
            if (isCompleted) {
                NineSliceBox(
                    skin = R.drawable.skin_score_plaque,
                    slice = 0.4f,
                    corner = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("academy_chapter_completed_banner_${chapter.number}"),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.badge_correct),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.academy_chapter_completed).uppercase(),
                            color = BrassRivetGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MaestroDisplayFont,
                            letterSpacing = 0.8.sp,
                        )
                    }
                }
            }

            // Sotto-moduli (lezioni pratiche) espandibili
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    chapterState.lessons.forEach { lessonState ->
                        AcademyLessonTile(
                            lessonState = lessonState,
                            chapterNumber = chapter.number,
                            onOpenLesson = { onOpenLesson(lessonState.lesson.id) },
                            onOpenReview = { onOpenReview(lessonState.lesson.id) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Singolo sotto-modulo (lezione) con stato di avanzamento e pulsante d'azione.
 */
@Composable
private fun AcademyLessonTile(
    lessonState: AcademyLessonCardState,
    chapterNumber: Int,
    onOpenLesson: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenReview: () -> Unit = {},
) {
    val lesson = lessonState.lesson
    val progress = lessonState.progress
    val isUnlocked = progress.isUnlocked
    val isCompleted = progress.isCompleted

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF23140A))
            .border(1.dp, if (isCompleted) Color(0xFFD4AF37) else Color(0xFF5D4037), RoundedCornerShape(10.dp))
            .padding(10.dp)
            .testTag("academy_lesson_tile_${chapterNumber}_${lesson.lessonNumber}"),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icona della lezione
                Image(
                    painter = painterResource(lesson.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.academy_lesson_number, chapterNumber, lesson.lessonNumber).uppercase(),
                        color = Color(0xFFD4AF37),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(lesson.titleRes),
                        color = Color(0xFFFFF8DC),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaestroDisplayFont,
                    )
                }

                if (isCompleted) {
                    Image(
                        painter = painterResource(R.drawable.badge_best),
                        contentDescription = null,
                        modifier = Modifier
                            .size(22.dp)
                            .testTag("academy_lesson_completed_icon_${chapterNumber}_${lesson.lessonNumber}"),
                    )
                }
            }

            Text(
                text = stringResource(lesson.descriptionRes),
                color = Color(0xFFE2D9C8),
                fontSize = 11.sp,
                lineHeight = 15.sp,
            )

            // Pulsante d'azione della lezione
            if (!isUnlocked) {
                MaestroPillButton(
                    text = stringResource(R.string.academy_button_locked),
                    onClick = {},
                    enabled = false,
                    variant = PillButtonVariant.WOOD,
                    height = 36.dp,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "academy_lesson_action_${chapterNumber}_${lesson.lessonNumber}",
                )
            } else if (isCompleted) {
                MaestroPillButton(
                    text = stringResource(
                        if (lessonState.isReviewedToday) R.string.academy_button_reviewed else R.string.academy_button_flash_review,
                    ),
                    onClick = onOpenReview,
                    enabled = true,
                    variant = PillButtonVariant.PRIMARY_GREEN,
                    height = 38.dp,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "academy_lesson_flash_${chapterNumber}_${lesson.lessonNumber}",
                )
                MaestroPillButton(
                    text = stringResource(R.string.academy_button_review),
                    onClick = onOpenLesson,
                    enabled = true,
                    variant = PillButtonVariant.SECONDARY_AMBER,
                    height = 38.dp,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "academy_lesson_action_${chapterNumber}_${lesson.lessonNumber}",
                )
            } else {
                MaestroPillButton(
                    text = stringResource(R.string.academy_button_start),
                    onClick = onOpenLesson,
                    enabled = true,
                    variant = PillButtonVariant.PRIMARY_GREEN,
                    height = 40.dp,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "academy_lesson_action_${chapterNumber}_${lesson.lessonNumber}",
                )
            }
        }
    }
}

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
                    LessonTableArea(step = step)

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

            // Carte disposte sul tavolo
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                step.tableCards.forEach { card ->
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
                            width = 62.dp,
                            height = 92.dp,
                            isPlayable = false,
                        )
                    }
                }
            }
        }
    }
}

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
