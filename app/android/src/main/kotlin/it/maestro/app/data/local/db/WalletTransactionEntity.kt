package it.maestro.app.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import it.maestro.app.data.local.wallet.TransactionReason
import it.maestro.app.data.local.wallet.WalletTransaction

/**
 * Entità Room per il registro contabile append-only del wallet Crediti Maestro (ADR-0006, task F2-07).
 * Ogni movimento ha id univoco, causale, timestamp e importo con segno.
 */
@Entity(
    tableName = "wallet_transactions",
    indices = [
        Index(value = ["reason", "referenceId"]),
        Index(value = ["timestamp"]),
    ],
)
public data class WalletTransactionEntity(
    @PrimaryKey
    val id: String,
    val amount: Int,
    val reason: String,
    val timestamp: Long,
    val referenceId: String?,
    val description: String?,
) {
    public fun toDomain(): WalletTransaction = WalletTransaction(
        id = id,
        amount = amount,
        reason = TransactionReason.valueOf(reason),
        timestamp = timestamp,
        referenceId = referenceId,
        description = description,
    )

    public companion object {
        public fun fromDomain(domain: WalletTransaction): WalletTransactionEntity = WalletTransactionEntity(
            id = domain.id,
            amount = domain.amount,
            reason = domain.reason.name,
            timestamp = domain.timestamp,
            referenceId = domain.referenceId,
            description = domain.description,
        )
    }
}
