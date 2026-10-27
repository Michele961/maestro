package it.maestro.app.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migrazioni esplicite del database (ADR-0017, task F1R-03): mai cancellare dati a un cambio di schema.
 *
 * Ogni nuova versione aggiunge qui la sua [Migration] e un test in `MaestroMigrationsTest`.
 */
public object MaestroMigrations {

    /**
     * 2 → 3: nessun cambio di tabelle. È la prima migrazione esplicita: da questa versione lo schema
     * è esportato in `app/android/schemas/` e la cancellazione distruttiva è vietata.
     */
    public val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) = Unit
    }

    /** Tutte le migrazioni, in ordine di versione. */
    public val ALL: Array<Migration> = arrayOf(MIGRATION_2_3)
}
