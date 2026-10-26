package it.maestro.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object per il registro contabile append-only del wallet (ADR-0006, task F2-07).
 */
@Dao
public interface WalletDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public suspend fun insert(transaction: WalletTransactionEntity)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM wallet_transactions")
    public fun observeBalance(): Flow<Int>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM wallet_transactions")
    public suspend fun getBalance(): Int

    @Query("SELECT COUNT(*) FROM wallet_transactions")
    public suspend fun getTransactionCount(): Int

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    public fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions WHERE reason = :reason ORDER BY timestamp DESC")
    public suspend fun getTransactionsByReason(reason: String): List<WalletTransactionEntity>

    @Query("SELECT COUNT(*) > 0 FROM wallet_transactions WHERE reason = :reason AND referenceId = :referenceId")
    public suspend fun hasTransactionWithReference(reason: String, referenceId: String): Boolean
}
