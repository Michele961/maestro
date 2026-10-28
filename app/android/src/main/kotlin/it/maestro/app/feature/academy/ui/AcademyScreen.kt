package it.maestro.app.feature.academy.ui

import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.maestro.app.R
import it.maestro.app.domain.academy.AcademyChapterId
import it.maestro.app.domain.academy.AcademyLessonId
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.NineSliceBox
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.WoodPanel
import it.maestro.app.designsystem.theme.BrassRivetGold
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
                speed = uiState.speed,
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.academy_chapter_completed).uppercase(),
                                color = BrassRivetGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MaestroDisplayFont,
                                letterSpacing = 0.8.sp,
                            )
                            Text(
                                text = stringResource(R.string.academy_chapter_rank, stringResource(chapter.rankTitleRes)),
                                color = Color(0xFFFFF8DC),
                                fontSize = 12.sp,
                                modifier = Modifier.testTag("academy_chapter_rank_${chapter.number}"),
                            )
                        }
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
