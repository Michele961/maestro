package it.maestro.app.domain.cosmetics

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
 *
 * Il dominio conosce solo identità e prezzo: testi e disegno vengono dal `CosmeticStyle` del design system (F1R-30a).
 */
public data class CosmeticItem(
    val id: String,
    val category: CosmeticCategory,
    val priceCredits: Int,
    val isDefault: Boolean = false,
) {
    public val isFree: Boolean get() = priceCredits == 0
}

/**
 * Catalogo ufficiale degli elementi cosmetici della Bottega del Maestro (task F2-11, F1R-30a).
 */
public object CosmeticCatalog {

    public val DEFAULT_UNLOCKED_IDS: Set<String> = setOf(
        "felt_classic_green",
        "back_classic_trevigiano",
        "avatar_default",
    )

    public val TABLES: List<CosmeticItem> = listOf(
        CosmeticItem("felt_classic_green", CosmeticCategory.TABLE_FELT, priceCredits = 0, isDefault = true),
        CosmeticItem("felt_royal_red", CosmeticCategory.TABLE_FELT, priceCredits = 40),
        CosmeticItem("felt_noble_blue", CosmeticCategory.TABLE_FELT, priceCredits = 50),
        CosmeticItem("felt_walnut_wood", CosmeticCategory.TABLE_FELT, priceCredits = 60),
    )

    public val CARD_BACKS: List<CosmeticItem> = listOf(
        CosmeticItem("back_classic_trevigiano", CosmeticCategory.CARD_BACK, priceCredits = 0, isDefault = true),
        CosmeticItem("back_vintage_red", CosmeticCategory.CARD_BACK, priceCredits = 35),
        CosmeticItem("back_golden_maestro", CosmeticCategory.CARD_BACK, priceCredits = 50),
    )

    public val AVATARS: List<CosmeticItem> = listOf(
        CosmeticItem("avatar_default", CosmeticCategory.AVATAR, priceCredits = 0, isDefault = true),
        CosmeticItem("avatar_alt", CosmeticCategory.AVATAR, priceCredits = 40),
        CosmeticItem("avatar_expert", CosmeticCategory.AVATAR, priceCredits = 75),
    )

    public val ALL_ITEMS: List<CosmeticItem> = TABLES + CARD_BACKS + AVATARS

    public fun findById(id: String): CosmeticItem? = ALL_ITEMS.firstOrNull { it.id == id }
}
