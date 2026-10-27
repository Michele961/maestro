package it.maestro.app.domain.match

import it.maestro.ai.bot.Bot
import it.maestro.ai.bot.PimcEvaluator
import it.maestro.ai.bot.SearchBudget
import it.maestro.game.CoreGame
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ReplayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaState
import it.maestro.game.scopa.ScopaView
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import it.maestro.game.scopa.replay
import it.maestro.game.scopa.viewFor

/** Esito di [MatchSession.play]: le mosse illegali non lanciano eccezioni (CLAUDE.md §4). */
public sealed interface SessionPlay {
    /** Mossa applicata: [session] è la partita dopo la mossa, [beat] la racconta al tavolo. */
    public data class Applied(val session: MatchSession, val beat: Beat) : SessionPlay

    /** Mossa rifiutata dal motore, con il motivo. */
    public data class Rejected(val reason: String) : SessionPlay
}

/**
 * Una partita in corso: l'unico punto dell'app che vede lo stato completo (ADR-0015, task F1R-08).
 *
 * È immutabile: [play] restituisce una nuova sessione. Fuori espone solo viste del giocatore, il
 * [MatchLog] (seed + configurazione + mosse, ADR-0003) e i dati pubblici della smazzata. Il bot
 * sceglie con [chooseBotMove], che gli passa solo la sua vista.
 */
public class MatchSession private constructor(
    private val state: ScopaState,
    public val log: MatchLog,
) {
    public val config: ScopaConfig get() = state.config
    public val phase: DealPhase get() = state.phase
    public val toMove: Seat get() = state.toMove
    public val totals: Map<Seat, Int> get() = state.totals
    public val winner: Seat? get() = state.winner

    /** Vista della partita per [seat]: niente carte nascoste. */
    public fun view(seat: Seat): ScopaView = viewFor(state, seat)

    /** Mosse legali di [seat]; vuota se non tocca a lui. */
    public fun legalMovesFor(seat: Seat): List<ScopaMove> =
        if (state.phase == DealPhase.PLAYING && state.toMove == seat) legalMoves(state) else emptyList()

    /** Applica [move] per chi è di turno; [observer] è il giocatore per cui si costruisce la [Beat]. */
    public fun play(move: ScopaMove, observer: Seat): SessionPlay =
        when (val result = play(state, move)) {
            is PlayResult.Applied -> {
                val next = MatchSession(result.state, log.copy(moves = log.moves + move))
                val beat = Beat(
                    id = next.log.moves.size.toLong(),
                    actor = state.toMove,
                    move = move,
                    events = result.events,
                    viewBefore = viewFor(state, observer),
                    viewAfter = viewFor(result.state, observer),
                )
                SessionPlay.Applied(next, beat)
            }
            is PlayResult.Rejected -> SessionPlay.Rejected(result.reason.toString())
        }

    /** Mossa di [bot] seduto in [seat], scelta sulla sola vista di [seat]; `null` se non è il suo turno. */
    public fun chooseBotMove(bot: Bot, seat: Seat, rng: SeededRng): ScopaMove? {
        val legal = legalMovesFor(seat)
        if (legal.isEmpty()) return null
        return bot.chooseMove(view = view(seat), legal = legal, rng = rng)
    }

    /** Suggerimento del Maestro per [seat] (modalità sparring); `null` se non è il suo turno. */
    public fun hint(seat: Seat): ScopaMove? {
        val legal = legalMovesFor(seat)
        if (legal.isEmpty()) return null
        val evaluations = PimcEvaluator.evaluate(
            view = view(seat),
            legal = legal,
            rng = SeededRng(state.rngState xor HINT_SEED_MASK),
            budget = SearchBudget(samples = 15, maxNodes = 60),
        )
        return evaluations.maxByOrNull { it.value }?.move ?: legal.first()
    }

    public companion object {
        private const val HINT_SEED_MASK = 0x7E7E7EL

        /** Nuova partita con [config] e [seed]. */
        public fun start(config: ScopaConfig, seed: Long): MatchSession = MatchSession(
            newMatch(config = config, seed = seed),
            MatchLog(rulesVersion = CoreGame.RULES_VERSION, config = config, seed = seed, moves = emptyList()),
        )

        /** Ricostruisce la partita rigiocando [log] (ADR-0003); `null` se il log non è rigiocabile. */
        public fun fromLog(log: MatchLog): MatchSession? =
            (replay(log) as? ReplayResult.Ok)?.let { MatchSession(it.state, log) }
    }
}
