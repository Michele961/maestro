package it.maestro.app.feature.table.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import it.maestro.app.R
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaMove

@Composable
internal fun rememberCardFormatter(): (Card) -> String {
    val assoStr = stringResource(id = R.string.game_card_rank_asso)
    val fanteStr = stringResource(id = R.string.game_card_rank_fante)
    val cavalloStr = stringResource(id = R.string.game_card_rank_cavallo)
    val reStr = stringResource(id = R.string.game_card_rank_re)
    val denariStr = stringResource(id = R.string.game_suit_denari)
    val coppeStr = stringResource(id = R.string.game_suit_coppe)
    val spadeStr = stringResource(id = R.string.game_suit_spade)
    val bastoniStr = stringResource(id = R.string.game_suit_bastoni)

    return remember(assoStr, fanteStr, cavalloStr, reStr, denariStr, coppeStr, spadeStr, bastoniStr) {
        { card: Card ->
            val rankText = when (card.rank) {
                Rank.ASSO -> assoStr
                Rank.FANTE -> fanteStr
                Rank.CAVALLO -> cavalloStr
                Rank.RE -> reStr
                else -> card.rank.value.toString()
            }
            val suitText = when (card.suit) {
                Suit.DENARI -> denariStr
                Suit.COPPE -> coppeStr
                Suit.SPADE -> spadeStr
                Suit.BASTONI -> bastoniStr
            }
            "$rankText $suitText"
        }
    }
}

internal fun determineLogicalHintRes(
    move: ScopaMove,
    tableCards: List<Card>,
): Int {
    val captures = move.capture
    val isSweep = captures.isNotEmpty() && captures.size == tableCards.size
    val involvesSettebello = (move.card.suit == Suit.DENARI && move.card.rank == Rank.SETTE) ||
        captures.any { it.suit == Suit.DENARI && it.rank == Rank.SETTE }
    val capturesDenari = captures.any { it.suit == Suit.DENARI } || move.card.suit == Suit.DENARI
    val capturesPrimiera = captures.any { it.rank == Rank.SETTE || it.rank == Rank.SEI }

    return when {
        isSweep -> R.string.hint_sweep_opportunity
        involvesSettebello -> R.string.hint_settebello_priority
        capturesDenari -> R.string.hint_denari_priority
        capturesPrimiera -> R.string.hint_primiera_priority
        captures.isEmpty() -> R.string.hint_safe_discard
        else -> R.string.review_hint_generic
    }
}

@Composable
internal fun formatHintMove(move: ScopaMove): String {
    val assoStr = stringResource(id = R.string.game_card_rank_asso).take(1)
    val fanteStr = stringResource(id = R.string.game_card_rank_fante).take(1)
    val cavalloStr = stringResource(id = R.string.game_card_rank_cavallo).take(1)
    val reStr = stringResource(id = R.string.game_card_rank_re).take(1)

    // Semi napoletani per nome (niente simboli francesi), es. "7 di denari"
    val suitNames = mapOf(
        Suit.DENARI to stringResource(id = R.string.game_suit_denari),
        Suit.COPPE to stringResource(id = R.string.game_suit_coppe),
        Suit.SPADE to stringResource(id = R.string.game_suit_spade),
        Suit.BASTONI to stringResource(id = R.string.game_suit_bastoni),
    )

    fun cardLabel(card: Card): String {
        val rankText = when (card.rank) {
            Rank.ASSO -> assoStr
            Rank.FANTE -> fanteStr
            Rank.CAVALLO -> cavalloStr
            Rank.RE -> reStr
            else -> card.rank.value.toString()
        }
        return "$rankText ${suitNames.getValue(card.suit)}"
    }

    val cardStr = cardLabel(move.card)
    return if (move.capture.isEmpty()) {
        cardStr
    } else {
        val captureStr = move.capture.joinToString(", ") { cardLabel(it) }
        "$cardStr → $captureStr"
    }
}
