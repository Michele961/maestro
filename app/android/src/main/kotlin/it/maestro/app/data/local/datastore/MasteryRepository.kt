package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.mastery.AbilityRadar
import it.maestro.ai.mastery.MasteryCalculator
import it.maestro.ai.mastery.MasteryProfile
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Repository per la persistenza e l'osservazione dell'Indice di Maestria (Elo Cognitivo)
 * e del Radar delle Abilità (task F2-04, `docs/vision.md` §5.A).
 */
public interface MasteryRepository {
    public fun observeMasteryProfile(): Flow<MasteryProfile>
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
    }

    override fun observeMasteryProfile(): Flow<MasteryProfile> =
        dataStore.data.map { preferences -> mapToProfile(preferences) }

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

        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MATCHES_PLAYED] = updated.matchesPlayed
            preferences[PreferencesKeys.ACCURACY_EMA] = updated.accuracyEma
            preferences[PreferencesKeys.COGNITIVE_ELO] = updated.cognitiveElo
            preferences[PreferencesKeys.RADAR_SEVENS] = updated.radar.sevens
            preferences[PreferencesKeys.RADAR_DENARI] = updated.radar.denari
            preferences[PreferencesKeys.RADAR_PRIMIERA] = updated.radar.primiera
            preferences[PreferencesKeys.RADAR_BOARD] = updated.radar.board
            preferences[PreferencesKeys.RADAR_ENDGAME] = updated.radar.endgame
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
}

/**
 * Implementazione in memoria per test unitari e preview Compose.
 */
public class InMemoryMasteryRepository(
    initialProfile: MasteryProfile = MasteryProfile.INITIAL,
) : MasteryRepository {

    private val profileState = MutableStateFlow(initialProfile)

    override fun observeMasteryProfile(): Flow<MasteryProfile> = profileState.asStateFlow()

    override suspend fun getMasteryProfile(): MasteryProfile = profileState.value

    override suspend fun recordMatch(
        matchAnalysis: MatchAnalysis,
        matchLog: MatchLog,
        humanSeat: Seat,
        trainingScore: Pair<Int, Int>?,
        trainingFundamental: TrainingFundamental,
    ): MasteryProfile {
        val current = profileState.value
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
        profileState.value = updated
        return updated
    }

    override suspend fun resetProfile() {
        profileState.value = MasteryProfile.INITIAL
    }
}
