package it.maestro.app.feature.table.presentation

import it.maestro.app.domain.match.MatchSession
import it.maestro.app.domain.match.SessionPlay
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig

/**
 * Generatore deterministico di una partita di test avanzata fino all'ultima mano (task F1-14 / debug).
 *
 * Permette di saltare la fase iniziale della partita e trovarsi direttamente nella smazzata decisiva
 * con 1 sola carta rimasta per giocatore, rendendo immediato il collaudo della fine partita,
 * del ricalcolo dell'Elo, dell'aggiornamento delle missioni e della Game Review.
 */
public object TestMatchGenerator {

    public fun generateEndgameMatch(seed: Long = 101L): MatchSession {
        val config = ScopaConfig(targetScore = 11)
        var session = MatchSession.start(config = config, seed = seed)

        // 1. Simula la partita fino alla sua naturale conclusione
        while (session.phase != DealPhase.MATCH_OVER) {
            val move = session.legalMovesFor(session.toMove).firstOrNull() ?: break
            session = (session.play(move, Seat.P1) as? SessionPlay.Applied)?.session ?: break
        }

        // 2. Riavvolge alle ultime 2 mosse (così che P1 e P2 abbiano ciascuno esattamente 1 carta da giocare)
        val moves = session.log.moves
        val cutoff = session.log.copy(moves = moves.take(maxOf(0, moves.size - 2)))
        return checkNotNull(MatchSession.fromLog(cutoff)) { "Il log troncato deve essere rigiocabile" }
    }
}
