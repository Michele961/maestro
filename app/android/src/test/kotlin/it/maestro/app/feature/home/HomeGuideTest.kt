package it.maestro.app.feature.home

import it.maestro.ai.mastery.MasteryProfile
import it.maestro.app.data.local.datastore.InMemoryAcademyRepository
import it.maestro.app.data.local.datastore.InMemoryHomeFeaturesRepository
import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.data.local.datastore.InMemoryOpponentRepository
import it.maestro.app.data.local.datastore.InMemoryRetentionRepository
import it.maestro.app.data.local.datastore.InMemorySeasonRepository
import it.maestro.app.data.local.datastore.InMemoryStoryRepository
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.domain.home.GuideSignals
import it.maestro.app.domain.home.HomeFeature
import it.maestro.app.domain.home.HomeGuide
import it.maestro.app.domain.home.HomeGuides
import it.maestro.app.domain.mastery.MatchHistoryEntry
import it.maestro.app.domain.retention.StreakState
import it.maestro.app.domain.time.FakeClock
import it.maestro.app.feature.home.presentation.HomeFeaturesViewModel
import it.maestro.app.feature.home.presentation.HomeLineKind
import it.maestro.app.feature.home.presentation.HomeLines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** F1G-16a: il suggerimento «cosa premere» in Home e il saluto di rientro. */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeGuideTest {
    private val clock = FakeClock(LocalDate.of(2026, 10, 31))

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    // --- Regole del suggerimento ---

    private val all = HomeFeature.ALL

    @Test
    fun withNoMatchPlayedTheMaestroSaysToPressPlay() {
        assertEquals(HomeGuide.NEW, HomeGuides.choose(GuideSignals(), emptySet()))
    }

    @Test
    fun afterTheFirstWinHeSendsToTheDailyPuzzle() {
        val signals = GuideSignals(matchesPlayed = 1, matchesWon = 1)
        assertEquals(HomeGuide.FIRST_WIN, HomeGuides.choose(signals, all))
    }

    @Test
    fun afterALossHeDoesNotPointToAPuzzleThatIsNotThereYet() {
        val signals = GuideSignals(matchesPlayed = 1, chapterToPlay = true)
        assertEquals(HomeGuide.CHAPTER_READY, HomeGuides.choose(signals, setOf(HomeFeature.SEASON)))
    }

    @Test
    fun anOpenDailyPuzzleComesBeforeTheNextChapter() {
        val signals = GuideSignals(matchesPlayed = 4, matchesWon = 2, puzzleEverSolved = true, chapterToPlay = true)
        assertEquals(HomeGuide.DAILY_OPEN, HomeGuides.choose(signals, all))
    }

    @Test
    fun withThePuzzleDoneTheNextChapterIsReady() {
        val signals = GuideSignals(4, 2, puzzleEverSolved = true, puzzleSolvedToday = true, chapterToPlay = true)
        assertEquals(HomeGuide.CHAPTER_READY, HomeGuides.choose(signals, all))
    }

    @Test
    fun withEverythingDoneHeSuggestsASparringHand() {
        val signals = GuideSignals(4, 2, puzzleEverSolved = true, puzzleSolvedToday = true)
        assertEquals(HomeGuide.ALL_DONE, HomeGuides.choose(signals, all))
    }

    @Test
    fun theMaestroNeverNamesAButtonThatIsNotShown() {
        val signals = GuideSignals(4, 2, puzzleEverSolved = true, chapterToPlay = true)
        assertEquals(HomeGuide.ALL_DONE, HomeGuides.choose(signals, emptySet()))
    }

    // --- La riga della Home ---

    @Test
    fun theGuideLineComesAfterAMatchToResumeAndBeforeStreakAndWelcome() {
        val guide = HomeGuide.DAILY_OPEN
        assertEquals(HomeLineKind.RESUME, HomeLines.choose(true, 5, true, 0, guide = guide).kind)
        assertEquals(HomeLineKind.GUIDE, HomeLines.choose(false, 5, true, 0, guide = guide).kind)
        assertEquals(guide, HomeLines.choose(false, 0, false, 0, guide = guide).guide)
        assertEquals(HomeLineKind.UNLOCK, HomeLines.choose(false, 0, true, 0, HomeFeature.SHOP, guide).kind)
    }

    @Test
    fun withoutAGuideTheExistingLinesAreUnchanged() {
        assertEquals(HomeLineKind.STREAK, HomeLines.choose(false, 5, true, 0).kind)
        assertEquals(HomeLineKind.WELCOME, HomeLines.choose(false, 0, false, 0).kind)
    }

    // --- Dal ViewModel ---

    private fun entry(win: Boolean) = MatchHistoryEntry(
        id = "m", timestampMillis = 0L, accuracy = 70, cognitiveElo = 1000, eloDelta = 0,
        humanScore = if (win) 11 else 5, botScore = if (win) 5 else 11, isWin = win,
    )

    private fun visit(lastPlayed: String, story: InMemoryStoryRepository = InMemoryStoryRepository()): HomeFeaturesViewModel {
        val season = InMemorySeasonRepository()
        return HomeFeaturesViewModel(
            repository = InMemoryHomeFeaturesRepository(),
            mastery = InMemoryMasteryRepository(MasteryProfile(matchesPlayed = 1), listOf(entry(win = false))),
            opponents = InMemoryOpponentRepository(),
            retention = InMemoryRetentionRepository(StreakState(currentStreak = 1, lastPlayedDate = lastPlayed)),
            dailyPuzzleSolved = flowOf(false),
            academyLessonsDone = InMemoryAcademyRepository().observeProgress().map { it.size },
            wallet = InMemoryWalletRepository(),
            clock = clock,
            seasonProgress = season.observeProgress(),
            story = story,
        )
    }

    @Test
    fun afterDaysAwayTheMaestroGreetsOnceAndNeverAgainForThatAbsence() = runTest {
        val story = InMemoryStoryRepository()
        val vm = visit(lastPlayed = "2026-10-20", story = story)
        val scene = assertNotNull(vm.state.value.returnScene)
        vm.onReturnSceneDone(scene.id)
        assertNull(vm.state.value.returnScene)
        assertNull(visit(lastPlayed = "2026-10-20", story = story).state.value.returnScene)
    }

    @Test
    fun whoPlayedYesterdayIsNotGreeted() = runTest {
        assertNull(visit(lastPlayed = "2026-10-30").state.value.returnScene)
    }

    @Test
    fun whoNeverPlayedIsNotGreeted() = runTest {
        assertNull(visit(lastPlayed = "").state.value.returnScene)
    }

    @Test
    fun theViewModelSendsAnExistingPlayerToTheOpenDailyPuzzle() = runTest {
        assertEquals(HomeGuide.DAILY_OPEN, visit(lastPlayed = "2026-10-30").state.value.guide)
    }
}
