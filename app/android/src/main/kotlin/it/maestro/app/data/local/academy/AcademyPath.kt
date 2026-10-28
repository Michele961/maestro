package it.maestro.app.data.local.academy

import it.maestro.app.domain.academy.AcademyLessonId

/**
 * Regole del percorso dell'Accademia (task F1R-28).
 *
 * Il capitolo 1 è sequenziale; dal capitolo 2 il percorso è libero, ma solo dopo aver
 * completato tutto il capitolo 1. Funzioni pure, condivise da DataStore e in-memory.
 */
public object AcademyPath {

    /**
     * Le lezioni sbloccate dato l'insieme di [completed].
     * Capitolo 1: la prima, poi ognuna dopo la precedente completata. Capitoli 2…4: tutte, a capitolo 1 finito.
     */
    public fun unlocked(completed: Set<AcademyLessonId>): Set<AcademyLessonId> {
        val chapters = AcademyContent.CHAPTERS
        val first = chapters.first().lessons.map { it.id }
        val firstDone = first.all { it in completed }
        val result = mutableSetOf<AcademyLessonId>()
        var previousCompleted = true
        first.forEach { id ->
            if (previousCompleted) result += id
            previousCompleted = id in completed
        }
        if (firstDone) {
            chapters.drop(1).forEach { chapter -> result += chapter.lessons.map { it.id } }
        }
        return result
    }
}
