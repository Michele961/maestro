package it.maestro.app.domain.retention

import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.GameEvent
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play

/**
 * Statistiche aggregate ricavate dal replay deterministico del [MatchLog] per l'avanzamento
 * delle missioni didattiche giornaliere (task F2-05).
 *
 * @property sweepsCount Numero totale di scope messe a segno dal giocatore umano nella partita.
 * @property denariCount Numero totale di carte del seme di Denari catturate dal giocatore umano.
 * @property isWin True se il giocatore umano ha vinto la partita.
 */
public data class MatchMissionStats(
    val sweepsCount: Int,
    val denariCount: Int,
    val isWin: Boolean,
)

/**
 * Estrae in modo puro e deterministico le statistiche di gioco da un [MatchLog] per aggiornare
 * le missioni giornaliere ("Piazza Pulita", "Cacciatore d'Oro", vittoria bot).
 */
public object MatchMissionStatsExtractor {

    public fun extract(matchLog: MatchLog, humanSeat: Seat = Seat.P1): MatchMissionStats {
        var state = newMatch(config = matchLog.config, seed = matchLog.seed)
        var sweeps = 0
        var denari = 0

        for (move in matchLog.moves) {
            val mover = state.toMove
            val tableBeforePlay = state.table
            val lastCapturerBeforePlay = state.lastCapturer

            when (val result = play(state, move)) {
                is PlayResult.Applied -> {
                    if (mover == humanSeat) {
                        if (result.events.any { it is GameEvent.Sweep && it.seat == humanSeat }) {
                            sweeps++
                        }
                        if (move.capture.isNotEmpty()) {
                            denari += (move.capture + move.card).count { it.suit == Suit.DENARI }
                        }
                    }

                    // Se la smazzata si conclude, le carte residue a terra vanno all'ultimo che ha preso
                    if (result.events.any { it is GameEvent.DealEnded }) {
                        val effectiveLastCapturer = if (move.capture.isNotEmpty()) mover else lastCapturerBeforePlay
                        if (effectiveLastCapturer == humanSeat) {
                            val tableRemaining = if (move.capture.isEmpty()) {
                                tableBeforePlay + move.card
                            } else {
                                tableBeforePlay.filterNot { it in move.capture }
                            }
                            denari += tableRemaining.count { it.suit == Suit.DENARI }
                        }
                    }

                    state = result.state
                }
                is PlayResult.Rejected -> break
            }
        }

        val opponentSeat = humanSeat.other()
        val humanScore = state.totals[humanSeat] ?: 0
        val opponentScore = state.totals[opponentSeat] ?: 0
        val isWin = state.winner?.let { it == humanSeat } ?: (humanScore >= opponentScore)

        return MatchMissionStats(
            sweepsCount = sweeps,
            denariCount = denari,
            isWin = isWin,
        )
    }
}
