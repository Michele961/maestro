package it.maestro.ai.analysis

import it.maestro.ai.bot.Bot
import it.maestro.ai.bot.GreedyBot
import it.maestro.ai.bot.PimcBot
import it.maestro.ai.bot.RandomBot
import it.maestro.ai.bot.SearchBudget
import it.maestro.game.CoreGame
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import it.maestro.game.scopa.viewFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Suite di test deterministici per la revisione con giocatore esperto (`docs/ai/maestro.md`,
 * `docs/backlog/f0-12-note.md`, task F0-12).
 *
 * Esegue e valida programmaticamente 20 partite complete bot contro bot (PIMC vs Greedy,
 * Greedy vs Random, PIMC vs PIMC, Random vs Random) con [MatchAnalyzer], verificando che
 * ogni partita termini regolarmente a [DealPhase.MATCH_OVER] e produca statistiche
 * di precisione, perdita media e classi [MoveGrade] coerenti con l'analisi teorica.
 */
class ExpertReviewGamesTest {

    data class GameSpec(
        val gameId: Int,
        val seed: Long,
        val p1Name: String,
        val p2Name: String,
        val p1Bot: Bot,
        val p2Bot: Bot,
    )

    data class GameResultData(
        val spec: GameSpec,
        val winner: Seat,
        val scoreP1: Int,
        val scoreP2: Int,
        val totalMoves: Int,
        val analysis: MatchAnalysis,
    )

    private fun playDeterministicGame(
        matchSeed: Long,
        p1Bot: Bot,
        p2Bot: Bot,
        p1RngSeed: Long,
        p2RngSeed: Long,
        config: ScopaConfig = ScopaConfig(),
    ): Pair<MatchLog, Pair<Int, Int>> {
        var state = newMatch(config = config, seed = matchSeed)
        val p1Rng = SeededRng(p1RngSeed)
        val p2Rng = SeededRng(p2RngSeed)
        val moves = ArrayList<ScopaMove>()

        while (state.phase != DealPhase.MATCH_OVER) {
            val mover = state.toMove
            val bot = if (mover == Seat.P1) p1Bot else p2Bot
            val rng = if (mover == Seat.P1) p1Rng else p2Rng

            val view = viewFor(state, mover)
            val legal = legalMoves(state)
            val chosen = bot.chooseMove(view, legal, rng)
            moves.add(chosen)

            state = when (val result = play(state, chosen)) {
                is PlayResult.Applied -> result.state
                is PlayResult.Rejected -> error("Mossa rifiutata: ${result.reason}")
            }
        }

        return Pair(
            MatchLog(
                rulesVersion = CoreGame.RULES_VERSION,
                config = config,
                seed = matchSeed,
                moves = moves,
            ),
            Pair(state.totalScore(Seat.P1), state.totalScore(Seat.P2)),
        )
    }

    private fun build20GameSpecs(): List<GameSpec> {
        val pimcBot = PimcBot(SearchBudget(samples = 6, maxNodes = 80))
        val greedyBot = GreedyBot()
        val randomBot = RandomBot

        val master = SeededRng(20261023L)
        val specs = ArrayList<GameSpec>(20)

        for (i in 1..20) {
            val seed = master.nextLong()
            val (p1Name, p2Name, p1Bot, p2Bot) = when {
                i <= 10 -> {
                    if (i % 2 == 1) {
                        listOf("PimcBot", "GreedyBot", pimcBot, greedyBot)
                    } else {
                        listOf("GreedyBot", "PimcBot", greedyBot, pimcBot)
                    }
                }
                i <= 15 -> {
                    if (i % 2 == 1) {
                        listOf("GreedyBot", "RandomBot", greedyBot, randomBot)
                    } else {
                        listOf("RandomBot", "GreedyBot", randomBot, greedyBot)
                    }
                }
                i <= 18 -> {
                    listOf("PimcBot", "PimcBot", pimcBot, pimcBot)
                }
                else -> {
                    listOf("RandomBot", "RandomBot", randomBot, randomBot)
                }
            }
            specs.add(
                GameSpec(
                    gameId = i,
                    seed = seed,
                    p1Name = p1Name as String,
                    p2Name = p2Name as String,
                    p1Bot = p1Bot as Bot,
                    p2Bot = p2Bot as Bot,
                ),
            )
        }
        return specs
    }

    @Test
    fun executeAndValidate20DeterministicGames() {
        val specs = build20GameSpecs()
        assertEquals(20, specs.size)

        val analyzerBudget = SearchBudget(samples = 8, maxNodes = 100)
        val analyzer = MatchAnalyzer(budget = analyzerBudget)

        val results = ArrayList<GameResultData>(20)
        var totalAnalyzedMoves = 0

        for (spec in specs) {
            val (log, scores) = playDeterministicGame(
                matchSeed = spec.seed,
                p1Bot = spec.p1Bot,
                p2Bot = spec.p2Bot,
                p1RngSeed = spec.seed xor 0x55555555L,
                p2RngSeed = spec.seed xor 0xAAAAAAAAL,
            )
            assertTrue(log.moves.isNotEmpty(), "La partita ${spec.gameId} non deve avere 0 mosse")
            totalAnalyzedMoves += log.moves.size

            val analysis = analyzer.analyze(log)
            val winner = if (scores.first > scores.second) Seat.P1 else Seat.P2

            val result = GameResultData(
                spec = spec,
                winner = winner,
                scoreP1 = scores.first,
                scoreP2 = scores.second,
                totalMoves = log.moves.size,
                analysis = analysis,
            )
            results.add(result)

            // Validazioni di coerenza su ciascun giocatore
            for (seat in Seat.entries) {
                val acc = analysis.accuracy(seat)
                val meanLoss = analysis.meanLoss(seat)
                val bests = analysis.count(seat, MoveGrade.BEST)
                val inaccs = analysis.count(seat, MoveGrade.INACCURACY)
                val blunders = analysis.count(seat, MoveGrade.BLUNDER)
                val totalSeatMoves = analysis.moves.count { it.seat == seat }

                assertEquals(totalSeatMoves, bests + inaccs + blunders)
                assertTrue(acc in 0..100, "Precisione deve essere in 0..100: $acc")
                assertTrue(meanLoss >= 0.0, "meanLoss deve essere >= 0: $meanLoss")
            }
        }

        assertEquals(20, results.size)
        assertEquals(2736, totalAnalyzedMoves, "Il totale delle mosse analizzate nelle 20 partite deve essere 2736 (PRIMIERA_DIFF=0.025 D2)")

        // Verifiche aggregate sui profili di abilità dei bot
        // 1. Partite PIMC vs PIMC (16, 17, 18): precisione sempre >= 94% e perdita media ridotta
        for (idx in 15..17) {
            val r = results[idx]
            assertTrue(r.analysis.accuracy(Seat.P1) >= 94, "PimcBot P1 in Game #${r.spec.gameId} deve avere acc >= 94%")
            assertTrue(r.analysis.accuracy(Seat.P2) >= 94, "PimcBot P2 in Game #${r.spec.gameId} deve avere acc >= 94%")
            assertTrue(r.analysis.meanLoss(Seat.P1) <= 0.025)
            assertTrue(r.analysis.meanLoss(Seat.P2) <= 0.025)
        }

        // 2. Partite Greedy vs Random (11..15): RandomBot genera sistematicamente almeno 5 errori gravi (BLUNDER)
        for (idx in 10..14) {
            val r = results[idx]
            val randomSeat = if (r.spec.p1Name == "RandomBot") Seat.P1 else Seat.P2
            val greedySeat = randomSeat.other()

            assertTrue(
                r.analysis.count(randomSeat, MoveGrade.BLUNDER) >= 5,
                "RandomBot in Game #${r.spec.gameId} deve commettere almeno 5 errori gravi (BLUNDER)",
            )
            assertTrue(
                r.analysis.accuracy(randomSeat) <= 75,
                "RandomBot in Game #${r.spec.gameId} deve avere precisione <= 75%",
            )
            assertTrue(
                r.analysis.accuracy(greedySeat) >= 75,
                "GreedyBot in Game #${r.spec.gameId} deve avere precisione >= 75% (trovata: ${r.analysis.accuracy(greedySeat)}%)",
            )
        }
    }

    @Test
    fun validateKeyTacticalScenariosFromExpertReview() {
        // Verifica specifica delle decisioni tattiche cardine documentate in docs/backlog/f0-12-note.md §2:
        // 1. Game #11 Turn #2: RandomBot ignora la presa del Settebello sul tavolo -> BLUNDER con perdita > 2.0
        val specs = build20GameSpecs()
        val analyzer = MatchAnalyzer(budget = SearchBudget(samples = 8, maxNodes = 100))

        val spec11 = specs[10] // Game #11
        val (log11, _) = playDeterministicGame(
            matchSeed = spec11.seed,
            p1Bot = spec11.p1Bot,
            p2Bot = spec11.p2Bot,
            p1RngSeed = spec11.seed xor 0x55555555L,
            p2RngSeed = spec11.seed xor 0xAAAAAAAAL,
        )
        val analysis11 = analyzer.analyze(log11)

        val settebelloBlunder = analysis11.moves[1] // Turn #2 (index 1)
        assertEquals(Seat.P2, settebelloBlunder.seat)
        assertEquals(MoveGrade.BLUNDER, settebelloBlunder.grade)
        assertTrue(settebelloBlunder.loss > 2.0, "La perdita per aver ignorato il Settebello deve essere > 2.0")

        // 2. Game #1 Turn #69: GreedyBot serve una scopa da 10 all'avversario -> BLUNDER con perdita esatta 1.0
        val spec1 = specs[0] // Game #1
        val (log1, _) = playDeterministicGame(
            matchSeed = spec1.seed,
            p1Bot = spec1.p1Bot,
            p2Bot = spec1.p2Bot,
            p1RngSeed = spec1.seed xor 0x55555555L,
            p2RngSeed = spec1.seed xor 0xAAAAAAAAL,
        )
        val analysis1 = analyzer.analyze(log1)

        val scopaConcededBlunder = analysis1.moves[68] // Turn #69 (index 68)
        assertEquals(Seat.P2, scopaConcededBlunder.seat)
        assertEquals(MoveGrade.BLUNDER, scopaConcededBlunder.grade)
        assertEquals(1.0, scopaConcededBlunder.loss, "La perdita per aver servito la scopa deve essere esattamente 1.0")
    }
}
