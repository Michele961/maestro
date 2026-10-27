package it.maestro.app.feature.table.presentation

import it.maestro.app.domain.coach.Chattiness
import it.maestro.app.domain.coach.MomentKind
import it.maestro.app.domain.coach.VoiceLine
import it.maestro.app.domain.match.MatchSession
import it.maestro.app.domain.match.SessionPlay
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Task F1R-24d (`docs/app/features/maestro-voice.md` §3.4): il Maestro commenta la partita senza esagerare. */
class MaestroVoiceTest {

    private val human = Seat.P1

    /** Gioca una partita intera sempre la prima mossa e raccoglie cosa avrebbe detto il Maestro, per ogni mossa. */
    private fun transcript(seed: Long, chattiness: Chattiness): List<Pair<Int, VoiceLine>> {
        val voice = MaestroVoice(seed, human)
        val lines = mutableListOf<Pair<Int, VoiceLine>>()
        var session = MatchSession.start(ScopaConfig(targetScore = 11), seed)
        voice.greeting(chattiness, hintEverUsed = false)?.let { lines += 0 to it }
        var move = 0
        while (session.phase == DealPhase.PLAYING) {
            val choice = session.legalMovesFor(session.toMove).firstOrNull() ?: break
            val applied = session.play(choice, human) as SessionPlay.Applied
            session = applied.session
            move++
            voice.onBeat(applied.beat, session, chattiness, hintEverUsed = false)?.let { lines += move to it }
        }
        return lines
    }

    @Test
    fun greetsAtTheStartWhenAllowed() {
        assertEquals(MomentKind.GREETING, MaestroVoice(1L, human).greeting(Chattiness.NORMAL, false)?.kind)
        assertNull(MaestroVoice(1L, human).greeting(Chattiness.OFF, false))
    }

    @Test
    fun silentWhenSetToNever() {
        (1L..20L).forEach { seed -> assertTrue(transcript(seed, Chattiness.OFF).isEmpty()) }
    }

    @Test
    fun speaksButNeverTwiceInARowOnConsecutiveMovesAtNormal() {
        var total = 0
        (1L..30L).forEach { seed ->
            val lines = transcript(seed, Chattiness.NORMAL)
            total += lines.size
            lines.zipWithNext().filter { (a, b) -> a.first >= 1 && !b.second.kind.urgent }.forEach { (a, b) ->
                // Tra due battute passano almeno `minGapMoves` mosse del giocatore (2 mosse per giro, meno una al bordo).
                assertTrue(b.first - a.first >= 2 * Chattiness.NORMAL.minGapMoves - 1 || b.second.kind.urgent, "Battute ravvicinate (seed $seed): ${a.first} e ${b.first}")
            }
        }
        assertTrue(total > 30, "Su 30 partite il Maestro deve parlare ogni tanto: $total")
    }

    @Test
    fun sameSeedSameTranscript() {
        assertEquals(transcript(7L, Chattiness.TALKATIVE), transcript(7L, Chattiness.TALKATIVE))
        assertNotNull(transcript(7L, Chattiness.TALKATIVE).firstOrNull())
    }
}
