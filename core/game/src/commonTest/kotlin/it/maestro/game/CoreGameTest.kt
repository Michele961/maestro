package it.maestro.game

import kotlin.test.Test
import kotlin.test.assertEquals

class CoreGameTest {
    @Test
    fun rulesVersionStartsAtZero() {
        assertEquals(0, CoreGame.RULES_VERSION)
    }
}
