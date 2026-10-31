package it.maestro.app.feature.table.presentation

import it.maestro.app.domain.guide.GuideLine
import it.maestro.app.domain.guide.GuideStep
import it.maestro.app.domain.guide.GuidedHand
import it.maestro.app.domain.match.HouseRules
import it.maestro.app.domain.opponents.Opponent
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaMove

/**
 * Gioca la mano d'esordio con un giocatore che ubbidisce al Maestro (F1G-02), senza interfaccia: serve a verificare
 * che il seme fisso insegni davvero i passi e che la mano sia sempre la stessa.
 */
internal object GuidedHandSimulation {

    /** Esito: i passi insegnati nell'ordine, la mano iniziale del giocatore e le mosse fatte. */
    data class Result(
        val taughtInOrder: List<GuideStep>,
        val firstHand: List<Card>,
        val moves: List<ScopaMove>,
        val finished: Boolean,
        val firstFeedback: GuideLine?,
    )

    private val settebello = Card(Rank.SETTE, Suit.DENARI)

    fun play(seed: Long): Result {
        val level = Opponent.GIGI.level
        val botRng = SeededRng(seed + 9999L)
        val config = HouseRules().toConfig(GuidedHand.TARGET_SCORE)
        var step = GameReducer.initial(GameIntent.StartMatch(config = config, level = level, seed = seed, guided = true))
        var state = step.state
        val firstHand = state.session.view(Seat.P1).hand
        val taught = mutableListOf<GuideStep>()
        val moves = mutableListOf<ScopaMove>()
        var firstFeedback: GuideLine? = null
        var guard = 0
        while (state.session.phase == DealPhase.PLAYING && state.pendingDealScore == null && guard++ < 200) {
            val view = state.session.view(Seat.P1)
            val move: ScopaMove
            if (view.toMove == Seat.P1) {
                val legal = state.session.legalMovesFor(Seat.P1)
                val line = state.toUiState().guide?.line
                move = legal.firstOrNull { line != null && fits(line.step, it, view.table) } ?: legal.first()
                moves += move
                state = GameReducer.reduce(state, GameIntent.PlayCard(move.card)).state
                if (state.pendingCapture != null) state = GameReducer.reduce(state, GameIntent.SelectCapture(move.card, move.capture)).state
                if (firstFeedback == null) firstFeedback = state.guide?.feedback
                val now = state.guide?.taught.orEmpty()
                taught += (now - taught.toSet())
            } else {
                val bot = state.session.chooseBotMove(level.toBot(), Seat.P2, botRng) ?: break
                step = GameReducer.reduce(state, GameIntent.ApplyBotMove(bot))
                state = step.state
            }
        }
        return Result(taught, firstHand, moves, state.pendingDealScore != null, firstFeedback)
    }

    private fun fits(step: GuideStep, move: ScopaMove, table: List<Card>): Boolean = when (step) {
        GuideStep.SETTE -> settebello in move.capture
        GuideStep.SCOPA -> move.capture.size == table.size
        GuideStep.SINGLE -> move.capture.size == 1 && move.capture.first().rank == move.card.rank
        GuideStep.SUM -> move.capture.size >= 2
        GuideStep.LAY -> move.capture.isEmpty()
        GuideStep.PICK, GuideStep.END -> true
    }
}
