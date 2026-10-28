package it.maestro.app.domain.progress

import it.maestro.app.data.local.datastore.InMemoryAchievementRepository
import it.maestro.app.domain.opponents.Opponent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Task F1R-29e: obiettivi con identificatori stabili, sbloccati una volta sola. */
class AchievementsTest {

    @Test
    fun identifiersAreStableAndUnique() {
        // Questi identificatori verranno agganciati a Google Play Games: non si cambiano né si riusano.
        assertEquals(
            listOf(
                "first_win", "beat_gigi", "beat_nonna_rosa", "beat_tonino", "beat_don_alfonso", "beat_maestro",
                "ladder_complete", "first_sweep", "ten_sweeps", "perfect_match", "matches_10", "matches_50",
                "matches_100", "streak_7", "streak_30", "first_sparring", "puzzle_full_day", "puzzle_week",
            ),
            AchievementId.entries.map { it.id },
        )
        assertTrue(AchievementId.entries.map { it.id }.all { Regex("[a-z0-9_]+").matches(it) })
    }

    @Test
    fun everyOpponentHasItsOwnAchievement() {
        assertEquals(Opponent.entries.size, Opponent.entries.map { AchievementId.beating(it) }.distinct().size)
    }

    @Test
    fun anEventWithNoFactsUnlocksNothing() {
        assertEquals(emptySet(), achievementsFor(AchievementFacts()))
    }

    @Test
    fun theFirstWinUnlocksFirstWinAndTheOpponentOnlyTheFirstTime() {
        val first = achievementsFor(AchievementFacts(winOverOpponent = Opponent.TONINO, firstTimeOverOpponent = true, defeatedOpponents = 3))
        assertEquals(setOf(AchievementId.FIRST_WIN, AchievementId.BEAT_TONINO), first)

        val again = achievementsFor(AchievementFacts(winOverOpponent = Opponent.TONINO, firstTimeOverOpponent = false, defeatedOpponents = 3))
        assertEquals(setOf(AchievementId.FIRST_WIN), again)
    }

    @Test
    fun beatingTheWholeLadderUnlocksTheCompletion() {
        val facts = AchievementFacts(winOverOpponent = Opponent.MAESTRO, firstTimeOverOpponent = true, defeatedOpponents = Opponent.entries.size)
        assertTrue(AchievementId.LADDER_COMPLETE in achievementsFor(facts))
        assertTrue(AchievementId.BEAT_MAESTRO in achievementsFor(facts))
    }

    @Test
    fun countingAchievementsUnlockAtTheirThreshold() {
        assertEquals(emptySet(), achievementsFor(AchievementFacts(matchesPlayed = 9, sweepsTotal = 0, streak = 6)))
        assertEquals(setOf(AchievementId.MATCHES_10), achievementsFor(AchievementFacts(matchesPlayed = 10)))
        assertEquals(
            setOf(AchievementId.MATCHES_10, AchievementId.MATCHES_50, AchievementId.MATCHES_100),
            achievementsFor(AchievementFacts(matchesPlayed = 100)),
        )
        assertEquals(setOf(AchievementId.FIRST_SWEEP), achievementsFor(AchievementFacts(sweepsTotal = 1)))
        assertEquals(setOf(AchievementId.FIRST_SWEEP, AchievementId.TEN_SWEEPS), achievementsFor(AchievementFacts(sweepsTotal = 10)))
        assertEquals(setOf(AchievementId.STREAK_7), achievementsFor(AchievementFacts(streak = 7)))
        assertEquals(setOf(AchievementId.STREAK_7, AchievementId.STREAK_30), achievementsFor(AchievementFacts(streak = 30)))
        assertEquals(setOf(AchievementId.PUZZLE_WEEK), achievementsFor(AchievementFacts(puzzlesSolved = 7)))
    }

    @Test
    fun perfectMatchSparringAndFullPuzzleDayUnlockTheirOwn() {
        assertEquals(setOf(AchievementId.PERFECT_MATCH), achievementsFor(AchievementFacts(perfectMatch = true)))
        assertEquals(setOf(AchievementId.FIRST_SPARRING), achievementsFor(AchievementFacts(sparringPlayed = true)))
        assertEquals(setOf(AchievementId.PUZZLE_FULL_DAY), achievementsFor(AchievementFacts(puzzleFullDay = true)))
    }

    @Test
    fun theRepositoryReturnsOnlyWhatIsNew() = runTest {
        val repo = InMemoryAchievementRepository()
        assertEquals(setOf(AchievementId.FIRST_WIN), repo.unlock(setOf(AchievementId.FIRST_WIN)))
        assertEquals(setOf(AchievementId.FIRST_SWEEP), repo.unlock(setOf(AchievementId.FIRST_WIN, AchievementId.FIRST_SWEEP)))
        assertEquals(emptySet(), repo.unlock(setOf(AchievementId.FIRST_WIN)))
    }

    @Test
    fun countersOnlyGoUp() = runTest {
        val repo = InMemoryAchievementRepository()
        assertEquals(3, repo.addToCounter(AchievementCounter.SWEEPS, 3))
        assertEquals(3, repo.addToCounter(AchievementCounter.SWEEPS, -5))
        assertEquals(1, repo.addToCounter(AchievementCounter.PUZZLES_SOLVED, 1))
    }
}
