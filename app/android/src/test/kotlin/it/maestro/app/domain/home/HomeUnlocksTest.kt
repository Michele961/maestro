package it.maestro.app.domain.home

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** F1G-03: tabella di sblocco della Home (`docs/design/testi-primo-avvio-e-home.md` §2). */
class HomeUnlocksTest {

    @Test
    fun aNewPlayerUnlocksNothingAndHasNoActivity() {
        assertEquals(emptySet(), HomeUnlocks.unlocked(HomeSignals()))
        assertFalse(HomeSignals().hasActivity)
    }

    @Test
    fun theDailyPuzzleComesAfterTheFirstWin() {
        assertFalse(HomeFeature.DAILY_PUZZLE in HomeUnlocks.unlocked(HomeSignals(matchesPlayed = 3, matchesWon = 0)))
        assertTrue(HomeFeature.DAILY_PUZZLE in HomeUnlocks.unlocked(HomeSignals(matchesPlayed = 1, matchesWon = 1)))
    }

    @Test
    fun theDailyMatchComesAfterTheSecondWin() {
        assertFalse(HomeFeature.DAILY_MATCH in HomeUnlocks.unlocked(HomeSignals(matchesPlayed = 1, matchesWon = 1)))
        assertTrue(HomeFeature.DAILY_MATCH in HomeUnlocks.unlocked(HomeSignals(matchesPlayed = 2, matchesWon = 2)))
    }

    @Test
    fun missionsComeAfterTheFirstSolvedDailyPuzzle() {
        assertFalse(HomeFeature.MISSIONS in HomeUnlocks.unlocked(HomeSignals(matchesPlayed = 2, matchesWon = 2)))
        assertTrue(HomeFeature.MISSIONS in HomeUnlocks.unlocked(HomeSignals(dailyPuzzleSolved = true)))
    }

    @Test
    fun theAcademyComesAfterTheFirstCheck() {
        assertFalse(HomeFeature.ACADEMY in HomeUnlocks.unlocked(HomeSignals(matchesPlayed = 2, matchesWon = 2)))
        assertTrue(HomeFeature.ACADEMY in HomeUnlocks.unlocked(HomeSignals(checkPlayed = true)))
    }

    @Test
    fun theShopAndProfileComeAfterTheFirstReward() {
        assertFalse(HomeFeature.SHOP in HomeUnlocks.unlocked(HomeSignals(matchesPlayed = 2, matchesWon = 2)))
        assertTrue(HomeFeature.SHOP in HomeUnlocks.unlocked(HomeSignals(rewardReceived = true)))
    }

    @Test
    fun theGameTilesAndTheSeasonComeAfterTheFirstFinishedMatchEvenIfLost() {
        val lost = HomeUnlocks.unlocked(HomeSignals(matchesPlayed = 1, matchesWon = 0))
        assertEquals(setOf(HomeFeature.GAME_TILES, HomeFeature.SEASON), lost)
    }

    @Test
    fun aFirstEvaluationOfAnExistingPlayerShowsEverythingAndIntroducesNothing() {
        val state = HomeUnlocks.advance(PresentedFeatures(), HomeSignals(matchesPlayed = 4, matchesWon = 1))
        assertEquals(HomeFeature.ALL, state.revealed)
        assertNull(state.pendingIntroduction)
        assertTrue(state.initialized)
    }

    @Test
    fun anyTraceOfActivityMakesAnExistingPlayer() {
        listOf(
            HomeSignals(opponentsDefeated = 1),
            HomeSignals(bestStreak = 2),
            HomeSignals(academyLessonsDone = 1),
            HomeSignals(dailyPuzzleSolved = true),
            HomeSignals(rewardReceived = true),
            HomeSignals(checkPlayed = true),
        ).forEach { assertEquals(HomeFeature.ALL, HomeUnlocks.advance(PresentedFeatures(), it).revealed) }
    }

    @Test
    fun aNewPlayerStartsEmptyAndIsNotTreatedAsExistingAfterTheFirstWin() {
        val start = HomeUnlocks.advance(PresentedFeatures(), HomeSignals())
        assertTrue(start.initialized)
        assertEquals(emptySet(), start.revealed)

        val afterWin = HomeUnlocks.advance(start, HomeSignals(matchesPlayed = 1, matchesWon = 1))
        assertEquals(setOf(HomeFeature.DAILY_PUZZLE, HomeFeature.GAME_TILES, HomeFeature.SEASON), afterWin.revealed)
        assertEquals(HomeFeature.DAILY_PUZZLE, afterWin.pendingIntroduction)
    }

    @Test
    fun unlockedFeaturesNeverHideAgainEvenIfTheSignalsDisappear() {
        val shown = HomeUnlocks.advance(
            HomeUnlocks.advance(PresentedFeatures(), HomeSignals()),
            HomeSignals(matchesPlayed = 2, matchesWon = 2),
        )
        val afterReset = HomeUnlocks.advance(shown, HomeSignals())
        assertEquals(shown.revealed, afterReset.revealed)
    }

    @Test
    fun theFeaturesAppearInTheApprovedOrderAlongAPlayerJourney() {
        val journey = listOf(
            HomeSignals(matchesPlayed = 1, matchesWon = 1) to HomeFeature.DAILY_PUZZLE,
            HomeSignals(matchesPlayed = 2, matchesWon = 2) to HomeFeature.DAILY_MATCH,
            HomeSignals(matchesPlayed = 2, matchesWon = 2, dailyPuzzleSolved = true) to HomeFeature.MISSIONS,
            HomeSignals(matchesPlayed = 3, matchesWon = 2, dailyPuzzleSolved = true, checkPlayed = true) to HomeFeature.ACADEMY,
            HomeSignals(
                matchesPlayed = 3,
                matchesWon = 2,
                dailyPuzzleSolved = true,
                checkPlayed = true,
                rewardReceived = true,
            ) to HomeFeature.SHOP,
        )
        var state = HomeUnlocks.advance(PresentedFeatures(), HomeSignals())
        journey.forEach { (signals, expectedNew) ->
            val before = state.revealed
            state = HomeUnlocks.advance(state, signals)
            assertTrue(expectedNew in state.revealed - before, "$expectedNew deve comparire a questo passo")
            assertTrue(state.revealed.containsAll(before))
        }
        assertEquals(HomeFeature.ALL, state.revealed)
    }

    @Test
    fun introductionsComeOneAtATimeInTableOrderAndOnlyForFeaturesWithAPhrase() {
        var state = PresentedFeatures(initialized = true, revealed = HomeFeature.ALL)
        val said = mutableListOf<HomeFeature>()
        while (true) {
            val next = state.pendingIntroduction ?: break
            said += next
            state = state.withIntroduced(next)
        }
        assertEquals(
            listOf(HomeFeature.DAILY_PUZZLE, HomeFeature.DAILY_MATCH, HomeFeature.MISSIONS, HomeFeature.ACADEMY, HomeFeature.SHOP),
            said,
        )
        assertTrue(HomeFeature.entries.filter { it.hasIntroduction }.toSet() == said.toSet())
    }
}
