package it.maestro.game.scopa

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.cards
import it.maestro.game.model.fullDeck
import it.maestro.game.rng.SeededRng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ScopaNewMatchTest {

    @Test
    fun spec8_defaultConfigMatchesSpecification() {
        val config = ScopaConfig()
        assertEquals(11, config.targetScore)
        assertEquals(true, config.redealOnThreeKings)
        assertEquals(false, config.reBello)
    }

    @Test
    fun spec7_1_targetScoreAccepts11And16And21AndRejectsOtherValues() {
        for (validTarget in listOf(11, 16, 21)) {
            val config = ScopaConfig(targetScore = validTarget)
            assertEquals(validTarget, config.targetScore)
        }

        val invalidTargets = listOf(-1, 0, 1, 10, 12, 15, 17, 20, 22, 31, 100)
        for (invalidTarget in invalidTargets) {
            assertFailsWith<IllegalArgumentException>(
                message = "Attesa IllegalArgumentException per targetScore = $invalidTarget",
            ) {
                ScopaConfig(targetScore = invalidTarget)
            }
        }
    }

    @Test
    fun spec2_2_sameSeedProducesIdenticalStateAnd100SeedsProduceAtLeast99DistinctStates() {
        val config = ScopaConfig()
        for (seed in 0L until 20L) {
            val stateA = newMatch(config, seed)
            val stateB = newMatch(config, seed)
            assertEquals(stateA, stateB)
        }

        val states = (0L until 100L).map { seed -> newMatch(config, seed) }.toSet()
        assertTrue(
            states.size >= 99,
            "Su 100 seed diversi attesi almeno 99 stati distinti, trovati ${states.size}",
        )
    }

    @Test
    fun spec2_2_spec3_1_spec3_2_spec4_1_newMatchFollowsAdr0009ConsumptionAndDealOrder() {
        // Seed 0: verifica passo per passo contro SeededRng(seed) secondo ADR-0009 e spec §3.2 / §4.1.
        for (seed in listOf(0L, 1L, 2L, 7L, 42L)) {
            val referenceRng = SeededRng(seed)
            val expectedDealer = if (referenceRng.nextInt(2) == 0) Seat.P1 else Seat.P2
            val expectedFirstReceiver = expectedDealer.other()

            var expectedShuffled: List<Card>
            do {
                expectedShuffled = referenceRng.shuffle(fullDeck())
            } while (expectedShuffled.subList(6, 10).count { it.rank == Rank.RE } >= 3)

            val state = newMatch(ScopaConfig(), seed)

            assertEquals(expectedDealer, state.dealer)
            assertEquals(expectedFirstReceiver, state.toMove)
            assertEquals(3, state.hand(Seat.P1).size)
            assertEquals(3, state.hand(Seat.P2).size)
            assertEquals(4, state.table.size)
            assertEquals(30, state.deck.size)

            // spec §3.2: una alla volta dalla cima (indice 0), alternando, iniziando dall'avversario del mazziere.
            assertEquals(
                listOf(expectedShuffled[0], expectedShuffled[2], expectedShuffled[4]),
                state.hand(expectedFirstReceiver),
            )
            assertEquals(
                listOf(expectedShuffled[1], expectedShuffled[3], expectedShuffled[5]),
                state.hand(expectedDealer),
            )
            assertEquals(expectedShuffled.subList(6, 10), state.table)
            assertEquals(expectedShuffled.subList(10, 40), state.deck)
            assertEquals(referenceRng.state, state.rngState)
        }
    }

    @Test
    fun spec3_3_tableWithTwoKingsDoesNotTriggerRedeal() {
        val deckWithTwoKingsOnTable = deckWithTableCards(cards("RD", "RC", "AD", "2C"))
        var dealsCount = 0

        val state = dealOpeningHand(
            config = ScopaConfig(redealOnThreeKings = true),
            dealer = Seat.P1,
            nextShuffledDeck = {
                dealsCount++
                deckWithTwoKingsOnTable
            },
            currentRngState = { 100L + dealsCount },
        )

        assertEquals(1, dealsCount)
        assertEquals(Seat.P1, state.dealer)
        assertEquals(Seat.P2, state.toMove)
        assertEquals(cards("RD", "RC", "AD", "2C"), state.table)
        assertEquals(101L, state.rngState)
    }

    @Test
    fun spec3_3_tableWithThreeKingsTriggersRedealKeepingSameDealerWhenEnabled() {
        val firstDeckWithThreeKings = deckWithTableCards(cards("RD", "RC", "RS", "AD"))
        val secondDeckValid = deckWithTableCards(cards("AD", "2C", "3S", "4B"))
        val sequence = listOf(firstDeckWithThreeKings, secondDeckValid).iterator()
        var dealsCount = 0

        val state = dealOpeningHand(
            config = ScopaConfig(redealOnThreeKings = true),
            dealer = Seat.P2,
            nextShuffledDeck = {
                dealsCount++
                sequence.next()
            },
            currentRngState = { 200L + dealsCount },
        )

        assertEquals(2, dealsCount)
        assertEquals(Seat.P2, state.dealer)
        assertEquals(Seat.P1, state.toMove)
        assertEquals(cards("AD", "2C", "3S", "4B"), state.table)
        assertEquals(
            listOf(secondDeckValid[0], secondDeckValid[2], secondDeckValid[4]),
            state.hand(Seat.P1),
        )
        assertEquals(
            listOf(secondDeckValid[1], secondDeckValid[3], secondDeckValid[5]),
            state.hand(Seat.P2),
        )
        assertEquals(202L, state.rngState)
    }

    @Test
    fun spec3_3_tableWithFourKingsTriggersRedealKeepingSameDealerWhenEnabled() {
        val firstDeckWithFourKings = deckWithTableCards(cards("RD", "RC", "RS", "RB"))
        val secondDeckValid = deckWithTableCards(cards("7D", "FC", "CS", "RB"))
        val sequence = listOf(firstDeckWithFourKings, secondDeckValid).iterator()
        var dealsCount = 0

        val state = dealOpeningHand(
            config = ScopaConfig(redealOnThreeKings = true),
            dealer = Seat.P1,
            nextShuffledDeck = {
                dealsCount++
                sequence.next()
            },
            currentRngState = { 300L + dealsCount },
        )

        assertEquals(2, dealsCount)
        assertEquals(Seat.P1, state.dealer)
        assertEquals(Seat.P2, state.toMove)
        assertEquals(cards("7D", "FC", "CS", "RB"), state.table)
        assertEquals(302L, state.rngState)
    }

    @Test
    fun spec3_3_threeOrFourKingsAreAcceptedWithoutRedealWhenRedealOnThreeKingsIsFalse() {
        val deckWithThreeKings = deckWithTableCards(cards("RD", "RC", "RS", "AD"))
        val deckWithFourKings = deckWithTableCards(cards("RD", "RC", "RS", "RB"))

        var threeKingsDeals = 0
        val stateThreeKings = dealOpeningHand(
            config = ScopaConfig(redealOnThreeKings = false),
            dealer = Seat.P1,
            nextShuffledDeck = {
                threeKingsDeals++
                deckWithThreeKings
            },
        )
        assertEquals(1, threeKingsDeals)
        assertEquals(cards("RD", "RC", "RS", "AD"), stateThreeKings.table)

        var fourKingsDeals = 0
        val stateFourKings = dealOpeningHand(
            config = ScopaConfig(redealOnThreeKings = false),
            dealer = Seat.P2,
            nextShuffledDeck = {
                fourKingsDeals++
                deckWithFourKings
            },
        )
        assertEquals(1, fourKingsDeals)
        assertEquals(cards("RD", "RC", "RS", "RB"), stateFourKings.table)
    }

    @Test
    fun spec1_1_spec3_2_fortyDistinctCardsInvariantHoldsAcross500Seeds() {
        val canonicalSet = fullDeck().toSet()
        for (seed in 0L until 500L) {
            val state = newMatch(ScopaConfig(), seed)
            val p1Hand = state.hand(Seat.P1)
            val p2Hand = state.hand(Seat.P2)

            assertEquals(3, p1Hand.size, "Mano P1 non ha 3 carte per seed=$seed")
            assertEquals(3, p2Hand.size, "Mano P2 non ha 3 carte per seed=$seed")
            assertEquals(4, state.table.size, "Tavolo non ha 4 carte per seed=$seed")
            assertEquals(30, state.deck.size, "Mazzo non ha 30 carte per seed=$seed")
            assertEquals(state.dealer.other(), state.toMove, "toMove != dealer.other() per seed=$seed")
            assertTrue(
                state.table.count { it.rank == Rank.RE } < 3,
                "Trovati >= 3 Re sul tavolo con redealOnThreeKings=true per seed=$seed",
            )

            val allCards = p1Hand + p2Hand + state.table + state.deck
            assertEquals(40, allCards.size, "Totale carte != 40 per seed=$seed")
            assertEquals(canonicalSet, allCards.toSet(), "Insieme carte != mazzo completo per seed=$seed")
        }
    }

    /**
     * Helper di test (autorizzato da F0-04) che costruisce un mazzo valido di 40 carte
     * posizionando esattamente [tableCards] negli indici `6..9` (le 4 carte scoperte sul tavolo).
     */
    private fun deckWithTableCards(tableCards: List<Card>): List<Card> {
        require(tableCards.size == 4 && tableCards.toSet().size == 4) {
            "Servono 4 carte distinte per il tavolo, ricevute: $tableCards"
        }
        val remaining = fullDeck().filterNot { it in tableCards }
        return buildList(40) {
            addAll(remaining.subList(0, 6))
            addAll(tableCards)
            addAll(remaining.subList(6, remaining.size))
        }
    }
}
