package it.maestro.app.feature.home

import it.maestro.app.domain.home.HomeFeature
import it.maestro.app.feature.home.presentation.HomeLineKind
import it.maestro.app.feature.home.presentation.HomeLines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeLinesTest {

    @Test
    fun anActiveMatchComesFirst() {
        assertEquals(HomeLineKind.RESUME, HomeLines.choose(hasActiveMatch = true, streakCount = 9, hasName = true, day = 0).kind)
    }

    @Test
    fun aStreakIsNamedOnlyFromTwoDays() {
        assertEquals(HomeLineKind.WELCOME_NAMED, HomeLines.choose(false, 1, hasName = true, day = 0).kind)
        assertEquals(HomeLineKind.STREAK, HomeLines.choose(false, HomeLines.MIN_STREAK, hasName = true, day = 0).kind)
    }

    @Test
    fun withoutANameTheWelcomeDoesNotUseOne() {
        assertEquals(HomeLineKind.WELCOME, HomeLines.choose(false, 0, hasName = false, day = 0).kind)
    }

    @Test
    fun theVariantChangesDayByDayAndStaysInRange() {
        val variants = (-3L..10L).map { HomeLines.choose(false, 0, true, it).variant }
        assertTrue(variants.all { it in 0 until HomeLines.VARIANTS })
        assertEquals(HomeLines.VARIANTS, variants.toSet().size)
        assertEquals(HomeLines.choose(false, 0, true, 5), HomeLines.choose(false, 0, true, 5))
    }

    @Test
    fun aPresentationBeatsEveryOtherLineAndKeepsTheFeature() {
        val line = HomeLines.choose(true, 9, true, 3, introducing = HomeFeature.SHOP)
        assertEquals(HomeLineKind.UNLOCK, line.kind)
        assertEquals(HomeFeature.SHOP, line.feature)
    }

    @Test
    fun withoutAPresentationTheExistingVariantsAreUnchanged() {
        assertEquals(HomeLineKind.RESUME, HomeLines.choose(true, 9, true, 3, introducing = null).kind)
        assertEquals(null, HomeLines.choose(false, 0, false, 3).feature)
    }
}
