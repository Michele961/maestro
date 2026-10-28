package it.maestro.app.feature.shop.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.designsystem.components.MaestroAlertDialog
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.ParchmentInk
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.cosmetics.CosmeticStyle
import it.maestro.app.domain.cosmetics.CosmeticCatalog
import it.maestro.app.domain.wallet.TransactionReason
import it.maestro.app.domain.wallet.WalletTransaction
import java.text.DateFormat
import java.util.Date

/** Etichetta italiana/inglese della causale di un movimento (task F1R-30d); un `when` esaustivo: una causale nuova non compila. */
@StringRes
internal fun TransactionReason.labelRes(): Int = when (this) {
    TransactionReason.WELCOME_BONUS -> R.string.movement_welcome_bonus
    TransactionReason.DAILY_LOGIN_BONUS -> R.string.movement_daily_login_bonus
    TransactionReason.DAILY_PUZZLE_REWARD -> R.string.movement_daily_puzzle_reward
    TransactionReason.DAILY_MISSION_REWARD -> R.string.movement_daily_mission_reward
    TransactionReason.ACADEMY_LESSON_REWARD -> R.string.movement_academy_lesson_reward
    TransactionReason.MATCH_VICTORY_REWARD -> R.string.movement_match_victory_reward
    TransactionReason.HINT_PURCHASE -> R.string.movement_hint_purchase
    TransactionReason.GAME_REVIEW_PURCHASE -> R.string.movement_game_review_purchase
    TransactionReason.PUZZLE_RETRY -> R.string.movement_puzzle_retry
    TransactionReason.COSMETIC_PURCHASE -> R.string.movement_cosmetic_purchase
    TransactionReason.REWARDED_AD -> R.string.movement_rewarded_ad
    TransactionReason.IAP_PACK -> R.string.movement_iap_pack
}

/**
 * Elenco dei movimenti del wallet (task F1R-30d): data, motivo e importo con segno, dal più recente.
 * Il registro è di sola lettura (ADR-0006).
 */
@Composable
internal fun MovementsDialog(movements: List<WalletTransaction>, onDismiss: () -> Unit) {
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
    MaestroAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("shop_movements_dialog"),
        title = { Text(text = stringResource(R.string.movements_title), fontWeight = FontWeight.Bold) },
        text = {
            if (movements.isEmpty()) {
                Text(
                    text = stringResource(R.string.movements_empty),
                    color = ParchmentInk,
                    fontSize = 13.sp,
                    modifier = Modifier.testTag("shop_movements_empty"),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    movements.forEach { movement ->
                        Row(
                            modifier = Modifier.fillMaxWidth().testTag("shop_movement_row"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = movementLabel(movement), color = ParchmentInk, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = dateFormat.format(Date(movement.timestamp)), color = ParchmentInk, fontSize = 11.sp)
                            }
                            Text(
                                text = if (movement.isCredit) "+${movement.amount}" else movement.amount.toString(),
                                color = if (movement.isCredit) Color(0xFF1B5E20) else Color(0xFF8B1E2B),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            MaestroPillButton(
                text = stringResource(R.string.shop_try_close),
                onClick = onDismiss,
                variant = PillButtonVariant.WOOD,
                height = 36.dp,
                fontSize = 11.sp,
                testTag = "shop_movements_close",
            )
        },
    )
}

@Composable
private fun movementLabel(movement: WalletTransaction): String {
    val purchased = if (movement.reason == TransactionReason.COSMETIC_PURCHASE) {
        movement.referenceId?.let { CosmeticCatalog.findById(it) }
    } else {
        null
    }
    return if (purchased != null) {
        stringResource(R.string.movements_purchase_of, stringResource(CosmeticStyle.textsOf(purchased).nameRes))
    } else {
        stringResource(movement.reason.labelRes())
    }
}
