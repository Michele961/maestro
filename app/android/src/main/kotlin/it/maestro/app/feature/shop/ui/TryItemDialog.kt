package it.maestro.app.feature.shop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.designsystem.components.CardBackView
import it.maestro.app.designsystem.components.CardView
import it.maestro.app.designsystem.components.FramedPortrait
import it.maestro.app.designsystem.components.MaestroAlertDialog
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.ParchmentInk
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.cosmetics.CosmeticStyle
import it.maestro.app.domain.cosmetics.CosmeticCategory
import it.maestro.app.feature.shop.presentation.ShopUiState
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit

/** Carte scoperte del mini-tavolo di prova: sempre le stesse, così le anteprime si confrontano. */
private val SampleCards = listOf(
    Card(Rank.ASSO, Suit.DENARI),
    Card(Rank.SETTE, Suit.DENARI),
    Card(Rank.RE, Suit.COPPE),
)

/**
 * Riquadro «Provalo» (task F1R-30b): mostra l'oggetto scelto già applicato a un mini-tavolo con carte e dorsi veri,
 * mentre gli altri due oggetti restano quelli equipaggiati. Da qui si equipaggia, si sblocca o si vede quanto manca.
 */
@Composable
internal fun TryItemDialog(
    uiState: ShopUiState,
    onEquip: () -> Unit,
    onUnlock: () -> Unit,
    onDismiss: () -> Unit,
) {
    val card = uiState.previewCard ?: return
    val item = card.item
    // Lo sblocco chiede sempre conferma, qui nel riquadro (F1R-30c).
    var confirming by remember(item.id) { mutableStateOf(false) }
    val tableId = if (item.category == CosmeticCategory.TABLE_FELT) item.id else uiState.equippedTableId
    val backId = if (item.category == CosmeticCategory.CARD_BACK) item.id else uiState.equippedCardBackId
    val avatarId = if (item.category == CosmeticCategory.AVATAR) item.id else uiState.equippedAvatarId

    MaestroAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("shop_try_dialog"),
        title = {
            Text(
                text = stringResource(CosmeticStyle.textsOf(item).nameRes),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniTable(tableId = tableId, backId = backId, avatarId = avatarId)
                Text(
                    text = stringResource(CosmeticStyle.textsOf(item).descriptionRes),
                    color = ParchmentInk,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (confirming) {
                    Text(
                        text = stringResource(
                            R.string.shop_confirm_text,
                            item.priceCredits,
                            stringResource(CosmeticStyle.textsOf(item).nameRes),
                        ),
                        color = ParchmentInk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.testTag("shop_try_confirm_text"),
                    )
                } else if (!card.isUnlocked && !card.canAfford) {
                    val missing = item.priceCredits - uiState.creditsBalance
                    Text(
                        text = stringResource(R.string.shop_try_missing, missing),
                        color = ParchmentInk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.testTag("shop_try_missing"),
                    )
                    Text(
                        text = stringResource(R.string.shop_try_earn),
                        color = ParchmentInk,
                        fontSize = 12.sp,
                    )
                }
            }
        },
        dismissButton = {
            MaestroPillButton(
                text = stringResource(if (confirming) R.string.shop_confirm_no else R.string.shop_try_close),
                onClick = { if (confirming) confirming = false else onDismiss() },
                variant = PillButtonVariant.WOOD,
                height = 36.dp,
                fontSize = 11.sp,
                testTag = if (confirming) "shop_try_cancel" else "shop_try_close",
            )
        },
        confirmButton = {
            when {
                confirming -> MaestroPillButton(
                    text = stringResource(R.string.shop_confirm_yes),
                    onClick = onUnlock,
                    variant = PillButtonVariant.PRIMARY_GREEN,
                    height = 36.dp,
                    fontSize = 11.sp,
                    testTag = "shop_try_confirm",
                )
                card.isEquipped -> MaestroPillButton(
                    text = stringResource(R.string.shop_action_equipped),
                    onClick = {},
                    variant = PillButtonVariant.DARK_GREEN,
                    height = 36.dp,
                    fontSize = 11.sp,
                    testTag = "shop_try_equipped",
                )
                card.isUnlocked -> MaestroPillButton(
                    text = stringResource(R.string.shop_action_equip),
                    onClick = onEquip,
                    variant = PillButtonVariant.SECONDARY_AMBER,
                    height = 36.dp,
                    fontSize = 11.sp,
                    testTag = "shop_try_equip",
                )
                card.canAfford -> MaestroPillButton(
                    text = stringResource(R.string.shop_action_unlock, item.priceCredits),
                    onClick = { confirming = true },
                    variant = PillButtonVariant.PRIMARY_GREEN,
                    height = 36.dp,
                    fontSize = 11.sp,
                    testTag = "shop_try_unlock",
                )
            }
        },
    )
}

/** Mini-tavolo: panno, carte scoperte, un dorso e il ritratto, tutti dalla stessa fonte del tavolo vero. */
@Composable
private fun MiniTable(tableId: String, backId: String, avatarId: String) {
    val style = remember(tableId) { CosmeticStyle.tableStyle(tableId) }
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(shape)
            .background(style.background)
            .border(2.dp, Color(0xFFD4AF37), shape)
            .testTag("shop_try_table"),
    ) {
        Image(
            painter = painterResource(style.feltDrawableRes ?: R.drawable.table_felt_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = style.feltTint?.let { ColorFilter.tint(it, BlendMode.Modulate) },
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FramedPortrait(portrait = CosmeticStyle.avatarDrawable(avatarId), size = 44.dp)
            SampleCards.forEach { sample -> CardView(card = sample, width = 40.dp, height = 60.dp) }
            CardBackView(width = 40.dp, height = 60.dp, cardBackThemeId = backId)
        }
    }
}
