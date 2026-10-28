package it.maestro.app.ui

import it.maestro.app.domain.cosmetics.CosmeticCatalog
import it.maestro.app.domain.cosmetics.CosmeticCategory
import it.maestro.app.data.local.datastore.AppSettings
import it.maestro.app.data.local.datastore.InMemorySettingsRepository
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.domain.wallet.TransactionReason
import it.maestro.app.designsystem.audio.NoOpSoundManager
import it.maestro.app.designsystem.audio.SoundEffect
import it.maestro.app.feature.shop.presentation.ShopEvent
import it.maestro.app.feature.shop.presentation.ShopViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Suite di test per [ShopViewModel] (task F2-11):
 * - Navigazione categorie cosmetiche (Tavoli, Dorsi, Avatar).
 * - Equipaggiamento immediato degli elementi già sbloccati.
 * - Sblocco con addebito sul wallet contabile (append-only) e auto-equipaggiamento.
 * - Rifiuto acquisto e saldo intatto in caso di crediti insufficienti.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ShopViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialUiState_loadsTablesAndDefaultUnlockedItems() = runTest {
        val settingsRepo = InMemorySettingsRepository()
        val walletRepo = InMemoryWalletRepository()
        walletRepo.ensureWelcomeBonus(50)

        val viewModel = ShopViewModel(settingsRepo, walletRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(CosmeticCategory.TABLE_FELT, state.selectedCategory)
        assertEquals(50, state.creditsBalance)
        assertEquals(CosmeticCatalog.TABLES.size, state.items.size)

        // Il tavolo predefinito è verde, sbloccato ed equipaggiato
        val classicGreen = state.items.first { it.item.id == "felt_classic_green" }
        assertTrue(classicGreen.isUnlocked)
        assertTrue(classicGreen.isEquipped)

        // Il tavolo rosso è bloccato ma acquistabile con 50 crediti (costa 40)
        val royalRed = state.items.first { it.item.id == "felt_royal_red" }
        assertFalse(royalRed.isUnlocked)
        assertFalse(royalRed.isEquipped)
        assertTrue(royalRed.canAfford)
    }

    @Test
    fun selectCategory_updatesDisplayedItems() = runTest {
        val settingsRepo = InMemorySettingsRepository()
        val walletRepo = InMemoryWalletRepository()

        val viewModel = ShopViewModel(settingsRepo, walletRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.selectCategory(CosmeticCategory.CARD_BACK)
        advanceUntilIdle()
        assertEquals(CosmeticCategory.CARD_BACK, viewModel.uiState.value.selectedCategory)
        assertEquals(CosmeticCatalog.CARD_BACKS.size, viewModel.uiState.value.items.size)

        viewModel.selectCategory(CosmeticCategory.AVATAR)
        advanceUntilIdle()
        assertEquals(CosmeticCategory.AVATAR, viewModel.uiState.value.selectedCategory)
        assertEquals(CosmeticCatalog.AVATARS.size, viewModel.uiState.value.items.size)
    }

    @Test
    fun equipUnlockedItem_updatesSettingsImmediately() = runTest {
        val settingsRepo = InMemorySettingsRepository(
            initialSettings = AppSettings(
                unlockedCosmetics = setOf("felt_classic_green", "felt_royal_red"),
            ),
        )
        val walletRepo = InMemoryWalletRepository()

        val viewModel = ShopViewModel(settingsRepo, walletRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        val redItem = CosmeticCatalog.TABLES.first { it.id == "felt_royal_red" }
        viewModel.equipCosmetic(redItem)
        advanceUntilIdle()

        val updatedGreen = viewModel.uiState.value.items.first { it.item.id == "felt_classic_green" }
        val updatedRed = viewModel.uiState.value.items.first { it.item.id == "felt_royal_red" }

        assertFalse(updatedGreen.isEquipped)
        assertTrue(updatedRed.isEquipped)
    }

    @Test
    fun unlockAndEquip_withSufficientCredits_deductsWalletAndEquipsItem() = runTest {
        val settingsRepo = InMemorySettingsRepository()
        val walletRepo = InMemoryWalletRepository()
        walletRepo.addCredits(50, TransactionReason.WELCOME_BONUS)

        val viewModel = ShopViewModel(settingsRepo, walletRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        val redItem = CosmeticCatalog.TABLES.first { it.id == "felt_royal_red" } // costa 40 crediti
        viewModel.unlockAndEquip(redItem)
        advanceUntilIdle()

        // 50 - 40 = 10 crediti residui
        assertEquals(10, walletRepo.getBalance())
        assertEquals(10, viewModel.uiState.value.creditsBalance)
        assertEquals(ShopEvent.Unlocked(redItem), viewModel.uiState.value.event)

        val updatedRed = viewModel.uiState.value.items.first { it.item.id == "felt_royal_red" }
        assertTrue(updatedRed.isUnlocked)
        assertTrue(updatedRed.isEquipped)
    }

    @Test
    fun unlockAndEquip_withInsufficientCredits_rejectsAndPreservesBalance() = runTest {
        val settingsRepo = InMemorySettingsRepository()
        val walletRepo = InMemoryWalletRepository()
        walletRepo.addCredits(20, TransactionReason.WELCOME_BONUS)

        val viewModel = ShopViewModel(settingsRepo, walletRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        val redItem = CosmeticCatalog.TABLES.first { it.id == "felt_royal_red" } // costa 40 crediti (> 20)
        viewModel.unlockAndEquip(redItem)
        advanceUntilIdle()

        // Saldo intatto a 20 e item ancora bloccato
        assertEquals(20, walletRepo.getBalance())
        assertEquals(20, viewModel.uiState.value.creditsBalance)
        assertEquals(ShopEvent.NotEnoughCredits(missing = 20), viewModel.uiState.value.event)

        val updatedRed = viewModel.uiState.value.items.first { it.item.id == "felt_royal_red" }
        assertFalse(updatedRed.isUnlocked)
        assertFalse(updatedRed.isEquipped)
    }

    @Test
    fun openPreview_showsItemWithEquippedOthers_andCloses() = runTest {
        val viewModel = ShopViewModel(InMemorySettingsRepository(), InMemoryWalletRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        val red = CosmeticCatalog.TABLES.first { it.id == "felt_royal_red" }
        viewModel.openPreview(red)
        advanceUntilIdle()
        val open = viewModel.uiState.value
        assertEquals("felt_royal_red", open.previewCard?.item?.id)
        assertEquals("felt_classic_green", open.equippedTableId)

        viewModel.selectCategory(CosmeticCategory.CARD_BACK)
        advanceUntilIdle()
        assertEquals(null, viewModel.uiState.value.previewCard)

        viewModel.openPreview(CosmeticCatalog.CARD_BACKS.last())
        advanceUntilIdle()
        viewModel.closePreview()
        advanceUntilIdle()
        assertEquals(null, viewModel.uiState.value.previewCard)
    }

    @Test
    fun unlockAndEquip_tappedTwice_chargesOnlyOnce_andPlaysSoundOnce() = runTest {
        val walletRepo = InMemoryWalletRepository()
        walletRepo.addCredits(100, TransactionReason.WELCOME_BONUS)
        val sound = NoOpSoundManager()
        val viewModel = ShopViewModel(InMemorySettingsRepository(), walletRepo, sound)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        val red = CosmeticCatalog.TABLES.first { it.id == "felt_royal_red" }
        viewModel.unlockAndEquip(red)
        viewModel.unlockAndEquip(red)
        advanceUntilIdle()

        assertEquals(60, walletRepo.getBalance())
        assertEquals(listOf(SoundEffect.CHECK_CORRECT), sound.playedEffects)
    }

    @Test
    fun consumeEvent_clearsTheOutcome() = runTest {
        val walletRepo = InMemoryWalletRepository()
        walletRepo.addCredits(100, TransactionReason.WELCOME_BONUS)
        val viewModel = ShopViewModel(InMemorySettingsRepository(), walletRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.unlockAndEquip(CosmeticCatalog.TABLES.first { it.id == "felt_royal_red" })
        advanceUntilIdle()
        viewModel.consumeEvent()
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.event)
    }
}
