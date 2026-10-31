package it.maestro.app.domain.season

import it.maestro.app.domain.academy.AcademyChapterId
import it.maestro.app.domain.opponents.Opponent
import it.maestro.game.training.TrainingFundamental

/**
 * Ciò che la Stagione legge dallo stato esistente dell'app (nessuna nuova fonte di verità).
 *
 * @property defeated avversari battuti in Classica.
 * @property dailyPuzzleSolvedToday la Sfida del giorno di oggi è risolta.
 * @property completedAcademyChapters capitoli dell'Accademia con tutte le lezioni completate.
 * @property sparringChecksPlayed Check di cui lo storico delle partite registra uno Sparring.
 */
public data class SeasonFacts(
    val defeated: Set<Opponent> = emptySet(),
    val dailyPuzzleSolvedToday: Boolean = false,
    val completedAcademyChapters: Set<AcademyChapterId> = emptySet(),
    val sparringChecksPlayed: Set<TrainingFundamental> = emptySet(),
)

/** Il capitolo si può giocare con le funzioni che ci sono oggi; altrimenti resta nascosto. */
public fun ChapterGoal.isSupported(): Boolean = when (this) {
    ChapterGoal.GuidedHand, ChapterGoal.ReviewLastMatch -> false
    is ChapterGoal.All -> goals.all { it.isSupported() }
    is ChapterGoal.DefeatOpponents,
    is ChapterGoal.SparringCheck,
    ChapterGoal.DailyPuzzle,
    is ChapterGoal.AcademyChapter,
    -> true
}

/** L'obiettivo è soddisfatto dallo stato [facts]. */
public fun ChapterGoal.isMet(facts: SeasonFacts): Boolean = when (this) {
    is ChapterGoal.DefeatOpponents -> facts.defeated.containsAll(opponents)
    is ChapterGoal.SparringCheck -> fundamental in facts.sparringChecksPlayed
    ChapterGoal.DailyPuzzle -> facts.dailyPuzzleSolvedToday
    is ChapterGoal.AcademyChapter -> chapter in facts.completedAcademyChapters
    ChapterGoal.GuidedHand, ChapterGoal.ReviewLastMatch -> false
    is ChapterGoal.All -> goals.all { it.isMet(facts) }
}

/** Dove si trova un capitolo nella Scalata. */
public enum class ChapterStatus { DONE, CURRENT, UPCOMING }

/** Un capitolo con il suo [status]; [midReached] vale per i capitoli a due partite dopo la prima. */
public data class ChapterState(
    val chapter: SeasonChapter,
    val status: ChapterStatus,
    val midReached: Boolean = false,
    /** Capitolo a due passi con il secondo già fatto e il primo no (la prova prima della lezione, cap. 9). */
    val reversed: Boolean = false,
)

/**
 * La Scalata derivata dallo stato: [chapters] sono solo quelli disponibili (gli altri restano nascosti), in ordine.
 * [toClaim] sono i capitoli finiti la cui ricompensa non è ancora stata riscossa. Nessun blocco a tempo.
 */
public data class SeasonProgress(val chapters: List<ChapterState>, val toClaim: List<SeasonChapter> = emptyList()) {
    /** Il capitolo da giocare ora, `null` a Scalata finita. */
    val current: ChapterState? get() = chapters.firstOrNull { it.status == ChapterStatus.CURRENT }

    val done: List<ChapterState> get() = chapters.filter { it.status == ChapterStatus.DONE }

    val isComplete: Boolean get() = chapters.isNotEmpty() && chapters.all { it.status == ChapterStatus.DONE }

    /** Quanti tavoli mancano all'Olimpo: i capitoli disponibili non ancora fatti (`docs/design/stagione-scene.md`). */
    val remaining: Int get() = chapters.size - done.size

    public companion object {
        /**
         * Un capitolo è fatto se la sua ricompensa è già stata riscossa ([claimed], numeri di capitolo) oppure,
         * nell'ordine della storia, se lo stato soddisfa il suo obiettivo; il corrente è il primo non fatto.
         */
        public fun derive(
            facts: SeasonFacts,
            claimed: Set<Int>,
            all: List<SeasonChapter> = SeasonChapters.ALL,
        ): SeasonProgress {
            var previousDone = true
            var currentFound = false
            val toClaim = mutableListOf<SeasonChapter>()
            val states = all.filter { it.goal.isSupported() }.map { chapter ->
                val done = chapter.number in claimed || (previousDone && chapter.goal.isMet(facts))
                if (done && chapter.number !in claimed) toClaim += chapter
                previousDone = done
                val status = when {
                    done -> ChapterStatus.DONE
                    !currentFound -> ChapterStatus.CURRENT.also { currentFound = true }
                    else -> ChapterStatus.UPCOMING
                }
                val current = status == ChapterStatus.CURRENT
                ChapterState(
                    chapter, status,
                    midReached = current && chapter.goal.isPartlyMet(facts),
                    reversed = current && chapter.goal.isReversed(facts),
                )
            }
            return SeasonProgress(states, toClaim)
        }

        private fun ChapterGoal.isPartlyMet(facts: SeasonFacts): Boolean = when (this) {
            is ChapterGoal.DefeatOpponents -> opponents.size > 1 && opponents.first() in facts.defeated
            is ChapterGoal.All -> goals.size > 1 && goals.first().isMet(facts)
            else -> false
        }

        private fun ChapterGoal.isReversed(facts: SeasonFacts): Boolean =
            this is ChapterGoal.All && goals.size > 1 && !goals.first().isMet(facts) && goals.drop(1).any { it.isMet(facts) }
    }
}
