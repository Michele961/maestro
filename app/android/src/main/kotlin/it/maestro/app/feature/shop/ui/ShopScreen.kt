package it.maestro.app.feature.shop.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import it.maestro.app.designsystem.components.CurrencyPill
import it.maestro.app.designsystem.components.FramedPortrait
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.WoodPanel
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.ButtonGoldBorder
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import it.maestro.app.designsystem.theme.WoodSurfaceDark
import org.koin.androidx.compose.koinViewModel
import it.maestro.app.designsystem.motion.MotionSpec
import it.maestro.app.designsystem.motion.MotionToken
import it.maestro.app.feature.shop.presentation.ShopEvent
import it.maestro.app.feature.shop.presentation.ShopItemCardState
import it.maestro.app.feature.shop.presentation.ShopUiState
import it.maestro.app.feature.shop.presentation.ShopViewModel

/**
 * Schermata "La Bottega del Maestro" per la personalizzazione cosmetica (task F2-11).
 * Consente di visionare, acquistare ed equipaggiare tavoli, dorsi di carte e avatar.
 */
@Composable
public fun ShopScreen(
    modifier: Modifier = Modifier,
    viewModel: ShopViewModel = koinViewModel(),
    onNavigateHome: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    ShopScreenContent(
        uiState = uiState,
        onSelectCategory = viewModel::selectCategory,
        onEquipItem = viewModel::equipCosmetic,
        onUnlockItem = viewModel::unlockAndEquip,
        onPreviewItem = viewModel::openPreview,
        onDismissPreview = viewModel::closePreview,
        onConsumeEvent = viewModel::consumeEvent,
        onNavigateHome = onNavigateHome,
        modifier = modifier,
    )
}

@Composable
public fun ShopScreenContent(
    uiState: ShopUiState,
    onSelectCategory: (CosmeticCategory) -> Unit,
    onEquipItem: (CosmeticItem) -> Unit,
    onUnlockItem: (CosmeticItem) -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
    onPreviewItem: (CosmeticItem) -> Unit = {},
    onDismissPreview: () -> Unit = {},
    onConsumeEvent: () -> Unit = {},
) {
    val balanceMillis = MotionSpec.millis(MotionToken.BALANCE_COUNT, uiState.gameSpeed).toInt()
    val shownBalance by animateIntAsState(
        targetValue = uiState.creditsBalance,
        animationSpec = tween(durationMillis = balanceMillis),
        label = "shop_balance",
    )
    val event = uiState.event
    var showMovements by remember { mutableStateOf(false) }
    if (showMovements) {
        MovementsDialog(movements = uiState.movements, onDismiss = { showMovements = false })
    }
    LaunchedEffect(event) {
        if (event != null) {
            MotionSpec.pause(MotionSpec.millis(MotionToken.PURCHASE_NOTE, uiState.gameSpeed))
            onConsumeEvent()
        }
    }
    uiState.previewCard?.let { preview ->
        TryItemDialog(
            uiState = uiState,
            onEquip = { onEquipItem(preview.item); onDismissPreview() },
            onUnlock = { onUnlockItem(preview.item); onDismissPreview() },
            onDismiss = onDismissPreview,
        )
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WoodSurfaceDark)
            .testTag("shop_screen"),
    ) {
        // Texture legno di noce a tutto schermo (zero grigio di sistema)
        Image(
            painter = painterResource(R.drawable.texture_wood),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
        ) {
            // Intestazione con pulsante di chiusura e contatore crediti
            WoodPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ShopBackButton(
                        onClick = onNavigateHome,
                        modifier = Modifier.testTag("shop_back_button"),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.shop_screen_title),
                            color = BrassRivetGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MaestroDisplayFont,
                        )
                        Text(
                            text = stringResource(R.string.shop_screen_subtitle),
                            color = Color(0xFFD4AF37),
                            fontSize = 11.sp,
                            maxLines = 1,
                        )
                    }
                    CurrencyPill(
                        icon = R.drawable.icon_coin,
                        value = shownBalance.toString(),
                        modifier = Modifier
                            .clickable { showMovements = true }
                            .testTag("shop_credits_pill"),
                    )
                }
            }

            // Esito dell'ultimo acquisto: battuta del Maestro o crediti mancanti (F1R-30c)
            when (event) {
                is ShopEvent.Unlocked -> ShopNote(
                    text = stringResource(R.string.shop_maestro_bought),
                    testTag = "shop_note_unlocked",
                )
                is ShopEvent.NotEnoughCredits -> ShopNote(
                    text = stringResource(R.string.shop_try_missing, event.missing),
                    testTag = "shop_note_missing",
                )
                null -> Unit
            }

            // Tab di selezione categoria
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShopTabButton(
                    text = stringResource(R.string.shop_tab_tables),
                    isSelected = uiState.selectedCategory == CosmeticCategory.TABLE_FELT,
                    onClick = { onSelectCategory(CosmeticCategory.TABLE_FELT) },
                    testTag = "shop_tab_tables",
                    modifier = Modifier.weight(1f),
                )
                ShopTabButton(
                    text = stringResource(R.string.shop_tab_card_backs),
                    isSelected = uiState.selectedCategory == CosmeticCategory.CARD_BACK,
                    onClick = { onSelectCategory(CosmeticCategory.CARD_BACK) },
                    testTag = "shop_tab_card_backs",
                    modifier = Modifier.weight(1f),
                )
                ShopTabButton(
                    text = stringResource(R.string.shop_tab_avatars),
                    isSelected = uiState.selectedCategory == CosmeticCategory.AVATAR,
                    onClick = { onSelectCategory(CosmeticCategory.AVATAR) },
                    testTag = "shop_tab_avatars",
                    modifier = Modifier.weight(1f),
                )
            }

            // Catalogo elementi della categoria attiva
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                uiState.items.forEach { card ->
                    ShopItemRow(
                        card = card,
                        onEquip = { onEquipItem(card.item) },
                        onPreview = { onPreviewItem(card.item) },
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Pulsante circolare di ritorno in legno di noce e ottone inciso.
 */
@Composable
private fun ShopBackButton(
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
private fun ShopTabButton(
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
private fun ShopNote(text: String, testTag: String) {
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
private fun ShopItemRow(
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
private fun CosmeticItemPreview(item: CosmeticItem) {
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
