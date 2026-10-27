package it.maestro.app.data.local.db

import android.app.Application
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * ADR-0017, task F1R-03: aggiornare l'app conserva storico, partita in corso e wallet.
 *
 * Il database v2 è quello delle installazioni di `main` prima di F1R-03 (schema `schemas/…/2.json`).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class MaestroMigrationsTest {

    private val dbName = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MaestroDatabase::class.java,
    )

    @Test
    fun migration2To3KeepsMatchesAndWallet() {
        helper.createDatabase(dbName, 2).apply {
            execSQL(
                "INSERT INTO saved_matches (id, matchLogText, levelName, targetScore, status, winnerSeat, " +
                    "p1Score, p2Score, createdAt, updatedAt) VALUES " +
                    "('active', 'log-attivo', 'INTERMEDIO', 11, 'IN_PROGRESS', NULL, 3, 2, 100, 200), " +
                    "('done', 'log-finito', 'MAESTRO', 16, 'COMPLETED', 'P1', 16, 9, 10, 20)",
            )
            execSQL(
                "INSERT INTO wallet_transactions (id, amount, reason, timestamp, referenceId, description) VALUES " +
                    "('t1', 50, 'WELCOME_BONUS', 1, NULL, NULL), ('t2', -20, 'COSMETIC_PURCHASE', 2, 'oro', NULL)",
            )
            close()
        }

        helper.runMigrationsAndValidate(dbName, 3, true, *MaestroMigrations.ALL).close()

        val db = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext<Application>(),
            MaestroDatabase::class.java,
            dbName,
        ).addMigrations(*MaestroMigrations.ALL).allowMainThreadQueries().build()
        try {
            runBlocking {
                val active = assertNotNull(db.savedMatchDao().getActiveMatch())
                assertEquals("log-attivo", active.matchLogText)
                assertEquals("log-finito", assertNotNull(db.savedMatchDao().getMatchById("done")).matchLogText)
                assertEquals(30, db.walletDao().getBalance())
                assertEquals(2, db.walletDao().getTransactionCount())
            }
        } finally {
            db.close()
        }
    }

    @Test
    fun everyVersionAfterTheFirstExportedHasAMigration() {
        val versions = MaestroMigrations.ALL.map { it.startVersion to it.endVersion }
        assertEquals(listOf(2 to 3), versions)
        assertEquals(3, versions.last().second, "L'ultima migrazione deve arrivare alla versione del database")
    }
}
