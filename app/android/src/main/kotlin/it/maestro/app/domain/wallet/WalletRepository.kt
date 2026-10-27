package it.maestro.app.domain.wallet

import kotlinx.coroutines.flow.Flow

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
