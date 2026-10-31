package it.maestro.app.feature.home

import it.maestro.ai.mastery.MasteryProfile
import it.maestro.app.data.local.datastore.InMemoryGuideRepository
import it.maestro.app.data.local.datastore.InMemoryHomeFeaturesRepository
import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.data.local.datastore.InMemoryOpponentRepository
import it.maestro.app.data.local.datastore.InMemoryRetentionRepository
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.domain.guide.GuideProgress
import it.maestro.app.domain.guide.GuidedHandStatus
import it.maestro.app.domain.mastery.MatchHistoryEntry
import it.maestro.app.domain.opponents.OpponentProgress
import it.maestro.app.domain.time.FakeClock
import it.maestro.app.domain.wallet.TransactionReason
import it.maestro.app.domain.wallet.WalletTransaction
import it.maestro.app.feature.home.presentation.HomeFeaturesUi
import it.maestro.app.feature.home.presentation.HomeFeaturesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** F1G-01: chi è nuovo vede il primo avvio, chi aggiorna l'app con dati esistenti no; il flag sopravvive. */
@OptIn(ExperimentalCoroutinesApi::class)
class FirstLaunchTest {
    private val clock = FakeClock(LocalDate.of(2026, 10, 31))

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun played() = MatchHistoryEntry(
        id = "a", timestampMillis = 0L, accuracy = 70, cognitiveElo = 1000, eloDelta = 0,
        humanScore = 11, botScore = 5, isWin = true,
    )

    private fun visit(guide: InMemoryGuideRepository, history: List<MatchHistoryEntry> = emptyList()) = HomeFeaturesViewModel(
        repository = InMemoryHomeFeaturesRepository(),
        mastery = InMemoryMasteryRepository(MasteryProfile(matchesPlayed = history.size), history),
        opponents = InMemoryOpponentRepository(OpponentProgress()),
        retention = InMemoryRetentionRepository(),
        dailyPuzzleSolved = flowOf(false),
        academyLessonsDone = flowOf(0),
        wallet = InMemoryWalletRepository(listOf(WalletTransaction(amount = 50, reason = TransactionReason.WELCOME_BONUS))),
        clock = clock,
        guide = guide,
    )

    @Test
    fun aNewPlayerSeesTheFirstLaunch() = runTest {
        val state = visit(InMemoryGuideRepository()).state.value
        assertTrue(state.firstLaunch)
        assertTrue(state.loaded)
    }

    @Test
    fun beforeTheDataIsReadNothingIsDecided() {
        assertFalse(HomeFeaturesUi.LOADING.loaded)
        assertFalse(HomeFeaturesUi.LOADING.firstLaunch)
    }

    @Test
    fun whoUpdatesWithExistingDataSeesTheHomeAsItIsAndTheGuideCountsAsSkipped() = runTest {
        val guide = InMemoryGuideRepository()
        val state = visit(guide, listOf(played())).state.value
        assertFalse(state.firstLaunch)
        assertEquals(GuidedHandStatus.SKIPPED, guide.observe().first().hand)
    }

    @Test
    fun onceTheHandIsPlayedOrSkippedTheFirstLaunchNeverComesBack() = runTest {
        for (status in listOf(GuidedHandStatus.DONE, GuidedHandStatus.SKIPPED)) {
            assertFalse(visit(InMemoryGuideRepository(GuideProgress(hand = status))).state.value.firstLaunch)
        }
    }

    @Test
    fun withoutAGuideRepositoryThereIsNoFirstLaunch() = runTest {
        val vm = HomeFeaturesViewModel(
            repository = InMemoryHomeFeaturesRepository(),
            mastery = InMemoryMasteryRepository(MasteryProfile(), emptyList()),
            opponents = InMemoryOpponentRepository(OpponentProgress()),
            retention = InMemoryRetentionRepository(),
            dailyPuzzleSolved = flowOf(false),
            academyLessonsDone = flowOf(0),
            wallet = InMemoryWalletRepository(emptyList()),
            clock = clock,
        )
        assertFalse(vm.state.value.firstLaunch)
    }
}
