package it.maestro.app.domain.match

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.game.training.TrainingFundamental
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog

/**
 * Risultato del ripristino di una partita interrotta (ADR-0003, task F1-08).
 */
public sealed interface RestoreMatchResult {
    public data class Success(
        val session: MatchSession,
        val matchLog: MatchLog,
        val level: DifficultyLevel,
        val targetScore: Int,
        /** Obiettivo dello Sparring con cui è iniziata la partita; `NONE` per la Classica. */
        val trainingFundamental: TrainingFundamental = TrainingFundamental.NONE,
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
        trainingFundamental: TrainingFundamental = TrainingFundamental.NONE,
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
