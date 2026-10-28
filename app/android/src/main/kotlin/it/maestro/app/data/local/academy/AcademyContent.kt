package it.maestro.app.data.local.academy

import it.maestro.app.domain.academy.AcademyLesson
import it.maestro.app.domain.academy.AcademyChapter

/**
 * Contenuti formativi completi dell'Accademia del Maestro: 4 Macro-Moduli (Capitoli) e 15 Sotto-Moduli (Lezioni).
 * Un file per capitolo: `AcademyChapter1.kt` … `AcademyChapter4.kt`.
 */
public object AcademyContent {

    public val CHAPTERS: List<AcademyChapter> = listOf(
        ACADEMY_CHAPTER_1,
        ACADEMY_CHAPTER_2,
        ACADEMY_CHAPTER_3,
        ACADEMY_CHAPTER_4,
    )

    /** Lista piatta di tutte le lezioni ordinate per progressione sequenziale. */
    public val ALL_LESSONS: List<AcademyLesson> = CHAPTERS.flatMap { it.lessons }
}
