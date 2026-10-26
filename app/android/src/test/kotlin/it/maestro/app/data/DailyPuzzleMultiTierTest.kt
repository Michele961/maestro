package it.maestro.app.data

import it.maestro.ai.puzzle.PuzzleGoal
import it.maestro.app.data.local.DailyPuzzleProvider
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DailyPuzzleMultiTierTest {

    private val testDate = LocalDate.of(2026, 10, 26)

    @Test
    fun dailyPuzzle_providesThreeIncreasingTiers() {
        val tiers = DailyPuzzleProvider.TIERS
        assertEquals(3, tiers.size)
        assertEquals(0, tiers[0].index)
        assertEquals(1, tiers[1].index)
        assertEquals(2, tiers[2].index)

        assertTrue(tiers[0].rewardCredits > 0)
        assertTrue(tiers[1].rewardCredits > tiers[0].rewardCredits)
        assertTrue(tiers[2].rewardCredits > tiers[1].rewardCredits)
    }

    @Test
    fun dailyPuzzle_generatesDistinctDeterministicPuzzlesPerTier() {
        val tier0 = DailyPuzzleProvider.getDailyPuzzle(testDate, 0)
        val tier1 = DailyPuzzleProvider.getDailyPuzzle(testDate, 1)
        val tier2 = DailyPuzzleProvider.getDailyPuzzle(testDate, 2)

        assertNotNull(tier0)
        assertNotNull(tier1)
        assertNotNull(tier2)

        // Tier 1 and 2 must have their respective targeted goals
        assertEquals(PuzzleGoal.MAXIMIZE_POINTS, tier1.goal)
        assertEquals(PuzzleGoal.WIN_MATCH, tier2.goal)

        // Different tiers must have different IDs
        assertNotEquals(tier0.id, tier1.id)
        assertNotEquals(tier1.id, tier2.id)
    }

    @Test
    fun dailyPuzzle_logicalConcept_returnsPedagogicalAdvice() {
        val tier0 = DailyPuzzleProvider.getDailyPuzzle(testDate, 0)
        val concept0 = DailyPuzzleProvider.getLogicalConcept(tier0)
        assertTrue(concept0.isNotBlank(), "Il concetto logico del puzzle tier 0 non deve essere vuoto")

        val tier1 = DailyPuzzleProvider.getDailyPuzzle(testDate, 1)
        val concept1 = DailyPuzzleProvider.getLogicalConcept(tier1)
        assertTrue(concept1.isNotBlank(), "Il concetto logico del puzzle tier 1 non deve essere vuoto")

        val tier2 = DailyPuzzleProvider.getDailyPuzzle(testDate, 2)
        val concept2 = DailyPuzzleProvider.getLogicalConcept(tier2)
        assertTrue(concept2.isNotBlank(), "Il concetto logico del puzzle tier 2 non deve essere vuoto")
    }
}
