package it.maestro.app.designsystem.cosmetics

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import it.maestro.app.R
import it.maestro.app.domain.cosmetics.CosmeticCategory
import it.maestro.app.domain.cosmetics.CosmeticItem

/** Testi e anteprima di un cosmetico nella Bottega (F1R-30a). */
public data class CosmeticTexts(
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val previewDrawableRes: Int,
)

/** Aspetto di un tavolo: tavolo, anteprima e Bottega leggono da qui, non ripetono i colori. */
public data class TableStyle(
    /** Texture del panno; `null` = quella predefinita (`table_felt_bg`). */
    @DrawableRes val feltDrawableRes: Int?,
    /** Tinta applicata alla texture; `null` = nessuna. */
    val feltTint: Color?,
    val background: Brush,
)

/** Aspetto del dorso delle carte; i `null` usano i colori predefiniti del design system. */
public data class CardBackStyle(
    val background: Color?,
    val border: Color?,
    val tint: Color?,
)

/** Unica fonte dell'aspetto dei cosmetici per id (F1R-30a); un id sconosciuto ricade sul predefinito. */
public object CosmeticStyle {

    private val texts: Map<String, CosmeticTexts> = mapOf(
        "felt_classic_green" to CosmeticTexts(R.string.cosmetic_table_green_title, R.string.cosmetic_table_green_desc, R.drawable.table_felt_bg),
        "felt_royal_red" to CosmeticTexts(R.string.cosmetic_table_red_title, R.string.cosmetic_table_red_desc, R.drawable.table_felt_bg),
        "felt_noble_blue" to CosmeticTexts(R.string.cosmetic_table_blue_title, R.string.cosmetic_table_blue_desc, R.drawable.table_felt_bg),
        "felt_walnut_wood" to CosmeticTexts(R.string.cosmetic_table_wood_title, R.string.cosmetic_table_wood_desc, R.drawable.texture_wood),
        "back_classic_trevigiano" to CosmeticTexts(R.string.cosmetic_card_back_classic_title, R.string.cosmetic_card_back_classic_desc, R.drawable.card_back),
        "back_vintage_red" to CosmeticTexts(R.string.cosmetic_card_back_red_title, R.string.cosmetic_card_back_red_desc, R.drawable.card_back),
        "back_golden_maestro" to CosmeticTexts(R.string.cosmetic_card_back_gold_title, R.string.cosmetic_card_back_gold_desc, R.drawable.card_back),
        "avatar_default" to CosmeticTexts(R.string.cosmetic_avatar_default_title, R.string.cosmetic_avatar_default_desc, R.drawable.avatar_player_default),
        "avatar_alt" to CosmeticTexts(R.string.cosmetic_avatar_alt_title, R.string.cosmetic_avatar_alt_desc, R.drawable.avatar_player_alt),
        "avatar_expert" to CosmeticTexts(R.string.cosmetic_avatar_expert_title, R.string.cosmetic_avatar_expert_desc, R.drawable.avatar_expert),
    )

    /** Testi e anteprima dell'elemento; un id senza stile (il test sul catalogo lo impedisce) ricade sul predefinito. */
    public fun textsOf(item: CosmeticItem): CosmeticTexts = texts[item.id] ?: texts.getValue(
        when (item.category) {
            CosmeticCategory.TABLE_FELT -> "felt_classic_green"
            CosmeticCategory.CARD_BACK -> "back_classic_trevigiano"
            CosmeticCategory.AVATAR -> "avatar_default"
        },
    )

    public fun tableStyle(id: String): TableStyle = when (id) {
        "felt_royal_red" -> TableStyle(
            feltDrawableRes = null,
            feltTint = Color(0xFFE57373),
            background = Brush.radialGradient(listOf(Color(0xFF8B1E2B), Color(0xFF5E121C), Color(0xFF38080F), Color(0xFF1E0307))),
        )
        "felt_noble_blue" -> TableStyle(
            feltDrawableRes = null,
            feltTint = Color(0xFF64B5F6),
            background = Brush.radialGradient(listOf(Color(0xFF1F487E), Color(0xFF14325C), Color(0xFF0C1F3B), Color(0xFF06101F))),
        )
        "felt_walnut_wood" -> TableStyle(
            feltDrawableRes = R.drawable.texture_wood,
            feltTint = null,
            background = Brush.radialGradient(listOf(Color(0xFF4A2810), Color(0xFF331808), Color(0xFF220E04), Color(0xFF120702))),
        )
        else -> TableStyle(
            feltDrawableRes = null,
            feltTint = null,
            background = Brush.radialGradient(listOf(Color(0xFF225740), Color(0xFF1B4332), Color(0xFF0D281E), Color(0xFF081C15))),
        )
    }

    public fun cardBackStyle(id: String): CardBackStyle = when (id) {
        "back_golden_maestro" -> CardBackStyle(Color(0xFF4A3800), Color(0xFFFFD54F), Color(0xFFFFD54F))
        "back_vintage_red" -> CardBackStyle(Color(0xFF3E0D15), Color(0xFFE57373), Color(0xFFFF8A80))
        else -> CardBackStyle(background = null, border = null, tint = null)
    }

    @DrawableRes
    public fun avatarDrawable(id: String): Int = when (id) {
        "avatar_alt" -> R.drawable.avatar_player_alt
        "avatar_expert" -> R.drawable.avatar_expert
        else -> R.drawable.avatar_player_default
    }
}
