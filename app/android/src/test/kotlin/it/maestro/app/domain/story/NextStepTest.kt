package it.maestro.app.domain.story

import it.maestro.app.R
import it.maestro.app.domain.academy.AcademyChapterId
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.season.ChapterState
import it.maestro.app.domain.season.ChapterStatus
import it.maestro.app.domain.season.SeasonChapters
import it.maestro.app.domain.season.SeasonFacts
import it.maestro.app.domain.season.SeasonProgress
import it.maestro.game.training.TrainingFundamental
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Task F1G-16a: ogni capitolo ha un «prossimo passo» e il pulsante porta alla destinazione giusta. */
class NextStepTest {

    private fun current(facts: SeasonFacts) = requireNotNull(SeasonProgress.derive(facts, emptySet()).current)

    private fun action(number: Int, midReached: Boolean = false, reversed: Boolean = false): StepAction {
        val chapter = SeasonChapters.ALL.first { it.number == number }
        return requireNotNull(ChapterState(chapter, ChapterStatus.CURRENT, midReached, reversed).nextStep).action
    }

    @Test
    fun playingAnOpponentGoesToThePreGameOfThatOpponent() {
        assertEquals(StepAction.PlayOpponent(Opponent.GIGI), action(2))
        assertEquals(StepAction.PlayOpponent(Opponent.NONNA_ROSA), action(5))
        assertEquals(StepAction.PlayOpponent(Opponent.TONINO), action(8))
        assertEquals(StepAction.PlayOpponent(Opponent.DON_ALFONSO), action(10))
    }

    @Test
    fun theSevensTestIsASparringWithTheCheckAlreadyChosen() {
        assertEquals(StepAction.Sparring(Opponent.GIGI, TrainingFundamental.SEVENS_COUNT), action(3))
    }

    @Test
    fun theBoardChapterOpensTheDailyPuzzleAndTheRulesChapterOpensTheAcademy() {
        assertEquals(StepAction.DailyPuzzle, action(4))
        assertEquals(StepAction.Academy, action(6))
    }

    @Test
    fun chapterNineFirstSendsToTheAcademyThenToThePrimieraCheck() {
        assertEquals(StepAction.Academy, action(9))
        assertEquals(R.string.story_ch09_next_text, nextOf(9).textRes)
        assertEquals(StepAction.Sparring(Opponent.GIGI, TrainingFundamental.PRIMIERA_LEAD), action(9, midReached = true))
        assertEquals(R.string.story_ch09_next2_text, nextOf(9, midReached = true).textRes)
    }

    @Test
    fun chapterNineWithTheTestDoneBeforeTheLessonUsesTheReverseText() {
        assertEquals(R.string.story_ch09_next_text_reverse, nextOf(9, reversed = true).textRes)
        assertEquals(StepAction.Academy, action(9, reversed = true))
    }

    @Test
    fun chapterTenSendsToAlfonsoThenToTheMaestro() {
        assertEquals(StepAction.PlayOpponent(Opponent.MAESTRO), action(10, midReached = true))
        assertEquals(R.string.story_ch10_next2_text, nextOf(10, midReached = true).textRes)
    }

    @Test
    fun theHiddenReviewChapterPointsToTheProfileWhereTheReviewLives() {
        assertEquals(StepAction.Profile, action(7))
    }

    @Test
    fun theDerivedStateKnowsWhichStepOfChapterNineItIsAt() {
        val lessonDone = SeasonFacts(
            defeated = setOf(Opponent.GIGI, Opponent.NONNA_ROSA, Opponent.TONINO),
            sparringChecksPlayed = setOf(TrainingFundamental.SEVENS_COUNT),
            dailyPuzzleSolvedToday = true,
            completedAcademyChapters = AcademyChapterId.entries.toSet(),
            reviewed = true,
        )
        val state = current(lessonDone)
        assertEquals(9, state.chapter.number)
        assertEquals(true, state.midReached)
        val testFirst = lessonDone.copy(
            completedAcademyChapters = setOf(AcademyChapterId.CHAPTER_1_FONDAMENTI),
            sparringChecksPlayed = TrainingFundamental.entries.toSet(),
        )
        assertEquals(true, current(testFirst).reversed)
    }

    @Test
    fun aChapterThatIsNotTheCurrentOneHasNoNextStep() {
        val chapter = SeasonChapters.ALL.first { it.number == 4 }
        assertNull(ChapterState(chapter, ChapterStatus.UPCOMING).nextStep)
        assertNull(ChapterState(chapter, ChapterStatus.DONE).nextStep)
    }

    private fun nextOf(number: Int, midReached: Boolean = false, reversed: Boolean = false): NextStep {
        val chapter = SeasonChapters.ALL.first { it.number == number }
        return requireNotNull(ChapterState(chapter, ChapterStatus.CURRENT, midReached, reversed).nextStep)
    }
}
