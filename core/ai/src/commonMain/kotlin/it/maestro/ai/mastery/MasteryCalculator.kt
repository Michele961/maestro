package it.maestro.ai.mastery

import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.analysis.MoveAnalysis
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.MatchLog
import it.maestro.game.training.TrainingFundamental
import kotlin.math.exp
import kotlin.math.roundToInt

/**
 * Funzioni pure e deterministiche per il calcolo dell'Indice di Maestria (Elo Cognitivo)
 * e dei 5 assi del Radar delle Abilità (task F2-04, `docs/vision.md` §5.A, `docs/ai/maestro.md`).
 */
public object MasteryCalculator {

    /**
     * Numero di mosse per smazzata nella Scopa a due giocatori (18 mani per giocatore = 36 mosse).
     * Le ultime 6 mosse (3 per giocatore) costituiscono l'Endgame a mazzo vuoto.
     */
    private const val MOVES_PER_DEAL: Int = 36
    private const val ENDGAME_START_OFFSET_IN_DEAL: Int = 30
    private const val RECENT_ENDGAME_WINDOW: Int = 6

    /**
     * Converte una precisione percentuale media (`0.0..100.0`) nel corrispondente Elo Cognitivo (`800..2000`).
     *
     * La formula mappa in modo lineare e continuo la precisione sui 5 livelli del Maestro (`docs/ai/maestro.md`):
     * - `0%` -> 800 (Principiante)
     * - `25%` -> 1100 (Dilettante)
     * - `50%` -> 1400 (Intermedio)
     * - `75%` -> 1700 (Avanzato)
     * - `100%` -> 2000 (Maestro)
     */
    public fun accuracyToCognitiveElo(accuracyPercentage: Double): Int {
        val clamped = accuracyPercentage.coerceIn(0.0, 100.0)
        return (800.0 + clamped * 12.0).roundToInt().coerceIn(800, 2000)
    }

    /**
     * Calcola il peso $\alpha$ della nuova partita per la media mobile esponenziale (EMA).
     *
     * Permette una calibrazione iniziale dinamica (primo match a peso pieno 1.0, poi rapido
     * aggiustamento nei primi match), stabilizzandosi asintoticamente su un fattore $\alpha \approx 0.15..0.20$.
     */
    public fun computeAlpha(currentMatchesPlayed: Int): Double {
        if (currentMatchesPlayed <= 0) return 1.0
        val targetAlpha = 2.0 / (currentMatchesPlayed + 2.0)
        return targetAlpha.coerceIn(0.15, 0.40)
    }

    /**
     * Aggiorna in modo puro e deterministico il [MasteryProfile] del giocatore
     * combinando il profilo pregresso con l'accuratezza e il radar della partita appena conclusa.
     */
    public fun updateMasteryProfile(
        current: MasteryProfile,
        matchAccuracy: Int,
        matchRadar: AbilityRadar,
    ): MasteryProfile {
        val clampedAccuracy = matchAccuracy.coerceIn(0, 100)
        val alpha = computeAlpha(current.matchesPlayed)

        val updatedAccuracyEma = if (current.matchesPlayed == 0) {
            clampedAccuracy.toDouble()
        } else {
            ((1.0 - alpha) * current.accuracyEma + alpha * clampedAccuracy).coerceIn(0.0, 100.0)
        }

        val updatedElo = accuracyToCognitiveElo(updatedAccuracyEma)

        val updatedRadar = if (current.matchesPlayed == 0) {
            matchRadar
        } else {
            AbilityRadar(
                sevens = blendAxis(current.radar.sevens, matchRadar.sevens, alpha),
                denari = blendAxis(current.radar.denari, matchRadar.denari, alpha),
                primiera = blendAxis(current.radar.primiera, matchRadar.primiera, alpha),
                board = blendAxis(current.radar.board, matchRadar.board, alpha),
                endgame = blendAxis(current.radar.endgame, matchRadar.endgame, alpha),
            )
        }

        return MasteryProfile(
            matchesPlayed = current.matchesPlayed + 1,
            accuracyEma = updatedAccuracyEma,
            cognitiveElo = updatedElo,
            radar = updatedRadar,
        )
    }

    /**
     * Calcola i punteggi del Radar a 5 assi per una singola partita completata,
     * estraendo le metriche dalle mosse del giocatore e dagli eventuali Check di allenamento svolti.
     *
     * @param matchAnalysis Risultato dell'analisi della partita da [it.maestro.ai.analysis.MatchAnalyzer].
     * @param matchLog Log completo della partita.
     * @param humanSeat Posto del giocatore umano analizzato (default [Seat.P1]).
     * @param trainingScore Risultati dei Check di allenamento (risposte corrette, totali), se presenti.
     * @param trainingFundamental Fondamentale di allenamento attivo durante la partita.
     */
    public fun computeMatchRadar(
        matchAnalysis: MatchAnalysis,
        matchLog: MatchLog,
        humanSeat: Seat = Seat.P1,
        trainingScore: Pair<Int, Int>? = null,
        trainingFundamental: TrainingFundamental = TrainingFundamental.NONE,
    ): AbilityRadar {
        val overallAccuracy = matchAnalysis.accuracy(humanSeat)
        val playerMoves = matchAnalysis.moves.filter { it.seat == humanSeat }

        if (playerMoves.isEmpty()) {
            return AbilityRadar(
                sevens = overallAccuracy,
                denari = overallAccuracy,
                primiera = overallAccuracy,
                board = overallAccuracy,
                endgame = overallAccuracy,
            )
        }

        // 1. Memoria Sette: mosse con un 7 giocato, preso o sul tavolo
        val sevensMoves = playerMoves.filter { move ->
            move.playedMove.card.rank == Rank.SETTE ||
                move.playedMove.capture.any { it.rank == Rank.SETTE } ||
                move.alternatives.any { it.move.card.rank == Rank.SETTE }
        }
        val sevensMoveAcc = accuracyForMoves(sevensMoves, overallAccuracy)
        val sevensFinal = blendWithTrainingCheck(
            baseAccuracy = sevensMoveAcc,
            trainingScore = trainingScore,
            isRelevantCheck = trainingFundamental == TrainingFundamental.SEVENS_COUNT,
        )

        // 2. Controllo Denari: mosse con carte di Denari (inclusa speciale attenzione al Settebello)
        val denariMoves = playerMoves.filter { move ->
            move.playedMove.card.suit == Suit.DENARI ||
                move.playedMove.capture.any { it.suit == Suit.DENARI }
        }
        val denariMoveAcc = accuracyForMoves(denariMoves, overallAccuracy)
        val denariFinal = blendWithTrainingCheck(
            baseAccuracy = denariMoveAcc,
            trainingScore = trainingScore,
            isRelevantCheck = trainingFundamental == TrainingFundamental.DENARI_COUNT ||
                trainingFundamental == TrainingFundamental.SETTEBELLO_LOCATION,
        )

        // 3. Primiera: mosse con Sette, Sei o Assi (carte decisive per la Primiera)
        val primieraRanks = setOf(Rank.SETTE, Rank.SEI, Rank.ASSO)
        val primieraMoves = playerMoves.filter { move ->
            move.playedMove.card.rank in primieraRanks ||
                move.playedMove.capture.any { it.rank in primieraRanks }
        }
        val primieraMoveAcc = accuracyForMoves(primieraMoves, overallAccuracy)
        val primieraFinal = blendWithTrainingCheck(
            baseAccuracy = primieraMoveAcc,
            trainingScore = trainingScore,
            isRelevantCheck = trainingFundamental == TrainingFundamental.PRIMIERA_LEAD,
        )

        // 4. Gestione Banco: accuratezza sulle calate (giocate senza presa) e sulle scope
        val boardMoves = playerMoves.filter { move ->
            move.playedMove.capture.isEmpty() || move.playedMove.capture.size > 1
        }
        val boardFinal = accuracyForMoves(boardMoves, overallAccuracy)

        // 5. Endgame: mosse giocate nell'ultima distribuzione di 3 carte a mazzo vuoto
        val totalMovesCount = matchLog.moves.size
        val endgameMoves = playerMoves.filter { move ->
            (move.moveIndex % MOVES_PER_DEAL >= ENDGAME_START_OFFSET_IN_DEAL) ||
                (move.moveIndex >= totalMovesCount - RECENT_ENDGAME_WINDOW)
        }
        val endgameFinal = accuracyForMoves(endgameMoves, overallAccuracy)

        return AbilityRadar(
            sevens = sevensFinal,
            denari = denariFinal,
            primiera = primieraFinal,
            board = boardFinal,
            endgame = endgameFinal,
        )
    }

    /**
     * Calcola la precisione percentuale `0..100` per un sottoinsieme di mosse
     * tramite la formula standard della Scopa (`100 * exp(-3 * perdita_media)`).
     */
    public fun accuracyForMoves(moves: List<MoveAnalysis>, fallback: Int): Int {
        if (moves.isEmpty()) return fallback
        val meanLoss = moves.map { it.loss }.average()
        return (100.0 * exp(-3.0 * meanLoss)).roundToInt().coerceIn(0, 100)
    }

    private fun blendAxis(previousValue: Int, newValue: Int, alpha: Double): Int {
        return ((1.0 - alpha) * previousValue + alpha * newValue).roundToInt().coerceIn(0, 100)
    }

    private fun blendWithTrainingCheck(
        baseAccuracy: Int,
        trainingScore: Pair<Int, Int>?,
        isRelevantCheck: Boolean,
    ): Int {
        if (!isRelevantCheck || trainingScore == null || trainingScore.second <= 0) {
            return baseAccuracy
        }
        val checkAccuracy = ((trainingScore.first.toDouble() / trainingScore.second) * 100.0)
            .roundToInt().coerceIn(0, 100)
        // 65% peso dalle scelte reali di gioco, 35% dal test di riflessione/memoria
        return (baseAccuracy * 0.65 + checkAccuracy * 0.35).roundToInt().coerceIn(0, 100)
    }
}
