package it.maestro.app.domain.opponents

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.data.local.datastore.InMemoryOpponentRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpponentProgressTest {

    @Test
    fun onlyTheFirstOpponentIsUnlockedAtTheStart() {
        val progress = OpponentProgress()

        assertTrue(progress.isUnlocked(Opponent.GIGI))
        assertFalse(progress.isUnlocked(Opponent.NONNA_ROSA))
        assertEquals(Opponent.GIGI, progress.next)
    }

    @Test
    fun beatingAnOpponentUnlocksTheNextOne() {
        val (progress, victory) = OpponentProgress().afterVictory(Opponent.GIGI)

        assertTrue(victory.firstTime)
        assertEquals(Opponent.NONNA_ROSA, victory.newlyUnlocked)
        assertTrue(progress.isUnlocked(Opponent.NONNA_ROSA))
        assertFalse(progress.isUnlocked(Opponent.TONINO))
        assertEquals(Opponent.NONNA_ROSA, progress.next)
    }

    @Test
    fun beatingAnOpponentAgainIsNotAFirstTimeAndUnlocksNothing() {
        val (once, _) = OpponentProgress().afterVictory(Opponent.GIGI)
        val (twice, victory) = once.afterVictory(Opponent.GIGI)

        assertFalse(victory.firstTime)
        assertNull(victory.newlyUnlocked)
        assertEquals(once, twice)
    }

    @Test
    fun aVictoryOverALockedOpponentDoesNotCount() {
        val (progress, victory) = OpponentProgress().afterVictory(Opponent.DON_ALFONSO)

        assertFalse(victory.firstTime)
        assertNull(victory.newlyUnlocked)
        assertTrue(progress.defeated.isEmpty())
    }

    @Test
    fun theLadderIsCompleteAfterTheLastOpponent() {
        val all = Opponent.entries.fold(OpponentProgress()) { acc, o -> acc.afterVictory(o).first }

        assertNull(all.next)
        assertNull(OpponentProgress(Opponent.entries.toSet()).afterVictory(Opponent.MAESTRO).second.newlyUnlocked)
    }

    @Test
    fun everyEngineLevelHasExactlyOneOpponentInIncreasingOrder() {
        assertEquals(DifficultyLevel.entries, Opponent.entries.map { it.level })
        DifficultyLevel.entries.forEach { assertEquals(it, Opponent.forLevel(it).level) }
    }

    @Test
    fun repositoryKeepsProgressBetweenVictories() = runTest {
        val repository = InMemoryOpponentRepository()

        val first = repository.recordVictory(Opponent.GIGI)
        repository.recordVictory(Opponent.NONNA_ROSA)

        assertTrue(first.firstTime)
        assertEquals(setOf(Opponent.GIGI, Opponent.NONNA_ROSA), repository.observeProgress().first().defeated)
    }
}
