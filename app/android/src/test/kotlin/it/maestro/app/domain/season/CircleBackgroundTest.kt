package it.maestro.app.domain.season

import it.maestro.app.domain.story.StoryDirector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Task F1G-21: ogni Circolo ha il suo sfondo e ogni scena sa in quale Circolo si svolge. */
class CircleBackgroundTest {

    @Test
    fun everyCircleHasItsOwnBackgroundAndNoTwoShareOne() {
        assertEquals(5, Circle.entries.size)
        val backgrounds = Circle.entries.map { it.backgroundRes }
        assertTrue(backgrounds.all { it != 0 })
        assertEquals(backgrounds.size, backgrounds.toSet().size)
    }

    @Test
    fun theWelcomeIsAtTheBarTableAndTheEpilogueOnTheOlympus() {
        assertEquals(Circle.BAR_TABLE, StoryDirector.circleOf(StoryDirector.WELCOME))
        assertEquals(Circle.OLYMPUS, StoryDirector.circleOf(StoryDirector.EPILOGUE))
    }

    @Test
    fun theScenesOfAChapterHappenInThatChaptersCircle() {
        for (chapter in SeasonChapters.ALL) {
            val scenes = listOfNotNull(StoryDirector.open(chapter.number), StoryDirector.mid(chapter.number), StoryDirector.close(chapter.number))
            for (scene in scenes) assertEquals(chapter.circle, StoryDirector.circleOf(scene), scene.id)
        }
    }

    @Test
    fun theGuideScenesHaveNoCircle() {
        assertNull(StoryDirector.circleOf(StoryDirector.GUIDE_INTRO))
        assertNull(StoryDirector.circleOf(StoryDirector.GUIDE_END))
        assertNull(StoryDirector.circleOf(StoryDirector.GUIDE_SKIP))
    }
}
