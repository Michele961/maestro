package it.maestro.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.ai.puzzle.PuzzleGoal
import it.maestro.ai.puzzle.TacticalPuzzle
import it.maestro.app.R
import it.maestro.app.data.local.DailyPuzzleProvider
import it.maestro.app.data.local.datastore.DailyPuzzleRepository
import it.maestro.app.data.local.datastore.RetentionRepository
import it.maestro.app.data.local.missions.DailyMissionType
import it.maestro.app.ui.components.MaestroMood
import it.maestro.game.model.Card
import it.maestro.game.scopa.ScopaView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    val shareText: String = "",
    val isAlreadySolvedToday: Boolean = false,
)

/**
 * ViewModel per la "Sfida del Giorno" con supporto a DataStore e coaching maieutico (task F2-03).
 */
public class DailyPuzzleViewModel(
    private val repository: DailyPuzzleRepository,
    private val retentionRepository: RetentionRepository? = null,
    private val customDate: LocalDate? = null,
    private val customPuzzle: TacticalPuzzle? = null,
) : ViewModel() {

    private val effectiveDate: LocalDate = customDate ?: LocalDate.now()
    private val dateString: String = effectiveDate.toString()
    private val puzzleNumber: Int = DailyPuzzleProvider.getPuzzleNumber(effectiveDate)

    private val _uiState = MutableStateFlow(
        DailyPuzzleUiState(
            puzzleNumber = puzzleNumber,
            dateString = dateString,
        ),
    )
    public val uiState: StateFlow<DailyPuzzleUiState> = _uiState.asStateFlow()

    init {
        loadDailyPuzzle()
    }

    private fun loadDailyPuzzle() {
        val puzzle = customPuzzle ?: DailyPuzzleProvider.getDailyPuzzle(effectiveDate)

        viewModelScope.launch {
            val record = repository.getDailyPuzzleRecord(dateString)
            if (record.isSolvedToday) {
                _uiState.value = DailyPuzzleUiState(
                    puzzleNumber = puzzleNumber,
                    dateString = dateString,
                    puzzle = puzzle,
                    currentView = puzzle.initialView,
                    status = DailyPuzzleStatus.Solved(record.attemptsToday),
                    attemptsCount = record.attemptsToday,
                    maestroMood = MaestroMood.TRIUMPHANT,
                    maestroMessageResId = R.string.puzzle_coach_already_solved,
                    shareText = PuzzleShareFormatter.formatShareText(
                        puzzleNumber = puzzleNumber,
                        attemptsCount = maxOf(1, record.attemptsToday),
                        solved = true,
                    ),
                    isAlreadySolvedToday = true,
                )
            } else {
                _uiState.value = DailyPuzzleUiState(
                    puzzleNumber = puzzleNumber,
                    dateString = dateString,
                    puzzle = puzzle,
                    currentView = puzzle.initialView,
                    status = DailyPuzzleStatus.Playing,
                    attemptsCount = 0,
                    maestroMood = MaestroMood.THINKING,
                    maestroMessageResId = R.string.puzzle_coach_initial,
                    isAlreadySolvedToday = false,
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
            val shareText = PuzzleShareFormatter.formatShareText(
                puzzleNumber = currentState.puzzleNumber,
                attemptsCount = newAttempts,
                solved = true,
            )
            _uiState.value = currentState.copy(
                status = DailyPuzzleStatus.Solved(newAttempts),
                attemptsCount = newAttempts,
                maestroMood = MaestroMood.TRIUMPHANT,
                maestroMessageResId = R.string.puzzle_coach_solved,
                shareText = shareText,
            )
            viewModelScope.launch {
                repository.recordPuzzleCompletion(
                    todayDate = dateString,
                    attempts = newAttempts,
                    solved = true,
                )
                retentionRepository?.let { retention ->
                    retention.updateMissionProgress(effectiveDate, DailyMissionType.SOLVE_DAILY_PUZZLE)
                    retention.recordGameActivity(effectiveDate)
                }
            }
        } else {
            val hintLevel = if (newAttempts == 1) 1 else 2
            val hintResId = when (hintLevel) {
                1 -> R.string.puzzle_coach_hint_level1
                else -> when (puzzle.goal) {
                    PuzzleGoal.MAKE_SWEEP -> R.string.puzzle_coach_hint_sweep
                    PuzzleGoal.WIN_MATCH -> R.string.puzzle_coach_hint_win
                    PuzzleGoal.MAXIMIZE_POINTS -> R.string.puzzle_coach_hint_points
                }
            }
            val mood = if (hintLevel == 1) MaestroMood.ENCOURAGING else MaestroMood.THINKING

            _uiState.value = currentState.copy(
                status = DailyPuzzleStatus.WrongAttempt(attemptNumber = newAttempts, hintLevel = hintLevel),
                attemptsCount = newAttempts,
                maestroMood = mood,
                maestroMessageResId = hintResId,
            )
        }
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
}
