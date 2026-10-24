package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import it.maestro.ai.bot.DifficultyLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Impostazioni utente dell'applicazione Maestro (task F1-08).
 */
public data class AppSettings(
    val language: String = "system",
    val soundEnabled: Boolean = true,
    val preferredLevel: DifficultyLevel = DifficultyLevel.INTERMEDIO,
    val preferredTargetScore: Int = 11,
)

/**
 * Interfaccia per l'accesso e la modifica delle impostazioni.
 */
public interface SettingsRepository {
    public val settingsFlow: Flow<AppSettings>
    public suspend fun updateLanguage(language: String)
    public suspend fun updateSoundEnabled(enabled: Boolean)
    public suspend fun updatePreferredLevel(level: DifficultyLevel)
    public suspend fun updatePreferredTargetScore(targetScore: Int)
}

/**
 * Implementazione su DataStore Preferences (ADR-0012, task F1-08).
 */
public class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private object PreferencesKeys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val PREFERRED_LEVEL = stringPreferencesKey("preferred_level")
        val PREFERRED_TARGET_SCORE = intPreferencesKey("preferred_target_score")
    }

    override val settingsFlow: Flow<AppSettings> = dataStore.data.map { preferences ->
        val lang = preferences[PreferencesKeys.LANGUAGE] ?: "system"
        val sound = preferences[PreferencesKeys.SOUND_ENABLED] ?: true
        val levelStr = preferences[PreferencesKeys.PREFERRED_LEVEL] ?: DifficultyLevel.INTERMEDIO.name
        val level = try {
            DifficultyLevel.valueOf(levelStr)
        } catch (_: IllegalArgumentException) {
            DifficultyLevel.INTERMEDIO
        }
        val targetScore = preferences[PreferencesKeys.PREFERRED_TARGET_SCORE] ?: 11

        AppSettings(
            language = lang,
            soundEnabled = sound,
            preferredLevel = level,
            preferredTargetScore = targetScore,
        )
    }

    override suspend fun updateLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    override suspend fun updateSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    override suspend fun updatePreferredLevel(level: DifficultyLevel) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_LEVEL] = level.name
        }
    }

    override suspend fun updatePreferredTargetScore(targetScore: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_TARGET_SCORE] = targetScore
        }
    }
}

/**
 * Implementazione in memoria per test o preview.
 */
public class InMemorySettingsRepository(
    initialSettings: AppSettings = AppSettings(),
) : SettingsRepository {
    private val _state = MutableStateFlow(initialSettings)
    override val settingsFlow: Flow<AppSettings> = _state.asStateFlow()

    override suspend fun updateLanguage(language: String) {
        _state.value = _state.value.copy(language = language)
    }

    override suspend fun updateSoundEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(soundEnabled = enabled)
    }

    override suspend fun updatePreferredLevel(level: DifficultyLevel) {
        _state.value = _state.value.copy(preferredLevel = level)
    }

    override suspend fun updatePreferredTargetScore(targetScore: Int) {
        _state.value = _state.value.copy(preferredTargetScore = targetScore)
    }
}
