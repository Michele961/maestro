package it.maestro.app.ui

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.R
import it.maestro.game.scopa.ScopaConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test di unità per la configurazione e le opzioni di PreGameScreen (task F1-07).
 */
class PreGameScreenTest {

    @Test
    fun difficultyLevelOptionsMapToValidLevels() {
        val expectedLevels = listOf(
            DifficultyLevel.PRINCIPIANTE,
            DifficultyLevel.DILETTANTE,
            DifficultyLevel.INTERMEDIO,
            DifficultyLevel.AVANZATO,
            DifficultyLevel.MAESTRO,
        )
        assertEquals(expectedLevels, DifficultyLevel.entries)
    }

    @Test
    fun targetScoreOptionsAreValidScopaTargetScores() {
        val targets = listOf(11, 16, 21)
        for (target in targets) {
            val config = ScopaConfig(targetScore = target)
            assertEquals(target, config.targetScore)
            assertTrue(config.targetScore in 1..100)
        }
    }
}
