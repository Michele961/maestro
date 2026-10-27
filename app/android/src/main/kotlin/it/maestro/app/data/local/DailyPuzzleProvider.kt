package it.maestro.app.data.local

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
 * Modello descrittivo per i livelli di difficoltà progressivi della Sfida del Giorno.
 */
public data class DailyPuzzleTier(
    val index: Int,
    val titleRes: Int,
    val subtitleRes: Int,
    val rewardCredits: Int,
)

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
     * Fornisce la spiegazione logica e pedagogica del concetto tattico illustrato dal puzzle.
     */
    public fun getLogicalConcept(puzzle: TacticalPuzzle): String {
        val first = puzzle.firstMove
        val card = first.card
        val capture = first.capture
        val table = puzzle.initialView.table

        val isSweep = table.isNotEmpty() && capture.containsAll(table) && table.containsAll(capture)
        val settebello = Card(Rank.SETTE, Suit.DENARI)
        val takesSettebello = capture.contains(settebello) || (card == settebello && capture.isNotEmpty())

        return when {
            isSweep || puzzle.goal == PuzzleGoal.MAKE_SWEEP ->
                "Svuotare il tavolo con una Scopa (+1 punto) priva l'avversario di qualsiasi combinazione e lo obbliga a calare a vuoto nel turno successivo."

            takesSettebello ->
                "Il 7 di Denari è la carta sovrana della Scopa: garantisce il punto del Settebello, assegna un Denaro prezioso e vale 21 per la Primiera."

            capture.any { it.rank == Rank.SETTE || it.rank == Rank.SEI } ->
                "Dare priorità a 7 e 6 (rispettivamente 21 e 18 punti di Primiera) protegge o ribalta il punto più conteso contro le Figure che ne valgono solo 10."

            capture.count { it.suit == Suit.DENARI } >= 2 ->
                "Catturare più carte di Denari assicura il controllo dell'oro e avvicina alla soglia decisiva di 6 Denari per la vittoria del punto."

            puzzle.goal == PuzzleGoal.WIN_MATCH ->
                "Nella smazzata decisiva ogni carta catturata sposta il saldo: calcolare la mossa che taglia il traguardo prima dell'avversario garantisce la vittoria finale."

            else ->
                "Ottimizzare il saldo delle prese massimizza il bottino di carte e impedisce all'avversario di realizzare combinazioni facili di Scopa."
        }
    }

    /**
     * Fornisce la regola d'oro e massima strategica del Maestro per il puzzle risolto.
     */
    public fun getGoldenRule(puzzle: TacticalPuzzle): String {
        val first = puzzle.firstMove
        val card = first.card
        val capture = first.capture
        val table = puzzle.initialView.table

        val isSweep = table.isNotEmpty() && capture.containsAll(table) && table.containsAll(capture)
        val settebello = Card(Rank.SETTE, Suit.DENARI)
        val takesSettebello = capture.contains(settebello) || (card == settebello && capture.isNotEmpty())

        return when {
            isSweep || puzzle.goal == PuzzleGoal.MAKE_SWEEP ->
                "Quando puoi ripulire il banco con una Scopa, fallo sempre: costringi l'avversario a calare a vuoto nel turno dopo."

            takesSettebello ->
                "Il Settebello non si regala e non si lascia a terra: è la carta più preziosa del mazzo e assegna un punto secco."

            capture.any { it.rank == Rank.SETTE || it.rank == Rank.SEI } ->
                "In Primiera un 7 o un 6 valgono più di due Figure: dai sempre la precedenza a 7 e 6 rispetto a Re e Cavalli."

            capture.count { it.suit == Suit.DENARI } >= 2 ->
                "Chi governa i Denari governa la partita: punta dritto alla soglia di 6 ori per blindare il punto di mazzo."

            puzzle.goal == PuzzleGoal.WIN_MATCH ->
                "All'ultima mano conta le prese prima dello stile: fai la giocata che ti assicura il traguardo matematico."

            else ->
                "Valuta sempre il saldo delle carte e nega all'avversario combinazioni facili di presa."
        }
    }
}
