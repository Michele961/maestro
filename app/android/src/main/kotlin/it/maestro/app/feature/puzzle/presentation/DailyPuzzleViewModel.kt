package it.maestro.app.feature.puzzle.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.ai.puzzle.PuzzleGoal
import it.maestro.ai.puzzle.TacticalPuzzle
import it.maestro.app.R
import it.maestro.app.data.local.DailyPuzzleProvider
import it.maestro.app.data.local.PuzzleLesson
import it.maestro.app.domain.coach.Phrase
import it.maestro.app.domain.coach.PuzzleCoach
import it.maestro.app.data.local.DailyPuzzleTier
import it.maestro.app.data.local.datastore.DailyPuzzleRecord
import it.maestro.app.data.local.datastore.DailyPuzzleRepository
import it.maestro.app.domain.retention.RetentionRepository
import it.maestro.app.domain.retention.DailyMissionType
import it.maestro.app.domain.wallet.TransactionReason
import it.maestro.app.domain.rewards.RewardEngine
import it.maestro.app.designsystem.components.MaestroMood
import it.maestro.game.model.Card
import it.maestro.game.scopa.ScopaView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import it.maestro.app.domain.time.Clock
import it.maestro.app.domain.time.SystemClock
import java.time.LocalDate

/**
 * Stato della risoluzione del puzzle quotidiano.
 */
public sealed interface DailyPuzzleStatus {
    public data object Playing : DailyPuzzleStatus
    public data class WrongAttempt(val attemptNumber: Int, val hintLevel: Int) : DailyPuzzleStatus
    public data class Solved(val attemptsCount: Int) : DailyPuzzleStatus
}

/**
 * Stato della UI della schermata "Sfida del Giorno" (task F2-03).
 */
public data class DailyPuzzleUiState(
    val puzzleNumber: Int = 1,
    val dateString: String = "",
    val puzzle: TacticalPuzzle? = null,
    val currentView: ScopaView? = null,
    val status: DailyPuzzleStatus = DailyPuzzleStatus.Playing,
    val attemptsCount: Int = 0,
    val maestroMood: MaestroMood = MaestroMood.THINKING,
    val maestroMessageResId: Int = R.string.puzzle_coach_initial,
    val goalDescriptionResId: Int = R.string.puzzle_goal_sweep_desc,
    val isAlreadySolvedToday: Boolean = false,
    val currentTierIndex: Int = 0,
    val availableTiers: List<DailyPuzzleTier> = DailyPuzzleProvider.TIERS,
    val solvedTiers: Set<Int> = emptySet(),
    val lesson: PuzzleLesson? = null,
    /** Battuta del Maestro composta con le carte della posizione (indizio o soluzione); vuota = vale [maestroMessageResId]. */
    val maestroPhrases: List<Phrase> = emptyList(),
    /** `true` dopo «Mostrami la soluzione»: il Maestro ha spiegato la mossa. */
    val solutionShown: Boolean = false,
)

/**
 * ViewModel per la "Sfida del Giorno" con supporto a DataStore e coaching maieutico (task F2-03).
 */
public class DailyPuzzleViewModel(
    private val repository: DailyPuzzleRepository,
    private val rewardEngine: RewardEngine? = null,
    private val customDate: LocalDate? = null,
    private val customPuzzle: TacticalPuzzle? = null,
    private val clock: Clock = SystemClock,
) : ViewModel() {

    // Il giorno si rilegge con refreshDay() (ritorno in primo piano), salvo una data fissata dai test.
    private var effectiveDate: LocalDate = customDate ?: clock.today()
    private val dateString: String get() = effectiveDate.toString()
    private val puzzleNumber: Int get() = DailyPuzzleProvider.getPuzzleNumber(effectiveDate)

    private val _uiState = MutableStateFlow(
        DailyPuzzleUiState(
            puzzleNumber = puzzleNumber,
            dateString = dateString,
        ),
    )
    public val uiState: StateFlow<DailyPuzzleUiState> = _uiState.asStateFlow()

    init {
        loadTier(0)
    }

    /**
     * Rilegge la data dal [Clock]; se è cambiato giorno carica la sfida nuova dal primo livello (task F1R-02).
     */
    public fun refreshDay() {
        if (customDate != null) return
        val today = clock.today()
        if (today == effectiveDate) return
        effectiveDate = today
        _uiState.value = DailyPuzzleUiState(puzzleNumber = puzzleNumber, dateString = dateString)
        loadTier(0)
    }

    /**
     * Seleziona uno dei 3 livelli progressivi del giorno (0: Riscaldamento, 1: Tattica, 2: Maestro).
     */
    public fun selectTier(tierIndex: Int) {
        if (tierIndex !in DailyPuzzleProvider.TIERS.indices) return
        loadTier(tierIndex)
    }

    /**
     * Passa al livello successivo se disponibile.
     */
    public fun nextTier() {
        val nextIndex = _uiState.value.currentTierIndex + 1
        if (nextIndex in DailyPuzzleProvider.TIERS.indices) {
            selectTier(nextIndex)
        }
    }

    private fun loadTier(tierIndex: Int) {
        val puzzle = if (customPuzzle != null && tierIndex == 0) {
            customPuzzle
        } else {
            DailyPuzzleProvider.getDailyPuzzle(effectiveDate, tierIndex)
        }
        val lesson = DailyPuzzleProvider.lessonOf(puzzle)

        viewModelScope.launch {
            val record = repository.getDailyPuzzleRecord(dateString)
            val isTierSolved = record.solvedTiersToday.contains(tierIndex) || (tierIndex == 0 && record.isSolvedToday)

            val goalMsg = when (puzzle.goal) {
                PuzzleGoal.MAKE_SWEEP -> R.string.puzzle_goal_sweep_desc
                PuzzleGoal.WIN_MATCH -> R.string.puzzle_goal_win_desc
                PuzzleGoal.MAXIMIZE_POINTS -> R.string.puzzle_goal_points_desc
            }

            val initialSpeech = if (tierIndex == 0) {
                R.string.puzzle_coach_initial
            } else {
                when (puzzle.goal) {
                    PuzzleGoal.MAKE_SWEEP -> R.string.puzzle_coach_initial_sweep
                    PuzzleGoal.MAXIMIZE_POINTS -> R.string.puzzle_coach_initial_points
                    PuzzleGoal.WIN_MATCH -> R.string.puzzle_coach_initial_win
                }
            }

            if (isTierSolved) {
                _uiState.value = _uiState.value.copy(
                    puzzleNumber = puzzleNumber,
                    dateString = dateString,
                    puzzle = puzzle,
                    currentView = puzzle.initialView,
                    status = DailyPuzzleStatus.Solved(record.attemptsToday),
                    attemptsCount = record.attemptsToday,
                    maestroMood = MaestroMood.TRIUMPHANT,
                    maestroMessageResId = R.string.puzzle_coach_already_solved,
                    goalDescriptionResId = goalMsg,
                    isAlreadySolvedToday = true,
                    currentTierIndex = tierIndex,
                    solvedTiers = record.solvedTiersToday,
                    lesson = lesson,
                    maestroPhrases = emptyList(),
                    solutionShown = false,
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    puzzleNumber = puzzleNumber,
                    dateString = dateString,
                    puzzle = puzzle,
                    currentView = puzzle.initialView,
                    status = DailyPuzzleStatus.Playing,
                    attemptsCount = 0,
                    maestroMood = MaestroMood.THINKING,
                    maestroMessageResId = initialSpeech,
                    goalDescriptionResId = goalMsg,
                    isAlreadySolvedToday = false,
                    currentTierIndex = tierIndex,
                    solvedTiers = record.solvedTiersToday,
                    lesson = lesson,
                    maestroPhrases = emptyList(),
                    solutionShown = false,
                )
            }
        }
    }

    /**
     * Gestisce la selezione di una carta dalla mano del giocatore.
     */
    public fun playCard(card: Card) {
        val currentState = _uiState.value
        val puzzle = currentState.puzzle ?: return
        if (currentState.status is DailyPuzzleStatus.Solved) {
            return
        }

        val expectedCard = puzzle.firstMove.card
        val isCorrect = (card == expectedCard)
        val newAttempts = currentState.attemptsCount + 1

        if (isCorrect) {
            val lesson = DailyPuzzleProvider.lessonOf(puzzle)
            val updatedSolvedTiers = currentState.solvedTiers + currentState.currentTierIndex
            val currentTier = DailyPuzzleProvider.TIERS.getOrNull(currentState.currentTierIndex)
                ?: DailyPuzzleProvider.TIERS[0]

            val wasAlreadySolved = currentState.isAlreadySolvedToday ||
                currentState.solvedTiers.contains(currentState.currentTierIndex)

            _uiState.value = currentState.copy(
                status = DailyPuzzleStatus.Solved(newAttempts),
                attemptsCount = newAttempts,
                maestroMood = MaestroMood.TRIUMPHANT,
                maestroMessageResId = R.string.puzzle_coach_solved,
                solvedTiers = updatedSolvedTiers,
                lesson = lesson,
                maestroPhrases = emptyList(),
            )
            if (!wasAlreadySolved) {
                viewModelScope.launch {
                    repository.recordPuzzleCompletion(
                        todayDate = dateString,
                        attempts = newAttempts,
                        solved = true,
                        tierIndex = currentState.currentTierIndex,
                    )
                    rewardEngine?.onPuzzleSolved(
                        date = effectiveDate,
                        tierIndex = currentState.currentTierIndex,
                        tierCredits = currentTier.rewardCredits,
                    )
                }
            }
        } else {
            val hintLevel = if (newAttempts == 1) 1 else 2
            val hint = PuzzleCoach.hint(puzzle.firstMove, puzzle.initialView.table, hintLevel)
            val mood = if (hintLevel == 1) MaestroMood.ENCOURAGING else MaestroMood.THINKING

            _uiState.value = currentState.copy(
                status = DailyPuzzleStatus.WrongAttempt(attemptNumber = newAttempts, hintLevel = hintLevel),
                attemptsCount = newAttempts,
                maestroMood = mood,
                maestroPhrases = hint.phrases,
            )
        }
    }

    /**
     * Fornisce un indizio logico progressivo senza svelare direttamente la carta.
     */
    public fun requestHint() {
        val currentState = _uiState.value
        if (currentState.status is DailyPuzzleStatus.Solved) return
        val puzzle = currentState.puzzle ?: return

        val currentLevel = when (val s = currentState.status) {
            is DailyPuzzleStatus.WrongAttempt -> s.hintLevel
            else -> if (currentState.maestroPhrases.firstOrNull()?.id == "puzzle_hint_card") 1 else 0
        }
        val nextLevel = (currentLevel + 1).coerceAtMost(2)
        val hint = PuzzleCoach.hint(puzzle.firstMove, puzzle.initialView.table, nextLevel)
        _uiState.value = currentState.copy(
            maestroMood = MaestroMood.ENCOURAGING,
            maestroPhrases = hint.phrases,
        )
    }

    /**
     * «Mostrami la soluzione»: il Maestro nomina la carta giusta e spiega perché. Non risolve la sfida: per i crediti
     * si gioca comunque la carta.
     */
    public fun showSolution() {
        val currentState = _uiState.value
        if (currentState.status is DailyPuzzleStatus.Solved) return
        val puzzle = currentState.puzzle ?: return
        val solution = PuzzleCoach.solution(puzzle.firstMove, puzzle.initialView.table)
        _uiState.value = currentState.copy(
            maestroMood = MaestroMood.PLEASED,
            maestroPhrases = solution.phrases,
            solutionShown = true,
        )
    }

    /**
     * Riavvia il tentativo tornando allo stato di riflessione e gioco.
     */
    public fun retryAttempt() {
        val currentState = _uiState.value
        if (currentState.status is DailyPuzzleStatus.WrongAttempt) {
            _uiState.value = currentState.copy(
                status = DailyPuzzleStatus.Playing,
            )
        }
    }

    /**
     * Permette di rigiocare la sfida del giorno per allenarsi liberamente anche se già completata oggi.
     */
    public fun replayPuzzle() {
        val currentState = _uiState.value
        val puzzle = currentState.puzzle ?: return
        val goalMsg = when (puzzle.goal) {
            PuzzleGoal.MAKE_SWEEP -> R.string.puzzle_goal_sweep_desc
            PuzzleGoal.WIN_MATCH -> R.string.puzzle_goal_win_desc
            PuzzleGoal.MAXIMIZE_POINTS -> R.string.puzzle_goal_points_desc
        }
        val initialSpeech = if (currentState.currentTierIndex == 0) {
            R.string.puzzle_coach_initial
        } else {
            when (puzzle.goal) {
                PuzzleGoal.MAKE_SWEEP -> R.string.puzzle_coach_initial_sweep
                PuzzleGoal.MAXIMIZE_POINTS -> R.string.puzzle_coach_initial_points
                PuzzleGoal.WIN_MATCH -> R.string.puzzle_coach_initial_win
            }
        }
        _uiState.value = currentState.copy(
            status = DailyPuzzleStatus.Playing,
            attemptsCount = 0,
            isAlreadySolvedToday = true,
            maestroMood = MaestroMood.THINKING,
            maestroMessageResId = initialSpeech,
            goalDescriptionResId = goalMsg,
            lesson = DailyPuzzleProvider.lessonOf(puzzle),
            maestroPhrases = emptyList(),
            solutionShown = false,
        )
    }
}
