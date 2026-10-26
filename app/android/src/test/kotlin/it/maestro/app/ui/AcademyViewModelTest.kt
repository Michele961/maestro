package it.maestro.app.ui

import it.maestro.app.data.local.academy.AcademyChapterId
import it.maestro.app.data.local.academy.AcademyContent
import it.maestro.app.data.local.academy.AcademyLessonId
import it.maestro.app.data.local.datastore.InMemoryAcademyRepository
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.data.local.wallet.TransactionReason
import it.maestro.app.ui.screens.academy.AcademyViewMode
import it.maestro.app.ui.screens.academy.AcademyViewModel
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
            walletRepository = walletRepo,
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
}
