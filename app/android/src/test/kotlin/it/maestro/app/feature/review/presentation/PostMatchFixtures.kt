package it.maestro.app.feature.review.presentation

import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MatchAnalyzer
import it.maestro.ai.bot.SearchBudget
import it.maestro.game.CoreGame
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play

/** Partita e analisi d'esempio per i test della fine partita (36 mosse giocando sempre la prima mossa legale). */
internal object PostMatchFixtures {

    val log: MatchLog by lazy {
        val config = ScopaConfig()
        var state = newMatch(config = config, seed = 7L)
        val moves = ArrayList<ScopaMove>()
        repeat(36) {
            val legal = legalMoves(state)
            if (legal.isEmpty()) return@repeat
            val move = legal.first()
            val result = play(state, move) as? PlayResult.Applied ?: return@repeat
            moves.add(move)
            state = result.state
        }
        MatchLog(rulesVersion = CoreGame.RULES_VERSION, config = config, seed = 7L, moves = moves)
    }

    val analysis: MatchAnalysis by lazy { MatchAnalyzer(budget = SearchBudget(samples = 2, maxNodes = 40)).analyze(log) }
}
