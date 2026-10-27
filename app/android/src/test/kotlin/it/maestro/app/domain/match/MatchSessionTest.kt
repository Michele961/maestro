package it.maestro.app.domain.match

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.feature.table.presentation.GameIntent
import it.maestro.app.feature.table.presentation.GameReducer
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MatchSessionTest {

    private val config = ScopaConfig(targetScore = 11)

    @Test
    fun playAppliesMoveAndProducesBeatWithViewsBeforeAndAfter() {
        val session = MatchSession.start(config, seed = 42L)
        val mover = session.toMove
        val move = session.legalMovesFor(mover).first()

        val applied = assertIs<SessionPlay.Applied>(session.play(move, Seat.P1))

        assertEquals(1L, applied.beat.id)
        assertEquals(mover, applied.beat.actor)
        assertEquals(move, applied.beat.move)
        assertEquals(listOf(move), applied.session.log.moves)
        assertEquals(session.view(Seat.P1), applied.beat.viewBefore)
        assertEquals(applied.session.view(Seat.P1), applied.beat.viewAfter)
    }

    @Test
    fun illegalMoveIsRejectedWithoutException() {
        val session = MatchSession.start(config, seed = 42L)
        val bogus = ScopaMove(card = session.view(Seat.P1).table.first(), capture = emptySet())

        assertIs<SessionPlay.Rejected>(session.play(bogus, Seat.P1))
    }

    @Test
    fun botMoveIsOnlyChosenOnItsTurn() {
        val session = MatchSession.start(config, seed = 42L)
        val bot = DifficultyLevel.PRINCIPIANTE.toBot()
        val other = session.toMove.other()

        assertNull(session.chooseBotMove(bot, other, SeededRng(1L)))
        assertNotNull(session.chooseBotMove(bot, session.toMove, SeededRng(1L)))
        assertTrue(session.legalMovesFor(other).isEmpty())
    }

    @Test
    fun fromLogReplaysToTheSameSessionAsPlaying() {
        var session = MatchSession.start(config, seed = 7L)
        repeat(12) {
            val move = session.legalMovesFor(session.toMove).first()
            session = (session.play(move, Seat.P1) as SessionPlay.Applied).session
        }

        val restored = assertNotNull(MatchSession.fromLog(session.log))

        assertEquals(session.log, restored.log)
        assertEquals(session.view(Seat.P1), restored.view(Seat.P1))
        assertEquals(session.view(Seat.P2), restored.view(Seat.P2))
    }

    @Test
    fun fullMatchOnFixedSeedProducesTheSameLogThroughReducerAndSession() {
        // Il reducer sulla sessione deve dare lo stesso MatchLog di una partita giocata direttamente (ADR-0003).
        var direct = MatchSession.start(config, seed = 101L)
        var internal = GameReducer.initial(GameIntent.StartMatch(config = config, seed = 101L)).state
        while (direct.phase != DealPhase.MATCH_OVER) {
            val move = direct.legalMovesFor(direct.toMove).first()
            direct = (direct.play(move, Seat.P1) as SessionPlay.Applied).session
            internal = if (internal.session.toMove == internal.humanSeat) {
                GameReducer.reduce(internal, GameIntent.SelectCapture(move.card, move.capture)).state
            } else {
                GameReducer.reduce(internal, GameIntent.ApplyBotMove(move)).state
            }
            if (internal.pendingDealScore != null) {
                internal = GameReducer.reduce(internal, GameIntent.DismissDealSummary).state
            }
        }

        assertEquals(direct.log, internal.matchLog)
    }
}
