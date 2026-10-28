package it.maestro.app.domain.progress

import it.maestro.app.domain.opponents.Opponent
import kotlinx.coroutines.flow.Flow

/**
 * L'esperienza del giocatore (`docs/app/features/missions.md` §3.3): un solo contatore che sale con le cose che
 * già succedono e segna la strada fatta. Non si compra e non si spende; la scrive solo `RewardEngine`.
 * I nomi dei livelli stanno nelle risorse (ADR-0007), non qui.
 */
public object Experience {
    /** Punti esperienza per salire di un livello [da calibrare]. */
    public const val PX_PER_LEVEL: Int = 250

    /** Livello massimo: oltre questo i punti continuano a salire, il livello no. */
    public const val MAX_LEVEL: Int = 10

    /** Punti per una missione riscossa. */
    public const val MISSION_CLAIMED: Int = 10

    /** Punti in più per le tre missioni del giorno riscosse («tre su tre»). */
    public const val ALL_MISSIONS_CLAIMED: Int = 25

    /** Punti per un capitolo dell'Accademia finito (lo assegna `onChapterCompleted`, F1R-28d). */
    public const val CHAPTER_DONE: Int = 50

    /** Livello (da 1) raggiunto con [points] punti. */
    public fun levelOf(points: Int): Int = minOf(MAX_LEVEL, points.coerceAtLeast(0) / PX_PER_LEVEL + 1)

    /** Punti già fatti dentro il livello corrente (0 al livello massimo, dove non c'è un livello successivo). */
    public fun pointsIntoLevel(points: Int): Int =
        if (levelOf(points) == MAX_LEVEL) 0 else points.coerceAtLeast(0) % PX_PER_LEVEL

    /** Punti per la prima vittoria su [opponent]: 100, 200, … 500 secondo l'ordine della scala. */
    public fun ladderFirstWin(opponent: Opponent): Int = 100 * (opponent.ordinal + 1)

    /** Punti per il livello [tierIndex] (da 0) della Sfida del Giorno risolto: 10, 15, 20. */
    public fun puzzleTier(tierIndex: Int): Int = when (tierIndex) {
        0 -> 10
        1 -> 15
        else -> 20
    }
}

/** Persistenza dei punti esperienza (oggi in locale, domani sul server). */
public interface ExperienceRepository {
    public fun observePoints(): Flow<Int>

    /** Somma [points] (positivi) al totale e restituisce il nuovo totale. */
    public suspend fun add(points: Int): Int
}
