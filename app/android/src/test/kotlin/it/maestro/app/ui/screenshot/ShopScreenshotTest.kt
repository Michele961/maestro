package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.app.data.local.cosmetics.CosmeticCategory
import it.maestro.app.data.local.cosmetics.CosmeticItem
import it.maestro.app.ui.screens.ShopItemCardState
import it.maestro.app.ui.screens.ShopScreenContent
import it.maestro.app.ui.screens.ShopUiState
import it.maestro.app.ui.theme.MaestroTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot e interaction test per "La Bottega del Maestro" (F2-11).
 * Verifica visuale dell'assenza di grigio di sistema, fedeltà dello sfondo noce e rendering degli elementi.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "it-w411dp-h891dp-xxhdpi", application = Application::class)
class ShopScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun sampleStateForCategory(
        category: CosmeticCategory,
        credits: Int = 120,
    ): ShopUiState {
        val items = when (category) {
            CosmeticCategory.TABLE_FELT -> listOf(
                ShopItemCardState(
                    item = it.maestro.app.data.local.cosmetics.CosmeticCatalog.TABLES[0],
                    isUnlocked = true,
                    isEquipped = true,
                    canAfford = true,
                ),
                ShopItemCardState(
                    item = it.maestro.app.data.local.cosmetics.CosmeticCatalog.TABLES[1],
                    isUnlocked = true,
                    isEquipped = false,
                    canAfford = true,
                ),
                ShopItemCardState(
                    item = it.maestro.app.data.local.cosmetics.CosmeticCatalog.TABLES[2],
                    isUnlocked = false,
                    isEquipped = false,
                    canAfford = true,
                ),
                ShopItemCardState(
                    item = it.maestro.app.data.local.cosmetics.CosmeticCatalog.TABLES[3],
                    isUnlocked = false,
                    isEquipped = false,
                    canAfford = true,
                ),
            )
            CosmeticCategory.CARD_BACK -> listOf(
                ShopItemCardState(
                    item = it.maestro.app.data.local.cosmetics.CosmeticCatalog.CARD_BACKS[0],
                    isUnlocked = true,
                    isEquipped = true,
                    canAfford = true,
                ),
                ShopItemCardState(
                    item = it.maestro.app.data.local.cosmetics.CosmeticCatalog.CARD_BACKS[1],
                    isUnlocked = false,
                    isEquipped = false,
                    canAfford = true,
                ),
                ShopItemCardState(
                    item = it.maestro.app.data.local.cosmetics.CosmeticCatalog.CARD_BACKS[2],
                    isUnlocked = false,
                    isEquipped = false,
                    canAfford = true,
                ),
            )
            CosmeticCategory.AVATAR -> listOf(
                ShopItemCardState(
                    item = it.maestro.app.data.local.cosmetics.CosmeticCatalog.AVATARS[0],
                    isUnlocked = true,
                    isEquipped = true,
                    canAfford = true,
                ),
                ShopItemCardState(
                    item = it.maestro.app.data.local.cosmetics.CosmeticCatalog.AVATARS[1],
                    isUnlocked = true,
                    isEquipped = false,
                    canAfford = true,
                ),
                ShopItemCardState(
                    item = it.maestro.app.data.local.cosmetics.CosmeticCatalog.AVATARS[2],
                    isUnlocked = false,
                    isEquipped = false,
                    canAfford = true,
                ),
            )
        }
        return ShopUiState(
            selectedCategory = category,
            items = items,
            creditsBalance = credits,
        )
    }

    @Test
    fun shop_screen_tables_screenshot() {
        val state = sampleStateForCategory(CosmeticCategory.TABLE_FELT)
        composeRule.setContent {
            MaestroTheme {
                ShopScreenContent(
                    uiState = state,
                    onSelectCategory = {},
                    onEquipItem = {},
                    onUnlockItem = {},
                    onNavigateHome = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/shop_screen_tables.png")
    }

    @Test
    fun shop_screen_card_backs_screenshot() {
        val state = sampleStateForCategory(CosmeticCategory.CARD_BACK)
        composeRule.setContent {
            MaestroTheme {
                ShopScreenContent(
                    uiState = state,
                    onSelectCategory = {},
                    onEquipItem = {},
                    onUnlockItem = {},
                    onNavigateHome = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/shop_screen_card_backs.png")
    }

    @Test
    fun shop_screen_avatars_screenshot() {
        val state = sampleStateForCategory(CosmeticCategory.AVATAR)
        composeRule.setContent {
            MaestroTheme {
                ShopScreenContent(
                    uiState = state,
                    onSelectCategory = {},
                    onEquipItem = {},
                    onUnlockItem = {},
                    onNavigateHome = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/shop_screen_avatars.png")
    }

    @Test
    fun category_tabs_switch_callback() {
        var selectedCategory: CosmeticCategory? = null
        val state = sampleStateForCategory(CosmeticCategory.TABLE_FELT)
        composeRule.setContent {
            MaestroTheme {
                ShopScreenContent(
                    uiState = state,
                    onSelectCategory = { selectedCategory = it },
                    onEquipItem = {},
                    onUnlockItem = {},
                    onNavigateHome = {},
                )
            }
        }

        composeRule.onNodeWithTag("shop_tab_card_backs").performClick()
        assertEquals(CosmeticCategory.CARD_BACK, selectedCategory)

        composeRule.onNodeWithTag("shop_tab_avatars").performClick()
        assertEquals(CosmeticCategory.AVATAR, selectedCategory)
    }

    @Test
    fun equip_and_unlock_buttons_trigger_callbacks() {
        var equippedItem: CosmeticItem? = null
        var unlockedItem: CosmeticItem? = null
        val state = sampleStateForCategory(CosmeticCategory.TABLE_FELT)
        composeRule.setContent {
            MaestroTheme {
                ShopScreenContent(
                    uiState = state,
                    onSelectCategory = {},
                    onEquipItem = { equippedItem = it },
                    onUnlockItem = { unlockedItem = it },
                    onNavigateHome = {},
                )
            }
        }

        // The second table is unlocked but not equipped: can click "Equip"
        composeRule.onNodeWithTag("shop_equip_button_felt_royal_red").performClick()
        assertEquals("felt_royal_red", equippedItem?.id)

        // The third table is locked: can click unlock button
        composeRule.onNodeWithTag("shop_unlock_button_felt_noble_blue").performClick()
        assertEquals("felt_noble_blue", unlockedItem?.id)
    }

    @Test
    fun close_button_navigates_home() {
        var closed = false
        val state = sampleStateForCategory(CosmeticCategory.TABLE_FELT)
        composeRule.setContent {
            MaestroTheme {
                ShopScreenContent(
                    uiState = state,
                    onSelectCategory = {},
                    onEquipItem = {},
                    onUnlockItem = {},
                    onNavigateHome = { closed = true },
                )
            }
        }

        composeRule.onNodeWithTag("shop_back_button").performClick()
        assertTrue(closed)
    }
}
