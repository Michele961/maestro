package it.maestro.app.data.local.datastore

import it.maestro.app.domain.mastery.MasteryRepository
import it.maestro.app.domain.mastery.MatchHistoryEntry

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
import it.maestro.app.domain.time.Clock
import it.maestro.app.domain.time.SystemClock

/**
 * Implementazione su DataStore Preferences (ADR-0012, task F2-04).
 */
public class DataStoreMasteryRepository(
    private val dataStore: DataStore<Preferences>,
    private val clock: Clock = SystemClock,
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
        opponent: String?,
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
            id = "match_${clock.nowMillis()}",
            timestampMillis = clock.nowMillis(),
            accuracy = matchAccuracy,
            cognitiveElo = updated.cognitiveElo,
            eloDelta = eloDelta,
            humanScore = humanTotal,
            botScore = opponentTotal,
            isWin = isWin,
            matchLogText = encodedLog,
            opponent = opponent,
            sparringObjective = trainingFundamental.takeIf { it != TrainingFundamental.NONE }?.name,
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
                    opponent = parts.getOrNull(9)?.takeIf { it.isNotBlank() },
                    sparringObjective = parts.getOrNull(10)?.takeIf { it.isNotBlank() },
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
            "${it.id}|${it.timestampMillis}|${it.accuracy}|${it.cognitiveElo}|${it.eloDelta}|${it.humanScore}|${it.botScore}|${if (it.isWin) 1 else 0}|$encodedLog|${it.opponent.orEmpty()}|${it.sparringObjective.orEmpty()}"
        }
}

/**
 * Implementazione in memoria per test unitari e preview Compose.
 */
public class InMemoryMasteryRepository(
    initialProfile: MasteryProfile = MasteryProfile.INITIAL,
    initialHistory: List<MatchHistoryEntry> = emptyList(),
    private val clock: Clock = SystemClock,
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
        opponent: String?,
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
            id = "match_${clock.nowMillis()}",
            timestampMillis = clock.nowMillis(),
            accuracy = matchAccuracy,
            cognitiveElo = updated.cognitiveElo,
            eloDelta = eloDelta,
            humanScore = humanTotal,
            botScore = opponentTotal,
            isWin = isWin,
            matchLogText = encodedLog,
            opponent = opponent,
            sparringObjective = trainingFundamental.takeIf { it != TrainingFundamental.NONE }?.name,
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
