package it.maestro.app.designsystem.motion

import it.maestro.app.domain.settings.GameSpeed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Task F1R-09: valori di `docs/app/game-feel.md` §2 e velocità di gioco. */
class MotionSpecTest {

    @Test
    fun normalSpeedMatchesTheGameFeelTable() {
        assertEquals(260L, MotionSpec.millis(MotionToken.CARD_TO_PLAY_ZONE))
        assertEquals(380L, MotionSpec.millis(MotionToken.CAPTURE_FLIGHT))
        assertEquals(1400L, MotionSpec.millis(MotionToken.SWEEP_CELEBRATION))
        assertEquals(140L, MotionSpec.millis(MotionToken.DEAL_CARD))
    }

    @Test
    fun fasterSpeedsDivideEveryDuration() {
        MotionToken.entries.forEach { token ->
            assertEquals(Math.round(token.baseMillis / 1.5), MotionSpec.millis(token, GameSpeed.FAST))
            assertEquals(Math.round(token.baseMillis / 2.0), MotionSpec.millis(token, GameSpeed.FASTER))
        }
    }

    @Test
    fun instantSpeedRemovesEveryWait() {
        MotionToken.entries.forEach { assertEquals(0L, MotionSpec.millis(it, GameSpeed.INSTANT)) }
        assertEquals(0L, MotionSpec.botThinkMillis(legalMoves = 5, speed = GameSpeed.INSTANT))
    }

    @Test
    fun botThinksLongerWithMoreChoices() {
        val single = MotionSpec.botThinkMillis(legalMoves = 1)
        val some = MotionSpec.botThinkMillis(legalMoves = 3)
        val many = MotionSpec.botThinkMillis(legalMoves = 12)
        assertEquals(MotionToken.BOT_THINK_MIN.baseMillis, single)
        assertEquals(MotionToken.BOT_THINK_MAX.baseMillis, many)
        assertTrue(some in (single + 1) until many)
    }

    @Test
    fun onlyNonInstantSpeedsAreOfferedToTheUser() {
        assertEquals(listOf(GameSpeed.NORMAL, GameSpeed.FAST, GameSpeed.FASTER), GameSpeed.USER_CHOICES)
    }
}
