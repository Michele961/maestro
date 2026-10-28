package it.maestro.app.feature.pregame

import it.maestro.ai.mastery.AbilityRadar
import it.maestro.ai.mastery.MasteryProfile
import it.maestro.app.feature.pregame.presentation.ObjectiveAdvisor
import it.maestro.game.training.TrainingFundamental
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** F1R-25e: l'obiettivo consigliato nasce dalle lacune del giocatore. */
class ObjectiveAdvisorTest {

    private fun profile(matches: Int, sevens: Int = 50, denari: Int = 50, primiera: Int = 50, board: Int = 50, endgame: Int = 50) =
        MasteryProfile(matchesPlayed = matches, radar = AbilityRadar(sevens, denari, primiera, board, endgame))

    @Test
    fun withTooFewMatchesThereIsNoSuggestion() {
        assertNull(ObjectiveAdvisor.suggest(profile(matches = ObjectiveAdvisor.MIN_MATCHES - 1, denari = 10)))
    }

    @Test
    fun suggestsTheObjectiveOfTheWeakestAxis() {
        assertEquals(TrainingFundamental.DENARI_COUNT, ObjectiveAdvisor.suggest(profile(5, sevens = 70, denari = 30, primiera = 60)))
        assertEquals(TrainingFundamental.PRIMIERA_LEAD, ObjectiveAdvisor.suggest(profile(5, sevens = 70, denari = 60, primiera = 20)))
    }

    @Test
    fun axesWithoutACheckAreIgnored() {
        assertEquals(TrainingFundamental.SEVENS_COUNT, ObjectiveAdvisor.suggest(profile(5, sevens = 40, board = 5, endgame = 5)))
    }

    @Test
    fun aTieFollowsTheFixedOrder() {
        assertEquals(TrainingFundamental.SEVENS_COUNT, ObjectiveAdvisor.suggest(profile(5, sevens = 40, denari = 40, primiera = 40)))
    }
}
