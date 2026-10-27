package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.mastery.AbilityRadar
import it.maestro.ai.mastery.MasteryCalculator
import it.maestro.ai.mastery.MasteryProfile
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ReplayResult
import it.maestro.game.scopa.replay
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

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

/**
 * Implementazione su DataStore Preferences (ADR-0012, task F2-04).
 */
public class DataStoreMasteryRepository(
    private val dataStore: DataStore<Preferences>,
) : MasteryRepository {

    private object PreferencesKeys {
        val MATCHES_PLAYED = intPreferencesKey("mastery_matches_played")
        val ACCURACY_EMA = doublePreferencesKey("mastery_accuracy_ema")
        val COGNITIVE_ELO = intPreferencesKey("mastery_cognitive_elo")
        val RADAR_SEVENS = intPreferencesKey("mastery_radar_sevens")
        val RADAR_DENARI = intPreferencesKey("mastery_radar_denari")
        val RADAR_PRIMIERA = intPreferencesKey("mastery_radar_primiera")
        val RADAR_BOARD = intPreferencesKey("mastery_radar_board")
        val RADAR_ENDGAME = intPreferencesKey("mastery_radar_endgame")
        val MATCH_HISTORY = stringPreferencesKey("mastery_match_history")
    }

    override fun observeMasteryProfile(): Flow<MasteryProfile> =
        dataStore.data.map { preferences -> mapToProfile(preferences) }

    override fun observeMatchHistory(): Flow<List<MatchHistoryEntry>> =
        dataStore.data.map { preferences -> parseHistory(preferences[PreferencesKeys.MATCH_HISTORY].orEmpty()) }

    override suspend fun getMasteryProfile(): MasteryProfile {
        val preferences = dataStore.data.first()
        return mapToProfile(preferences)
    }

    override suspend fun recordMatch(
        matchAnalysis: MatchAnalysis,
        matchLog: MatchLog,
        humanSeat: Seat,
        trainingScore: Pair<Int, Int>?,
        trainingFundamental: TrainingFundamental,
    ): MasteryProfile {
        val current = getMasteryProfile()
        val rawHistory = dataStore.data.first()[PreferencesKeys.MATCH_HISTORY].orEmpty()
        val existingHistory = parseHistory(rawHistory)
        val encodedLog = matchLog.toText()
        if (existingHistory.any { it.matchLogText == encodedLog }) {
            return current
        }
        val matchAccuracy = matchAnalysis.accuracy(humanSeat)
        val matchRadar = MasteryCalculator.computeMatchRadar(
            matchAnalysis = matchAnalysis,
            matchLog = matchLog,
            humanSeat = humanSeat,
            trainingScore = trainingScore,
            trainingFundamental = trainingFundamental,
        )
        val updated = MasteryCalculator.updateMasteryProfile(
            current = current,
            matchAccuracy = matchAccuracy,
            matchRadar = matchRadar,
        )

        val finalState = (replay(matchLog) as? ReplayResult.Ok)?.state
        val opponentSeat = humanSeat.other()
        val humanTotal = finalState?.totals?.get(humanSeat) ?: 0
        val opponentTotal = finalState?.totals?.get(opponentSeat) ?: 0
        val isWin = finalState?.winner?.let { it == humanSeat } ?: (humanTotal >= opponentTotal)
        val eloDelta = updated.cognitiveElo - current.cognitiveElo
        val entry = MatchHistoryEntry(
            id = "match_${System.currentTimeMillis()}",
            timestampMillis = System.currentTimeMillis(),
            accuracy = matchAccuracy,
            cognitiveElo = updated.cognitiveElo,
            eloDelta = eloDelta,
            humanScore = humanTotal,
            botScore = opponentTotal,
            isWin = isWin,
            matchLogText = encodedLog,
        )

        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MATCHES_PLAYED] = updated.matchesPlayed
            preferences[PreferencesKeys.ACCURACY_EMA] = updated.accuracyEma
            preferences[PreferencesKeys.COGNITIVE_ELO] = updated.cognitiveElo
            preferences[PreferencesKeys.RADAR_SEVENS] = updated.radar.sevens
            preferences[PreferencesKeys.RADAR_DENARI] = updated.radar.denari
            preferences[PreferencesKeys.RADAR_PRIMIERA] = updated.radar.primiera
            preferences[PreferencesKeys.RADAR_BOARD] = updated.radar.board
            preferences[PreferencesKeys.RADAR_ENDGAME] = updated.radar.endgame

            val newHistory = (listOf(entry) + existingHistory).take(20)
            preferences[PreferencesKeys.MATCH_HISTORY] = serializeHistory(newHistory)
        }

        return updated
    }

    override suspend fun resetProfile() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.MATCHES_PLAYED)
            preferences.remove(PreferencesKeys.ACCURACY_EMA)
            preferences.remove(PreferencesKeys.COGNITIVE_ELO)
            preferences.remove(PreferencesKeys.RADAR_SEVENS)
            preferences.remove(PreferencesKeys.RADAR_DENARI)
            preferences.remove(PreferencesKeys.RADAR_PRIMIERA)
            preferences.remove(PreferencesKeys.RADAR_BOARD)
            preferences.remove(PreferencesKeys.RADAR_ENDGAME)
            preferences.remove(PreferencesKeys.MATCH_HISTORY)
        }
    }

    private fun mapToProfile(preferences: Preferences): MasteryProfile {
        val matchesPlayed = preferences[PreferencesKeys.MATCHES_PLAYED] ?: 0
        if (matchesPlayed <= 0) {
            return MasteryProfile.INITIAL
        }
        val accuracyEma = preferences[PreferencesKeys.ACCURACY_EMA] ?: 50.0
        val cognitiveElo = preferences[PreferencesKeys.COGNITIVE_ELO] ?: 1000
        val radar = AbilityRadar(
            sevens = preferences[PreferencesKeys.RADAR_SEVENS] ?: 50,
            denari = preferences[PreferencesKeys.RADAR_DENARI] ?: 50,
            primiera = preferences[PreferencesKeys.RADAR_PRIMIERA] ?: 50,
            board = preferences[PreferencesKeys.RADAR_BOARD] ?: 50,
            endgame = preferences[PreferencesKeys.RADAR_ENDGAME] ?: 50,
        )
        return MasteryProfile(
            matchesPlayed = matchesPlayed,
            accuracyEma = accuracyEma.coerceIn(0.0, 100.0),
            cognitiveElo = cognitiveElo.coerceIn(800, 2000),
            radar = radar,
        )
    }

    private fun parseHistory(raw: String): List<MatchHistoryEntry> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { token ->
            val parts = token.split("|")
            if (parts.size >= 8) {
                val matchLogText = if (parts.size >= 9 && parts[8].isNotBlank()) {
                    try {
                        String(java.util.Base64.getUrlDecoder().decode(parts[8]), Charsets.UTF_8)
                    } catch (_: Exception) {
                        null
                    }
                } else {
                    null
                }
                MatchHistoryEntry(
                    id = parts[0],
                    timestampMillis = parts[1].toLongOrNull() ?: 0L,
                    accuracy = parts[2].toIntOrNull() ?: 0,
                    cognitiveElo = parts[3].toIntOrNull() ?: 1000,
                    eloDelta = parts[4].toIntOrNull() ?: 0,
                    humanScore = parts[5].toIntOrNull() ?: 0,
                    botScore = parts[6].toIntOrNull() ?: 0,
                    isWin = parts[7] == "1",
                    matchLogText = matchLogText,
                )
            } else {
                null
            }
        }
    }

    private fun serializeHistory(entries: List<MatchHistoryEntry>): String =
        entries.joinToString(";") {
            val encodedLog = it.matchLogText?.let { text ->
                java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(text.toByteArray(Charsets.UTF_8))
            }.orEmpty()
            "${it.id}|${it.timestampMillis}|${it.accuracy}|${it.cognitiveElo}|${it.eloDelta}|${it.humanScore}|${it.botScore}|${if (it.isWin) 1 else 0}|$encodedLog"
        }
}

/**
 * Implementazione in memoria per test unitari e preview Compose.
 */
public class InMemoryMasteryRepository(
    initialProfile: MasteryProfile = MasteryProfile.INITIAL,
    initialHistory: List<MatchHistoryEntry> = emptyList(),
) : MasteryRepository {

    private val profileState = MutableStateFlow(initialProfile)
    private val historyState = MutableStateFlow(initialHistory)

    override fun observeMasteryProfile(): Flow<MasteryProfile> = profileState.asStateFlow()

    override fun observeMatchHistory(): Flow<List<MatchHistoryEntry>> = historyState.asStateFlow()

    override suspend fun getMasteryProfile(): MasteryProfile = profileState.value

    override suspend fun recordMatch(
        matchAnalysis: MatchAnalysis,
        matchLog: MatchLog,
        humanSeat: Seat,
        trainingScore: Pair<Int, Int>?,
        trainingFundamental: TrainingFundamental,
    ): MasteryProfile {
        val current = profileState.value
        val encodedLog = matchLog.toText()
        if (historyState.value.any { it.matchLogText == encodedLog }) {
            return current
        }

        val matchAccuracy = matchAnalysis.accuracy(humanSeat)
        val matchRadar = MasteryCalculator.computeMatchRadar(
            matchAnalysis = matchAnalysis,
            matchLog = matchLog,
            humanSeat = humanSeat,
            trainingScore = trainingScore,
            trainingFundamental = trainingFundamental,
        )
        val updated = MasteryCalculator.updateMasteryProfile(
            current = current,
            matchAccuracy = matchAccuracy,
            matchRadar = matchRadar,
        )

        val finalState = (replay(matchLog) as? ReplayResult.Ok)?.state
        val opponentSeat = humanSeat.other()
        val humanTotal = finalState?.totals?.get(humanSeat) ?: 0
        val opponentTotal = finalState?.totals?.get(opponentSeat) ?: 0
        val isWin = finalState?.winner?.let { it == humanSeat } ?: (humanTotal >= opponentTotal)
        val eloDelta = updated.cognitiveElo - current.cognitiveElo
        val entry = MatchHistoryEntry(
            id = "match_${System.currentTimeMillis()}",
            timestampMillis = System.currentTimeMillis(),
            accuracy = matchAccuracy,
            cognitiveElo = updated.cognitiveElo,
            eloDelta = eloDelta,
            humanScore = humanTotal,
            botScore = opponentTotal,
            isWin = isWin,
            matchLogText = encodedLog,
        )

        profileState.value = updated
        historyState.value = (listOf(entry) + historyState.value).take(20)
        return updated
    }

    override suspend fun resetProfile() {
        profileState.value = MasteryProfile.INITIAL
        historyState.value = emptyList()
    }
}
