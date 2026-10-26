package it.maestro.app.data

import it.maestro.app.data.local.academy.AcademyLessonId
import it.maestro.app.data.local.datastore.InMemoryAcademyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test unitari della progressione e persistenza a tappe dei capitoli e delle lezioni (task F2-06).
 */
class AcademyRepositoryTest {

    @Test
    fun initialProgressHasOnlyFirstLessonUnlocked() = runTest {
        val repo = InMemoryAcademyRepository()
        val progress = repo.getProgress()

        val l1_1 = progress[AcademyLessonId.LESSON_1_1_PRESA_SINGOLA]!!
        val l1_2 = progress[AcademyLessonId.LESSON_1_2_PUNTI_MAZZO]!!
        val l2_1 = progress[AcademyLessonId.LESSON_2_1_SCALA_VALORI]!!

        assertTrue(l1_1.isUnlocked, "La prima lezione del capitolo 1 deve essere sbloccata di default")
        assertFalse(l1_1.isCompleted)

        assertFalse(l1_2.isUnlocked, "La lezione 1.2 deve essere bloccata inizialmente")
        assertFalse(l1_2.isCompleted)

        assertFalse(l2_1.isUnlocked, "La lezione 2.1 deve essere bloccata inizialmente")
        assertFalse(l2_1.isCompleted)
    }

    @Test
    fun completingLessonOneUnlocksLessonTwo() = runTest {
        val repo = InMemoryAcademyRepository()
        repo.completeLesson(AcademyLessonId.LESSON_1_1_PRESA_SINGOLA)

        val progress = repo.observeProgress().first()
        val l1_1 = progress[AcademyLessonId.LESSON_1_1_PRESA_SINGOLA]!!
        val l1_2 = progress[AcademyLessonId.LESSON_1_2_PUNTI_MAZZO]!!
        val l1_3 = progress[AcademyLessonId.LESSON_1_3_SETTEBELLO]!!

        assertTrue(l1_1.isCompleted)
        assertTrue(l1_2.isUnlocked, "Completare la lezione 1.1 deve sbloccare la 1.2")
        assertFalse(l1_2.isCompleted)
        assertFalse(l1_3.isUnlocked, "La lezione 1.3 deve rimanere bloccata")
    }

    @Test
    fun completingChapterOneLessonsUnlocksChapterTwo() = runTest {
        val repo = InMemoryAcademyRepository(
            initialCompleted = setOf(
                AcademyLessonId.LESSON_1_1_PRESA_SINGOLA,
                AcademyLessonId.LESSON_1_2_PUNTI_MAZZO,
                AcademyLessonId.LESSON_1_3_SETTEBELLO,
            ),
        )
        repo.completeLesson(AcademyLessonId.LESSON_1_4_SCELTA_SOMME)

        val progress = repo.getProgress()
        val l1_4 = progress[AcademyLessonId.LESSON_1_4_SCELTA_SOMME]!!
        val l2_1 = progress[AcademyLessonId.LESSON_2_1_SCALA_VALORI]!!

        assertTrue(l1_4.isCompleted)
        assertTrue(l2_1.isUnlocked, "Completare l'ultima lezione del Capitolo 1 deve sbloccare la prima del Capitolo 2")
        assertFalse(l2_1.isCompleted)
    }

    @Test
    fun completingAllLessonsMarksAllCompleted() = runTest {
        val allLessons = AcademyLessonId.entries.toSet()
        val repo = InMemoryAcademyRepository(initialCompleted = allLessons)

        val progress = repo.getProgress()
        assertEquals(allLessons.size, progress.size)
        assertTrue(progress.values.all { it.isCompleted })
        assertTrue(progress.values.all { it.isUnlocked })
    }

    @Test
    fun resetProgressRestoresInitialState() = runTest {
        val repo = InMemoryAcademyRepository(
            initialCompleted = setOf(
                AcademyLessonId.LESSON_1_1_PRESA_SINGOLA,
                AcademyLessonId.LESSON_1_2_PUNTI_MAZZO,
            ),
        )
        repo.resetProgress()

        val progress = repo.getProgress()
        assertTrue(progress[AcademyLessonId.LESSON_1_1_PRESA_SINGOLA]!!.isUnlocked)
        assertFalse(progress[AcademyLessonId.LESSON_1_1_PRESA_SINGOLA]!!.isCompleted)
        assertFalse(progress[AcademyLessonId.LESSON_1_2_PUNTI_MAZZO]!!.isUnlocked)
    }
}
