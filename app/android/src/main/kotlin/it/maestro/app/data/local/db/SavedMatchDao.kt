package it.maestro.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object Room per le partite salvate (ADR-0003, task F1-08).
 */
@Dao
public interface SavedMatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertOrUpdate(entity: SavedMatchEntity)

    @Query("SELECT * FROM saved_matches WHERE status = 'IN_PROGRESS' ORDER BY updatedAt DESC LIMIT 1")
    public suspend fun getActiveMatch(): SavedMatchEntity?

    @Query("SELECT * FROM saved_matches WHERE id = :id")
    public suspend fun getMatchById(id: String): SavedMatchEntity?

    @Query("SELECT * FROM saved_matches ORDER BY updatedAt DESC")
    public fun getAllMatches(): Flow<List<SavedMatchEntity>>

    @Query("DELETE FROM saved_matches WHERE id = :id")
    public suspend fun deleteMatch(id: String)

    @Query("DELETE FROM saved_matches WHERE status = 'IN_PROGRESS'")
    public suspend fun clearActiveMatches()
}
