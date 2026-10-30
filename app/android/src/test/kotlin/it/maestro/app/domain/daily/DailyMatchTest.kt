package it.maestro.app.domain.daily

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DailyMatchTest {
    private val day = LocalDate.of(2026, 10, 30)

    @Test
    fun `stesso giorno, stesso seed e quindi stessa mano di partenza`() {
        assertEquals(DailyMatch.seedFor(day), DailyMatch.seedFor(LocalDate.of(2026, 10, 30)))
    }

    @Test
    fun `giorni diversi hanno seed diversi`() {
        assertNotEquals(DailyMatch.seedFor(day), DailyMatch.seedFor(day.plusDays(1)))
    }

    @Test
    fun `il seed del giorno si riconosce solo nel giorno`() {
        assertTrue(DailyMatch.isDailySeed(DailyMatch.seedFor(day), day))
        assertFalse(DailyMatch.isDailySeed(DailyMatch.seedFor(day), day.plusDays(1)))
    }

    @Test
    fun `il primo risultato di una data resta`() {
        val first = DailyMatchResult(day, won = true, humanTotal = 21, opponentTotal = 14)
        val again = DailyMatchResult(day, won = false, humanTotal = 10, opponentTotal = 21)

        assertEquals(listOf(first), DailyMatchHistory.add(DailyMatchHistory.add(emptyList(), first), again))
    }

    @Test
    fun `la cronologia e' locale, ordinata e limitata`() {
        val history = (0 until DailyMatchHistory.MAX_ENTRIES + 5).fold(emptyList<DailyMatchResult>()) { acc, i ->
            DailyMatchHistory.add(acc, DailyMatchResult(day.plusDays(i.toLong()), i % 2 == 0, 21, 5))
        }

        assertEquals(DailyMatchHistory.MAX_ENTRIES, history.size)
        assertEquals(day.plusDays(DailyMatchHistory.MAX_ENTRIES + 4L), history.first().date)
        assertEquals(history.count { it.won }, DailyMatchHistory.wins(history))
    }

    @Test
    fun `la cronologia sopravvive al salvataggio e scarta le voci rotte`() {
        val history = listOf(
            DailyMatchResult(day.plusDays(1), won = false, humanTotal = 9, opponentTotal = 21),
            DailyMatchResult(day, won = true, humanTotal = 21, opponentTotal = 14),
        )

        assertEquals(history, DailyMatchHistory.decode(DailyMatchHistory.encode(history)))
        assertEquals(history.take(1), DailyMatchHistory.decode(DailyMatchHistory.encode(history.take(1)) + ";rotta"))
        assertEquals(emptyList(), DailyMatchHistory.decode(""))
    }
}
