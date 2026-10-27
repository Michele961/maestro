package it.maestro.app.feature.shop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.data.local.cosmetics.CosmeticCatalog
import it.maestro.app.data.local.cosmetics.CosmeticCategory
import it.maestro.app.data.local.cosmetics.CosmeticItem
import it.maestro.app.data.local.datastore.SettingsRepository
import it.maestro.app.data.local.wallet.TransactionReason
import it.maestro.app.data.local.wallet.WalletOperationResult
import it.maestro.app.data.local.wallet.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

/**
 * Stato della UI della Bottega del Maestro (task F2-11).
 */
public data class ShopUiState(
    val selectedCategory: CosmeticCategory = CosmeticCategory.TABLE_FELT,
    val items: List<ShopItemCardState> = emptyList(),
    val creditsBalance: Int = 0,
    val feedbackMessage: String? = null,
)

/**
 * ViewModel che governa la consultazione, sblocco ed equipaggiamento dei cosmetici (task F2-11).
 */
public class ShopViewModel(
    private val settingsRepository: SettingsRepository,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val categoryState = MutableStateFlow(CosmeticCategory.TABLE_FELT)
    private val feedbackState = MutableStateFlow<String?>(null)

    public val uiState: StateFlow<ShopUiState> = combine(
        categoryState,
        settingsRepository.settingsFlow,
        walletRepository.observeBalance(),
        feedbackState,
    ) { category, settings, balance, feedback ->
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
            feedbackMessage = feedback,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShopUiState(),
    )

    public fun selectCategory(category: CosmeticCategory) {
        categoryState.value = category
    }

    public fun clearFeedback() {
        feedbackState.value = null
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
        viewModelScope.launch {
            val result = walletRepository.spendCredits(
                amount = item.priceCredits,
                reason = TransactionReason.COSMETIC_PURCHASE,
                referenceId = item.id,
                description = "Sblocco cosmetico: ${item.id}",
            )
            when (result) {
                is WalletOperationResult.Success -> {
                    settingsRepository.unlockCosmetic(item.id)
                    equipCosmetic(item)
                    feedbackState.value = item.id
                }
                is WalletOperationResult.InsufficientFunds -> {
                    feedbackState.value = "insufficient_funds"
                }
                is WalletOperationResult.AlreadyClaimed -> {
                    equipCosmetic(item)
                }
            }
        }
    }
}
