package it.maestro.app.feature.academy.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.R
import it.maestro.app.data.local.academy.AcademyChapter
import it.maestro.app.data.local.academy.AcademyChapterId
import it.maestro.app.data.local.academy.AcademyContent
import it.maestro.app.data.local.academy.AcademyLesson
import it.maestro.app.data.local.academy.AcademyLessonId
import it.maestro.app.data.local.academy.AcademyLessonProgress
import it.maestro.app.data.local.academy.AcademyStep
import it.maestro.app.data.local.academy.AcademyStepType
import it.maestro.app.feature.profile.presentation.RadarAxis
import it.maestro.app.feature.profile.presentation.weakestAxis
import it.maestro.ai.mastery.MasteryProfile
import it.maestro.app.data.local.datastore.AcademyRepository
import it.maestro.app.domain.mastery.MasteryRepository
import it.maestro.app.domain.time.Clock
import it.maestro.app.domain.time.SystemClock
import it.maestro.app.domain.wallet.TransactionReason
import it.maestro.app.domain.rewards.RewardEngine
import it.maestro.app.designsystem.components.MaestroMood
import it.maestro.game.model.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Partite minime perché il radar abbia senso per consigliare un capitolo. */
public const val ACADEMY_MIN_MATCHES: Int = 3

/**
 * Il capitolo che il Maestro consiglia in base al radar: Primiera→2, Banco→3, Sette e Finale→4,
 * Denari→1 (se non finito) altrimenti 2. `null` con meno di [ACADEMY_MIN_MATCHES] partite.
 * Se il capitolo 1 non è completato, il consiglio è sempre il capitolo 1 (gli altri sono chiusi).
 */
public fun recommendedChapter(
    profile: MasteryProfile,
    completed: Set<AcademyLessonId>,
): AcademyChapterId? {
    if (profile.matchesPlayed < ACADEMY_MIN_MATCHES) return null
    val firstDone = AcademyContent.CHAPTERS.first().lessons.all { it.id in completed }
    if (!firstDone) return AcademyChapterId.CHAPTER_1_FONDAMENTI
    return when (profile.radar.weakestAxis()) {
        RadarAxis.PRIMIERA, RadarAxis.DENARI -> AcademyChapterId.CHAPTER_2_PRIMIERA
        RadarAxis.BOARD -> AcademyChapterId.CHAPTER_3_DIFESA
        RadarAxis.SEVENS, RadarAxis.ENDGAME -> AcademyChapterId.CHAPTER_4_MAESTRIA
    }
}

/**
 * Stato della card di una singola lezione (sotto-modulo) all'interno di un capitolo.
 */
public data class AcademyLessonCardState(
    public val lesson: AcademyLesson,
    public val progress: AcademyLessonProgress,
    /** `true` se la lezione è stata ripassata (ripasso lampo) oggi. */
    public val isReviewedToday: Boolean = false,
)

/**
 * Stato della card di un capitolo (macro-modulo) con relative lezioni e progressione complessiva.
 */
public data class AcademyChapterCardState(
    public val chapter: AcademyChapter,
    public val lessons: List<AcademyLessonCardState>,
    public val completedCount: Int,
    public val totalCount: Int,
    public val isCompleted: Boolean,
    public val isUnlocked: Boolean,
)

/**
 * Modalità di visualizzazione dell'Accademia: Mappa dei capitoli oppure Lezione al tavolo.
 */
public sealed interface AcademyViewMode {
    public data object ChapterMap : AcademyViewMode

    public data class Lesson(
        public val lesson: AcademyLesson,
        public val currentStepIndex: Int,
        public val selectedOptionId: String? = null,
        public val isAnswerCorrect: Boolean? = null,
        public val isCardActionCompleted: Boolean = false,
        @StringRes public val feedbackRes: Int? = null,
        public val dynamicMaestroMood: MaestroMood? = null,
        /** Ripasso lampo: una domanda e la regola d'oro, senza crediti. */
        public val isReview: Boolean = false,
    ) : AcademyViewMode {
        public val currentStep: AcademyStep
            get() = lesson.steps.getOrElse(currentStepIndex) { lesson.steps.first() }

        public val effectiveMaestroMood: MaestroMood
            get() = dynamicMaestroMood ?: currentStep.maestroMood

        public val isStepCompleted: Boolean
            get() = when (currentStep.type) {
                AcademyStepType.THEORY_EXPLANATION -> true
                AcademyStepType.INTERACTIVE_QUESTION -> isAnswerCorrect == true
                AcademyStepType.CARD_ACTION -> isCardActionCompleted
                AcademyStepType.SUMMARY_AND_BADGE -> true
            }

        public val isLastStep: Boolean
            get() = currentStepIndex >= lesson.steps.size - 1
    }
}

/**
 * Stato complessivo dell'Accademia del Maestro arricchita (task F2-06).
 */
public data class AcademyUiState(
    public val chapters: List<AcademyChapterCardState> = emptyList(),
    public val expandedChapterId: AcademyChapterId? = AcademyChapterId.CHAPTER_1_FONDAMENTI,
    public val viewMode: AcademyViewMode = AcademyViewMode.ChapterMap,
    /** Il capitolo consigliato dal Maestro in base al radar; `null` senza abbastanza partite. */
    public val recommendedChapterId: AcademyChapterId? = null,
)

/**
 * ViewModel che governa la progressione didattica a capitoli e lezioni al tavolo del Maestro.
 */
public class AcademyViewModel(
    private val academyRepository: AcademyRepository,
    private val rewardEngine: RewardEngine? = null,
    private val availableChapters: List<AcademyChapter> = AcademyContent.CHAPTERS,
    private val masteryRepository: MasteryRepository? = null,
    private val clock: Clock = SystemClock,
) : ViewModel() {

    private val viewModeState = MutableStateFlow<AcademyViewMode>(AcademyViewMode.ChapterMap)
    private val expandedChapterState = MutableStateFlow<AcademyChapterId?>(AcademyChapterId.CHAPTER_1_FONDAMENTI)

    public val uiState: StateFlow<AcademyUiState> = combine(
        academyRepository.observeProgress(),
        viewModeState,
        expandedChapterState,
        masteryRepository?.observeMasteryProfile() ?: flowOf(null),
    ) { progressMap, currentViewMode, expandedChapterId, mastery ->
        val chapterStates = availableChapters.map { chapter ->
            val lessonStates = chapter.lessons.map { lesson ->
                val progress = progressMap[lesson.id] ?: AcademyLessonProgress(
                    lessonId = lesson.id,
                    isUnlocked = chapter.number == 1 && lesson.lessonNumber == 1,
                    isCompleted = false,
                )
                AcademyLessonCardState(
                    lesson = lesson,
                    progress = progress,
                    isReviewedToday = progress.reviewedEpochDay == clock.today().toEpochDay(),
                )
            }
            val completedCount = lessonStates.count { it.progress.isCompleted }
            val totalCount = lessonStates.size
            val isCompleted = completedCount == totalCount && totalCount > 0
            val isUnlocked = lessonStates.any { it.progress.isUnlocked }

            AcademyChapterCardState(
                chapter = chapter,
                lessons = lessonStates,
                completedCount = completedCount,
                totalCount = totalCount,
                isCompleted = isCompleted,
                isUnlocked = isUnlocked,
            )
        }

        AcademyUiState(
            chapters = chapterStates,
            expandedChapterId = expandedChapterId,
            viewMode = currentViewMode,
            recommendedChapterId = mastery?.let {
                recommendedChapter(it, progressMap.filterValues { p -> p.isCompleted }.keys)
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AcademyUiState(),
    )

    /**
     * Espande o chiude la sezione del capitolo nella mappa.
     */
    public fun toggleChapter(chapterId: AcademyChapterId) {
        expandedChapterState.value = if (expandedChapterState.value == chapterId) null else chapterId
    }

    /**
     * Apre la lezione specificata, partendo dal primo step.
     */
    public fun openLesson(lessonId: AcademyLessonId) {
        val lesson = AcademyContent.ALL_LESSONS.firstOrNull { it.id == lessonId } ?: return
        viewModeState.value = AcademyViewMode.Lesson(
            lesson = lesson,
            currentStepIndex = 0,
        )
    }

    /**
     * Apre il ripasso lampo di una lezione: la domanda di verifica e la regola d'oro (nessun credito).
     */
    public fun openReview(lessonId: AcademyLessonId) {
        val lesson = AcademyContent.ALL_LESSONS.firstOrNull { it.id == lessonId } ?: return
        val summary = lesson.steps.lastOrNull { it.type == AcademyStepType.SUMMARY_AND_BADGE } ?: return
        val question = lesson.steps.firstOrNull { it.type == AcademyStepType.INTERACTIVE_QUESTION }
            ?: lesson.steps.first { it.type != AcademyStepType.SUMMARY_AND_BADGE }
        val flash = lesson.copy(steps = listOf(question, summary))
        viewModeState.value = AcademyViewMode.Lesson(
            lesson = flash,
            currentStepIndex = 0,
            isReview = true,
        )
    }

    /**
     * Ritorna alla mappa dei capitoli.
     */
    public fun backToMap() {
        viewModeState.value = AcademyViewMode.ChapterMap
    }

    /**
     * Gestisce la risposta a un quesito a scelta multipla.
     */
    public fun selectOption(optionId: String) {
        val currentLesson = viewModeState.value as? AcademyViewMode.Lesson ?: return
        val currentStep = currentLesson.currentStep
        val option = currentStep.options.firstOrNull { it.id == optionId } ?: return

        viewModeState.value = currentLesson.copy(
            selectedOptionId = optionId,
            isAnswerCorrect = option.isCorrect,
            feedbackRes = option.feedbackRes,
            dynamicMaestroMood = if (option.isCorrect) MaestroMood.PLEASED else MaestroMood.WARNING,
        )
    }

    /**
     * Gestisce il tocco / giocata di una carta dalla mano del giocatore.
     */
    public fun playCard(card: Card) {
        val currentLesson = viewModeState.value as? AcademyViewMode.Lesson ?: return
        val currentStep = currentLesson.currentStep

        if (currentStep.type != AcademyStepType.CARD_ACTION) return

        if (card == currentStep.targetCard) {
            viewModeState.value = currentLesson.copy(
                isCardActionCompleted = true,
                isAnswerCorrect = true,
                feedbackRes = R.string.academy_action_correct_card,
                dynamicMaestroMood = MaestroMood.APPLAUDING,
            )
        } else {
            viewModeState.value = currentLesson.copy(
                isCardActionCompleted = false,
                isAnswerCorrect = false,
                feedbackRes = R.string.academy_action_wrong_card,
                dynamicMaestroMood = MaestroMood.WARNING,
            )
        }
    }

    /**
     * Avanza al passo successivo della lezione, oppure conclude la lezione salvandola nel repository.
     */
    public fun nextStep() {
        val currentLesson = viewModeState.value as? AcademyViewMode.Lesson ?: return
        val lesson = currentLesson.lesson

        if (currentLesson.isLastStep && currentLesson.isReview) {
            viewModelScope.launch { academyRepository.markReviewed(lesson.id) }
            viewModeState.value = AcademyViewMode.ChapterMap
        } else if (currentLesson.isLastStep) {
            viewModelScope.launch {
                academyRepository.completeLesson(lesson.id)
                rewardEngine?.onLessonCompleted(lesson.id.name)
            }
            viewModeState.value = AcademyViewMode.ChapterMap
        } else {
            val nextIndex = currentLesson.currentStepIndex + 1
            viewModeState.value = AcademyViewMode.Lesson(
                lesson = lesson,
                currentStepIndex = nextIndex,
                dynamicMaestroMood = lesson.steps[nextIndex].maestroMood,
                isReview = currentLesson.isReview,
            )
        }
    }
}
