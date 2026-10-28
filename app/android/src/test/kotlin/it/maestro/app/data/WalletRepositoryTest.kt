package it.maestro.app.data

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import it.maestro.app.data.local.db.MaestroDatabase
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.data.local.wallet.RoomWalletRepository
import it.maestro.app.domain.wallet.TransactionReason
import it.maestro.app.domain.wallet.WalletOperationResult
import it.maestro.app.domain.wallet.WalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Suite di test per il wallet contabile dei Crediti Maestro (ADR-0006, task F2-07).
 * Verifica sia l'implementazione Room che l'implementazione InMemory:
 * - Calcolo saldo append-only (balance = sum(amount)).
 * - Impossibilità di andare in saldo negativo su addebito.
 * - Idempotenza delle ricompense con referenceId.
 * - Erogazione singola del bonus di benvenuto.
 * - Reattività del flusso observeBalance().
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class WalletRepositoryTest {

    private lateinit var database: MaestroDatabase
    private lateinit var roomRepository: RoomWalletRepository
    private lateinit var inMemoryRepository: InMemoryWalletRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MaestroDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        roomRepository = RoomWalletRepository(database.walletDao(), Dispatchers.Unconfined)
        inMemoryRepository = InMemoryWalletRepository()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun room_initialBalanceIsZero() = runBlocking {
        assertEquals(0, roomRepository.getBalance())
        assertEquals(0, roomRepository.observeBalance().first())
    }

    @Test
    fun room_welcomeBonusGrantedOnlyOnce() = runBlocking {
        val grantedFirst = roomRepository.ensureWelcomeBonus(bonusAmount = 50)
        assertTrue(grantedFirst, "Il bonus di benvenuto deve essere erogato alla prima apertura")
        assertEquals(50, roomRepository.getBalance())

        val grantedSecond = roomRepository.ensureWelcomeBonus(bonusAmount = 50)
        assertFalse(grantedSecond, "Il bonus di benvenuto non deve essere duplicato se il wallet ha già transazioni")
        assertEquals(50, roomRepository.getBalance())
    }

    @Test
    fun room_addCreditsIncreasesBalance() = runBlocking {
        val result = roomRepository.addCredits(
            amount = 15,
            reason = TransactionReason.DAILY_PUZZLE_REWARD,
            referenceId = "puzzle_2026-10-26",
            description = "Sfida del giorno completata",
        )
        assertIs<WalletOperationResult.Success>(result)
        assertEquals(15, result.newBalance)
        assertEquals(15, roomRepository.getBalance())
        assertEquals(15, roomRepository.observeBalance().first())
    }

    @Test
    fun room_idempotentRewardPreventsDuplication() = runBlocking {
        val firstResult = roomRepository.addCredits(
            amount = 10,
            reason = TransactionReason.DAILY_MISSION_REWARD,
            referenceId = "mission_sweeps_1",
        )
        assertIs<WalletOperationResult.Success>(firstResult)
        assertEquals(10, roomRepository.getBalance())
        assertTrue(roomRepository.hasClaimed(TransactionReason.DAILY_MISSION_REWARD, "mission_sweeps_1"))

        // Secondo tentativo con stesso referenceId
        val secondResult = roomRepository.addCredits(
            amount = 10,
            reason = TransactionReason.DAILY_MISSION_REWARD,
            referenceId = "mission_sweeps_1",
        )
        assertIs<WalletOperationResult.AlreadyClaimed>(secondResult)
        assertEquals(10, roomRepository.getBalance(), "Il saldo non deve cambiare su ricompensa duplicata")
    }

    @Test
    fun room_spendCreditsWithSameReferenceChargesOnlyOnce() = runBlocking {
        roomRepository.addCredits(amount = 100, reason = TransactionReason.WELCOME_BONUS)

        val first = roomRepository.spendCredits(40, TransactionReason.COSMETIC_PURCHASE, referenceId = "felt_royal_red")
        val second = roomRepository.spendCredits(40, TransactionReason.COSMETIC_PURCHASE, referenceId = "felt_royal_red")
        val other = roomRepository.spendCredits(10, TransactionReason.COSMETIC_PURCHASE, referenceId = "back_vintage_red")

        assertIs<WalletOperationResult.Success>(first)
        assertIs<WalletOperationResult.AlreadyClaimed>(second)
        assertIs<WalletOperationResult.Success>(other)
        assertEquals(50, roomRepository.getBalance(), "Il secondo addebito con lo stesso riferimento non scala il saldo")
    }

    @Test
    fun room_spendCreditsWithSufficientFundsSucceeds() = runBlocking {
        roomRepository.addCredits(amount = 50, reason = TransactionReason.WELCOME_BONUS)
        assertEquals(50, roomRepository.getBalance())

        val spendResult = roomRepository.spendCredits(
            amount = 20,
            reason = TransactionReason.HINT_PURCHASE,
            description = "Suggerimento tattico Maestro",
        )
        assertIs<WalletOperationResult.Success>(spendResult)
        assertEquals(30, spendResult.newBalance)
        assertEquals(30, roomRepository.getBalance())
    }

    @Test
    fun room_spendCreditsWithInsufficientFundsFailsAndNeverGoesNegative() = runBlocking {
        roomRepository.addCredits(amount = 25, reason = TransactionReason.WELCOME_BONUS)
        assertEquals(25, roomRepository.getBalance())

        // Tentativo di addebito maggiore del saldo disponibile
        val spendResult = roomRepository.spendCredits(
            amount = 30,
            reason = TransactionReason.GAME_REVIEW_PURCHASE,
            description = "Game Review approfondita",
        )
        assertIs<WalletOperationResult.InsufficientFunds>(spendResult)
        assertEquals(25, spendResult.currentBalance)
        assertEquals(30, spendResult.requiredAmount)

        // Il saldo deve rimanere rigorosamente intatto a 25 (MAI negativo)
        assertEquals(25, roomRepository.getBalance())
        assertEquals(25, roomRepository.observeBalance().first())
    }

    @Test
    fun room_transactionsHistoryPreservesChronology() = runBlocking {
        roomRepository.addCredits(10, TransactionReason.DAILY_MISSION_REWARD, referenceId = "m1")
        roomRepository.addCredits(20, TransactionReason.DAILY_PUZZLE_REWARD, referenceId = "p1")
        roomRepository.spendCredits(5, TransactionReason.HINT_PURCHASE)

        val txs = roomRepository.observeTransactions().first()
        assertEquals(3, txs.size)
        // La somma dei movimenti deve equivalere esattamente al saldo (append-only ledger)
        assertEquals(25, txs.sumOf { it.amount })
        assertEquals(25, roomRepository.getBalance())
    }

    @Test
    fun inMemory_repositoryBehavesIdentically() = runBlocking {
        testWalletContract(inMemoryRepository)
    }

    private suspend fun testWalletContract(repository: WalletRepository) {
        assertEquals(0, repository.getBalance())
        assertTrue(repository.ensureWelcomeBonus(50))
        assertEquals(50, repository.getBalance())
        assertFalse(repository.ensureWelcomeBonus(50))

        val addResult = repository.addCredits(25, TransactionReason.MATCH_VICTORY_REWARD, referenceId = "match_123")
        assertIs<WalletOperationResult.Success>(addResult)
        assertEquals(75, repository.getBalance())

        val duplicate = repository.addCredits(25, TransactionReason.MATCH_VICTORY_REWARD, referenceId = "match_123")
        assertIs<WalletOperationResult.AlreadyClaimed>(duplicate)
        assertEquals(75, repository.getBalance())

        val spendOk = repository.spendCredits(35, TransactionReason.COSMETIC_PURCHASE)
        assertIs<WalletOperationResult.Success>(spendOk)
        assertEquals(40, repository.getBalance())

        val chargedTwice = listOf(
            repository.spendCredits(5, TransactionReason.HINT_PURCHASE, referenceId = "hint_1"),
            repository.spendCredits(5, TransactionReason.HINT_PURCHASE, referenceId = "hint_1"),
        )
        assertIs<WalletOperationResult.Success>(chargedTwice[0])
        assertIs<WalletOperationResult.AlreadyClaimed>(chargedTwice[1])
        assertEquals(35, repository.getBalance())

        val spendTooMuch = repository.spendCredits(100, TransactionReason.COSMETIC_PURCHASE)
        assertIs<WalletOperationResult.InsufficientFunds>(spendTooMuch)
        assertEquals(35, repository.getBalance())
    }
}
