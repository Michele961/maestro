package it.maestro.app.domain.season

import kotlinx.coroutines.flow.Flow

/** Progresso della Scalata: derivato dallo stato esistente più i capitoli con ricompensa già riscossa. */
public interface SeasonRepository {
    public fun observeProgress(): Flow<SeasonProgress>

    /** Segna riscossa la ricompensa del capitolo [number]; `true` solo la prima volta. */
    public suspend fun markClaimed(number: Int): Boolean
}
