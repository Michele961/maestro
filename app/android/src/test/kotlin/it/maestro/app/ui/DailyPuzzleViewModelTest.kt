package it.maestro.app.ui

import it.maestro.ai.puzzle.PuzzleGoal
import it.maestro.ai.puzzle.TacticalPuzzle
import it.maestro.app.R
import it.maestro.app.data.local.datastore.DailyPuzzleRecord
import it.maestro.app.data.local.datastore.InMemoryDailyPuzzleRepository
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.data.local.wallet.TransactionReason
import it.maestro.app.ui.components.MaestroMood
import it.maestro.app.ui.screens.DailyPuzzleStatus
import it.maestro.app.ui.screens.DailyPuzzleViewModel
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.time.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DailyPuzzleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testCardWinning = Card(Rank.SETTE, Suit.DENARI)
    private val testCardAlternate = Card(Rank.ASSO, Suit.COPPE)
    private val testCardThird = Card(Rank.DUE, Suit.SPADE)
    private val testWinningMove = ScopaMove(card = testCardWinning, capture = setOf(Card(Rank.SETTE, Suit.SPADE)))

    private val sampleView = ScopaView(
        seat = Seat.P1,
        config = ScopaConfig(),
        dealer = Seat.P2,
        toMove = Seat.P1,
        phase = DealPhase.PLAYING,
        hand = listOf(testCardWinning, testCardAlternate, testCardThird),
        table = listOf(Card(Rank.SETTE, Suit.SPADE)),
        capturedBy = mapOf(Seat.P1 to emptyList(), Seat.P2 to emptyList()),
        playedCards = emptyList(),
        opponentHandSize = 3,
        deckSize = 0,
        sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        totals = mapOf(Seat.P1 to 0, Seat.P2 to 0),
    )

    private val samplePuzzle = TacticalPuzzle(
        id = "test_puzzle_01",
        title = "puzzle_title_make_sweep",
        initialView = sampleView,
        goal = PuzzleGoal.MAKE_SWEEP,
        solutionMoves = listOf(testWinningMove),
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsPuzzleInPlayingStateWithThinkingMood() = runTest {
        val repo = InMemoryDailyPuzzleRepository()
        val testDate = LocalDate.of(2026, 10, 26)
        val viewModel = DailyPuzzleViewModel(
            repository = repo,
            customDate = testDate,
            customPuzzle = samplePuzzle,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(samplePuzzle, state.puzzle)
        assertEquals(DailyPuzzleStatus.Playing, state.status)
        assertEquals(MaestroMood.THINKING, state.maestroMood)
        assertEquals(R.string.puzzle_coach_initial, state.maestroMessageResId)
        assertFalse(state.isAlreadySolvedToday)
    }

    @Test
    fun playingWrongCard_triggersProgressiveHintingAndEncouragingMood() = runTest {
        val repo = InMemoryDailyPuzzleRepository()
        val testDate = LocalDate.of(2026, 10, 26)
        val viewModel = DailyPuzzleViewModel(
            repository = repo,
            customDate = testDate,
            customPuzzle = samplePuzzle,
        )
        advanceUntilIdle()

        // 1° tentativo errato -> Livello 1 (stimolo visivo, incoraggiante)
        viewModel.playCard(testCardAlternate)
        val stateAfter1 = viewModel.uiState.value
        val status1 = stateAfter1.status as DailyPuzzleStatus.WrongAttempt
        assertEquals(1, status1.attemptNumber)
        assertEquals(1, status1.hintLevel)
        assertEquals(MaestroMood.ENCOURAGING, stateAfter1.maestroMood)
        assertEquals(R.string.puzzle_coach_hint_level1, stateAfter1.maestroMessageResId)

        // Riprova
        viewModel.retryAttempt()
        assertEquals(DailyPuzzleStatus.Playing, viewModel.uiState.value.status)

        // 2° tentativo errato -> Livello 2 (indizio tematico sulla Scopa)
        viewModel.playCard(testCardThird)
        val stateAfter2 = viewModel.uiState.value
        val status2 = stateAfter2.status as DailyPuzzleStatus.WrongAttempt
        assertEquals(2, status2.attemptNumber)
        assertEquals(2, status2.hintLevel)
        assertEquals(MaestroMood.THINKING, stateAfter2.maestroMood)
        assertEquals(R.string.puzzle_coach_hint_sweep, stateAfter2.maestroMessageResId)
    }

    @Test
    fun playingWinningCard_celebratesAndSavesToRepository() = runTest {
        val repo = InMemoryDailyPuzzleRepository()
        val testDate = LocalDate.of(2026, 10, 26)
        val viewModel = DailyPuzzleViewModel(
            repository = repo,
            customDate = testDate,
            customPuzzle = samplePuzzle,
        )
        advanceUntilIdle()

        // Gioca subito la mossa corretta (1° tentativo)
        viewModel.playCard(testCardWinning)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val solvedStatus = state.status as DailyPuzzleStatus.Solved
        assertEquals(1, solvedStatus.attemptsCount)
        assertEquals(MaestroMood.TRIUMPHANT, state.maestroMood)
        assertEquals(R.string.puzzle_coach_solved, state.maestroMessageResId)

        // Verifica formato di condivisione senza spoiler
        assertTrue(
            state.shareText.contains("Maestro Scopa #"),
            "Il testo di share deve contenere l'hashtag del puzzle",
        )
        assertTrue(
            state.shareText.contains("Risolto al 1° tentativo!"),
            "Il testo di share deve celebrare il primo tentativo",
        )

        // Verifica salvataggio nel repository
        val record = repo.getDailyPuzzleRecord("2026-10-26")
        assertTrue(record.isSolvedToday)
        assertEquals(1, record.attemptsToday)
        assertEquals(1, record.streak)
    }

    @Test
    fun alreadySolvedToday_initializesInSolvedStateWithNotification() = runTest {
        val today = "2026-10-26"
        val repo = InMemoryDailyPuzzleRepository(
            initialRecord = DailyPuzzleRecord(
                lastCompletedDate = today,
                isSolvedToday = true,
                attemptsToday = 2,
                streak = 5,
            ),
        )
        val testDate = LocalDate.of(2026, 10, 26)
        val viewModel = DailyPuzzleViewModel(
            repository = repo,
            customDate = testDate,
            customPuzzle = samplePuzzle,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isAlreadySolvedToday)
        val status = state.status as DailyPuzzleStatus.Solved
        assertEquals(2, status.attemptsCount)
        assertEquals(MaestroMood.TRIUMPHANT, state.maestroMood)
        assertEquals(R.string.puzzle_coach_already_solved, state.maestroMessageResId)
    }

    @Test
    fun solvingPuzzleAwardsCreditsToWallet() = runTest {
        val repo = InMemoryDailyPuzzleRepository()
        val walletRepo = InMemoryWalletRepository()
        val testDate = LocalDate.of(2026, 10, 26)
        val viewModel = DailyPuzzleViewModel(
            repository = repo,
            walletRepository = walletRepo,
            customDate = testDate,
            customPuzzle = samplePuzzle,
        )
        advanceUntilIdle()

        viewModel.playCard(testCardWinning)
        advanceUntilIdle()

        assertEquals(15, walletRepo.getBalance(), "Risolvere la sfida del giorno deve accreditare 15 Crediti Maestro")
        assertTrue(walletRepo.hasClaimed(TransactionReason.DAILY_PUZZLE_REWARD, "2026-10-26"))
    }
}
