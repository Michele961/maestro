package it.maestro.game.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CardModelTest {
    @Test
    fun spec1_1_fullDeckHas40DistinctCardsInCanonicalOrder() {
        val deck = fullDeck()
        assertEquals(40, deck.size)
        assertEquals(40, deck.toSet().size)

        val expectedSuits = listOf(Suit.DENARI, Suit.COPPE, Suit.SPADE, Suit.BASTONI)
        assertEquals(expectedSuits, Suit.entries)

        for (suit in expectedSuits) {
            assertEquals(10, deck.count { it.suit == suit })
        }
        for (rank in Rank.entries) {
            assertEquals(4, deck.count { it.rank == rank })
        }

        val expectedOrder = buildList {
            for (suit in expectedSuits) {
                for (rank in Rank.entries.sortedBy { it.value }) {
                    add(Card(rank = rank, suit = suit))
                }
            }
        }
        assertEquals(expectedOrder, deck)
        assertEquals(deck, deck.sortedWith(CardOrder))
        assertEquals(deck, deck.sorted())
    }

    @Test
    fun spec1_2_rankValuesMatchSpecification() {
        assertEquals(10, Rank.entries.size)
        assertEquals(1, Rank.ASSO.value)
        assertEquals(2, Rank.DUE.value)
        assertEquals(3, Rank.TRE.value)
        assertEquals(4, Rank.QUATTRO.value)
        assertEquals(5, Rank.CINQUE.value)
        assertEquals(6, Rank.SEI.value)
        assertEquals(7, Rank.SETTE.value)
        assertEquals(8, Rank.FANTE.value)
        assertEquals(9, Rank.CAVALLO.value)
        assertEquals(10, Rank.RE.value)
    }

    @Test
    fun spec4_9_canonicalCardOrderMatchesSpecificationExample() {
        // spec §4.8 / §4.9: tavolo AD, 3C, 4S, 5B, 7C -> ordine canonico AD, 3C, 7C, 4S, 5B
        val input = cards("AD", "3C", "4S", "5B", "7C")
        val expected = cards("AD", "3C", "7C", "4S", "5B")
        assertEquals(expected, input.sortedWith(CardOrder))
        assertEquals(expected, input.sorted())
    }

    @Test
    fun seatOtherReturnsOppositeSeat() {
        assertEquals(Seat.P2, Seat.P1.other())
        assertEquals(Seat.P1, Seat.P2.other())
    }

    @Test
    fun testNotationParsesAll40ValidCards() {
        val rankSymbols = listOf(
            'A' to Rank.ASSO,
            '2' to Rank.DUE,
            '3' to Rank.TRE,
            '4' to Rank.QUATTRO,
            '5' to Rank.CINQUE,
            '6' to Rank.SEI,
            '7' to Rank.SETTE,
            'F' to Rank.FANTE,
            'C' to Rank.CAVALLO,
            'R' to Rank.RE,
        )
        val suitSymbols = listOf(
            'D' to Suit.DENARI,
            'C' to Suit.COPPE,
            'S' to Suit.SPADE,
            'B' to Suit.BASTONI,
        )

        val notations = mutableListOf<String>()
        for ((suitChar, suit) in suitSymbols) {
            for ((rankChar, rank) in rankSymbols) {
                val notation = "$rankChar$suitChar"
                notations.add(notation)
                assertEquals(Card(rank = rank, suit = suit), card(notation))
            }
        }
        assertEquals(fullDeck(), cards(*notations.toTypedArray()))
    }

    @Test
    fun testNotationRejectsInvalidInputsWithClearMessage() {
        val invalidNotations = listOf(
            "",
            "A",
            "7",
            "8D",
            "9C",
            "10D",
            "KD",
            "QD",
            "JD",
            "1D",
            "0D",
            "AX",
            "7X",
            "ad",
            "7d",
            "7D ",
            " 7D",
        )
        for (invalid in invalidNotations) {
            val error = assertFailsWith<IllegalArgumentException>(
                message = "Attesa IllegalArgumentException per notazione '$invalid'",
            ) {
                card(invalid)
            }
            val message = error.message.orEmpty()
            assertTrue(
                message.contains(invalid),
                "Il messaggio di errore '$message' dovrebbe citare l'input non valido '$invalid'",
            )
        }
    }
}
