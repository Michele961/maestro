package it.maestro.app.feature.shop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.domain.cosmetics.CosmeticCatalog
import it.maestro.app.domain.cosmetics.CosmeticCategory
import it.maestro.app.domain.cosmetics.CosmeticItem
import it.maestro.app.domain.settings.SettingsRepository
import it.maestro.app.designsystem.audio.SoundEffect
import it.maestro.app.designsystem.audio.SoundManager
import it.maestro.app.domain.settings.GameSpeed
import it.maestro.app.domain.wallet.TransactionReason
import it.maestro.app.domain.wallet.WalletOperationResult
import it.maestro.app.domain.wallet.WalletRepository
import it.maestro.app.domain.wallet.WalletTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Stato di presentazione di un singolo elemento cosmetico nello Shop.
 */
public data class ShopItemCardState(
    val item: CosmeticItem,
    val isUnlocked: Boolean,
    val isEquipped: Boolean,
    val canAfford: Boolean,
)

/** Esito di un acquisto da mostrare una volta (task F1R-30c); il ViewModel non produce testi. */
public sealed interface ShopEvent {
    /** [item] sbloccato ed equipaggiato. */
    public data class Unlocked(val item: CosmeticItem) : ShopEvent

    /** Mancano [missing] crediti per l'acquisto. */
    public data class NotEnoughCredits(val missing: Int) : ShopEvent
}

/**
 * Stato della UI della Bottega del Maestro (task F2-11).
 */
public data class ShopUiState(
    val selectedCategory: CosmeticCategory = CosmeticCategory.TABLE_FELT,
    val items: List<ShopItemCardState> = emptyList(),
    val creditsBalance: Int = 0,
    val event: ShopEvent? = null,
    /** Ultimi movimenti del wallet, dal più recente (F1R-30d). */
    val movements: List<WalletTransaction> = emptyList(),
    val gameSpeed: GameSpeed = GameSpeed.NORMAL,
    /** Oggetto aperto nel riquadro «Provalo» (F1R-30b); `null` se il riquadro è chiuso. */
    val previewCard: ShopItemCardState? = null,
    val equippedTableId: String = CosmeticCatalog.TABLES.first().id,
    val equippedCardBackId: String = CosmeticCatalog.CARD_BACKS.first().id,
    val equippedAvatarId: String = CosmeticCatalog.AVATARS.first().id,
)

/**
 * ViewModel che governa la consultazione, sblocco ed equipaggiamento dei cosmetici (task F2-11).
 */
public class ShopViewModel(
    private val settingsRepository: SettingsRepository,
    private val walletRepository: WalletRepository,
    private val soundManager: SoundManager? = null,
) : ViewModel() {

    private val categoryState = MutableStateFlow(CosmeticCategory.TABLE_FELT)
    private val eventState = MutableStateFlow<ShopEvent?>(null)
    private val previewId = MutableStateFlow<String?>(null)
    private val purchasing = mutableSetOf<String>()

    public val uiState: StateFlow<ShopUiState> = combine(
        categoryState,
        settingsRepository.settingsFlow,
        walletRepository.observeBalance(),
        combine(eventState, previewId, walletRepository.observeTransactions()) { event, preview, transactions ->
            Triple(event, preview, transactions)
        },
    ) { category, settings, balance, (event, preview, transactions) ->
        val categoryItems = when (category) {
            CosmeticCategory.TABLE_FELT -> CosmeticCatalog.TABLES
            CosmeticCategory.CARD_BACK -> CosmeticCatalog.CARD_BACKS
            CosmeticCategory.AVATAR -> CosmeticCatalog.AVATARS
        }

        val cards = categoryItems.map { item ->
            val isUnlocked = item.isFree || item.id in settings.unlockedCosmetics
            val isEquipped = when (item.category) {
                CosmeticCategory.TABLE_FELT -> settings.selectedTableTheme == item.id
                CosmeticCategory.CARD_BACK -> settings.selectedCardBackTheme == item.id
                CosmeticCategory.AVATAR -> settings.selectedAvatar == item.id
            }
            ShopItemCardState(
                item = item,
                isUnlocked = isUnlocked,
                isEquipped = isEquipped,
                canAfford = balance >= item.priceCredits,
            )
        }

        ShopUiState(
            selectedCategory = category,
            items = cards,
            creditsBalance = balance,
            event = event,
            movements = transactions.sortedByDescending { it.timestamp }.take(MAX_MOVEMENTS),
            gameSpeed = settings.gameSpeed,
            previewCard = cards.firstOrNull { it.item.id == preview },
            equippedTableId = settings.selectedTableTheme,
            equippedCardBackId = settings.selectedCardBackTheme,
            equippedAvatarId = settings.selectedAvatar,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShopUiState(),
    )

    public fun selectCategory(category: CosmeticCategory) {
        categoryState.value = category
        previewId.value = null
    }

    /** Apre il riquadro «Provalo» sull'oggetto (F1R-30b). */
    public fun openPreview(item: CosmeticItem) {
        previewId.value = item.id
    }

    public fun closePreview() {
        previewId.value = null
    }

    /** L'esito è stato mostrato: lo toglie dallo stato. */
    public fun consumeEvent() {
        eventState.value = null
    }

    public fun equipCosmetic(item: CosmeticItem) {
        viewModelScope.launch {
            when (item.category) {
                CosmeticCategory.TABLE_FELT -> settingsRepository.updateSelectedTableTheme(item.id)
                CosmeticCategory.CARD_BACK -> settingsRepository.updateSelectedCardBackTheme(item.id)
                CosmeticCategory.AVATAR -> settingsRepository.updateSelectedAvatar(item.id)
            }
        }
    }

    public fun unlockAndEquip(item: CosmeticItem) {
        // Un secondo tocco mentre l'acquisto è in corso non deve addebitare due volte (F1R-30c).
        if (!purchasing.add(item.id)) return
        viewModelScope.launch {
            try {
                purchase(item)
            } finally {
                purchasing.remove(item.id)
            }
        }
    }

    private suspend fun purchase(item: CosmeticItem) {
        if (item.isFree || item.id in settingsRepository.settingsFlow.first().unlockedCosmetics) {
            equipCosmetic(item)
            return
        }
        val result = walletRepository.spendCredits(
            amount = item.priceCredits,
            reason = TransactionReason.COSMETIC_PURCHASE,
            referenceId = item.id,
            description = "cosmetic_purchase:${item.id}",
        )
        when (result) {
            is WalletOperationResult.Success -> {
                settingsRepository.unlockCosmetic(item.id)
                equipCosmetic(item)
                eventState.value = ShopEvent.Unlocked(item)
                soundManager?.play(SoundEffect.PURCHASE)
            }
            is WalletOperationResult.InsufficientFunds -> {
                eventState.value = ShopEvent.NotEnoughCredits(item.priceCredits - walletRepository.getBalance())
            }
            is WalletOperationResult.AlreadyClaimed -> {
                equipCosmetic(item)
            }
        }
    }
}

/** Quanti movimenti mostra l'elenco della Bottega. */
private const val MAX_MOVEMENTS: Int = 50
