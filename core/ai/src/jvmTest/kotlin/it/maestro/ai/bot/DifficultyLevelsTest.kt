package it.maestro.ai.bot

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView
import it.maestro.game.scopa.determinize
import it.maestro.game.scopa.sampleCompatibleDistribution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DifficultyLevelsTest {

    @Test
    fun provisionalPrimieraWeightIs0025() {
        assertEquals(0.025, ProvisionalHeuristicWeights.PRIMIERA_DIFF, 1e-9)
    }

    @Test
    fun allFiveLevelsHaveStrictlyIncreasingTargetElo() {
        val levels = DifficultyLevel.entries
        assertEquals(5, levels.size)
        assertEquals(800, DifficultyLevel.PRINCIPIANTE.targetElo)
        assertEquals(1100, DifficultyLevel.DILETTANTE.targetElo)
        assertEquals(1400, DifficultyLevel.INTERMEDIO.targetElo)
        assertEquals(1700, DifficultyLevel.AVANZATO.targetElo)
        assertEquals(2000, DifficultyLevel.MAESTRO.targetElo)

        for (i in 0 until levels.size - 1) {
            assertTrue(
                levels[i + 1].targetElo > levels[i].targetElo,
                "L'Elo di ${levels[i + 1]} deve essere maggiore di ${levels[i]}",
            )
            assertTrue(
                levels[i + 1].config.budget.samples >= levels[i].config.budget.samples,
                "I campioni di ${levels[i + 1]} devono essere >= ${levels[i]}",
            )
            assertTrue(
                levels[i + 1].config.noiseProbability <= levels[i].config.noiseProbability,
                "Il rumore di ${levels[i + 1]} deve essere <= ${levels[i]}",
            )
            assertTrue(
                levels[i + 1].config.forgetRate <= levels[i].config.forgetRate,
                "Il forgetRate di ${levels[i + 1]} deve essere <= ${levels[i]}",
            )
        }
    }

    @Test
    fun fromNameOrNullResolvesAllLevelsAndPrefix() {
        for (level in DifficultyLevel.entries) {
            assertEquals(level, DifficultyLevel.fromNameOrNull(level.name))
            assertEquals(level, DifficultyLevel.fromNameOrNull(level.name.lowercase()))
            assertEquals(level, DifficultyLevel.fromNameOrNull("level:${level.name}"))
            assertEquals(level, DifficultyLevel.fromNameOrNull("level:${level.name.lowercase()}"))
        }
        assertEquals(null, DifficultyLevel.fromNameOrNull("invalid"))
    }

    @Test
    fun everyLevelIsDeterministicWithSameSeed() {
        val view = createSampleMidgameView()
        val legal = listOf(
            ScopaMove.Discard(Card(Rank.QUATTRO, Suit.SPADE)),
            ScopaMove.Capture(
                card = Card(Rank.SETTE, Suit.COPPE),
                taken = listOf(Card(Rank.SETTE, Suit.BASTONI)),
            ),
        )

        for (level in DifficultyLevel.entries) {
            val bot1 = level.toBot()
            val bot2 = level.toBot()

            for (seed in 1001L..1020L) {
                val move1 = bot1.chooseMove(view, legal, SeededRng(seed))
                val move2 = bot2.chooseMove(view, legal, SeededRng(seed))
                assertEquals(
                    move1,
                    move2,
                    "Livello $level non deterministico con seed $seed",
                )
            }
        }
    }

    @Test
    fun imperfectMemoryUsesOnlyViewAndPreserves40CardsInvariant() {
        val originalView = createSampleMidgameView()
        val rng = SeededRng(42L)

        // Verifica con forgetRate 1.0 (dimentica e riassegna casualmente le prese passate)
        val degradedView = applyImperfectMemory(originalView, forgetRate = 1.0, rng = rng)

        // La mano corrente, la tavola, le dimensioni e il mazzo non devono mai essere modificate
        assertEquals(originalView.hand, degradedView.hand)
        assertEquals(originalView.table, degradedView.table)
        assertEquals(originalView.opponentHandSize, degradedView.opponentHandSize)
        assertEquals(originalView.deckSize, degradedView.deckSize)

        // Il conteggio complessivo delle carte prese rimane identico (nessuna carta viene persa o inventata)
        val originalCapturedCount = originalView.capturedBy.values.sumOf { it.size }
        val degradedCapturedCount = degradedView.capturedBy.values.sumOf { it.size }
        assertEquals(originalCapturedCount, degradedCapturedCount)

        // L'invariante delle 40 carte totali per la determinizzazione è preservato perfettamente
        val knownLocations = degradedView.hand.size + degradedView.table.size + degradedCapturedCount
        val totalCards = knownLocations + degradedView.opponentHandSize + degradedView.deckSize
        assertEquals(40, totalCards)

        // Verifichiamo che sampleCompatibleDistribution e determinize funzionino senza eccezioni
        val distribution = sampleCompatibleDistribution(degradedView, unseenCards(degradedView))
        val determinizedState = determinize(
            view = degradedView,
            opponentHand = distribution.opponentHand,
            deckOrder = distribution.deckOrder,
        )
        assertEquals(40, determinizedState.allKnownCardsCount())
    }

    @Test
    fun imperfectMemoryWithZeroRateReturnsSameView() {
        val view = createSampleMidgameView()
        val rng = SeededRng(12345L)
        val result = applyImperfectMemory(view, forgetRate = 0.0, rng = rng)
        assertEquals(view, result)
    }

    private fun createSampleMidgameView(): ScopaView {
        val hand = listOf(
            Card(Rank.SETTE, Suit.COPPE),
            Card(Rank.QUATTRO, Suit.SPADE),
            Card(Rank.DUE, Suit.DENARI),
        )
        val table = listOf(
            Card(Rank.SETTE, Suit.BASTONI),
            Card(Rank.CINQUE, Suit.DENARI),
        )
        val p1Captured = listOf(
            Card(Rank.ASSO, Suit.DENARI),
            Card(Rank.RE, Suit.DENARI),
        )
        val p2Captured = listOf(
            Card(Rank.TRE, Suit.COPPE),
            Card(Rank.FANTE, Suit.SPADE),
        )
        val played = listOf(
            Card(Rank.ASSO, Suit.DENARI),
            Card(Rank.TRE, Suit.COPPE),
            Card(Rank.RE, Suit.DENARI),
            Card(Rank.FANTE, Suit.SPADE),
        )
        // 3 in hand + 2 on table + 2 P1 + 2 P2 = 9 note.
        // 40 - 9 = 31 incognite: 3 mano avversario + 28 tallone.
        return ScopaView(
            seat = Seat.P1,
            config = ScopaConfig(),
            dealer = Seat.P2,
            toMove = Seat.P1,
            phase = DealPhase.PLAYING,
            hand = hand,
            table = table,
            capturedBy = mapOf(Seat.P1 to p1Captured, Seat.P2 to p2Captured),
            playedCards = played,
            opponentHandSize = 3,
            deckSize = 28,
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
            totals = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        )
    }

    private fun it.maestro.game.scopa.ScopaState.allKnownCardsCount(): Int {
        val p1Hand = hands[Seat.P1].orEmpty().size
        val p2Hand = hands[Seat.P2].orEmpty().size
        val tbl = table.size
        val dck = deck.size
        val p1Cap = capturedBy[Seat.P1].orEmpty().size
        val p2Cap = capturedBy[Seat.P2].orEmpty().size
        return p1Hand + p2Hand + tbl + dck + p1Cap + p2Cap
    }
}
