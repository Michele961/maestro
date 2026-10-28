package it.maestro.app.data.local

import it.maestro.app.domain.puzzle.PuzzleLesson
import it.maestro.app.domain.puzzle.DailyPuzzleTier

import androidx.annotation.StringRes
import it.maestro.ai.puzzle.PuzzleGoal
import it.maestro.ai.puzzle.TacticalPuzzle
import it.maestro.ai.puzzle.TacticalPuzzleGenerator
import it.maestro.app.R
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit
import it.maestro.app.domain.time.SystemClock
import java.time.LocalDate

/**
 * Provider deterministico per la selezione e generazione della "Sfida del Giorno" (task F2-03).
 *
 * Mappa deterministicamente la data del calendario locale a un seed a 64 bit identico per tutti
 * i giocatori nel medesimo giorno, offrendo 3 livelli di difficoltà crescente (Riscaldamento,
 * Tattica, Maestro) per circa 5 minuti di allenamento giornaliero mirato.
 */
public object DailyPuzzleProvider {

    /**
     * I 3 livelli giornalieri progressivi.
     */
    public val TIERS: List<DailyPuzzleTier> = listOf(
        DailyPuzzleTier(
            index = 0,
            titleRes = R.string.puzzle_tier_easy_title,
            subtitleRes = R.string.puzzle_tier_easy_subtitle,
            rewardCredits = 15,
        ),
        DailyPuzzleTier(
            index = 1,
            titleRes = R.string.puzzle_tier_medium_title,
            subtitleRes = R.string.puzzle_tier_medium_subtitle,
            rewardCredits = 20,
        ),
        DailyPuzzleTier(
            index = 2,
            titleRes = R.string.puzzle_tier_hard_title,
            subtitleRes = R.string.puzzle_tier_hard_subtitle,
            rewardCredits = 25,
        ),
    )

    /**
     * Data di riferimento per la numerazione progressiva dei puzzle quotidiani (#1 = 2026-10-12).
     */
    private val REFERENCE_EPOCH_DATE: LocalDate = LocalDate.of(2026, 10, 12)

    /**
     * Calcola il numero progressivo del puzzle a partire dalla data (es. #14, #15).
     */
    public fun getPuzzleNumber(date: LocalDate = SystemClock.today()): Int {
        val days = date.toEpochDay() - REFERENCE_EPOCH_DATE.toEpochDay() + 1
        return maxOf(1, days.toInt())
    }

    /**
     * Calcola il seed deterministico a 64 bit a partire dalla data del giorno.
     */
    public fun getDailySeed(date: LocalDate = SystemClock.today()): Long {
        return date.year * 10000L + date.monthValue * 100L + date.dayOfMonth
    }

    /**
     * Genera deterministicamente il [TacticalPuzzle] della giornata per il livello specificato.
     *
     * Per retrocompatibilità con seed e test esistenti, il livello 0 usa esattamente il generatore base.
     */
    public fun getDailyPuzzle(date: LocalDate = SystemClock.today(), tierIndex: Int = 0): TacticalPuzzle {
        val baseSeed = getDailySeed(date)
        return when (tierIndex) {
            0 -> TacticalPuzzleGenerator.generate(seed = baseSeed)
            1 -> TacticalPuzzleGenerator.generate(seed = baseSeed + 1000L, goal = PuzzleGoal.MAXIMIZE_POINTS)
            2 -> TacticalPuzzleGenerator.generate(seed = baseSeed + 2000L, goal = PuzzleGoal.WIN_MATCH)
            else -> TacticalPuzzleGenerator.generate(seed = baseSeed + tierIndex * 1000L)
        }
    }

    /**
     * Il codice della lezione che il puzzle illustra: dice cosa insegna la mossa giusta. I testi (concetto e
     * regola d'oro) stanno in `strings.xml`, non qui (ADR-0007).
     */
    public fun lessonOf(puzzle: TacticalPuzzle): PuzzleLesson {
        val first = puzzle.firstMove
        val card = first.card
        val capture = first.capture
        val table = puzzle.initialView.table

        val isSweep = table.isNotEmpty() && capture.containsAll(table) && table.containsAll(capture)
        val settebello = Card(Rank.SETTE, Suit.DENARI)
        val takesSettebello = capture.contains(settebello) || (card == settebello && capture.isNotEmpty())

        return when {
            isSweep || puzzle.goal == PuzzleGoal.MAKE_SWEEP -> PuzzleLesson.SWEEP
            takesSettebello -> PuzzleLesson.SETTEBELLO
            capture.any { it.rank == Rank.SETTE || it.rank == Rank.SEI } -> PuzzleLesson.SEVEN_OR_SIX
            capture.count { it.suit == Suit.DENARI } >= 2 -> PuzzleLesson.DENARI
            puzzle.goal == PuzzleGoal.WIN_MATCH -> PuzzleLesson.WIN_MATCH
            else -> PuzzleLesson.BALANCE
        }
    }
}
