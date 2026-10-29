package it.maestro.app.audio

import it.maestro.app.domain.audio.AmbientTrack
import it.maestro.app.domain.audio.Backdrops
import it.maestro.app.domain.audio.MusicTrack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/** Sfondo sonoro (F1R-20): giorno fino alle 19, sera dopo; tesa all'ultima smazzata; fine partita una volta sola. */
class BackdropsTest {

    @Test
    fun circleMusicIsDayUntilSevenInTheEveningAndEveningAfter() {
        assertEquals(MusicTrack.CIRCLE_DAY, Backdrops.circleMusic(6))
        assertEquals(MusicTrack.CIRCLE_DAY, Backdrops.circleMusic(18))
        assertEquals(MusicTrack.CIRCLE_EVENING, Backdrops.circleMusic(19))
        assertEquals(MusicTrack.CIRCLE_EVENING, Backdrops.circleMusic(23))
        assertEquals(MusicTrack.CIRCLE_EVENING, Backdrops.circleMusic(0))
        assertEquals(MusicTrack.CIRCLE_EVENING, Backdrops.circleMusic(5))
    }

    @Test
    fun circleHasTheBarAmbience() {
        assertEquals(AmbientTrack.BAR, Backdrops.circle(12).ambient)
    }

    @Test
    fun tableHasTableAmbienceAndTheMusicTurnsTenseInTheLastDeal() {
        assertEquals(MusicTrack.CIRCLE_DAY, Backdrops.table(hour = 12, lastDeal = false).music)
        assertEquals(MusicTrack.CIRCLE_EVENING, Backdrops.table(hour = 21, lastDeal = false).music)
        assertEquals(MusicTrack.TENSE, Backdrops.table(hour = 12, lastDeal = true).music)
        assertEquals(AmbientTrack.TABLE, Backdrops.table(hour = 12, lastDeal = true).ambient)
    }

    @Test
    fun theResultPlaysOnceWithoutAmbience() {
        val won = Backdrops.result(won = true)
        assertEquals(MusicTrack.VICTORY, won.music)
        assertEquals(MusicTrack.DEFEAT, Backdrops.result(won = false).music)
        assertFalse(won.musicLoops)
        assertNull(won.ambient)
    }
}
