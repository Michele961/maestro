package it.maestro.app.ui

import it.maestro.app.data.local.academy.AcademyContent
import it.maestro.app.data.local.academy.AcademyModuleId
import it.maestro.app.data.local.datastore.InMemoryAcademyRepository
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
 * Test di unità per il ViewModel dell'Accademia del Maestro (task F2-06).
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
    fun initialUiStateReflectsDefaultProgression() = runTest {
        val repo = InMemoryAcademyRepository()
        val viewModel = AcademyViewModel(
            academyRepository = repo,
            availableModules = AcademyContent.MODULES,
        )

        advanceUntilIdle()
        val state = viewModel.uiState.first { it.modules.isNotEmpty() }

        assertEquals(3, state.modules.size)
        assertTrue(state.modules[0].progress.isUnlocked)
        assertFalse(state.modules[0].progress.isCompleted)
        assertFalse(state.modules[1].progress.isUnlocked)
        assertFalse(state.modules[2].progress.isUnlocked)
        assertEquals(AcademyViewMode.ModuleMap, state.viewMode)
    }

    @Test
    fun openModuleTransitionsToLessonAtStepZero() = runTest {
        val repo = InMemoryAcademyRepository()
        val viewModel = AcademyViewModel(
            academyRepository = repo,
            availableModules = AcademyContent.MODULES,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        advanceUntilIdle()
        viewModel.openModule(AcademyModuleId.MODULE_1_ABBECEDARIO)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val lesson = state.viewMode as? AcademyViewMode.Lesson
        assertNotNull(lesson)
        assertEquals(AcademyModuleId.MODULE_1_ABBECEDARIO, lesson.module.id)
        assertEquals(0, lesson.currentStepIndex)
        assertEquals(1, lesson.currentStep.stepNumber)
        assertFalse(lesson.isStepCompleted)
    }

    @Test
    fun questionStepValidatesCorrectAndWrongAnswers() = runTest {
        val repo = InMemoryAcademyRepository()
        val viewModel = AcademyViewModel(
            academyRepository = repo,
            availableModules = AcademyContent.MODULES,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        advanceUntilIdle()
        viewModel.openModule(AcademyModuleId.MODULE_1_ABBECEDARIO)
        advanceUntilIdle()

        // Seleziona opzione errata
        viewModel.selectOption("m1_s1_opt2")
        advanceUntilIdle()
        var lesson = viewModel.uiState.value.viewMode as AcademyViewMode.Lesson
        assertEquals(false, lesson.isAnswerCorrect)
        assertFalse(lesson.isStepCompleted)
        assertNotNull(lesson.feedbackRes)

        // Seleziona opzione corretta
        viewModel.selectOption("m1_s1_opt1")
        advanceUntilIdle()
        lesson = viewModel.uiState.value.viewMode as AcademyViewMode.Lesson
        assertEquals(true, lesson.isAnswerCorrect)
        assertTrue(lesson.isStepCompleted)
    }

    @Test
    fun cardActionStepRequiresTargetCard() = runTest {
        val repo = InMemoryAcademyRepository()
        val viewModel = AcademyViewModel(
            academyRepository = repo,
            availableModules = AcademyContent.MODULES,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        advanceUntilIdle()
        viewModel.openModule(AcademyModuleId.MODULE_1_ABBECEDARIO)
        advanceUntilIdle()

        // Supera lo step 1
        viewModel.selectOption("m1_s1_opt1")
        advanceUntilIdle()
        viewModel.nextStep()
        advanceUntilIdle()

        var lesson = viewModel.uiState.value.viewMode as AcademyViewMode.Lesson
        assertEquals(1, lesson.currentStepIndex)
        assertFalse(lesson.isCardActionCompleted)

        // Gioca carta errata (4 di Coppe)
        viewModel.playCard(Card(Rank.QUATTRO, Suit.COPPE))
        advanceUntilIdle()
        lesson = viewModel.uiState.value.viewMode as AcademyViewMode.Lesson
        assertFalse(lesson.isCardActionCompleted)
        assertFalse(lesson.isStepCompleted)

        // Gioca carta bersaglio corretta (7 di Bastoni)
        viewModel.playCard(Card(Rank.SETTE, Suit.BASTONI))
        advanceUntilIdle()
        lesson = viewModel.uiState.value.viewMode as AcademyViewMode.Lesson
        assertTrue(lesson.isCardActionCompleted)
        assertTrue(lesson.isStepCompleted)
    }

    @Test
    fun fullModuleCompletionUnlocksNextModuleInRepository() = runTest {
        val repo = InMemoryAcademyRepository()
        val viewModel = AcademyViewModel(
            academyRepository = repo,
            availableModules = AcademyContent.MODULES,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        advanceUntilIdle()
        viewModel.openModule(AcademyModuleId.MODULE_1_ABBECEDARIO)
        advanceUntilIdle()

        // Step 1
        viewModel.selectOption("m1_s1_opt1")
        advanceUntilIdle()
        viewModel.nextStep()
        advanceUntilIdle()

        // Step 2
        viewModel.playCard(Card(Rank.SETTE, Suit.BASTONI))
        advanceUntilIdle()
        viewModel.nextStep()
        advanceUntilIdle()

        // Step 3
        viewModel.selectOption("m1_s3_opt1")
        advanceUntilIdle()
        viewModel.nextStep()
        advanceUntilIdle()

        // Step 4 (Summary & Badge)
        val lessonStep4 = viewModel.uiState.value.viewMode as AcademyViewMode.Lesson
        assertEquals(3, lessonStep4.currentStepIndex)
        assertTrue(lessonStep4.isLastStep)
        assertTrue(lessonStep4.isStepCompleted)

        // Completamento finale
        viewModel.nextStep()
        advanceUntilIdle()

        // Verifica che sia tornato alla mappa e che il modulo 2 sia ora sbloccato
        val finalState = viewModel.uiState.value
        assertEquals(AcademyViewMode.ModuleMap, finalState.viewMode)
        assertTrue(finalState.modules[0].progress.isCompleted)
        assertTrue(finalState.modules[1].progress.isUnlocked)
        assertFalse(finalState.modules[2].progress.isUnlocked)
    }
}
