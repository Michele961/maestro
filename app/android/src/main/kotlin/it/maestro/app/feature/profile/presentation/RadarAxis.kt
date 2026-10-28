package it.maestro.app.feature.profile.presentation

import it.maestro.ai.mastery.AbilityRadar
import it.maestro.game.training.TrainingFundamental

/** I cinque assi del Radar delle Abilità, nell'ordine in cui si leggono (e in cui si decidono i pareggi). */
public enum class RadarAxis(
    /** L'obiettivo di Sparring che allena l'asse; `null` per gli assi senza Check (Banco e Finale). */
    public val objective: TrainingFundamental?,
) {
    SEVENS(TrainingFundamental.SEVENS_COUNT),
    DENARI(TrainingFundamental.DENARI_COUNT),
    PRIMIERA(TrainingFundamental.PRIMIERA_LEAD),
    BOARD(null),
    ENDGAME(null),
}

/** Il valore (0..100) dell'asse [axis]. */
public fun AbilityRadar.valueOf(axis: RadarAxis): Int = when (axis) {
    RadarAxis.SEVENS -> sevens
    RadarAxis.DENARI -> denari
    RadarAxis.PRIMIERA -> primiera
    RadarAxis.BOARD -> board
    RadarAxis.ENDGAME -> endgame
}

/** L'asse più debole; a parità vince il primo nell'ordine di [RadarAxis]. */
public fun AbilityRadar.weakestAxis(): RadarAxis = RadarAxis.entries.minBy { valueOf(it) }
