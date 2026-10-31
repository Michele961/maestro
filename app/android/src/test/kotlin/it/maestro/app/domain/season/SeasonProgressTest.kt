package it.maestro.app.domain.season

import it.maestro.app.domain.academy.AcademyChapterId
import it.maestro.app.domain.guide.GuidedHandStatus
import it.maestro.app.domain.opponents.Opponent
import it.maestro.game.training.TrainingFundamental
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Task F1G-07: il capitolo corrente si deriva dallo stato esistente, senza una fonte di verità propria. */
class SeasonProgressTest {

    private fun numbers(progress: SeasonProgress) = progress.chapters.map { it.chapter.number }

    private fun current(facts: SeasonFacts, claimed: Set<Int> = emptySet()) =
        SeasonProgress.derive(facts, claimed).current?.chapter?.number

    @Test
    fun theFirstChapterIsHiddenUntilTheGuidedHandHasBeenPlayedOrSkipped() {
        val progress = SeasonProgress.derive(SeasonFacts(), claimed = emptySet())
        // Cap. 1 (mano guidata, F1G-02) nascosto finché non c'è il segnale; il cap. 7 (revisione) c'è (F1G-16b).
        assertEquals(listOf(2, 3, 4, 5, 6, 7, 8, 9, 10), numbers(progress))
        assertFalse(ChapterGoal.GuidedHand.isSupported(SeasonFacts()))
        assertTrue(ChapterGoal.ReviewLastMatch.isSupported(SeasonFacts()))
        assertTrue(ChapterGoal.GuidedHand.isSupported(SeasonFacts(guidedHand = GuidedHandStatus.DONE)))
        assertTrue(ChapterGoal.GuidedHand.isSupported(SeasonFacts(guidedHand = GuidedHandStatus.SKIPPED)))
    }

    @Test
    fun hiddenChaptersAreNeverCompleted() {
        val everything = SeasonFacts(
            defeated = Opponent.entries.toSet(),
            dailyPuzzleSolvedToday = true,
            completedAcademyChapters = AcademyChapterId.entries.toSet(),
            sparringChecksPlayed = TrainingFundamental.entries.toSet(),
            reviewed = true,
        )
        val progress = SeasonProgress.derive(everything, claimed = emptySet())
        assertTrue(progress.isComplete)
        assertEquals(listOf(2, 3, 4, 5, 6, 7, 8, 9, 10), progress.toClaim.map { it.number })
    }

    @Test
    fun theCurrentChapterIsTheFirstNotDone() {
        assertEquals(2, current(SeasonFacts()))
        assertEquals(3, current(SeasonFacts(defeated = setOf(Opponent.GIGI))))
    }

    @Test
    fun chaptersAreCompletedInOrderEvenIfALaterGoalIsAlreadyMet() {
        // Nonna Rosa battuta, ma il Check sui Sette (cap. 3) non ancora giocato: il cap. 5 aspetta.
        val facts = SeasonFacts(defeated = setOf(Opponent.GIGI, Opponent.NONNA_ROSA), dailyPuzzleSolvedToday = true)
        val progress = SeasonProgress.derive(facts, emptySet())
        assertEquals(3, progress.current?.chapter?.number)
        assertEquals(listOf(2), progress.done.map { it.chapter.number })
    }

    @Test
    fun aMetGoalWithoutClaimIsDoneAndToBeClaimed() {
        val facts = SeasonFacts(defeated = setOf(Opponent.GIGI))
        assertEquals(listOf(2), SeasonProgress.derive(facts, emptySet()).toClaim.map { it.number })
        val claimed = SeasonProgress.derive(facts, setOf(2))
        assertTrue(claimed.toClaim.isEmpty())
        assertEquals(listOf(2), claimed.done.map { it.chapter.number })
    }

    @Test
    fun aClaimedChapterStaysDoneWhenTheDailyPuzzleResetsTomorrow() {
        val solved = SeasonFacts(
            defeated = setOf(Opponent.GIGI),
            sparringChecksPlayed = setOf(TrainingFundamental.SEVENS_COUNT),
            dailyPuzzleSolvedToday = true,
        )
        assertEquals(5, current(solved, setOf(2, 3, 4)))
        val tomorrow = solved.copy(dailyPuzzleSolvedToday = false)
        assertEquals(5, current(tomorrow, setOf(2, 3, 4)))
        assertTrue(SeasonProgress.derive(tomorrow, setOf(2, 3, 4)).done.any { it.chapter.number == 4 })
    }

    @Test
    fun primieraChapterNeedsBothTheAcademyChapterAndTheCheck() {
        val base = SeasonFacts(
            defeated = setOf(Opponent.GIGI, Opponent.NONNA_ROSA, Opponent.TONINO),
            dailyPuzzleSolvedToday = true,
            completedAcademyChapters = setOf(AcademyChapterId.CHAPTER_1_FONDAMENTI),
            sparringChecksPlayed = setOf(TrainingFundamental.SEVENS_COUNT),
            reviewed = true,
        )
        val academyOnly = base.copy(completedAcademyChapters = base.completedAcademyChapters + AcademyChapterId.CHAPTER_2_PRIMIERA)
        assertEquals(9, current(academyOnly))
        val both = academyOnly.copy(sparringChecksPlayed = academyOnly.sparringChecksPlayed + TrainingFundamental.PRIMIERA_LEAD)
        assertEquals(10, current(both))
    }

    @Test
    fun finalChapterHasAMidPointAfterDonAlfonso() {
        val ladder = setOf(Opponent.GIGI, Opponent.NONNA_ROSA, Opponent.TONINO)
        val base = SeasonFacts(
            defeated = ladder,
            dailyPuzzleSolvedToday = true,
            completedAcademyChapters = setOf(AcademyChapterId.CHAPTER_1_FONDAMENTI, AcademyChapterId.CHAPTER_2_PRIMIERA),
            sparringChecksPlayed = setOf(TrainingFundamental.SEVENS_COUNT, TrainingFundamental.PRIMIERA_LEAD),
            reviewed = true,
        )
        assertFalse(SeasonProgress.derive(base, emptySet()).current!!.midReached)
        val afterAlfonso = SeasonProgress.derive(base.copy(defeated = ladder + Opponent.DON_ALFONSO), emptySet())
        assertEquals(10, afterAlfonso.current?.chapter?.number)
        assertTrue(afterAlfonso.current!!.midReached)
        val finished = SeasonProgress.derive(base.copy(defeated = Opponent.entries.toSet()), emptySet())
        assertNull(finished.current)
        assertTrue(finished.isComplete)
    }

    @Test
    fun theTenChaptersAreDataWithUniqueNumbers() {
        assertEquals((1..10).toList(), SeasonChapters.ALL.map { it.number })
        assertTrue(SeasonChapters.ALL.all { it.rewardCredits > 0 })
    }
}
