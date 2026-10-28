package it.maestro.app.domain.progress

import it.maestro.app.data.local.datastore.InMemoryExperienceRepository
import it.maestro.app.domain.opponents.Opponent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Task F1R-29d: l'esperienza è un contatore con livelli, senza spesa. */
class ExperienceTest {

    @Test
    fun aNewPlayerIsAtLevelOne() {
        assertEquals(1, Experience.levelOf(0))
        assertEquals(1, Experience.levelOf(Experience.PX_PER_LEVEL - 1))
    }

    @Test
    fun everyLevelNeedsTheSameNumberOfPoints() {
        assertEquals(2, Experience.levelOf(Experience.PX_PER_LEVEL))
        assertEquals(5, Experience.levelOf(4 * Experience.PX_PER_LEVEL))
    }

    @Test
    fun theLevelStopsAtTheMaximum() {
        assertEquals(Experience.MAX_LEVEL, Experience.levelOf(Experience.PX_PER_LEVEL * 9))
        assertEquals(Experience.MAX_LEVEL, Experience.levelOf(1_000_000))
        assertEquals(0, Experience.pointsIntoLevel(1_000_000))
    }

    @Test
    fun pointsIntoTheCurrentLevelRestartAtEachLevel() {
        assertEquals(40, Experience.pointsIntoLevel(Experience.PX_PER_LEVEL + 40))
    }

    @Test
    fun theFirstWinOverEachOpponentGivesHundredTimesItsPlace() {
        assertEquals(listOf(100, 200, 300, 400, 500), Opponent.entries.map { Experience.ladderFirstWin(it) })
    }

    @Test
    fun puzzleTiersGiveTenFifteenTwenty() {
        assertEquals(listOf(10, 15, 20), (0..2).map { Experience.puzzleTier(it) })
    }

    @Test
    fun theRepositorySumsAndNeverGoesDown() = runTest {
        val repo = InMemoryExperienceRepository()
        assertEquals(30, repo.add(30))
        assertEquals(30, repo.add(-10), "Un valore negativo non toglie esperienza")
        assertEquals(55, repo.add(25))
    }
}
