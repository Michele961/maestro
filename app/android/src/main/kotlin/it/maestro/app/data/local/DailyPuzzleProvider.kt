package it.maestro.app.data.local

import it.maestro.ai.puzzle.TacticalPuzzle
import it.maestro.ai.puzzle.TacticalPuzzleGenerator
import java.time.LocalDate

/**
 * Provider deterministico per la selezione e generazione della "Sfida del Giorno" (task F2-03).
 *
 * Mappa deterministicamente la data del calendario locale a un seed a 64 bit identico per tutti
 * i giocatori nel medesimo giorno.
 */
public object DailyPuzzleProvider {

    /**
     * Data di riferimento per la numerazione progressiva dei puzzle quotidiani (#1 = 2026-10-12).
     */
    private val REFERENCE_EPOCH_DATE: LocalDate = LocalDate.of(2026, 10, 12)

    /**
     * Calcola il numero progressivo del puzzle a partire dalla data (es. #14, #15).
     */
    public fun getPuzzleNumber(date: LocalDate = LocalDate.now()): Int {
        val days = date.toEpochDay() - REFERENCE_EPOCH_DATE.toEpochDay() + 1
        return maxOf(1, days.toInt())
    }

    /**
     * Calcola il seed deterministico a 64 bit a partire dalla data del giorno.
     */
    public fun getDailySeed(date: LocalDate = LocalDate.now()): Long {
        return date.year * 10000L + date.monthValue * 100L + date.dayOfMonth
    }

    /**
     * Genera deterministicamente il [TacticalPuzzle] della giornata corrente o della data specificata.
     */
    public fun getDailyPuzzle(date: LocalDate = LocalDate.now()): TacticalPuzzle {
        val seed = getDailySeed(date)
        return TacticalPuzzleGenerator.generate(seed = seed)
    }
}
