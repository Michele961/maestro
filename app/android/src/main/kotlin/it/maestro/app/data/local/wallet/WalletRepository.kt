package it.maestro.app.data.local.wallet

import it.maestro.app.data.local.db.WalletDao
import it.maestro.app.data.local.db.WalletTransactionEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Contratto del repository per il wallet dei Crediti Maestro (ADR-0006, task F2-07).
 * Tutte le transazioni sono persistite in un registro contabile append-only.
 * Questo contratto astrae l'implementazione locale (Room) e predispone la futura
 * migrazione/sincronizzazione autoritativa con il server di backend.
 */
public interface WalletRepository {

    /**
     * Flusso reattivo del saldo contabile corrente (`sum(amount)`).
     */
    public fun observeBalance(): Flow<Int>

    /**
     * Restituisce il saldo contabile corrente in modo sincrono/sospeso.
     */
    public suspend fun getBalance(): Int

    /**
     * Accredita un importo di crediti registrando un nuovo movimento positivo nel ledger.
     * Se [referenceId] è fornito e già presente per la stessa causale, l'accredito viene
     * respinto con [WalletOperationResult.AlreadyClaimed] per garantire l'idempotenza.
     */
    public suspend fun addCredits(
        amount: Int,
        reason: TransactionReason,
        referenceId: String? = null,
        description: String? = null,
    ): WalletOperationResult

    /**
     * Addebita un importo di crediti registrando un nuovo movimento negativo nel ledger.
     * Se il saldo contabile attuale è inferiore ad [amount], l'operazione viene respinta
     * con [WalletOperationResult.InsufficientFunds] senza alterare il saldo.
     */
    public suspend fun spendCredits(
        amount: Int,
        reason: TransactionReason,
        referenceId: String? = null,
        description: String? = null,
    ): WalletOperationResult

    /**
     * Verifica se una ricompensa è già stata riscossa per la combinazione specificata di causale e riferimento.
     */
    public suspend fun hasClaimed(reason: TransactionReason, referenceId: String): Boolean

    /**
     * Assegna il bonus di benvenuto iniziale se il wallet è completamente vuoto.
     * Restituisce `true` se il bonus è stato erogato, `false` se il wallet conteneva già transazioni.
     */
    public suspend fun ensureWelcomeBonus(bonusAmount: Int = 50): Boolean

    /**
     * Flusso reattivo dello storico completo di tutte le transazioni registrate.
     */
    public fun observeTransactions(): Flow<List<WalletTransaction>>
}

/**
 * Implementazione Room del repository contabile (ADR-0006, task F2-07).
 */
public class RoomWalletRepository(
    private val dao: WalletDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : WalletRepository {

    private val mutex = Mutex()

    override fun observeBalance(): Flow<Int> = dao.observeBalance()

    override suspend fun getBalance(): Int = withContext(ioDispatcher) {
        dao.getBalance()
    }

    override suspend fun addCredits(
        amount: Int,
        reason: TransactionReason,
        referenceId: String?,
        description: String?,
    ): WalletOperationResult = withContext(ioDispatcher) {
        require(amount > 0) { "L'importo di accredito deve essere strettamente positivo: $amount" }
        mutex.withLock {
            if (referenceId != null && dao.hasTransactionWithReference(reason.name, referenceId)) {
                return@withContext WalletOperationResult.AlreadyClaimed(reason, referenceId)
            }
            val transaction = WalletTransaction(
                amount = amount,
                reason = reason,
                referenceId = referenceId,
                description = description,
            )
            dao.insert(WalletTransactionEntity.fromDomain(transaction))
            val newBalance = dao.getBalance()
            WalletOperationResult.Success(transaction, newBalance)
        }
    }

    override suspend fun spendCredits(
        amount: Int,
        reason: TransactionReason,
        referenceId: String?,
        description: String?,
    ): WalletOperationResult = withContext(ioDispatcher) {
        require(amount > 0) { "L'importo di addebito deve essere strettamente positivo: $amount" }
        mutex.withLock {
            val currentBalance = dao.getBalance()
            if (currentBalance < amount) {
                return@withContext WalletOperationResult.InsufficientFunds(
                    currentBalance = currentBalance,
                    requiredAmount = amount,
                )
            }
            val transaction = WalletTransaction(
                amount = -amount,
                reason = reason,
                referenceId = referenceId,
                description = description,
            )
            dao.insert(WalletTransactionEntity.fromDomain(transaction))
            val newBalance = dao.getBalance()
            WalletOperationResult.Success(transaction, newBalance)
        }
    }

    override suspend fun hasClaimed(reason: TransactionReason, referenceId: String): Boolean = withContext(ioDispatcher) {
        dao.hasTransactionWithReference(reason.name, referenceId)
    }

    override suspend fun ensureWelcomeBonus(bonusAmount: Int): Boolean = withContext(ioDispatcher) {
        mutex.withLock {
            if (dao.getTransactionCount() == 0) {
                val transaction = WalletTransaction(
                    amount = bonusAmount,
                    reason = TransactionReason.WELCOME_BONUS,
                    description = "Bonus iniziale del Circolo",
                )
                dao.insert(WalletTransactionEntity.fromDomain(transaction))
                true
            } else {
                false
            }
        }
    }

    override fun observeTransactions(): Flow<List<WalletTransaction>> {
        return dao.getAllTransactions().map { list -> list.map { it.toDomain() } }
    }
}

/**
 * Implementazione in-memory per test veloci e isolati.
 */
public class InMemoryWalletRepository(
    initialTransactions: List<WalletTransaction> = emptyList(),
) : WalletRepository {

    private val mutex = Mutex()
    private val transactions = MutableStateFlow(initialTransactions)
    private val balance = MutableStateFlow(initialTransactions.sumOf { it.amount })

    override fun observeBalance(): Flow<Int> = balance.asStateFlow()

    override suspend fun getBalance(): Int = balance.value

    override suspend fun addCredits(
        amount: Int,
        reason: TransactionReason,
        referenceId: String?,
        description: String?,
    ): WalletOperationResult {
        require(amount > 0) { "L'importo di accredito deve essere positivo: $amount" }
        return mutex.withLock {
            if (referenceId != null && transactions.value.any { it.reason == reason && it.referenceId == referenceId }) {
                return@withLock WalletOperationResult.AlreadyClaimed(reason, referenceId)
            }
            val transaction = WalletTransaction(
                amount = amount,
                reason = reason,
                referenceId = referenceId,
                description = description,
            )
            val updatedList = listOf(transaction) + transactions.value
            transactions.value = updatedList
            val newBalance = updatedList.sumOf { it.amount }
            balance.value = newBalance
            WalletOperationResult.Success(transaction, newBalance)
        }
    }

    override suspend fun spendCredits(
        amount: Int,
        reason: TransactionReason,
        referenceId: String?,
        description: String?,
    ): WalletOperationResult {
        require(amount > 0) { "L'importo di addebito deve essere positivo: $amount" }
        return mutex.withLock {
            val current = balance.value
            if (current < amount) {
                return@withLock WalletOperationResult.InsufficientFunds(
                    currentBalance = current,
                    requiredAmount = amount,
                )
            }
            val transaction = WalletTransaction(
                amount = -amount,
                reason = reason,
                referenceId = referenceId,
                description = description,
            )
            val updatedList = listOf(transaction) + transactions.value
            transactions.value = updatedList
            val newBalance = updatedList.sumOf { it.amount }
            balance.value = newBalance
            WalletOperationResult.Success(transaction, newBalance)
        }
    }

    override suspend fun hasClaimed(reason: TransactionReason, referenceId: String): Boolean {
        return transactions.value.any { it.reason == reason && it.referenceId == referenceId }
    }

    override suspend fun ensureWelcomeBonus(bonusAmount: Int): Boolean {
        return mutex.withLock {
            if (transactions.value.isEmpty()) {
                val transaction = WalletTransaction(
                    amount = bonusAmount,
                    reason = TransactionReason.WELCOME_BONUS,
                    description = "Bonus iniziale del Circolo",
                )
                transactions.value = listOf(transaction)
                balance.value = bonusAmount
                true
            } else {
                false
            }
        }
    }

    override fun observeTransactions(): Flow<List<WalletTransaction>> = transactions.asStateFlow()
}
