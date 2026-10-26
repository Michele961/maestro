package it.maestro.app.ui.screens.academy

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.R
import it.maestro.app.data.local.academy.AcademyContent
import it.maestro.app.data.local.academy.AcademyModule
import it.maestro.app.data.local.academy.AcademyModuleId
import it.maestro.app.data.local.academy.AcademyProgress
import it.maestro.app.data.local.academy.AcademyStep
import it.maestro.app.data.local.academy.AcademyStepType
import it.maestro.app.data.local.datastore.AcademyRepository
import it.maestro.app.ui.components.MaestroMood
import it.maestro.game.model.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Stato della card di un singolo modulo nella mappa dell'Accademia.
 */
public data class AcademyModuleCardState(
    public val module: AcademyModule,
    public val progress: AcademyProgress,
)

/**
 * Modalità di visualizzazione dell'Accademia: Mappa del percorso oppure Lezione interattiva al tavolo.
 */
public sealed interface AcademyViewMode {
    public data object ModuleMap : AcademyViewMode

    public data class Lesson(
        public val module: AcademyModule,
        public val currentStepIndex: Int,
        public val selectedOptionId: String? = null,
        public val isAnswerCorrect: Boolean? = null,
        public val isCardActionCompleted: Boolean = false,
        @StringRes public val feedbackRes: Int? = null,
        public val dynamicMaestroMood: MaestroMood? = null,
    ) : AcademyViewMode {
        public val currentStep: AcademyStep
            get() = module.steps.getOrElse(currentStepIndex) { module.steps.first() }

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
            get() = currentStepIndex >= module.steps.size - 1
    }
}

/**
 * Stato complessivo della UI dell'Accademia del Maestro (task F2-06).
 */
public data class AcademyUiState(
    public val modules: List<AcademyModuleCardState> = emptyList(),
    public val viewMode: AcademyViewMode = AcademyViewMode.ModuleMap,
)

/**
 * ViewModel che governa la progressione didattica, l'avanzamento dei moduli e i quesiti al tavolo.
 */
public class AcademyViewModel(
    private val academyRepository: AcademyRepository,
    private val availableModules: List<AcademyModule> = AcademyContent.MODULES,
) : ViewModel() {

    private val viewModeState = MutableStateFlow<AcademyViewMode>(AcademyViewMode.ModuleMap)

    public val uiState: StateFlow<AcademyUiState> = combine(
        academyRepository.observeProgress(),
        viewModeState,
    ) { progressMap, currentViewMode ->
        val cardStates = availableModules.map { module ->
            val progress = progressMap[module.id] ?: AcademyProgress(
                moduleId = module.id,
                isUnlocked = module.number == 1,
                isCompleted = false,
            )
            AcademyModuleCardState(
                module = module,
                progress = progress,
            )
        }
        AcademyUiState(
            modules = cardStates,
            viewMode = currentViewMode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AcademyUiState(),
    )

    /**
     * Apre la lezione del modulo specificato, partendo dal primo step.
     */
    public fun openModule(moduleId: AcademyModuleId) {
        val module = availableModules.firstOrNull { it.id == moduleId } ?: return
        viewModeState.value = AcademyViewMode.Lesson(
            module = module,
            currentStepIndex = 0,
        )
    }

    /**
     * Ritorna alla mappa dell'Accademia.
     */
    public fun backToMap() {
        viewModeState.value = AcademyViewMode.ModuleMap
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
                feedbackRes = null,
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
     * Avanza al passo successivo della lezione, oppure conclude il modulo salvando la progressione.
     */
    public fun nextStep() {
        val currentLesson = viewModeState.value as? AcademyViewMode.Lesson ?: return
        val module = currentLesson.module

        if (currentLesson.isLastStep) {
            // Completamento del modulo
            viewModelScope.launch {
                academyRepository.completeModule(module.id)
            }
            viewModeState.value = AcademyViewMode.ModuleMap
        } else {
            val nextIndex = currentLesson.currentStepIndex + 1
            viewModeState.value = AcademyViewMode.Lesson(
                module = module,
                currentStepIndex = nextIndex,
                dynamicMaestroMood = module.steps[nextIndex].maestroMood,
            )
        }
    }
}
