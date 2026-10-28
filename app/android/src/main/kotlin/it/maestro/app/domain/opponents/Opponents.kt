package it.maestro.app.domain.opponents

import it.maestro.ai.bot.DifficultyLevel

/**
 * Gli avversari del circolo, dal più debole al più forte (`docs/app/features/pre-game.md`): uno per livello del
 * motore, in ordine di sblocco. Nomi, titoli e ritratti stanno nelle risorse (ADR-0007), non qui.
 */
public enum class Opponent(public val level: DifficultyLevel) {
    GIGI(DifficultyLevel.PRINCIPIANTE),
    NONNA_ROSA(DifficultyLevel.DILETTANTE),
    TONINO(DifficultyLevel.INTERMEDIO),
    DON_ALFONSO(DifficultyLevel.AVANZATO),
    MAESTRO(DifficultyLevel.MAESTRO),
    ;

    public companion object {
        /** L'avversario che gioca al [level] del motore. */
        public fun forLevel(level: DifficultyLevel): Opponent = entries.first { it.level == level }
    }
}

/**
 * Esito di una vittoria registrata sulla scala.
 *
 * @property firstTime è la prima volta che [defeated] viene battuto (solo allora scattano le ricompense).
 * @property newlyUnlocked l'avversario che questa vittoria ha sbloccato, se ce n'è uno.
 */
public data class LadderVictory(
    val defeated: Opponent,
    val firstTime: Boolean,
    val newlyUnlocked: Opponent?,
)

/**
 * Avanzamento sulla scala degli avversari: si parte dal primo e si sblocca il successivo battendo il precedente
 * in una partita completa in Classica. Gli avversari battuti restano rigiocabili.
 */
public data class OpponentProgress(val defeated: Set<Opponent> = emptySet()) {

    /** Si può affrontare [opponent]: è il primo, oppure il precedente è stato battuto. */
    public fun isUnlocked(opponent: Opponent): Boolean {
        val index = Opponent.entries.indexOf(opponent)
        return index == 0 || Opponent.entries[index - 1] in defeated
    }

    public fun isDefeated(opponent: Opponent): Boolean = opponent in defeated

    /** Il prossimo avversario da battere, `null` quando la scala è completa. */
    public val next: Opponent? get() = Opponent.entries.firstOrNull { it !in defeated }

    /** Registra una vittoria su [opponent]; se non è ancora sbloccato non conta. */
    public fun afterVictory(opponent: Opponent): Pair<OpponentProgress, LadderVictory> {
        if (!isUnlocked(opponent)) return this to LadderVictory(opponent, firstTime = false, newlyUnlocked = null)
        val firstTime = opponent !in defeated
        val updated = copy(defeated = defeated + opponent)
        val unlocked = Opponent.entries.firstOrNull { updated.isUnlocked(it) && !isUnlocked(it) }
        return updated to LadderVictory(opponent, firstTime, unlocked)
    }
}
