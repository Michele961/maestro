package it.maestro.app.data.repository

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.data.local.db.SavedMatchDao
import it.maestro.app.data.local.db.SavedMatchEntity
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ReplayResult
import it.maestro.game.scopa.ScopaState
import it.maestro.game.scopa.replay
import it.maestro.app.domain.time.Clock
import it.maestro.app.domain.time.SystemClock

/**
 * Risultato del ripristino di una partita interrotta (ADR-0003, task F1-08).
 */
public sealed interface RestoreMatchResult {
    public data class Success(
        val state: ScopaState,
        val matchLog: MatchLog,
        val level: DifficultyLevel,
        val targetScore: Int,
    ) : RestoreMatchResult

    public data object None : RestoreMatchResult

    public data class WrongRulesVersion(
        val found: Int,
        val expected: Int,
    ) : RestoreMatchResult

    public data class Corrupted(val reason: String) : RestoreMatchResult
}

/**
 * Repository per il salvataggio e la ripresa dello stato partita basato su [MatchLog] (ADR-0003).
 */
public interface MatchRepository {
    public suspend fun saveActiveMatch(
        log: MatchLog,
        level: DifficultyLevel,
        targetScore: Int,
        p1Score: Int = 0,
        p2Score: Int = 0,
    )

    public suspend fun markCompleted(
        id: String = "active_match",
        winner: Seat?,
        p1Score: Int,
        p2Score: Int,
    )

    public suspend fun restoreActiveMatch(): RestoreMatchResult

    public suspend fun clearActiveMatch()
}

/**
 * Implementazione Room del repository partita.
 */
public class RoomMatchRepository(
    private val dao: SavedMatchDao,
    private val clock: Clock = SystemClock,
) : MatchRepository {

    override suspend fun saveActiveMatch(
        log: MatchLog,
        level: DifficultyLevel,
        targetScore: Int,
        p1Score: Int,
        p2Score: Int,
    ) {
        val now = clock.nowMillis()
        val existing = dao.getActiveMatch()
        val entity = SavedMatchEntity(
            id = existing?.id ?: "active_match",
            matchLogText = log.toText(),
            levelName = level.name,
            targetScore = targetScore,
            status = "IN_PROGRESS",
            winnerSeat = null,
            p1Score = p1Score,
            p2Score = p2Score,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
        dao.insertOrUpdate(entity)
    }

    override suspend fun markCompleted(id: String, winner: Seat?, p1Score: Int, p2Score: Int) {
        val match = dao.getMatchById(id) ?: return
        dao.insertOrUpdate(
            match.copy(
                status = "COMPLETED",
                winnerSeat = winner?.name,
                p1Score = p1Score,
                p2Score = p2Score,
                updatedAt = clock.nowMillis(),
            ),
        )
    }

    override suspend fun restoreActiveMatch(): RestoreMatchResult {
        val active = dao.getActiveMatch() ?: return RestoreMatchResult.None
        val parsedLog = try {
            MatchLog.parse(active.matchLogText)
        } catch (e: Exception) {
            return RestoreMatchResult.Corrupted("Log non valido o illeggibile: ${e.message}")
        }

        return when (val result = replay(parsedLog)) {
            is ReplayResult.Ok -> {
                val level = try {
                    DifficultyLevel.valueOf(active.levelName)
                } catch (_: IllegalArgumentException) {
                    DifficultyLevel.INTERMEDIO
                }
                RestoreMatchResult.Success(
                    state = result.state,
                    matchLog = parsedLog,
                    level = level,
                    targetScore = active.targetScore,
                )
            }
            is ReplayResult.WrongRulesVersion -> {
                RestoreMatchResult.WrongRulesVersion(
                    found = result.found,
                    expected = result.expected,
                )
            }
            is ReplayResult.IllegalMove -> {
                RestoreMatchResult.Corrupted("Mossa non consentita all'indice ${result.index}")
            }
        }
    }

    override suspend fun clearActiveMatch() {
        dao.clearActiveMatches()
    }
}

/**
 * Implementazione in memoria per test o preview.
 */
public class InMemoryMatchRepository(
    private val clock: Clock = SystemClock,
) : MatchRepository {
    private var activeMatch: SavedMatchEntity? = null

    override suspend fun saveActiveMatch(
        log: MatchLog,
        level: DifficultyLevel,
        targetScore: Int,
        p1Score: Int,
        p2Score: Int,
    ) {
        val now = clock.nowMillis()
        activeMatch = SavedMatchEntity(
            id = "active_match",
            matchLogText = log.toText(),
            levelName = level.name,
            targetScore = targetScore,
            status = "IN_PROGRESS",
            winnerSeat = null,
            p1Score = p1Score,
            p2Score = p2Score,
            createdAt = activeMatch?.createdAt ?: now,
            updatedAt = now,
        )
    }

    override suspend fun markCompleted(id: String, winner: Seat?, p1Score: Int, p2Score: Int) {
        val current = activeMatch ?: return
        activeMatch = current.copy(
            status = "COMPLETED",
            winnerSeat = winner?.name,
            p1Score = p1Score,
            p2Score = p2Score,
            updatedAt = clock.nowMillis(),
        )
    }

    override suspend fun restoreActiveMatch(): RestoreMatchResult {
        val active = activeMatch ?: return RestoreMatchResult.None
        if (active.status != "IN_PROGRESS") return RestoreMatchResult.None
        val parsedLog = try {
            MatchLog.parse(active.matchLogText)
        } catch (e: Exception) {
            return RestoreMatchResult.Corrupted("Log non valido o illeggibile: ${e.message}")
        }

        return when (val result = replay(parsedLog)) {
            is ReplayResult.Ok -> {
                val level = try {
                    DifficultyLevel.valueOf(active.levelName)
                } catch (_: IllegalArgumentException) {
                    DifficultyLevel.INTERMEDIO
                }
                RestoreMatchResult.Success(
                    state = result.state,
                    matchLog = parsedLog,
                    level = level,
                    targetScore = active.targetScore,
                )
            }
            is ReplayResult.WrongRulesVersion -> {
                RestoreMatchResult.WrongRulesVersion(
                    found = result.found,
                    expected = result.expected,
                )
            }
            is ReplayResult.IllegalMove -> {
                RestoreMatchResult.Corrupted("Mossa non consentita all'indice ${result.index}")
            }
        }
    }

    override suspend fun clearActiveMatch() {
        activeMatch = null
    }

    public fun setActiveMatchDirectly(entity: SavedMatchEntity?) {
        activeMatch = entity
    }
}
