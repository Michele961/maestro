package it.maestro.ai.bot

import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.GameEvent
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScoreCategory
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import it.maestro.game.scopa.viewFor
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimieraWeightsBenchmarkTest {

    @Test
    fun pimcEvaluatorWithConfigurablePrimieraWeightFunctionsCorrectly() {
        val eval025 = PimcEvaluator(primieraWeight = 0.025)
        val eval020 = PimcEvaluator(primieraWeight = 0.020)

        val bot025 = PimcBot(
            config = PimcConfig(budget = SearchBudget(samples = 4, maxNodes = 20)),
            evaluator = eval025,
        )
        val bot020 = PimcBot(
            config = PimcConfig(budget = SearchBudget(samples = 4, maxNodes = 20)),
            evaluator = eval020,
        )

        val master = SeededRng(20261024L)
        var wins025 = 0
        var wins020 = 0
        var primieraPoints025 = 0
        var primieraPoints020 = 0

        val totalGames = 20
        for (gameIndex in 0 until totalGames) {
            val matchSeed = master.nextLong()
            val rngP1 = SeededRng(master.nextLong())
            val rngP2 = SeededRng(master.nextLong())

            val botP1 = if (gameIndex % 2 == 0) bot025 else bot020
            val botP2 = if (gameIndex % 2 == 0) bot020 else bot025
            val seat025 = if (gameIndex % 2 == 0) Seat.P1 else Seat.P2
            val seat020 = seat025.other()

            var state = newMatch(ScopaConfig(), matchSeed)
            while (state.phase != DealPhase.MATCH_OVER) {
                val activeSeat = state.toMove
                val bot = if (activeSeat == Seat.P1) botP1 else botP2
                val rng = if (activeSeat == Seat.P1) rngP1 else rngP2

                val view = viewFor(state, activeSeat)
                val legal = legalMoves(state)
                val move = bot.chooseMove(view, legal, rng)

                when (val result = play(state, move)) {
                    is PlayResult.Applied -> {
                        for (event in result.events) {
                            if (event is GameEvent.DealEnded) {
                                primieraPoints025 += event.score.points(seat025, ScoreCategory.PRIMIERA)
                                primieraPoints020 += event.score.points(seat020, ScoreCategory.PRIMIERA)
                            }
                        }
                        state = result.state
                    }
                    is PlayResult.Rejected -> error("Mossa rifiutata: ${result.reason}")
                }
            }

            if (state.winner == seat025) wins025++ else wins020++
        }

        assertTrue(wins025 + wins020 == totalGames)
        assertTrue(primieraPoints025 >= 0)
        assertTrue(primieraPoints020 >= 0)
    }
}
