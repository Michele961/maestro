package it.maestro.app.data

import it.maestro.ai.mastery.AbilityRadar
import it.maestro.ai.mastery.MasteryProfile
import it.maestro.app.data.local.academy.AcademyChapterId
import it.maestro.app.data.local.academy.AcademyContent
import it.maestro.app.data.local.academy.AcademyLessonId
import it.maestro.app.data.local.academy.AcademyPath
import it.maestro.app.feature.academy.presentation.recommendedChapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Percorso libero dal capitolo 2 e capitolo consigliato dal radar (task F1R-28). */
class AcademyPathTest {

    private val chapterOne = AcademyContent.CHAPTERS.first().lessons.map { it.id }.toSet()

    private fun profile(axis: String, matches: Int = 5): MasteryProfile {
        val low = 10
        val base = AbilityRadar(80, 80, 80, 80, 80)
        val radar = when (axis) {
            "sevens" -> base.copy(sevens = low)
            "denari" -> base.copy(denari = low)
            "primiera" -> base.copy(primiera = low)
            "board" -> base.copy(board = low)
            else -> base.copy(endgame = low)
        }
        return MasteryProfile(matchesPlayed = matches, radar = radar)
    }

    @Test
    fun chapterOneIsSequential() {
        val unlocked = AcademyPath.unlocked(setOf(AcademyLessonId.LESSON_1_1_PRESA_SINGOLA))
        assertTrue(AcademyLessonId.LESSON_1_2_PUNTI_MAZZO in unlocked)
        assertFalse(AcademyLessonId.LESSON_1_3_SETTEBELLO in unlocked)
        assertFalse(AcademyLessonId.LESSON_3_1_SOGLIA_DIECI in unlocked)
    }

    @Test
    fun chaptersTwoToFourAreFreeOnceChapterOneIsDone() {
        val unlocked = AcademyPath.unlocked(chapterOne)
        assertEquals(AcademyLessonId.entries.toSet(), unlocked)
    }

    @Test
    fun recommendedIsNullWithTooFewMatches() {
        assertNull(recommendedChapter(profile("primiera", matches = 2), chapterOne))
    }

    @Test
    fun recommendedFollowsWeakestAxis() {
        assertEquals(AcademyChapterId.CHAPTER_2_PRIMIERA, recommendedChapter(profile("primiera"), chapterOne))
        assertEquals(AcademyChapterId.CHAPTER_3_DIFESA, recommendedChapter(profile("board"), chapterOne))
        assertEquals(AcademyChapterId.CHAPTER_4_MAESTRIA, recommendedChapter(profile("sevens"), chapterOne))
        assertEquals(AcademyChapterId.CHAPTER_4_MAESTRIA, recommendedChapter(profile("endgame"), chapterOne))
        assertEquals(AcademyChapterId.CHAPTER_2_PRIMIERA, recommendedChapter(profile("denari"), chapterOne))
    }

    @Test
    fun recommendedIsChapterOneWhileItIsNotFinished() {
        assertEquals(AcademyChapterId.CHAPTER_1_FONDAMENTI, recommendedChapter(profile("board"), emptySet()))
        assertEquals(AcademyChapterId.CHAPTER_1_FONDAMENTI, recommendedChapter(profile("denari"), emptySet()))
    }
}
