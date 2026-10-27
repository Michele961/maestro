package it.maestro.app.domain.mastery

import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.mastery.MasteryProfile
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.flow.Flow

/**
 * Singola voce nello storico delle partite giocate per la schermata Profilo.
 *
 * @property id Identificativo univoco della partita.
 * @property timestampMillis Timestamp epoch in millisecondi di quando la partita è stata registrata.
 * @property accuracy Percentuale di accuratezza del giocatore (0..100).
 * @property cognitiveElo Valore Elo Cognitivo raggiunto al termine del match.
 * @property eloDelta Variazione Elo generata da questa partita (es. +18 o -12).
 * @property humanScore Punti totalizzati dal giocatore umano.
 * @property botScore Punti totalizzati dal bot o Maestro.
 * @property isWin True se il giocatore umano ha vinto la partita.
 * @property matchLogText Rappresentazione testuale canonica del MatchLog per la consultazione e Game Review.
 */
public data class MatchHistoryEntry(
    val id: String,
    val timestampMillis: Long,
    val accuracy: Int,
    val cognitiveElo: Int,
    val eloDelta: Int,
    val humanScore: Int,
    val botScore: Int,
    val isWin: Boolean,
    val matchLogText: String? = null,
)

/**
 * Repository per la persistenza e l'osservazione dell'Indice di Maestria (Elo Cognitivo)
 * e del Radar delle Abilità (task F2-04, `docs/vision.md` §5.A).
 */
public interface MasteryRepository {
    public fun observeMasteryProfile(): Flow<MasteryProfile>
    public fun observeMatchHistory(): Flow<List<MatchHistoryEntry>>
    public suspend fun getMasteryProfile(): MasteryProfile
    public suspend fun recordMatch(
        matchAnalysis: MatchAnalysis,
        matchLog: MatchLog,
        humanSeat: Seat = Seat.P1,
        trainingScore: Pair<Int, Int>? = null,
        trainingFundamental: TrainingFundamental = TrainingFundamental.NONE,
    ): MasteryProfile
    public suspend fun resetProfile()
}
