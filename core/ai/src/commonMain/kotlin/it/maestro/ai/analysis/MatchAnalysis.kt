package it.maestro.ai.analysis

import it.maestro.game.model.Card
import it.maestro.game.model.CardOrder
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaMove
import kotlin.math.abs
import kotlin.math.roundToLong

private const val REPORT_DECIMAL_SCALE: Long = 10_000L
private const val REPORT_DECIMAL_DIGITS: Int = 4

/**
 * Risultato complessivo dell'analisi di un [it.maestro.game.scopa.MatchLog] prodotto da [MatchAnalyzer]
 * (`docs/ai/maestro.md` §Analizzatore, ADR-0005, task F0-11).
 *
 * Raccoglie la sequenza ordinata di [MoveAnalysis] per ogni giocata del log, la perdita media
 * ([meanLossBySeat]), la precisione percentuale intera ([accuracyBySeat]) e la distribuzione
 * delle classi [MoveGrade] per ciascun giocatore ([countsBySeatAndGrade]).
 *
 * @property moves Analisi ordinata di tutte le mosse del log.
 * @property accuracyBySeat Precisione percentuale (`0..100`) per ciascun [Seat].
 * @property meanLossBySeat Perdita media in punti di smazzata attesi per ciascun [Seat].
 * @property countsBySeatAndGrade Conteggio delle mosse per [Seat] e [MoveGrade].
 */
public data class MatchAnalysis(
    public val moves: List<MoveAnalysis>,
    public val accuracyBySeat: Map<Seat, Int>,
    public val meanLossBySeat: Map<Seat, Double>,
    public val countsBySeatAndGrade: Map<Seat, Map<MoveGrade, Int>>,
) {
    /** Restituisce la precisione percentuale intera (`0..100`) calcolata per [seat]. */
    public fun accuracy(seat: Seat): Int = accuracyBySeat.getValue(seat)

    /** Restituisce la perdita media delle mosse giocate da [seat]. */
    public fun meanLoss(seat: Seat): Double = meanLossBySeat.getValue(seat)

    /** Restituisce il numero di mosse di classe [grade] giocate da [seat]. */
    public fun count(seat: Seat, grade: MoveGrade): Int =
        countsBySeatAndGrade.getValue(seat).getValue(grade)

    /**
     * Formatta l'analisi della partita in un report testuale leggibile per mossa e per giocatore
     * (`docs/backlog/fase-0.md` F0-11 criterio 3).
     *
     * Per ogni mossa riporta:
     * - numero della mossa (`#1`, `#2`, ...),
     * - giocatore di turno (`P1` o `P2`),
     * - carta e presa giocate con valore atteso e perdita,
     * - classe ([MoveGrade]),
     * - migliori 3 alternative ([MoveAnalysis.topAlternatives]) con il relativo valore atteso.
     */
    public fun toReadableReport(): String = buildString {
        appendLine("maestro-analysis report")
        for (seat in Seat.entries) {
            val acc = accuracy(seat)
            val meanLoss = formatFixed4(meanLoss(seat))
            val bestCount = count(seat, MoveGrade.BEST)
            val inaccuracyCount = count(seat, MoveGrade.INACCURACY)
            val blunderCount = count(seat, MoveGrade.BLUNDER)
            appendLine(
                "${seat.name}: accuracy=$acc% | meanLoss=$meanLoss | " +
                    "BEST=$bestCount, INACCURACY=$inaccuracyCount, BLUNDER=$blunderCount",
            )
        }
        for (moveAnalysis in moves) {
            val playedText = formatMoveNotation(moveAnalysis.playedMove)
            val playedVal = formatFixed4(moveAnalysis.playedValue)
            val lossVal = formatFixed4(moveAnalysis.loss)
            val topText = moveAnalysis.topAlternatives.joinToString(", ") { candidate ->
                "${formatMoveNotation(candidate.move)}=${formatFixed4(candidate.value)}"
            }
            appendLine(
                "#${moveAnalysis.moveNumber} ${moveAnalysis.seat.name} | " +
                    "played: $playedText ($playedVal, loss=$lossVal) | " +
                    "grade: ${moveAnalysis.grade.name} | " +
                    "top3: [$topText]",
            )
        }
    }.trimEnd()
}

internal fun formatMoveNotation(move: ScopaMove): String {
    val captureText = if (move.capture.isEmpty()) {
        "-"
    } else {
        move.capture
            .sortedWith(CardOrder)
            .joinToString(",") { formatCardNotation(it) }
    }
    return "${formatCardNotation(move.card)} $captureText"
}

private fun formatCardNotation(card: Card): String {
    val rankSymbol = when (card.rank) {
        Rank.ASSO -> 'A'
        Rank.DUE -> '2'
        Rank.TRE -> '3'
        Rank.QUATTRO -> '4'
        Rank.CINQUE -> '5'
        Rank.SEI -> '6'
        Rank.SETTE -> '7'
        Rank.FANTE -> 'F'
        Rank.CAVALLO -> 'C'
        Rank.RE -> 'R'
    }
    val suitSymbol = when (card.suit) {
        Suit.DENARI -> 'D'
        Suit.COPPE -> 'C'
        Suit.SPADE -> 'S'
        Suit.BASTONI -> 'B'
    }
    return "$rankSymbol$suitSymbol"
}

private fun formatFixed4(value: Double): String {
    val scaled = (value * REPORT_DECIMAL_SCALE).roundToLong()
    val sign = if (scaled < 0L) "-" else ""
    val absScaled = abs(scaled)
    val integerPart = absScaled / REPORT_DECIMAL_SCALE
    val fractionalPart = (absScaled % REPORT_DECIMAL_SCALE).toString().padStart(REPORT_DECIMAL_DIGITS, '0')
    return "$sign$integerPart.$fractionalPart"
}
