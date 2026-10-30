package it.maestro.ai.bot

import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import it.maestro.game.scopa.viewFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** I bot giocano partite intere con napola e asso che taglia attivi (spec §4.10, §6.4): mosse sempre legali, partita che finisce. */
class BotVariantsTest {
    private val variants = ScopaConfig(targetScore = 16, reBello = true, napola = true, assoPigliatutto = true)

    @Test
    fun everyBotFinishesWholeMatchesWithNapolaAndAssoPigliatutto() {
        val bots = listOf(RandomBot(), GreedyBot(), PimcBot(SearchBudget(samples = 2)))
        for (bot in bots) {
            for (seed in 1L..3L) {
                var state = newMatch(config = variants, seed = seed)
                val rng = SeededRng(seed + 100)
                var plays = 0
                while (state.phase != DealPhase.MATCH_OVER) {
                    val legal = legalMoves(state)
                    val move = bot.chooseMove(viewFor(state, state.toMove), legal, rng)
                    assertTrue(move in legal, "${bot::class.simpleName} ha scelto una mossa illegale (seed $seed)")
                    val result = play(state, move)
                    assertTrue(result is PlayResult.Applied)
                    state = result.state
                    assertTrue(++plays < 2000, "partita che non finisce (seed $seed)")
                }
                assertTrue(state.winner in listOf(Seat.P1, Seat.P2))
                assertEquals(true, state.totals.values.any { it >= 16 })
            }
        }
    }
}
