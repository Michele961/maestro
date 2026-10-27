package it.maestro.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Database Room principale dell'applicazione Maestro (ADR-0003, task F1-08).
 */
@Database(
    entities = [
        SavedMatchEntity::class,
        WalletTransactionEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
public abstract class MaestroDatabase : RoomDatabase() {
    public abstract fun savedMatchDao(): SavedMatchDao
    public abstract fun walletDao(): WalletDao
}
