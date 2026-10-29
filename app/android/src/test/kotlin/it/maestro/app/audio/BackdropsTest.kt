package it.maestro.app.audio

import it.maestro.app.domain.audio.AmbientTrack
import it.maestro.app.domain.audio.Backdrops
import it.maestro.app.domain.audio.MusicTrack
import kotlin.test.Test
import kotlin.test.assertEquals

/** Sfondo sonoro (F1R-20): giorno fino alle 19, sera dopo; la musica non cambia tra circolo e tavolo. */
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
    fun tableKeepsTheSameMusicAsTheCircleAndChangesOnlyTheAmbience() {
        for (hour in listOf(3, 12, 21)) {
            assertEquals(Backdrops.circle(hour).music, Backdrops.table(hour).music)
        }
        assertEquals(AmbientTrack.TABLE, Backdrops.table(12).ambient)
    }
}
