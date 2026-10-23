package it.maestro.game

import kotlin.test.Test
import kotlin.test.assertEquals

class CoreGameTest {
    @Test
    fun rulesVersionIsOne() {
        assertEquals(1, CoreGame.RULES_VERSION)
    }
}
