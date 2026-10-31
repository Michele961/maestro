package it.maestro.app.domain.season

import it.maestro.app.domain.academy.AcademyChapterId
import it.maestro.app.domain.guide.GuidedHandStatus
import it.maestro.app.domain.opponents.Opponent
import it.maestro.game.training.TrainingFundamental

/**
 * Ciò che la Stagione legge dallo stato esistente dell'app (nessuna nuova fonte di verità).
 *
 * @property defeated avversari battuti in Classica.
 * @property dailyPuzzleSolvedToday la Sfida del giorno di oggi è risolta.
 * @property completedAcademyChapters capitoli dell'Accademia con tutte le lezioni completate.
 * @property sparringChecksPlayed Check di cui lo storico delle partite registra uno Sparring.
 * @property guidedHand come è andata la mano d'esordio guidata (F1G-02); [GuidedHandStatus.NOT_DONE] tiene nascosto il capitolo 1.
 * @property sparringPlayed almeno una partita di Sparring registrata (chiude il capitolo 1 dopo la mano guidata).
 * @property reviewed il giocatore ha aperto la revisione di una partita (capitolo 7).
 * @property hasPlayedMatch lo storico ha almeno una partita da rivedere.
 * @property lostTo l'avversario dell'ultima partita registrata, se il giocatore l'ha persa.
 */
public data class SeasonFacts(
    val defeated: Set<Opponent> = emptySet(),
    val dailyPuzzleSolvedToday: Boolean = false,
    val completedAcademyChapters: Set<AcademyChapterId> = emptySet(),
    val sparringChecksPlayed: Set<TrainingFundamental> = emptySet(),
    val guidedHand: GuidedHandStatus = GuidedHandStatus.NOT_DONE,
    val sparringPlayed: Boolean = false,
    val reviewed: Boolean = false,
    val hasPlayedMatch: Boolean = false,
    val lostTo: Opponent? = null,
)

/** Il capitolo si può giocare con lo stato [facts]; altrimenti resta nascosto (il capitolo 1 finché la mano guidata non c'è stata). */
public fun ChapterGoal.isSupported(facts: SeasonFacts): Boolean = when (this) {
    ChapterGoal.GuidedHand -> facts.guidedHand != GuidedHandStatus.NOT_DONE
    is ChapterGoal.All -> goals.all { it.isSupported(facts) }
    ChapterGoal.ReviewLastMatch,
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
    // Mano guidata saltata: il capitolo è fatto; giocata: serve poi una partita di Sparring contro Gigi.
    ChapterGoal.GuidedHand -> facts.guidedHand == GuidedHandStatus.SKIPPED ||
        (facts.guidedHand == GuidedHandStatus.DONE && facts.sparringPlayed)
    ChapterGoal.ReviewLastMatch -> facts.reviewed
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
    /** Come si presenta il passo oggi (partita persa, niente da rivedere): cambia la frase del «prossimo passo». */
    val situation: StepSituation = StepSituation.NORMAL,
)

/** Casi limite del «prossimo passo» (`docs/design/stagione-scene.md`, «Casi limite»). */
public enum class StepSituation { NORMAL, LOST, NOTHING_TO_REVIEW }

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
            val states = all.filter { it.goal.isSupported(facts) }.map { chapter ->
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
                    situation = if (current) situationOf(chapter, facts) else StepSituation.NORMAL,
                )
            }
            return SeasonProgress(states, toClaim)
        }

        private fun situationOf(chapter: SeasonChapter, facts: SeasonFacts): StepSituation = when {
            chapter.goal == ChapterGoal.ReviewLastMatch && !facts.hasPlayedMatch -> StepSituation.NOTHING_TO_REVIEW
            facts.lostTo != null && chapter.goal.opponentToBeat(facts) == facts.lostTo -> StepSituation.LOST
            else -> StepSituation.NORMAL
        }

        // L'avversario che il giocatore deve ancora battere in questo capitolo, se il capitolo è una sfida.
        private fun ChapterGoal.opponentToBeat(facts: SeasonFacts): Opponent? =
            (this as? ChapterGoal.DefeatOpponents)?.opponents?.firstOrNull { it !in facts.defeated }

        private fun ChapterGoal.isPartlyMet(facts: SeasonFacts): Boolean = when (this) {
            is ChapterGoal.DefeatOpponents -> opponents.size > 1 && opponents.first() in facts.defeated
            is ChapterGoal.All -> goals.size > 1 && goals.first().isMet(facts)
            else -> false
        }

        private fun ChapterGoal.isReversed(facts: SeasonFacts): Boolean =
            this is ChapterGoal.All && goals.size > 1 && !goals.first().isMet(facts) && goals.drop(1).any { it.isMet(facts) }
    }
}
