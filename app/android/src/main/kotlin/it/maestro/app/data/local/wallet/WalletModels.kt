package it.maestro.app.data.local.wallet

import java.util.UUID
import it.maestro.app.domain.time.SystemClock

/**
 * Causali tipizzate per i movimenti nel registro contabile dei Crediti Maestro (ADR-0006, task F2-07).
 */
public enum class TransactionReason {
    /** Bonus di benvenuto all'apertura del gioco. */
    WELCOME_BONUS,

    /** Bonus fedeltà per accesso consecutivo giornaliero. */
    DAILY_LOGIN_BONUS,

    /** Ricompensa per aver risolto la Sfida del Giorno (puzzle). */
    DAILY_PUZZLE_REWARD,

    /** Ricompensa per il completamento di una missione didattica. */
    DAILY_MISSION_REWARD,

    /** Ricompensa per aver superato una lezione dell'Accademia. */
    ACADEMY_LESSON_REWARD,

    /** Ricompensa per la vittoria di una partita contro il bot. */
    MATCH_VICTORY_REWARD,

    /** Addebito per richiesta di suggerimento mossa al Maestro durante la partita. */
    HINT_PURCHASE,

    /** Addebito per la smazzata approfondita della Game Review. */
    GAME_REVIEW_PURCHASE,

    /** Addebito per riprovare un puzzle fallito. */
    PUZZLE_RETRY,

    /** Acquisto di cosmetici (tavoli, dorsi di carte). */
    COSMETIC_PURCHASE,

    /** Crediti gratuiti ottenuti guardando un annuncio Rewarded. */
    REWARDED_AD,

    /** Pacchetto di crediti acquistato tramite Google Play Billing. */
    IAP_PACK,
}

/**
 * Singola transazione immutabile nel wallet (registro append-only, ADR-0006).
 *
 * @property id Identificativo univoco della transazione (UUID).
 * @property amount Importo con segno (positivo per accredito, negativo per addebito).
 * @property reason Causale della transazione.
 * @property timestamp Millisecondi epoch del momento della transazione.
 * @property referenceId Riferimento facoltativo per garantire idempotenza (es. id missione, data puzzle).
 * @property description Nota testuale o dettaglio aggiuntivo.
 */
public data class WalletTransaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Int,
    val reason: TransactionReason,
    val timestamp: Long = SystemClock.nowMillis(),
    val referenceId: String? = null,
    val description: String? = null,
) {
    init {
        require(amount != 0) { "L'importo di una transazione non può essere zero." }
    }

    public val isCredit: Boolean get() = amount > 0
    public val isDebit: Boolean get() = amount < 0
}

/**
 * Esito di un'operazione sul wallet contabile (ADR-0006, task F2-07).
 */
public sealed interface WalletOperationResult {
    /**
     * Operazione completata con successo.
     *
     * @property transaction La transazione registrata nel ledger.
     * @property newBalance Il saldo risultante dopo la transazione.
     */
    public data class Success(
        val transaction: WalletTransaction,
        val newBalance: Int,
    ) : WalletOperationResult

    /**
     * Operazione di addebito respinta per fondi insufficienti.
     * Garantisce che il saldo del wallet non diventi mai negativo.
     *
     * @property currentBalance Il saldo attuale prima dell'operazione.
     * @property requiredAmount L'importo richiesto per l'addebito.
     */
    public data class InsufficientFunds(
        val currentBalance: Int,
        val requiredAmount: Int,
    ) : WalletOperationResult

    /**
     * Operazione respinta perché la ricompensa per il riferimento specificato è già stata riscossa.
     */
    public data class AlreadyClaimed(
        val reason: TransactionReason,
        val referenceId: String,
    ) : WalletOperationResult
}
