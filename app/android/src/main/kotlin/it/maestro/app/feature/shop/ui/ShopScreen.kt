package it.maestro.app.feature.shop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.domain.cosmetics.CosmeticCategory
import it.maestro.app.domain.cosmetics.CosmeticItem
import it.maestro.app.designsystem.components.CurrencyPill
import it.maestro.app.designsystem.components.WoodPanel
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import it.maestro.app.designsystem.theme.WoodSurfaceDark
import org.koin.androidx.compose.koinViewModel
import it.maestro.app.designsystem.motion.MotionSpec
import it.maestro.app.designsystem.motion.MotionToken
import it.maestro.app.feature.shop.presentation.ShopEvent
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
