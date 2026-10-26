package it.maestro.app.game

import it.maestro.game.CoreGame
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaState
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play

/**
 * Generatore deterministico di una partita di test avanzata fino all'ultima mano (task F1-14 / debug).
 *
 * Permette di saltare la fase iniziale della partita e trovarsi direttamente nella smazzata decisiva
 * con 1 sola carta rimasta per giocatore, rendendo immediato il collaudo della fine partita,
 * del ricalcolo dell'Elo, dell'aggiornamento delle missioni e della Game Review.
 */
public object TestMatchGenerator {

    public fun generateEndgameMatch(seed: Long = 101L): Pair<ScopaState, MatchLog> {
        val config = ScopaConfig(targetScore = 11)
        var state = newMatch(config = config, seed = seed)
        val allMoves = mutableListOf<ScopaMove>()

        // 1. Simula la partita fino alla sua naturale conclusione
        while (state.phase != DealPhase.MATCH_OVER) {
            val legal = legalMoves(state)
            if (legal.isEmpty()) break
            val move = legal.first()
            val result = play(state, move)
            if (result is PlayResult.Applied) {
                state = result.state
                allMoves.add(move)
            } else {
                break
            }
        }

        // 2. Riavvolge alle ultime 2 mosse (così che P1 e P2 abbiano ciascuno esattamente 1 carta da giocare)
        val cutoffMovesCount = maxOf(0, allMoves.size - 2)
        var replayState = newMatch(config = config, seed = seed)
        val replayMoves = mutableListOf<ScopaMove>()

        for (i in 0 until cutoffMovesCount) {
            val move = allMoves[i]
            val result = play(replayState, move)
            if (result is PlayResult.Applied) {
                replayState = result.state
                replayMoves.add(move)
            } else {
                break
            }
        }

        val matchLog = MatchLog(
            rulesVersion = CoreGame.RULES_VERSION,
            config = config,
            seed = seed,
            moves = replayMoves,
        )

        return Pair(replayState, matchLog)
    }
}
