package it.maestro.app.feature.home

import it.maestro.ai.mastery.MasteryProfile
import it.maestro.app.data.local.datastore.DailyPuzzleRecord
import it.maestro.app.data.local.datastore.InMemoryAcademyRepository
import it.maestro.app.data.local.datastore.InMemoryDailyPuzzleRepository
import it.maestro.app.data.local.datastore.InMemoryHomeFeaturesRepository
import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.data.local.datastore.InMemoryOpponentRepository
import it.maestro.app.data.local.datastore.InMemoryRetentionRepository
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.domain.home.HomeFeature
import it.maestro.app.domain.home.HomeFeaturesRepository
import it.maestro.app.domain.mastery.MatchHistoryEntry
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.opponents.OpponentProgress
import it.maestro.app.domain.time.FakeClock
import it.maestro.app.domain.wallet.TransactionReason
import it.maestro.app.domain.wallet.WalletTransaction
import it.maestro.app.feature.home.presentation.HomeFeaturesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** F1G-03: la Home si accende dai segnali esistenti; la frase di presentazione è detta una sola volta. */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeFeaturesViewModelTest {
    private val clock = FakeClock(LocalDate.of(2026, 10, 31))

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun entry(id: String, win: Boolean, sparring: String? = null) = MatchHistoryEntry(
        id = id, timestampMillis = 0L, accuracy = 70, cognitiveElo = 1000, eloDelta = 0,
        humanScore = if (win) 11 else 5, botScore = if (win) 5 else 11, isWin = win, sparringObjective = sparring,
    )

    private fun reward(reason: TransactionReason) = WalletTransaction(amount = 10, reason = reason)

    /** Una visita alla Home: un ViewModel nuovo sullo stesso stato salvato. */
    private fun visit(
        store: HomeFeaturesRepository,
        played: List<MatchHistoryEntry> = emptyList(),
        puzzleDate: String = "",
        defeated: Set<Opponent> = emptySet(),
        transactions: List<WalletTransaction> = listOf(reward(TransactionReason.WELCOME_BONUS)),
    ) = HomeFeaturesViewModel(
        repository = store,
        mastery = InMemoryMasteryRepository(MasteryProfile(matchesPlayed = played.size), played),
        opponents = InMemoryOpponentRepository(OpponentProgress(defeated)),
        retention = InMemoryRetentionRepository(),
        dailyPuzzleSolved = InMemoryDailyPuzzleRepository(DailyPuzzleRecord(lastCompletedDate = puzzleDate))
            .observeDailyPuzzle(clock.today().toString()).map { it.lastCompletedDate.isNotEmpty() },
        academyLessonsDone = InMemoryAcademyRepository().observeProgress().map { it.values.count { l -> l.isCompleted } },
        wallet = InMemoryWalletRepository(transactions),
        clock = clock,
    )

    @Test
    fun aNewPlayerSeesOnlyPlay() = runTest {
        val state = visit(InMemoryHomeFeaturesRepository()).state.value
        assertEquals(emptySet(), state.visible)
        assertNull(state.introduction)
    }

    @Test
    fun theWelcomeBonusIsNotTheFirstReward() = runTest {
        val state = visit(InMemoryHomeFeaturesRepository()).state.value
        assertTrue(HomeFeature.SHOP !in state.visible)
    }

    @Test
    fun anExistingPlayerSeesEverythingAndHearsNoIntroduction() = runTest {
        val store = InMemoryHomeFeaturesRepository()
        val state = visit(store, played = listOf(entry("a", win = false)), defeated = setOf(Opponent.GIGI)).state.value
        assertEquals(HomeFeature.ALL, state.visible)
        assertNull(state.introduction)
        assertNull(store.load().pendingIntroduction)
        assertTrue(store.load().initialized)
    }

    @Test
    fun theFirstWinShowsTheDailyPuzzleAndTheMaestroIntroducesItOnlyOnce() = runTest {
        val store = InMemoryHomeFeaturesRepository()
        visit(store) // la prima visita, da nuovo: salva lo stato di partenza
        val won = listOf(entry("a", win = true))

        val first = visit(store, played = won).state.value
        assertEquals(HomeFeature.DAILY_PUZZLE, first.introduction)
        assertTrue(HomeFeature.DAILY_PUZZLE in first.visible)
        assertTrue(HomeFeature.DAILY_MATCH !in first.visible)

        val second = visit(store, played = won).state.value
        assertNull(second.introduction)
        assertTrue(HomeFeature.DAILY_PUZZLE in second.visible)
    }

    @Test
    fun morePhrasesWaitTheirTurnOnePerVisit() = runTest {
        val store = InMemoryHomeFeaturesRepository()
        visit(store)
        val won = listOf(entry("a", win = true))
        val credits = listOf(reward(TransactionReason.WELCOME_BONUS), reward(TransactionReason.MATCH_VICTORY_REWARD))

        assertEquals(HomeFeature.DAILY_PUZZLE, visit(store, won, transactions = credits).state.value.introduction)
        assertEquals(HomeFeature.SHOP, visit(store, won, transactions = credits).state.value.introduction)
        assertNull(visit(store, won, transactions = credits).state.value.introduction)
    }

    @Test
    fun theSolvedPuzzleAndTheFirstCheckLightUpMissionsAndAcademy() = runTest {
        val store = InMemoryHomeFeaturesRepository()
        visit(store)
        val state = visit(
            store,
            played = listOf(entry("a", win = false, sparring = "SEVENS_COUNT")),
            puzzleDate = clock.today().toString(),
        ).state.value
        assertTrue(HomeFeature.MISSIONS in state.visible)
        assertTrue(HomeFeature.ACADEMY in state.visible)
        assertTrue(HomeFeature.DAILY_PUZZLE !in state.visible)
    }
}
