package it.maestro.app.data

import it.maestro.ai.puzzle.PuzzleGoal
import it.maestro.app.data.local.DailyPuzzleProvider
import it.maestro.app.data.local.PuzzleLesson
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
    fun dailyPuzzle_everyTierHasALessonWithConceptAndGoldenRuleTexts() {
        // I testi stanno in strings.xml (ADR-0007): il provider restituisce solo il codice della lezione.
        (0..2).forEach { tierIndex ->
            val lesson = DailyPuzzleProvider.lessonOf(DailyPuzzleProvider.getDailyPuzzle(testDate, tierIndex))
            assertTrue(lesson.conceptRes != 0 && lesson.ruleRes != 0, "Il livello $tierIndex deve avere i testi di lezione")
        }
    }

    @Test
    fun puzzleLesson_everyCodeHasItsOwnTexts() {
        val concepts = PuzzleLesson.entries.map { it.conceptRes }
        val rules = PuzzleLesson.entries.map { it.ruleRes }
        assertEquals(PuzzleLesson.entries.size, concepts.distinct().size)
        assertEquals(PuzzleLesson.entries.size, rules.distinct().size)
    }
}
