package it.maestro.app.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entità Room per la persistenza delle partite di Scopa (ADR-0003, task F1-08).
 * Memorizza solo il [it.maestro.game.scopa.MatchLog] serializzato come testo canonico e i metadati associati.
 */
@Entity(tableName = "saved_matches")
public data class SavedMatchEntity(
    @PrimaryKey
    val id: String,
    val matchLogText: String,
    val levelName: String,
    val targetScore: Int,
    val status: String,
    val winnerSeat: String?,
    val p1Score: Int,
    val p2Score: Int,
    val createdAt: Long,
    val updatedAt: Long,
    /** Obiettivo dello Sparring con cui è iniziata la partita; `NONE` = Classica (migrazione 3 → 4). */
    @ColumnInfo(defaultValue = "NONE")
    val trainingFundamental: String = "NONE",
)
