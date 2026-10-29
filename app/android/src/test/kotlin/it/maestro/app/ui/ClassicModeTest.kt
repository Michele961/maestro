package it.maestro.app.ui

import it.maestro.app.feature.table.presentation.GameIntent
import it.maestro.app.feature.table.presentation.GameReducer
import it.maestro.game.training.TrainingFundamental
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Classica non ha aiuti (`maestro-voice.md` §6): niente Maestro, consigli o Check; Sparring sì. */
class ClassicModeTest {

    @Test
    fun aClassicMatchIsNotSparring() {
        val state = GameReducer.initial(GameIntent.StartMatch(sparring = false)).state
        assertFalse(state.isSparring)
    }

    @Test
    fun aSparringMatchKeepsTheCoach() {
        val state = GameReducer.initial(GameIntent.StartMatch(trainingFundamental = TrainingFundamental.SEVENS_COUNT)).state
        assertTrue(state.isSparring)
    }

    @Test
    fun aHintIsNeverRequestedInClassic() {
        val start = GameReducer.initial(GameIntent.StartMatch(sparring = false)).state
        val result = GameReducer.reduce(start, GameIntent.RequestHint)
        assertEquals(null, result.effect)
        assertNull(result.state.suggestedMove)
        assertFalse(result.state.isHintLoading)
    }
}
