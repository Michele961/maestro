package it.maestro.app.feature.review.presentation

import it.maestro.ai.analysis.MatchAnalyzer
import it.maestro.ai.analysis.MoveGrade
import it.maestro.ai.bot.SearchBudget
import it.maestro.game.CoreGame
import it.maestro.game.model.Card
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaState
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import it.maestro.game.scopa.viewFor
import java.io.File

/**
 * Simulazione della revisione (task F1R-24g): gioca partite con un giocatore che sbaglia, analizza le sue mosse e
 * scrive per ognuna situazione, mossa giocata, mossa migliore e spiegazione, per verificare a occhio e con test
 * che le spiegazioni siano vere e chiare.
 */
internal object ReviewSimulation {

    /** Un errore del giocatore con la situazione in cui è avvenuto. */
    data class Case(
        val seed: Long,
        val moveIndex: Int,
        val grade: MoveGrade,
        val loss: Double,
        val hand: List<Card>,
        val table: List<Card>,
        val played: ScopaMove,
        val best: ScopaMove,
        val legalCount: Int,
        val state: ScopaState,
    )

    private fun name(card: Card) = "${card.rank.value}${card.suit.name.first()}"
    private fun name(move: ScopaMove) = name(move.card) + if (move.capture.isEmpty()) " (cala)" else " x " + move.capture.joinToString("+") { name(it) }

    /** Gioca [games] partite con P1 che sceglie a caso e P2 che preferisce le prese; restituisce gli errori di P1. */
    fun cases(games: Int, firstSeed: Long = 1L): List<Case> {
        val analyzer = MatchAnalyzer(budget = SearchBudget(samples = 12, maxNodes = 120))
        val out = mutableListOf<Case>()
        for (seed in firstSeed until firstSeed + games) {
            val config = ScopaConfig()
            var state = newMatch(config = config, seed = seed)
            val moves = ArrayList<ScopaMove>()
            val rng = SeededRng(seed * 31 + 7)
            while (state.winner == null && moves.size < 400) {
                val legal = legalMoves(state)
                if (legal.isEmpty()) break
                val move = if (state.toMove == Seat.P1) legal[rng.nextInt(legal.size)] else (legal.firstOrNull { it.capture.isNotEmpty() } ?: legal.first())
                val result = play(state, move) as? PlayResult.Applied ?: break
                moves += move
                state = result.state
            }
            val log = MatchLog(rulesVersion = CoreGame.RULES_VERSION, config = config, seed = seed, moves = moves)
            val analysis = analyzer.analyze(log)
            var replay = newMatch(config = config, seed = seed)
            analysis.moves.forEachIndexed { i, step ->
                if (step.seat == Seat.P1 && step.grade != MoveGrade.BEST) {
                    val view = viewFor(replay, Seat.P1)
                    out += Case(seed, i, step.grade, step.loss, view.hand, view.table, step.playedMove, step.bestMove, step.alternatives.size, replay)
                }
                replay = (play(replay, moves[i]) as PlayResult.Applied).state
            }
        }
        return out
    }

    /** Scrive il rapporto in Markdown, una riga per errore. */
    fun report(cases: List<Case>, file: File, explain: (Case) -> String) {
        file.parentFile.mkdirs()
        file.printWriter().use { out ->
            out.println("# Simulazione della revisione: ${cases.size} errori\n")
            cases.forEach { c ->
                out.println("## seed ${c.seed} · mossa ${c.moveIndex} · ${c.grade} · scelte ${c.legalCount}")
                out.println("- mano: ${c.hand.joinToString(" ") { name(it) }} · banco: ${c.table.joinToString(" ") { name(it) }}")
                out.println("- giocata: ${name(c.played)} · migliore: ${name(c.best)}")
                out.println("- ${explain(c)}\n")
            }
        }
    }
}
