package it.maestro.app.ui

import it.maestro.app.feature.puzzle.presentation.PuzzleShareFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/** Task F1R-27c: la condivisione mostra i livelli in simboli e non dice quanti errori sono serviti. */
class PuzzleShareFormatterTest {

    @Test
    fun tiersAreShownAsSymbolsInOrder() {
        assertEquals("✔ ✔ ✔", PuzzleShareFormatter.tierSymbols(setOf(0, 1, 2), 3))
        assertEquals("✔ ○ ✔", PuzzleShareFormatter.tierSymbols(setOf(0, 2), 3))
        assertEquals("○ ○ ○", PuzzleShareFormatter.tierSymbols(emptySet(), 3))
    }

    @Test
    fun theSymbolsNeverContainAttemptNumbers() {
        assertFalse(PuzzleShareFormatter.tierSymbols(setOf(0, 1, 2), 3).any { it.isDigit() })
    }
}
