package it.maestro.app.ui

import it.maestro.app.data.local.datastore.InMemoryRetentionRepository
import it.maestro.app.domain.time.FakeClock
import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.domain.rewards.RewardEngine
import it.maestro.app.domain.academy.AcademyChapterId
import it.maestro.app.data.local.academy.AcademyContent
import it.maestro.app.domain.academy.AcademyLessonId
import it.maestro.app.data.local.datastore.InMemoryAcademyRepository
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.domain.wallet.TransactionReason
import it.maestro.app.feature.academy.presentation.AcademyViewMode
import it.maestro.app.feature.academy.presentation.AcademyViewModel
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test unitari per il ViewModel dell'Accademia con capitoli e sotto-moduli (task F2-06).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AcademyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialUiStateReflectsChaptersAndFirstLessonUnlocked() = runTest {
        val repo = InMemoryAcademyRepository()
        val viewModel = AcademyViewModel(
            academyRepository = repo,
            availableChapters = AcademyContent.CHAPTERS,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        advanceUntilIdle()
        val state = viewModel.uiState.first { it.chapters.isNotEmpty() }

        assertEquals(4, state.chapters.size)
        // Capitolo 1 è sbloccato
        assertTrue(state.chapters[0].isUnlocked)
        assertFalse(state.chapters[0].isCompleted)
        assertEquals(0, state.chapters[0].completedCount)
        assertEquals(4, state.chapters[0].totalCount)

        // Prima lezione di Cap 1 è sbloccata
        assertTrue(state.chapters[0].lessons[0].progress.isUnlocked)
        assertFalse(state.chapters[0].lessons[0].progress.isCompleted)
        assertFalse(state.chapters[0].lessons[1].progress.isUnlocked)

        // Capitolo 2 è bloccato
        assertFalse(state.chapters[1].isUnlocked)
        assertEquals(AcademyViewMode.ChapterMap, state.viewMode)
    }

    @Test
    fun toggleChapterExpandsAndCollapsesSection() = runTest {
        val repo = InMemoryAcademyRepository()
        val viewModel = AcademyViewModel(
            academyRepository = repo,
            availableChapters = AcademyContent.CHAPTERS,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        advanceUntilIdle()
        assertEquals(AcademyChapterId.CHAPTER_1_FONDAMENTI, viewModel.uiState.value.expandedChapterId)

        viewModel.toggleChapter(AcademyChapterId.CHAPTER_1_FONDAMENTI)
        advanceUntilIdle()
        assertEquals(null, viewModel.uiState.value.expandedChapterId)

        viewModel.toggleChapter(AcademyChapterId.CHAPTER_2_PRIMIERA)
        advanceUntilIdle()
        assertEquals(AcademyChapterId.CHAPTER_2_PRIMIERA, viewModel.uiState.value.expandedChapterId)
    }

    @Test
    fun openLessonTransitionsToLessonView() = runTest {
        val repo = InMemoryAcademyRepository()
        val viewModel = AcademyViewModel(
            academyRepository = repo,
            availableChapters = AcademyContent.CHAPTERS,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        advanceUntilIdle()
        viewModel.openLesson(AcademyLessonId.LESSON_1_1_PRESA_SINGOLA)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val lessonMode = state.viewMode as? AcademyViewMode.Lesson
        assertNotNull(lessonMode)
        assertEquals(AcademyLessonId.LESSON_1_1_PRESA_SINGOLA, lessonMode.lesson.id)
        assertEquals(0, lessonMode.currentStepIndex)
        assertFalse(lessonMode.isStepCompleted)
    }

    @Test
    fun completeLessonUnlocksNextLessonInState() = runTest {
        val repo = InMemoryAcademyRepository()
        val viewModel = AcademyViewModel(
            academyRepository = repo,
            availableChapters = AcademyContent.CHAPTERS,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        advanceUntilIdle()
        viewModel.openLesson(AcademyLessonId.LESSON_1_1_PRESA_SINGOLA)
        advanceUntilIdle()

        // Step 1: Risponde correttamente
        viewModel.selectOption("c1_l1_opt1")
        advanceUntilIdle()
        var lesson = viewModel.uiState.value.viewMode as AcademyViewMode.Lesson
        assertTrue(lesson.isStepCompleted)

        // Passa allo Step 2 (Summary & Badge)
        viewModel.nextStep()
        advanceUntilIdle()
        lesson = viewModel.uiState.value.viewMode as AcademyViewMode.Lesson
        assertTrue(lesson.isLastStep)
        assertTrue(lesson.isStepCompleted)

        // Completa la lezione
        viewModel.nextStep()
        advanceUntilIdle()

        // Verifica ritorno a ChapterMap e sblocco della lezione 1.2
        val state = viewModel.uiState.value
        assertEquals(AcademyViewMode.ChapterMap, state.viewMode)
        assertTrue(state.chapters[0].lessons[0].progress.isCompleted)
        assertTrue(state.chapters[0].lessons[1].progress.isUnlocked)
        assertEquals(1, state.chapters[0].completedCount)
    }

    @Test
    fun cardActionValidationWorksCorrectly() = runTest {
        val repo = InMemoryAcademyRepository()
        val viewModel = AcademyViewModel(
            academyRepository = repo,
            availableChapters = AcademyContent.CHAPTERS,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        advanceUntilIdle()
        // Lezione 1.3: Il Re dei Denari (Card Action)
        viewModel.openLesson(AcademyLessonId.LESSON_1_3_SETTEBELLO)
        advanceUntilIdle()

        var lesson = viewModel.uiState.value.viewMode as AcademyViewMode.Lesson
        assertFalse(lesson.isCardActionCompleted)

        // Carta errata
        viewModel.playCard(Card(Rank.QUATTRO, Suit.COPPE))
        advanceUntilIdle()
        lesson = viewModel.uiState.value.viewMode as AcademyViewMode.Lesson
        assertFalse(lesson.isCardActionCompleted)
        assertFalse(lesson.isStepCompleted)

        // Carta corretta: 7 di Bastoni
        viewModel.playCard(Card(Rank.SETTE, Suit.BASTONI))
        advanceUntilIdle()
        lesson = viewModel.uiState.value.viewMode as AcademyViewMode.Lesson
        assertTrue(lesson.isCardActionCompleted)
        assertTrue(lesson.isStepCompleted)
    }

    @Test
    fun completingLessonAwardsCreditsToWallet() = runTest {
        val repo = InMemoryAcademyRepository()
        val walletRepo = InMemoryWalletRepository()
        val viewModel = AcademyViewModel(
            academyRepository = repo,
            rewardEngine = RewardEngine(walletRepo, InMemoryRetentionRepository(), InMemoryMasteryRepository(), FakeClock(java.time.LocalDate.of(2026, 10, 26))),
            availableChapters = AcademyContent.CHAPTERS,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        advanceUntilIdle()
        viewModel.openLesson(AcademyLessonId.LESSON_1_1_PRESA_SINGOLA)
        advanceUntilIdle()

        viewModel.selectOption("c1_l1_opt1")
        advanceUntilIdle()
        viewModel.nextStep() // To Step 2 (summary step)
        advanceUntilIdle()
        viewModel.nextStep() // Complete lesson
        advanceUntilIdle()

        assertEquals(5, walletRepo.getBalance(), "Completare una lezione dell'Accademia deve accreditare 5 crediti")
        assertTrue(walletRepo.hasClaimed(TransactionReason.ACADEMY_LESSON_REWARD, AcademyLessonId.LESSON_1_1_PRESA_SINGOLA.name))
    }

    @Test
    fun flashReviewShowsQuestionAndRuleWithoutCreditsAndMarksReviewedToday() = runTest {
        val clock = FakeClock(java.time.LocalDate.of(2026, 10, 28))
        val repo = InMemoryAcademyRepository(
            initialCompleted = setOf(AcademyLessonId.LESSON_1_1_PRESA_SINGOLA),
            clock = clock,
        )
        val walletRepo = InMemoryWalletRepository()
        val viewModel = AcademyViewModel(
            academyRepository = repo,
            rewardEngine = RewardEngine(walletRepo, InMemoryRetentionRepository(), InMemoryMasteryRepository(), clock),
            clock = clock,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        val before = walletRepo.getBalance()
        assertFalse(viewModel.uiState.value.chapters[0].lessons[0].isReviewedToday)

        viewModel.openReview(AcademyLessonId.LESSON_1_1_PRESA_SINGOLA)
        advanceUntilIdle()
        val review = viewModel.uiState.value.viewMode as AcademyViewMode.Lesson
        assertTrue(review.isReview)
        assertEquals(2, review.lesson.steps.size)

        viewModel.selectOption("c1_l1_opt1")
        viewModel.nextStep()
        advanceUntilIdle()
        assertTrue((viewModel.uiState.value.viewMode as AcademyViewMode.Lesson).isReview)
        viewModel.nextStep()
        advanceUntilIdle()

        assertEquals(AcademyViewMode.ChapterMap, viewModel.uiState.value.viewMode)
        assertTrue(viewModel.uiState.value.chapters[0].lessons[0].isReviewedToday)
        assertEquals(before, walletRepo.getBalance())

        clock.date = clock.date.plusDays(1)
        val next = AcademyViewModel(academyRepository = repo, clock = clock)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { next.uiState.collect() }
        advanceUntilIdle()
        assertFalse(next.uiState.value.chapters[0].lessons[0].isReviewedToday)
    }

    @Test
    fun finishingTheLastLessonOfAChapterGivesChapterPxOnlyOnce() = runTest {
        val clock = FakeClock(java.time.LocalDate.of(2026, 10, 28))
        val chapterOne = AcademyContent.CHAPTERS.first().lessons.map { it.id }
        val last = chapterOne.last()
        val repo = InMemoryAcademyRepository(initialCompleted = chapterOne.dropLast(1).toSet(), clock = clock)
        val xp = it.maestro.app.data.local.datastore.InMemoryExperienceRepository()
        val viewModel = AcademyViewModel(
            academyRepository = repo,
            rewardEngine = RewardEngine(InMemoryWalletRepository(), InMemoryRetentionRepository(), InMemoryMasteryRepository(), clock, experience = xp),
            clock = clock,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        repeat(2) { // la seconda volta è un rigioco: il capitolo era già finito
            viewModel.openLesson(last)
            advanceUntilIdle()
            while (viewModel.uiState.value.viewMode is AcademyViewMode.Lesson) {
                val mode = viewModel.uiState.value.viewMode as AcademyViewMode.Lesson
                mode.currentStep.options.firstOrNull { it.isCorrect }?.let { viewModel.selectOption(it.id) }
                mode.currentStep.targetCard?.let { viewModel.playCard(it) }
                viewModel.nextStep()
                advanceUntilIdle()
            }
        }

        assertEquals(it.maestro.app.domain.progress.Experience.CHAPTER_DONE, xp.observePoints().first())
    }
}
