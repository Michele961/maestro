package it.maestro.ai

import kotlin.test.Test
import kotlin.test.assertNotNull

class CoreAiTest {
    @Test
    fun moduleLoads() {
        assertNotNull(CoreAi)
    }
}
