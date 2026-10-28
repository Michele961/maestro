package it.maestro.app.domain.profile

import it.maestro.app.domain.mastery.MatchHistoryEntry
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.opponents.OpponentProgress
import kotlinx.coroutines.flow.Flow

/**
 * I titoli del giocatore (`docs/app/features/profile.md` §3): oggi uno per ogni avversario battuto sulla scala
 * (testi di F1R-18e). Il giocatore sceglie quale mostrare; l'elenco accoglierà i titoli futuri (sfide, classifiche).
 * I testi stanno nelle risorse (ADR-0007).
 */
public object PlayerTitles {
    /** Titoli guadagnati, nell'ordine della scala. */
    public fun earned(progress: OpponentProgress): List<Opponent> =
        Opponent.entries.filter { progress.isDefeated(it) }

    /**
     * Titolo mostrato: quello scelto se è ancora tra i guadagnati, altrimenti l'ultimo guadagnato.
     * `null` se non ha ancora battuto nessuno («Nuovo al circolo»).
     */
    public fun current(progress: OpponentProgress, selected: String?): Opponent? {
        val earned = earned(progress)
        return earned.firstOrNull { it.name == selected } ?: earned.lastOrNull()
    }
}

/** Andamento della Maestria sulle ultime partite. */
public enum class MasteryTrend { UP, FLAT, DOWN }

private const val TREND_WINDOW = 5

/** Somma delle variazioni dell'Indice nelle ultime [TREND_WINDOW] partite; [history] è dalla più recente. */
public fun masteryTrendOf(history: List<MatchHistoryEntry>): MasteryTrend {
    val delta = history.take(TREND_WINDOW).sumOf { it.eloDelta }
    return when {
        delta > 0 -> MasteryTrend.UP
        delta < 0 -> MasteryTrend.DOWN
        else -> MasteryTrend.FLAT
    }
}

/** Titolo scelto dal giocatore, salvato come nome dell'avversario che l'ha dato. */
public interface PlayerTitleRepository {
    public fun observeSelected(): Flow<String?>

    public suspend fun select(titleId: String)
}
