package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.app.domain.cosmetics.CosmeticCategory
import it.maestro.app.domain.cosmetics.CosmeticItem
import it.maestro.app.domain.settings.GameSpeed
import it.maestro.app.feature.shop.presentation.ShopEvent
import it.maestro.app.feature.shop.presentation.ShopItemCardState
import it.maestro.app.feature.shop.ui.ShopScreenContent
import it.maestro.app.feature.shop.presentation.ShopUiState
import it.maestro.app.designsystem.theme.MaestroTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot e interaction test per "La Bottega del Maestro" (F2-11).
 * Verifica visuale dell'assenza di grigio di sistema, fedeltà dello sfondo noce e rendering degli elementi.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class ShopScreenshotTest(private val screen: ScreenConfig) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenConfig.parameters()
    }

    @get:Rule(order = 0)
    val screenRule = ScreenConfigRule(screen)


    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private fun sampleStateForCategory(
        category: CosmeticCategory,
        credits: Int = 120,
    ): ShopUiState {
        val items = when (category) {
            CosmeticCategory.TABLE_FELT -> listOf(
                ShopItemCardState(
                    item = it.maestro.app.domain.cosmetics.CosmeticCatalog.TABLES[0],
                    isUnlocked = true,
                    isEquipped = true,
                    canAfford = true,
                ),
                ShopItemCardState(
                    item = it.maestro.app.domain.cosmetics.CosmeticCatalog.TABLES[1],
                    isUnlocked = true,
                    isEquipped = false,
                    canAfford = true,
                ),
                ShopItemCardState(
                    item = it.maestro.app.domain.cosmetics.CosmeticCatalog.TABLES[2],
                    isUnlocked = false,
                    isEquipped = false,
                    canAfford = true,
                ),
                ShopItemCardState(
                    item = it.maestro.app.domain.cosmetics.CosmeticCatalog.TABLES[3],
                    isUnlocked = false,
                    isEquipped = false,
                    canAfford = true,
                ),
            )
            CosmeticCategory.CARD_BACK -> listOf(
                ShopItemCardState(
                    item = it.maestro.app.domain.cosmetics.CosmeticCatalog.CARD_BACKS[0],
                    isUnlocked = true,
                    isEquipped = true,
                    canAfford = true,
                ),
                ShopItemCardState(
                    item = it.maestro.app.domain.cosmetics.CosmeticCatalog.CARD_BACKS[1],
                    isUnlocked = false,
                    isEquipped = false,
                    canAfford = true,
                ),
                ShopItemCardState(
                    item = it.maestro.app.domain.cosmetics.CosmeticCatalog.CARD_BACKS[2],
                    isUnlocked = false,
                    isEquipped = false,
                    canAfford = true,
                ),
            )
            CosmeticCategory.AVATAR -> listOf(
                ShopItemCardState(
                    item = it.maestro.app.domain.cosmetics.CosmeticCatalog.AVATARS[0],
                    isUnlocked = true,
                    isEquipped = true,
                    canAfford = true,
                ),
                ShopItemCardState(
                    item = it.maestro.app.domain.cosmetics.CosmeticCatalog.AVATARS[1],
                    isUnlocked = true,
                    isEquipped = false,
                    canAfford = true,
                ),
                ShopItemCardState(
                    item = it.maestro.app.domain.cosmetics.CosmeticCatalog.AVATARS[2],
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
        composeRule.onRoot().captureRoboImage(screen.path("shop_screen_tables"))
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
        composeRule.onRoot().captureRoboImage(screen.path("shop_screen_card_backs"))
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
        composeRule.onRoot().captureRoboImage(screen.path("shop_screen_avatars"))
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

        // Lo sblocco non spende dalla lista: serve la conferma nel riquadro (F1R-30c)
        composeRule.onNodeWithTag("shop_unlock_button_felt_noble_blue").performClick()
        assertEquals(null, unlockedItem)
    }

    @Test
    fun unlock_spends_only_after_confirming_in_the_dialog() {
        var unlockedItem: CosmeticItem? = null
        val base = sampleStateForCategory(CosmeticCategory.TABLE_FELT)
        composeRule.setContent {
            MaestroTheme {
                ShopScreenContent(
                    uiState = base.copy(previewCard = base.items[2]),
                    onSelectCategory = {},
                    onEquipItem = {},
                    onUnlockItem = { unlockedItem = it },
                    onNavigateHome = {},
                )
            }
        }
        composeRule.onNodeWithTag("shop_try_unlock").performClick()
        assertEquals(null, unlockedItem)
        composeRule.onNodeWithTag("shop_try_confirm_text").assertExists()

        composeRule.onNodeWithTag("shop_try_cancel").performClick()
        assertEquals(null, unlockedItem)
        composeRule.onNodeWithTag("shop_try_unlock").performClick()
        composeRule.onNodeWithTag("shop_try_confirm").performClick()
        assertEquals("felt_noble_blue", unlockedItem?.id)
    }

    @Test
    fun purchase_outcome_shows_the_maestro_line_then_is_consumed() {
        var consumed = false
        val base = sampleStateForCategory(CosmeticCategory.TABLE_FELT)
        composeRule.setContent {
            MaestroTheme {
                ShopScreenContent(
                    uiState = base.copy(
                        event = ShopEvent.Unlocked(base.items[2].item),
                        gameSpeed = GameSpeed.INSTANT,
                    ),
                    onSelectCategory = {},
                    onEquipItem = {},
                    onUnlockItem = {},
                    onNavigateHome = {},
                    onConsumeEvent = { consumed = true },
                )
            }
        }
        composeRule.onNodeWithTag("shop_note_unlocked").assertExists()
        composeRule.waitForIdle()
        assertTrue(consumed)
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

    private fun tryDialogState(index: Int, credits: Int): ShopUiState {
        val base = sampleStateForCategory(CosmeticCategory.TABLE_FELT, credits)
        val card = base.items[index].let { it.copy(canAfford = credits >= it.item.priceCredits) }
        return base.copy(previewCard = card)
    }

    private fun showTryDialog(state: ShopUiState) {
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
    }

    @Test
    fun try_dialog_owned_item_offers_equip() {
        showTryDialog(tryDialogState(index = 1, credits = 120))
        composeRule.onNodeWithTag("shop_try_equip").assertExists()
        composeRule.onNodeWithTag("shop_try_table").captureRoboImage(screen.path("shop_try_owned"))
    }

    @Test
    fun try_dialog_affordable_item_offers_unlock() {
        showTryDialog(tryDialogState(index = 2, credits = 120))
        composeRule.onNodeWithTag("shop_try_unlock").assertExists()
        composeRule.onNodeWithTag("shop_try_table").captureRoboImage(screen.path("shop_try_affordable"))
    }

    @Test
    fun try_dialog_unaffordable_item_shows_missing_credits() {
        showTryDialog(tryDialogState(index = 3, credits = 20))
        composeRule.onNodeWithTag("shop_try_missing").assertExists()
        composeRule.onNodeWithTag("shop_try_unlock").assertDoesNotExist()
        composeRule.onNodeWithTag("shop_try_table").captureRoboImage(screen.path("shop_try_unaffordable"))
    }

    @Test
    fun tapping_an_item_asks_to_preview_it() {
        var previewed: CosmeticItem? = null
        val state = sampleStateForCategory(CosmeticCategory.TABLE_FELT)
        composeRule.setContent {
            MaestroTheme {
                ShopScreenContent(
                    uiState = state,
                    onSelectCategory = {},
                    onEquipItem = {},
                    onUnlockItem = {},
                    onNavigateHome = {},
                    onPreviewItem = { previewed = it },
                )
            }
        }
        composeRule.onNodeWithTag("shop_item_felt_noble_blue").performClick()
        assertEquals("felt_noble_blue", previewed?.id)
    }
}
