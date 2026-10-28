package it.maestro.app.feature.profile

import it.maestro.ai.mastery.AbilityRadar
import it.maestro.ai.mastery.MasteryProfile
import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.feature.profile.presentation.ProfileViewModel
import it.maestro.app.feature.profile.presentation.RadarAxis
import it.maestro.app.feature.profile.presentation.weakestAxis
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** F1R-26d: asse più debole e «Allena questo» solo per gli assi che hanno un Check. */
@OptIn(ExperimentalCoroutinesApi::class)
class RadarAxisTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun radar(sevens: Int = 80, denari: Int = 80, primiera: Int = 80, board: Int = 80, endgame: Int = 80) =
        AbilityRadar(sevens, denari, primiera, board, endgame)

    private suspend fun stateFor(profile: MasteryProfile) =
        ProfileViewModel(InMemoryMasteryRepository(profile)).also { dispatcher.scheduler.advanceUntilIdle() }
            .uiState.first { !it.isLoading }

    @Test
    fun theWeakestAxisWinsAndTiesFollowTheAxisOrder() {
        assertEquals(RadarAxis.PRIMIERA, radar(primiera = 40).weakestAxis())
        assertEquals(RadarAxis.ENDGAME, radar(endgame = 10).weakestAxis())
        assertEquals(RadarAxis.SEVENS, radar().weakestAxis())
        assertEquals(RadarAxis.DENARI, radar(denari = 30, board = 30).weakestAxis())
    }

    @Test
    fun onlySevensDenariAndPrimieraHaveACheck() {
        assertEquals(
            listOf(TrainingFundamental.SEVENS_COUNT, TrainingFundamental.DENARI_COUNT, TrainingFundamental.PRIMIERA_LEAD, null, null),
            RadarAxis.entries.map { it.objective },
        )
    }

    @Test
    fun aWeakCheckAxisOffersTrainingOnThatObjective() = runTest(dispatcher) {
        val state = stateFor(MasteryProfile(matchesPlayed = 5, radar = radar(denari = 30)))

        assertEquals(RadarAxis.DENARI, state.weakestAxis)
        assertEquals(TrainingFundamental.DENARI_COUNT, state.trainObjective)
    }

    @Test
    fun aWeakBoardOrEndgameAxisOffersNoTraining() = runTest(dispatcher) {
        val board = stateFor(MasteryProfile(matchesPlayed = 5, radar = radar(board = 20)))
        val endgame = stateFor(MasteryProfile(matchesPlayed = 5, radar = radar(endgame = 20)))

        assertEquals(RadarAxis.BOARD, board.weakestAxis)
        assertEquals(null, board.trainObjective)
        assertEquals(RadarAxis.ENDGAME, endgame.weakestAxis)
        assertEquals(null, endgame.trainObjective)
    }

    @Test
    fun withTooFewMatchesThereIsNoWeakAxisAndNoTraining() = runTest(dispatcher) {
        val state = stateFor(MasteryProfile(matchesPlayed = 2, radar = radar(sevens = 10)))

        assertEquals(null, state.weakestAxis)
        assertEquals(null, state.trainObjective)
    }
}
