package it.maestro.app.data

import it.maestro.app.data.local.missions.MatchMissionStatsExtractor
import it.maestro.game.CoreGame
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchMissionStatsExtractorTest {

    @Test
    fun extract_computesSweepsAndDenariFromReplay() {
        val config = ScopaConfig(targetScore = 11)
        var state = newMatch(config = config, seed = 42L)
        val moves = mutableListOf<ScopaMove>()

        // Simula una smazzata completa (36 mosse)
        for (i in 0 until 36) {
            val legal = legalMoves(state)
            if (legal.isEmpty()) break
            val move = legal.first()
            val result = play(state, move) as PlayResult.Applied
            moves.add(move)
            state = result.state
        }

        val log = MatchLog(
            rulesVersion = CoreGame.RULES_VERSION,
            config = config,
            seed = 42L,
            moves = moves,
        )

        val stats = MatchMissionStatsExtractor.extract(log, Seat.P1)
        assertNotNull(stats)
        assertTrue("Sweeps deve essere >= 0", stats.sweepsCount >= 0)
        assertTrue("Denari deve essere >= 0 e <= 10", stats.denariCount in 0..10)
    }
}
