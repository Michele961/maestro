package it.maestro.app.feature.shop.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.domain.cosmetics.CosmeticCategory
import it.maestro.app.domain.cosmetics.CosmeticItem
import it.maestro.app.designsystem.cosmetics.CosmeticStyle
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.WoodPanel
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.ButtonGoldBorder
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import it.maestro.app.feature.shop.presentation.ShopItemCardState

/**
 * Pulsante circolare di ritorno in legno di noce e ottone inciso.
 */
@Composable
internal fun ShopBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(targetValue = if (isPressed) 0.94f else 1f, label = "shop_back_press")

    Box(
        modifier = modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF543319),
                        Color(0xFF291508),
                    ),
                ),
            )
            .border(
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFE082),
                            Color(0xFF8C6418),
                        ),
                    ),
                ),
                shape = CircleShape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val strokeWidth = 2.4.dp.toPx()
            val w = size.width
            val h = size.height

            // Ombra incisa per profondità tattile
            val shadowY = 1.dp.toPx()
            val shadowColor = Color(0x99000000)
            drawLine(
                color = shadowColor,
                start = Offset(w * 0.22f, h * 0.5f + shadowY),
                end = Offset(w * 0.78f, h * 0.5f + shadowY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = shadowColor,
                start = Offset(w * 0.22f, h * 0.5f + shadowY),
                end = Offset(w * 0.52f, h * 0.22f + shadowY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = shadowColor,
                start = Offset(w * 0.22f, h * 0.5f + shadowY),
                end = Offset(w * 0.52f, h * 0.78f + shadowY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )

            // Freccia in ottone dorato lucido
            val brassColor = BrassRivetGold
            drawLine(
                color = brassColor,
                start = Offset(w * 0.22f, h * 0.5f),
                end = Offset(w * 0.78f, h * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = brassColor,
                start = Offset(w * 0.22f, h * 0.5f),
                end = Offset(w * 0.52f, h * 0.22f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = brassColor,
                start = Offset(w * 0.22f, h * 0.5f),
                end = Offset(w * 0.52f, h * 0.78f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

/**
 * Pulsante per la selezione della categoria cosmetica nello Shop.
 * Finitura noce e ottone caldo, senza rosso acceso.
 */
@Composable
internal fun ShopTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(shape)
            .then(
                if (isSelected) {
                    Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF8A531E),
                                    Color(0xFF56300E),
                                ),
                            ),
                        )
                        .border(1.5.dp, BrassRivetGold, shape)
                } else {
                    Modifier
                        .background(Color(0xFF24150A))
                        .border(1.dp, Color(0x66B8860B), shape)
                },
            )
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            color = if (isSelected) Color(0xFFFFFBE8) else Color(0xFFC0A67E),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MaestroDisplayFont,
            textAlign = TextAlign.Center,
        )
    }
}

/** Riga di testo sotto l'intestazione per l'esito di un acquisto. */
@Composable
internal fun ShopNote(text: String, testTag: String) {
    Text(
        text = text,
        color = BrassRivetGold,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag(testTag),
    )
}

/**
 * Riga di un elemento cosmetico nel catalogo.
 */
@Composable
internal fun ShopItemRow(
    card: ShopItemCardState,
    onEquip: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = card.item

    WoodPanel(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPreview)
            .testTag("shop_item_${item.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Anteprima visiva dell'elemento cosmetico
            CosmeticItemPreview(item = item)

            Spacer(modifier = Modifier.width(12.dp))

            // Dettagli (titolo e descrizione)
            val texts = CosmeticStyle.textsOf(item)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(texts.nameRes),
                    color = Color(0xFFFFF8DC),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MaestroDisplayFont,
                )
                Text(
                    text = stringResource(texts.descriptionRes),
                    color = Color(0xFFE2D9C8),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Azione: Equipaggiato / Equipaggia / Sblocca
            when {
                card.isEquipped -> {
                    MaestroPillButton(
                        text = stringResource(R.string.shop_action_equipped),
                        onClick = {},
                        variant = PillButtonVariant.DARK_GREEN,
                        height = 36.dp,
                        fontSize = 11.sp,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        testTag = "shop_equipped_badge_${item.id}",
                    )
                }
                card.isUnlocked -> {
                    MaestroPillButton(
                        text = stringResource(R.string.shop_action_equip),
                        onClick = onEquip,
                        variant = PillButtonVariant.SECONDARY_AMBER,
                        height = 36.dp,
                        fontSize = 11.sp,
                        testTag = "shop_equip_button_${item.id}",
                    )
                }
                card.canAfford -> {
                    MaestroPillButton(
                        text = stringResource(R.string.shop_action_unlock, item.priceCredits),
                        onClick = onPreview,
                        variant = PillButtonVariant.PRIMARY_GREEN,
                        height = 36.dp,
                        fontSize = 11.sp,
                        testTag = "shop_unlock_button_${item.id}",
                    )
                }
                else -> {
                    MaestroPillButton(
                        text = stringResource(R.string.shop_action_unlock, item.priceCredits),
                        onClick = {},
                        enabled = false,
                        variant = PillButtonVariant.WOOD,
                        height = 36.dp,
                        fontSize = 11.sp,
                        testTag = "shop_locked_button_${item.id}",
                    )
                }
            }
        }
    }
}

/**
 * Riquadro di anteprima grafica fedele del cosmetico (tavolo, dorso, o avatar).
 */
@Composable
internal fun CosmeticItemPreview(item: CosmeticItem) {
    val shape = RoundedCornerShape(8.dp)
    val texts = CosmeticStyle.textsOf(item)

    when (item.category) {
        CosmeticCategory.TABLE_FELT -> {
            val feltTint = CosmeticStyle.tableStyle(item.id).feltTint
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 54.dp)
                    .clip(shape)
                    .border(1.5.dp, ButtonGoldBorder, shape),
            ) {
                Image(
                    painter = painterResource(texts.previewDrawableRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = feltTint?.let { ColorFilter.tint(it, BlendMode.Modulate) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        CosmeticCategory.CARD_BACK -> {
            val cardTint = CosmeticStyle.cardBackStyle(item.id).tint
            Box(
                modifier = Modifier
                    .size(width = 42.dp, height = 58.dp)
                    .clip(shape)
                    .border(1.5.dp, ButtonGoldBorder, shape),
            ) {
                Image(
                    painter = painterResource(texts.previewDrawableRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = cardTint?.let { ColorFilter.tint(it, BlendMode.Modulate) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        CosmeticCategory.AVATAR -> {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A170C))
                    .border(1.5.dp, ButtonGoldBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(texts.previewDrawableRes),
                    contentDescription = null,
                    modifier = Modifier.size(46.dp),
                )
            }
        }
    }
}
