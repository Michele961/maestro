package it.maestro.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.ai.puzzle.PuzzleGoal
import it.maestro.ai.puzzle.TacticalPuzzle
import it.maestro.app.R
import it.maestro.app.data.local.DailyPuzzleProvider
import it.maestro.app.data.local.DailyPuzzleTier
import it.maestro.app.data.local.datastore.DailyPuzzleRecord
import it.maestro.app.data.local.datastore.DailyPuzzleRepository
import it.maestro.app.data.local.datastore.RetentionRepository
import it.maestro.app.data.local.missions.DailyMissionType
import it.maestro.app.data.local.wallet.TransactionReason
import it.maestro.app.data.local.wallet.WalletRepository
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
    val currentTierIndex: Int = 0,
    val availableTiers: List<DailyPuzzleTier> = DailyPuzzleProvider.TIERS,
    val solvedTiers: Set<Int> = emptySet(),
    val logicalConcept: String = "",
)

/**
 * ViewModel per la "Sfida del Giorno" con supporto a DataStore e coaching maieutico (task F2-03).
 */
public class DailyPuzzleViewModel(
    private val repository: DailyPuzzleRepository,
    private val retentionRepository: RetentionRepository? = null,
    private val walletRepository: WalletRepository? = null,
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
        val concept = DailyPuzzleProvider.getLogicalConcept(puzzle)

        viewModelScope.launch {
            val record = repository.getDailyPuzzleRecord(dateString)
            val isTierSolved = record.solvedTiersToday.contains(tierIndex) || (tierIndex == 0 && record.isSolvedToday)

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
                    shareText = PuzzleShareFormatter.formatShareText(
                        puzzleNumber = puzzleNumber,
                        attemptsCount = maxOf(1, record.attemptsToday),
                        solved = true,
                    ),
                    isAlreadySolvedToday = true,
                    currentTierIndex = tierIndex,
                    solvedTiers = record.solvedTiersToday,
                    logicalConcept = concept,
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
                    maestroMessageResId = R.string.puzzle_coach_initial,
                    isAlreadySolvedToday = false,
                    currentTierIndex = tierIndex,
                    solvedTiers = record.solvedTiersToday,
                    logicalConcept = concept,
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
            val concept = DailyPuzzleProvider.getLogicalConcept(puzzle)
            val updatedSolvedTiers = currentState.solvedTiers + currentState.currentTierIndex
            val currentTier = DailyPuzzleProvider.TIERS.getOrNull(currentState.currentTierIndex)
                ?: DailyPuzzleProvider.TIERS[0]

            _uiState.value = currentState.copy(
                status = DailyPuzzleStatus.Solved(newAttempts),
                attemptsCount = newAttempts,
                maestroMood = MaestroMood.TRIUMPHANT,
                maestroMessageResId = R.string.puzzle_coach_solved,
                shareText = shareText,
                solvedTiers = updatedSolvedTiers,
                logicalConcept = concept,
            )
            viewModelScope.launch {
                repository.recordPuzzleCompletion(
                    todayDate = dateString,
                    attempts = newAttempts,
                    solved = true,
                    tierIndex = currentState.currentTierIndex,
                )
                retentionRepository?.let { retention ->
                    retention.updateMissionProgress(effectiveDate, DailyMissionType.SOLVE_DAILY_PUZZLE)
                    retention.recordGameActivity(effectiveDate)
                }
                val refId = if (currentState.currentTierIndex == 0) dateString else "$dateString-tier-${currentState.currentTierIndex}"
                walletRepository?.addCredits(
                    amount = currentTier.rewardCredits,
                    reason = TransactionReason.DAILY_PUZZLE_REWARD,
                    referenceId = refId,
                    description = "Sfida del Giorno completata: ${currentTier.index + 1}",
                )
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
