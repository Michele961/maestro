package it.maestro.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.ui.theme.DeckTheme
import it.maestro.app.ui.theme.NeapolitanDeckTheme
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit

public val SuitDenariColor: Color = Color(0xFFD4AF37)
public val SuitSpadeColor: Color = Color(0xFF2C3E50)
public val SuitCoppeColor: Color = Color(0xFFC0392B)
public val SuitBastoniColor: Color = Color(0xFF27AE60)

public val CardBackground: Color = Color(0xFFFFFDF8)
public val CardBorderDefault: Color = Color(0xFFBCAAA4)
public val CardBorderPlayable: Color = Color(0xFFD4AF37)
public val CardBackBackground: Color = Color(0xFF1E3A8A)
public val CardBackBorder: Color = Color(0xFFFBBF24)

/**
 * Vista di una carta da gioco italiana con supporto al tema del mazzo sostituibile (ADR-0008, task F1-13).
 * Se il drawable dell'illustrazione è presente, viene renderizzato; altrimenti si attiva il fallback vettoriale.
 */
@Composable
public fun CardView(
    card: Card,
    modifier: Modifier = Modifier,
    isPlayable: Boolean = true,
    width: Dp = 64.dp,
    height: Dp = 96.dp,
    deckTheme: DeckTheme = NeapolitanDeckTheme,
    onClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val customDrawableResId = remember(card, deckTheme) {
        deckTheme.getCardDrawableResId(context, card)
    }

    Card(
        modifier = modifier
            .width(width)
            .height(height)
            .testTag("card_${card.suit.name}_${card.rank.name}")
            .clickable(enabled = isPlayable, onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPlayable) 4.dp else 1.dp),
        border = BorderStroke(
            width = if (isPlayable) 2.dp else 1.dp,
            color = if (isPlayable) CardBorderPlayable else CardBorderDefault,
        ),
    ) {
        if (customDrawableResId != null) {
            Image(
                painter = painterResource(id = customDrawableResId),
                contentDescription = "${card.rank.name} ${card.suit.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            ProceduralCardContent(card = card)
        }
    }
}

/**
 * Alias per compatibilità con il codice esistente (ADR-0008, task F1-06).
 */
@Composable
public fun PlaceholderCardView(
    card: Card,
    modifier: Modifier = Modifier,
    isPlayable: Boolean = true,
    width: Dp = 64.dp,
    height: Dp = 96.dp,
    deckTheme: DeckTheme = NeapolitanDeckTheme,
    onClick: () -> Unit = {},
) {
    CardView(
        card = card,
        modifier = modifier,
        isPlayable = isPlayable,
        width = width,
        height = height,
        deckTheme = deckTheme,
        onClick = onClick,
    )
}

@Composable
private fun ProceduralCardContent(card: Card) {
    val suitColor = when (card.suit) {
        Suit.DENARI -> SuitDenariColor
        Suit.SPADE -> SuitSpadeColor
        Suit.COPPE -> SuitCoppeColor
        Suit.BASTONI -> SuitBastoniColor
    }

    val suitSymbol = when (card.suit) {
        Suit.DENARI -> "◆"
        Suit.SPADE -> "♠"
        Suit.COPPE -> "♥"
        Suit.BASTONI -> "♣"
    }

    val rankStringRes = when (card.rank) {
        Rank.ASSO -> R.string.game_card_rank_asso
        Rank.FANTE -> R.string.game_card_rank_fante
        Rank.CAVALLO -> R.string.game_card_rank_cavallo
        Rank.RE -> R.string.game_card_rank_re
        else -> null
    }

    val rankDisplay = rankStringRes?.let { stringResource(id = it).take(1) }
        ?: card.rank.value.toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = rankDisplay,
            color = suitColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start),
        )
        Text(
            text = suitSymbol,
            color = suitColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = rankDisplay,
            color = suitColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

/**
 * Dorso della carta coperta per l'avversario e per il tallone (ADR-0008, task F1-06, F1-13).
 */
@Composable
public fun CardBackView(
    modifier: Modifier = Modifier,
    width: Dp = 64.dp,
    height: Dp = 96.dp,
    deckTheme: DeckTheme = NeapolitanDeckTheme,
) {
    val context = LocalContext.current
    val customDrawableResId = remember(deckTheme) {
        deckTheme.getCardBackDrawableResId(context)
    }

    Card(
        modifier = modifier
            .width(width)
            .height(height)
            .testTag("card_back"),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackBackground),
        border = BorderStroke(1.5.dp, CardBackBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        if (customDrawableResId != null) {
            Image(
                painter = painterResource(id = customDrawableResId),
                contentDescription = stringResource(id = R.string.game_deck_label),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .background(Color(0xFF172554), shape = RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "❖",
                    color = CardBackBorder,
                    fontSize = 20.sp,
                )
            }
        }
    }
}
