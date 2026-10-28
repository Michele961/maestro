package it.maestro.app.feature.pregame.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import it.maestro.app.R
import it.maestro.app.domain.opponents.Opponent

/** Come si presenta un avversario: nome, titolo, battuta, ritratto con cornice e stemma (risorse, ADR-0007). */
internal data class OpponentStyle(
    @param:StringRes val name: Int,
    @param:StringRes val title: Int,
    @param:StringRes val line: Int,
    @param:DrawableRes val portrait: Int,
    @param:DrawableRes val crest: Int,
    /** Difficoltà da 1 a 5, mostrata a pallini al posto dell'Elo. */
    val difficulty: Int,
)

internal fun Opponent.style(): OpponentStyle = when (this) {
    Opponent.GIGI -> OpponentStyle(
        R.string.opponent_gigi_name, R.string.opponent_gigi_title, R.string.opponent_gigi_line,
        R.drawable.opponent_1_neutral, R.drawable.crest_1, difficulty = 1,
    )
    Opponent.NONNA_ROSA -> OpponentStyle(
        R.string.opponent_rosa_name, R.string.opponent_rosa_title, R.string.opponent_rosa_line,
        R.drawable.opponent_2_neutral, R.drawable.crest_2, difficulty = 2,
    )
    Opponent.TONINO -> OpponentStyle(
        R.string.opponent_tonino_name, R.string.opponent_tonino_title, R.string.opponent_tonino_line,
        R.drawable.opponent_3_neutral, R.drawable.crest_3, difficulty = 3,
    )
    Opponent.DON_ALFONSO -> OpponentStyle(
        R.string.opponent_alfonso_name, R.string.opponent_alfonso_title, R.string.opponent_alfonso_line,
        R.drawable.opponent_4_neutral, R.drawable.crest_4, difficulty = 4,
    )
    Opponent.MAESTRO -> OpponentStyle(
        R.string.opponent_maestro_name, R.string.opponent_maestro_title, R.string.opponent_maestro_line,
        R.drawable.opponent_5_neutral, R.drawable.crest_5, difficulty = 5,
    )
}
