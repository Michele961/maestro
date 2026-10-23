package it.maestro.ai.analysis

import it.maestro.ai.bot.MoveEvaluator
import it.maestro.ai.bot.PimcEvaluator
import it.maestro.ai.bot.SearchBudget
import it.maestro.game.CoreGame
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import it.maestro.game.scopa.viewFor
import kotlin.math.exp
import kotlin.math.roundToInt

/**
 * Analizzatore deterministico delle mosse di un [MatchLog] (`docs/ai/maestro.md` §Analizzatore,
 * ADR-0005, task F0-11).
 *
 * Rigioca dall'inizio la partita descritta da [MatchLog] per ricostruirne l'evoluzione turno dopo turno,
 * ma per il **Principio 1 (`Solo informazione lecita`)** invoca [evaluator] passando esclusivamente
 * `viewFor(state, state.toMove)`, senza mai esporre `ScopaState` al valutatore.
 *
 * Per ogni mossa del log:
 * - valuta tutte le mosse legali dal punto di vista del giocatore di turno;
 * - ordina le alternative per valore atteso decrescente (preservando a parità di valore l'ordine canonico
 *   di `legalMoves`);
 * - calcola la perdita `loss = maxOf(0.0, bestValue - playedValue)`;
 * - assegna la classe [MoveGrade] secondo le soglie provvisorie ([BEST_MAX_LOSS], [INACCURACY_MAX_LOSS]);
 * - calcola a fine partita la precisione percentuale intera per giocatore tramite la formula provvisoria
 *   `100 * exp(-3 * perdita media)` ([computeAccuracy]).
 *
 * Tutte le soglie e i parametri della formula di precisione sono centralizzati come costanti nominate
 * nel `companion object` di questa classe (`docs/backlog/fase-0.md` F0-11 criterio 2).
 *
 * @property evaluator Valutatore delle mosse legali (default [PimcEvaluator]).
 * @property budget Budget di ricerca passato a [evaluator] per ogni turno (default [SearchBudget]).
 */
public class MatchAnalyzer(
    public val evaluator: MoveEvaluator = PimcEvaluator,
    public val budget: SearchBudget = SearchBudget(),
) {
    /**
     * Analizza tutte le mosse di [log] utilizzando [rng] per il campionamento del valutatore.
     *
     * @param log Log completo o parziale della partita da analizzare.
     * @param rng Generatore pseudocasuale con seed usato per la valutazione (default `SeededRng(log.seed)`).
     * @return [MatchAnalysis] con il dettaglio di ogni mossa e le metriche aggregate per [Seat].
     * @throws IllegalArgumentException se `log.rulesVersion != CoreGame.RULES_VERSION`, se una mossa del log
     *   è illegale nello stato corrispondente, o se il valutatore non restituisce il valore per tutte le mosse legali.
     */
    public fun analyze(
        log: MatchLog,
        rng: SeededRng = SeededRng(log.seed),
    ): MatchAnalysis {
        require(log.rulesVersion == CoreGame.RULES_VERSION) {
            "Versione delle regole non supportata nel log: trovata ${log.rulesVersion}, attesa ${CoreGame.RULES_VERSION}"
        }

        var state = newMatch(config = log.config, seed = log.seed)
        val analyzedMoves = ArrayList<MoveAnalysis>(log.moves.size)

        for ((index, playedMove) in log.moves.withIndex()) {
            val mover = state.toMove
            val legal = legalMoves(state)
            require(playedMove in legal) {
                "Mossa illegale nel log all'indice $index per $mover: $playedMove"
            }

            // Principio 1: il valutatore riceve esclusivamente la vista del giocatore di turno.
            val view = viewFor(state, mover)
            val evaluations = evaluator.evaluate(
                view = view,
                legal = legal,
                rng = rng,
                budget = budget,
            )
            require(evaluations.size == legal.size) {
                "Il valutatore ha restituito ${evaluations.size} valutazioni a fronte di ${legal.size} mosse legali"
            }

            val playedMoveValue = evaluations.firstOrNull { it.move == playedMove }
                ?: throw IllegalArgumentException(
                    "Il valutatore non ha restituito una valutazione per la mossa giocata all'indice $index: $playedMove",
                )

            // sortedByDescending è stabile in Kotlin: a parità di valore preserva l'ordine canonico di legal.
            val sortedAlternatives = evaluations.sortedByDescending { it.value }
            val bestMoveValue = sortedAlternatives.first()
            val rawLoss = maxOf(0.0, bestMoveValue.value - playedMoveValue.value)
            val grade = classifyLoss(rawLoss)

            analyzedMoves.add(
                MoveAnalysis(
                    moveIndex = index,
                    seat = mover,
                    playedMove = playedMove,
                    playedValue = playedMoveValue.value,
                    bestMove = bestMoveValue.move,
                    bestValue = bestMoveValue.value,
                    loss = rawLoss,
                    grade = grade,
                    alternatives = sortedAlternatives,
                ),
            )

            state = when (val result = play(state, playedMove)) {
                is PlayResult.Applied -> result.state
                is PlayResult.Rejected -> throw IllegalArgumentException(
                    "Mossa rifiutata durante il replay dell'analisi all'indice $index: ${result.reason}",
                )
            }
        }

        val meanLossBySeat = Seat.entries.associateWith { seat ->
            val seatMoves = analyzedMoves.filter { it.seat == seat }
            if (seatMoves.isEmpty()) {
                0.0
            } else {
                seatMoves.sumOf { it.loss } / seatMoves.size.toDouble()
            }
        }

        val accuracyBySeat = Seat.entries.associateWith { seat ->
            computeAccuracy(meanLossBySeat.getValue(seat))
        }

        val countsBySeatAndGrade = Seat.entries.associateWith { seat ->
            val seatMoves = analyzedMoves.filter { it.seat == seat }
            MoveGrade.entries.associateWith { grade ->
                seatMoves.count { it.grade == grade }
            }
        }

        return MatchAnalysis(
            moves = analyzedMoves,
            accuracyBySeat = accuracyBySeat,
            meanLossBySeat = meanLossBySeat,
            countsBySeatAndGrade = countsBySeatAndGrade,
        )
    }

    public companion object {
        /**
         * Soglia massima di perdita (in punti di smazzata attesi) per classificare una mossa come [MoveGrade.BEST]
         * (`perdita <= 0.05`, `docs/ai/maestro.md` §Analizzatore).
         */
        public const val BEST_MAX_LOSS: Double = 0.05

        /**
         * Soglia massima di perdita (in punti di smazzata attesi) per classificare una mossa come [MoveGrade.INACCURACY]
         * (`0.05 < perdita <= 0.30`; oltre `0.30` è [MoveGrade.BLUNDER], `docs/ai/maestro.md` §Analizzatore).
         */
        public const val INACCURACY_MAX_LOSS: Double = 0.30

        /** Moltiplicatore percentuale nella formula provvisoria della precisione (`100 * exp(-3 * perdita media)`). */
        public const val ACCURACY_MAX_PERCENT: Double = 100.0

        /** Coefficiente di decadimento esponenziale nella formula provvisoria della precisione (`3.0`). */
        public const val ACCURACY_DECAY_FACTOR: Double = 3.0

        /** Numero di migliori alternative esposte in [MoveAnalysis.topAlternatives] e nel report leggibile. */
        public const val TOP_ALTERNATIVES_COUNT: Int = 3

        /**
         * Classifica una perdita non negativa [loss] in [MoveGrade] usando le soglie provvisorie
         * [BEST_MAX_LOSS] e [INACCURACY_MAX_LOSS] (`docs/ai/maestro.md` §Analizzatore).
         *
         * @throws IllegalArgumentException se [loss] `< 0.0` o `NaN`.
         */
        public fun classifyLoss(loss: Double): MoveGrade {
            require(!loss.isNaN() && loss >= 0.0) {
                "La perdita (loss) deve essere >= 0.0, trovata: $loss"
            }
            return when {
                loss <= BEST_MAX_LOSS -> MoveGrade.BEST
                loss <= INACCURACY_MAX_LOSS -> MoveGrade.INACCURACY
                else -> MoveGrade.BLUNDER
            }
        }

        /**
         * Calcola la precisione percentuale intera (`0..100`) a partire dalla perdita media [meanLoss]
         * secondo la formula provvisoria `100 * exp(-3 * perdita media)`, arrotondata all'intero più vicino
         * (`docs/ai/maestro.md` §Analizzatore).
         *
         * @throws IllegalArgumentException se [meanLoss] `< 0.0` o `NaN`.
         */
        public fun computeAccuracy(meanLoss: Double): Int {
            require(!meanLoss.isNaN() && meanLoss >= 0.0) {
                "La perdita media (meanLoss) deve essere >= 0.0, trovata: $meanLoss"
            }
            val rawPercent = ACCURACY_MAX_PERCENT * exp(-ACCURACY_DECAY_FACTOR * meanLoss)
            return rawPercent.roundToInt().coerceIn(0, ACCURACY_MAX_PERCENT.toInt())
        }
    }
}
