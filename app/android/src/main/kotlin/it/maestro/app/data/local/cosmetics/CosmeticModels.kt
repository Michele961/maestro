package it.maestro.app.data.local.cosmetics

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import it.maestro.app.R

/**
 * Categoria di elemento cosmetico personalizzabile (task F2-11).
 */
public enum class CosmeticCategory {
    TABLE_FELT,
    CARD_BACK,
    AVATAR,
}

/**
 * Singolo elemento estetico acquistabile o equipaggiabile nella Bottega del Maestro.
 */
public data class CosmeticItem(
    val id: String,
    val category: CosmeticCategory,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val priceCredits: Int,
    @DrawableRes val previewDrawableRes: Int,
    val isDefault: Boolean = false,
) {
    public val isFree: Boolean get() = priceCredits == 0
}

/**
 * Catalogo ufficiale degli elementi cosmetici della Bottega del Maestro (task F2-11).
 */
public object CosmeticCatalog {

    public val DEFAULT_UNLOCKED_IDS: Set<String> = setOf(
        "felt_classic_green",
        "back_classic_trevigiano",
        "avatar_default",
    )

    public val TABLES: List<CosmeticItem> = listOf(
        CosmeticItem(
            id = "felt_classic_green",
            category = CosmeticCategory.TABLE_FELT,
            nameRes = R.string.cosmetic_table_green_title,
            descriptionRes = R.string.cosmetic_table_green_desc,
            priceCredits = 0,
            previewDrawableRes = R.drawable.table_felt_bg,
            isDefault = true,
        ),
        CosmeticItem(
            id = "felt_royal_red",
            category = CosmeticCategory.TABLE_FELT,
            nameRes = R.string.cosmetic_table_red_title,
            descriptionRes = R.string.cosmetic_table_red_desc,
            priceCredits = 40,
            previewDrawableRes = R.drawable.table_felt_bg,
        ),
        CosmeticItem(
            id = "felt_noble_blue",
            category = CosmeticCategory.TABLE_FELT,
            nameRes = R.string.cosmetic_table_blue_title,
            descriptionRes = R.string.cosmetic_table_blue_desc,
            priceCredits = 50,
            previewDrawableRes = R.drawable.table_felt_bg,
        ),
        CosmeticItem(
            id = "felt_walnut_wood",
            category = CosmeticCategory.TABLE_FELT,
            nameRes = R.string.cosmetic_table_wood_title,
            descriptionRes = R.string.cosmetic_table_wood_desc,
            priceCredits = 60,
            previewDrawableRes = R.drawable.texture_wood,
        ),
    )

    public val CARD_BACKS: List<CosmeticItem> = listOf(
        CosmeticItem(
            id = "back_classic_trevigiano",
            category = CosmeticCategory.CARD_BACK,
            nameRes = R.string.cosmetic_card_back_classic_title,
            descriptionRes = R.string.cosmetic_card_back_classic_desc,
            priceCredits = 0,
            previewDrawableRes = R.drawable.card_back,
            isDefault = true,
        ),
        CosmeticItem(
            id = "back_vintage_red",
            category = CosmeticCategory.CARD_BACK,
            nameRes = R.string.cosmetic_card_back_red_title,
            descriptionRes = R.string.cosmetic_card_back_red_desc,
            priceCredits = 35,
            previewDrawableRes = R.drawable.card_back,
        ),
        CosmeticItem(
            id = "back_golden_maestro",
            category = CosmeticCategory.CARD_BACK,
            nameRes = R.string.cosmetic_card_back_gold_title,
            descriptionRes = R.string.cosmetic_card_back_gold_desc,
            priceCredits = 50,
            previewDrawableRes = R.drawable.card_back,
        ),
    )

    public val AVATARS: List<CosmeticItem> = listOf(
        CosmeticItem(
            id = "avatar_default",
            category = CosmeticCategory.AVATAR,
            nameRes = R.string.cosmetic_avatar_default_title,
            descriptionRes = R.string.cosmetic_avatar_default_desc,
            priceCredits = 0,
            previewDrawableRes = R.drawable.avatar_player_default,
            isDefault = true,
        ),
        CosmeticItem(
            id = "avatar_alt",
            category = CosmeticCategory.AVATAR,
            nameRes = R.string.cosmetic_avatar_alt_title,
            descriptionRes = R.string.cosmetic_avatar_alt_desc,
            priceCredits = 40,
            previewDrawableRes = R.drawable.avatar_player_alt,
        ),
        CosmeticItem(
            id = "avatar_expert",
            category = CosmeticCategory.AVATAR,
            nameRes = R.string.cosmetic_avatar_expert_title,
            descriptionRes = R.string.cosmetic_avatar_expert_desc,
            priceCredits = 75,
            previewDrawableRes = R.drawable.avatar_expert,
        ),
    )

    public val ALL_ITEMS: List<CosmeticItem> = TABLES + CARD_BACKS + AVATARS

    public fun findById(id: String): CosmeticItem? = ALL_ITEMS.firstOrNull { it.id == id }
}
