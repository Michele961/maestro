package it.maestro.game.rng

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class SeededRngTest {

    @Test
    fun splitMix64ReferenceVectorsSeed0() {
        val rng = SeededRng(0L)
        val expected = listOf(
            "e220a8397b1dcdaf",
            "6e789e6aa1b965f4",
            "06c45d188009454f",
            "f88bb8a8724c81ec",
            "1b39896a51a8749b",
        )
        val actual = List(5) { rng.nextLong().toHexUnsigned() }
        assertEquals(expected, actual)
    }

    @Test
    fun splitMix64ReferenceVectorsSeed42() {
        val rng = SeededRng(42L)
        val expected = listOf(
            "bdd732262feb6e95",
            "28efe333b266f103",
            "47526757130f9f52",
            "581ce1ff0e4ae394",
            "09bc585a244823f2",
        )
        val actual = List(5) { rng.nextLong().toHexUnsigned() }
        assertEquals(expected, actual)
    }

    @Test
    fun nextIntRejectsNonPositiveBound() {
        val rng = SeededRng(1L)
        assertFailsWith<IllegalArgumentException> { rng.nextInt(0) }
        assertFailsWith<IllegalArgumentException> { rng.nextInt(-1) }
        assertFailsWith<IllegalArgumentException> { rng.nextInt(Int.MIN_VALUE) }
    }

    @Test
    fun nextIntStaysWithinBoundsAndHandlesEdgeCases() {
        val rng = SeededRng(12345L)
        repeat(100) {
            assertEquals(0, rng.nextInt(1))
        }
        repeat(1_000) {
            val v = rng.nextInt(40)
            assertTrue(v in 0 until 40, "Expected value in 0..39, got $v")
        }
    }

    @Test
    fun stateCanBeExportedAndRestored() {
        val rng = SeededRng(99L)
        repeat(10) { rng.nextLong() }
        val savedState = rng.state

        val restoredViaConstructor = SeededRng(savedState)
        val restoredViaFactory = SeededRng.fromState(savedState)

        val expectedLongs = List(20) { rng.nextLong() }
        val actualFromConstructor = List(20) { restoredViaConstructor.nextLong() }
        val actualFromFactory = List(20) { restoredViaFactory.nextLong() }

        assertEquals(expectedLongs, actualFromConstructor)
        assertEquals(expectedLongs, actualFromFactory)
        assertEquals(rng.state, restoredViaConstructor.state)
        assertEquals(rng.state, restoredViaFactory.state)
    }

    @Test
    fun forkAdvancesParentByOneNextLongAndCreatesDeterministicChild() {
        val rng1 = SeededRng(7L)
        val rng2 = SeededRng(7L)

        val child = rng1.fork()
        val expectedChildSeed = rng2.nextLong()
        assertEquals(rng2.state, rng1.state)

        val expectedChildSequence = SeededRng(expectedChildSeed)
        repeat(10) {
            assertEquals(expectedChildSequence.nextLong(), child.nextLong())
            assertEquals(rng2.nextLong(), rng1.nextLong())
        }
    }

    @Test
    fun spec3_1_shuffleReturnsPermutationWithoutMutatingInputAndIsDeterministic() {
        val original = (0 until 40).toList()
        val snapshot = original.toList()

        val rngA = SeededRng(42L)
        val rngB = SeededRng(42L)

        val shuffledA = rngA.shuffle(original)
        val shuffledB = rngB.shuffle(original)

        assertEquals(snapshot, original)
        assertNotSame(original, shuffledA)
        assertEquals(shuffledA, shuffledB)
        assertEquals(original.size, shuffledA.size)
        assertEquals(original.toSet(), shuffledA.toSet())
    }

    @Test
    fun spec3_1_shuffleOfEmptyOrSingleElementDoesNotConsumeRng() {
        val rng = SeededRng(10L)
        val initialState = rng.state

        assertEquals(emptyList(), rng.shuffle(emptyList<Int>()))
        assertEquals(initialState, rng.state)

        assertEquals(listOf(1), rng.shuffle(listOf(1)))
        assertEquals(initialState, rng.state)
    }

    @Test
    fun spec3_1_shuffleUniformityOver100kIterationsOf40Elements() {
        val n = 40
        val iterations = 100_000
        val deck = (0 until n).toList()
        val rng = SeededRng(1L)
        val counts = Array(n) { IntArray(n) }

        repeat(iterations) {
            val shuffled = rng.shuffle(deck)
            for (pos in 0 until n) {
                counts[shuffled[pos]][pos]++
            }
        }

        val expected = iterations / n // 2_500
        val minAllowed = (expected * 0.8).toInt() // 2_000
        val maxAllowed = (expected * 1.2).toInt() // 3_000

        for (element in 0 until n) {
            for (pos in 0 until n) {
                val c = counts[element][pos]
                assertTrue(
                    c in minAllowed..maxAllowed,
                    "Frequenza fuori tolleranza per elemento $element in posizione $pos: $c (atteso $minAllowed..$maxAllowed)",
                )
            }
        }
    }

    private fun Long.toHexUnsigned(): String =
        toULong().toString(16).padStart(16, '0')
}
