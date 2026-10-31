package it.maestro.app.domain.season

import it.maestro.app.R
import it.maestro.app.domain.guide.GuidedHandStatus
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.story.StepAction
import it.maestro.app.domain.story.StoryDirector
import it.maestro.app.domain.story.nextStep
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** F1G-16b: i capitoli 1 (mano guidata) e 7 (revisione) hanno il loro segnale, e i casi limite cambiano il «prossimo passo». */
class GuidedChaptersTest {

    private fun derive(facts: SeasonFacts) = SeasonProgress.derive(facts, emptySet())

    @Test
    fun skippingTheGuideMarksChapterOneDoneAndOpensChapterTwo() {
        val progress = derive(SeasonFacts(guidedHand = GuidedHandStatus.SKIPPED))
        assertEquals(listOf(1), progress.done.map { it.chapter.number })
        assertEquals(2, progress.current?.chapter?.number)
    }

    @Test
    fun afterPlayingTheGuidedHandChapterOneWaitsForASparringGame() {
        val played = SeasonFacts(guidedHand = GuidedHandStatus.DONE)
        assertEquals(1, derive(played).current?.chapter?.number)
        assertEquals(2, derive(played.copy(sparringPlayed = true)).current?.chapter?.number)
    }

    @Test
    fun chapterSevenIsDoneWhenAMatchReviewHasBeenOpened() {
        val upToSix = SeasonFacts(
            guidedHand = GuidedHandStatus.SKIPPED,
            defeated = setOf(Opponent.GIGI, Opponent.NONNA_ROSA),
            dailyPuzzleSolvedToday = true,
            completedAcademyChapters = setOf(it.maestro.app.domain.academy.AcademyChapterId.CHAPTER_1_FONDAMENTI),
            sparringChecksPlayed = setOf(it.maestro.game.training.TrainingFundamental.SEVENS_COUNT),
            hasPlayedMatch = true,
        )
        assertEquals(7, derive(upToSix).current?.chapter?.number)
        assertEquals(8, derive(upToSix.copy(reviewed = true)).current?.chapter?.number)
    }

    @Test
    fun withoutAnyMatchToReviewChapterSevenSendsToPlayInstead() {
        val facts = SeasonFacts(reviewed = false, hasPlayedMatch = false)
        val seven = SeasonChapters.ALL.first { it.number == 7 }
        val step = ChapterState(seven, ChapterStatus.CURRENT, situation = StepSituation.NOTHING_TO_REVIEW).nextStep
        assertEquals(R.string.story_edge_review_empty_text, step?.textRes)
        assertEquals(StepAction.Play, step?.action)
        assertEquals(StepSituation.NORMAL, derive(facts).current?.situation)
    }

    @Test
    fun afterALossToTheChapterOpponentTheMaestroEncouragesToTryAgain() {
        val facts = SeasonFacts(guidedHand = GuidedHandStatus.SKIPPED, lostTo = Opponent.GIGI)
        val current = requireNotNull(derive(facts).current)
        assertEquals(2, current.chapter.number)
        assertEquals(StepSituation.LOST, current.situation)
        assertEquals(R.string.story_edge_lost_text, current.nextStep?.textRes)
        assertEquals(StepAction.PlayOpponent(Opponent.GIGI), current.nextStep?.action)
    }

    @Test
    fun aLossToSomeoneElseDoesNotChangeTheStep() {
        val facts = SeasonFacts(guidedHand = GuidedHandStatus.SKIPPED, lostTo = Opponent.TONINO)
        assertEquals(StepSituation.NORMAL, derive(facts).current?.situation)
        assertEquals(R.string.story_ch02_next_text, derive(facts).current?.nextStep?.textRes)
    }

    @Test
    fun theWelcomeAndTheEpilogueEndWithAButtonAndTheGuideScenesToo() {
        assertEquals(R.string.story_welcome_next_button, StoryDirector.WELCOME.end?.buttonRes)
        assertEquals(R.string.story_epilogue_next_button, StoryDirector.EPILOGUE.end?.buttonRes)
        assertEquals(R.string.story_epilogue_next_text, StoryDirector.EPILOGUE.end?.textRes)
        assertEquals(R.string.guide_end_button, StoryDirector.GUIDE_END.end?.buttonRes)
        assertEquals(R.string.guide_skip_scene_button, StoryDirector.GUIDE_SKIP.end?.buttonRes)
        assertNull(StoryDirector.GUIDE_INTRO.end)
        assertNull(StoryDirector.open(2)?.end)
        assertNotNull(StoryDirector.GUIDE_SKIP.beats.size == 3)
        assertTrue(StoryDirector.GUIDE_SKIP.beats.size == 3)
    }
}
