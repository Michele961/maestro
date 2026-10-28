package it.maestro.app.domain.retention

import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.data.local.datastore.InMemoryRetentionRepository
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.domain.rewards.RewardEngine
import it.maestro.app.domain.time.FakeClock
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Task F1R-29c: il Gettone Riposo si ricarica con la serie, fino a un massimo di due. */
class FreezeRechargeTest {

    private val start = LocalDate.of(2026, 11, 1)

    private fun play(state: StreakState, days: Int): StreakState =
        (0 until days).fold(state) { s, offset -> StreakState.recordActivity(s, start.plusDays(offset.toLong())) }

    @Test
    fun aNewPlayerStartsWithOneToken() {
        assertEquals(1, StreakState.INITIAL.freezeTokens)
    }

    @Test
    fun sixDaysInARowDoNotEarnAToken() {
        assertEquals(1, play(StreakState.INITIAL, 6).freezeTokens)
    }

    @Test
    fun theSeventhDayEarnsAToken() {
        val state = play(StreakState.INITIAL, 7)
        assertEquals(7, state.currentStreak)
        assertEquals(2, state.freezeTokens)
    }

    @Test
    fun theTokensStopAtTwo() {
        val state = play(StreakState.INITIAL, 21)
        assertEquals(StreakState.MAX_FREEZE_TOKENS, state.freezeTokens)
    }

    @Test
    fun playingTwiceOnTheSeventhDayEarnsOnlyOnce() {
        val seventh = play(StreakState.INITIAL.copy(freezeTokens = 0), 7)
        assertEquals(1, seventh.freezeTokens)
        assertEquals(1, StreakState.recordActivity(seventh, start.plusDays(6)).freezeTokens)
    }

    @Test
    fun aBrokenStreakRestartsTheCountWithoutRemovingTokens() {
        val week = play(StreakState.INITIAL, 7)
        val afterBreak = StreakState.recordActivity(week, start.plusDays(12))
        assertEquals(1, afterBreak.currentStreak)
        assertEquals(week.freezeTokens, afterBreak.freezeTokens, "Perdere la serie non toglie i gettoni")
    }

    @Test
    fun theSummaryTellsWhenATokenWasEarned() = runTest {
        val retention = InMemoryRetentionRepository(
            initialStreak = play(StreakState.INITIAL, 6),
        )
        val engine = RewardEngine(
            InMemoryWalletRepository(),
            retention,
            InMemoryMasteryRepository(),
            FakeClock(start.plusDays(6)),
        )
        val summary = engine.onPuzzleSolved(start.plusDays(6), tierIndex = 0, tierCredits = 15)
        assertTrue(summary.freezeTokenEarned)
        assertEquals(2, retention.getStreak(start.plusDays(6)).freezeTokens)

        val again = engine.onPuzzleSolved(start.plusDays(6), tierIndex = 1, tierCredits = 20)
        assertFalse(again.freezeTokenEarned)
    }
}
