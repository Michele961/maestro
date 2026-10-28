package it.maestro.app.domain.opponents

import kotlinx.coroutines.flow.Flow

/** Persistenza dell'avanzamento sulla scala degli avversari (oggi in locale, domani sul server). */
public interface OpponentRepository {
    public fun observeProgress(): Flow<OpponentProgress>

    /** Registra la vittoria del giocatore su [opponent] in una partita completa in Classica. */
    public suspend fun recordVictory(opponent: Opponent): LadderVictory
}
