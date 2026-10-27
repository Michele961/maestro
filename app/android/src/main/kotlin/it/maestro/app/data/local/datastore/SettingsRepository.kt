package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.data.local.cosmetics.CosmeticCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import it.maestro.app.domain.coach.Chattiness
import it.maestro.app.domain.settings.GameSpeed

/**
 * Impostazioni utente dell'applicazione Maestro (task F1-08).
 */
public data class AppSettings(
    val language: String = "system",
    val soundEnabled: Boolean = true,
    val gameSpeed: GameSpeed = GameSpeed.NORMAL,
    /** Quanto parla il Maestro (impostazione «Chiacchiere», `docs/app/features/maestro-voice.md` §3.4). */
    val maestroChattiness: Chattiness = Chattiness.NORMAL,
    /** Il giocatore ha già chiesto almeno un consiglio: il Maestro non glielo introduce più. */
    val hintEverUsed: Boolean = false,
    /** Nome scelto dal giocatore; `null` finché non l'ha detto (si chiede e si può cambiare, `docs/app/features/home.md`). */
    val playerName: String? = null,
    /** Il nome è già stato chiesto (anche se il giocatore ha saltato): non si richiede a ogni avvio. */
    val playerNameAsked: Boolean = false,
    /** Mostra al tavolo la corsa ai punti (Carte, Denari, Settebello, Primiera): spenta di default, è un aiuto. */
    val showPointsRace: Boolean = false,
    val preferredLevel: DifficultyLevel = DifficultyLevel.INTERMEDIO,
    val preferredTargetScore: Int = 11,
    val selectedTableTheme: String = "felt_classic_green",
    val selectedCardBackTheme: String = "back_classic_trevigiano",
    val selectedAvatar: String = "avatar_default",
    val unlockedCosmetics: Set<String> = CosmeticCatalog.DEFAULT_UNLOCKED_IDS,
)

/**
 * Interfaccia per l'accesso e la modifica delle impostazioni.
 */
public interface SettingsRepository {
    public val settingsFlow: Flow<AppSettings>
    public suspend fun updateLanguage(language: String)
    public suspend fun updateSoundEnabled(enabled: Boolean)
    public suspend fun updateGameSpeed(speed: GameSpeed)
    public suspend fun updateMaestroChattiness(chattiness: Chattiness)
    public suspend fun updateShowPointsRace(show: Boolean)
    public suspend fun updatePlayerName(name: String)

    /** Ricorda che il nome è stato chiesto, anche se il giocatore ha preferito non dirlo. */
    public suspend fun markPlayerNameAsked()
    public suspend fun markHintUsed()
    public suspend fun updatePreferredLevel(level: DifficultyLevel)
    public suspend fun updatePreferredTargetScore(targetScore: Int)
    public suspend fun updateSelectedTableTheme(tableThemeId: String)
    public suspend fun updateSelectedCardBackTheme(cardBackThemeId: String)
    public suspend fun updateSelectedAvatar(avatarId: String)
    public suspend fun unlockCosmetic(cosmeticId: String)
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
        val GAME_SPEED = stringPreferencesKey("game_speed")
        val MAESTRO_CHATTINESS = stringPreferencesKey("maestro_chattiness")
        val HINT_EVER_USED = booleanPreferencesKey("hint_ever_used")
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val PLAYER_NAME_ASKED = booleanPreferencesKey("player_name_asked")
        val SHOW_POINTS_RACE = booleanPreferencesKey("show_points_race")
        val PREFERRED_LEVEL = stringPreferencesKey("preferred_level")
        val PREFERRED_TARGET_SCORE = intPreferencesKey("preferred_target_score")
        val SELECTED_TABLE_THEME = stringPreferencesKey("selected_table_theme")
        val SELECTED_CARD_BACK_THEME = stringPreferencesKey("selected_card_back_theme")
        val SELECTED_AVATAR = stringPreferencesKey("selected_avatar")
        val UNLOCKED_COSMETICS = stringSetPreferencesKey("unlocked_cosmetics")
    }

    override val settingsFlow: Flow<AppSettings> = dataStore.data.map { preferences ->
        val lang = preferences[PreferencesKeys.LANGUAGE] ?: "system"
        val sound = preferences[PreferencesKeys.SOUND_ENABLED] ?: true
        val speed = preferences[PreferencesKeys.GAME_SPEED]
            ?.let { name -> GameSpeed.USER_CHOICES.firstOrNull { it.name == name } }
            ?: GameSpeed.NORMAL
        val chattiness = preferences[PreferencesKeys.MAESTRO_CHATTINESS]
            ?.let { name -> Chattiness.entries.firstOrNull { it.name == name } }
            ?: Chattiness.NORMAL
        val levelStr = preferences[PreferencesKeys.PREFERRED_LEVEL] ?: DifficultyLevel.INTERMEDIO.name
        val level = try {
            DifficultyLevel.valueOf(levelStr)
        } catch (_: IllegalArgumentException) {
            DifficultyLevel.INTERMEDIO
        }
        val targetScore = preferences[PreferencesKeys.PREFERRED_TARGET_SCORE] ?: 11
        val tableTheme = preferences[PreferencesKeys.SELECTED_TABLE_THEME] ?: "felt_classic_green"
        val cardBackTheme = preferences[PreferencesKeys.SELECTED_CARD_BACK_THEME] ?: "back_classic_trevigiano"
        val avatar = preferences[PreferencesKeys.SELECTED_AVATAR] ?: "avatar_default"
        val unlocked = preferences[PreferencesKeys.UNLOCKED_COSMETICS] ?: CosmeticCatalog.DEFAULT_UNLOCKED_IDS

        AppSettings(
            language = lang,
            soundEnabled = sound,
            gameSpeed = speed,
            maestroChattiness = chattiness,
            hintEverUsed = preferences[PreferencesKeys.HINT_EVER_USED] ?: false,
            playerName = preferences[PreferencesKeys.PLAYER_NAME]?.takeIf { it.isNotBlank() },
            playerNameAsked = preferences[PreferencesKeys.PLAYER_NAME_ASKED] ?: false,
            showPointsRace = preferences[PreferencesKeys.SHOW_POINTS_RACE] ?: false,
            preferredLevel = level,
            preferredTargetScore = targetScore,
            selectedTableTheme = tableTheme,
            selectedCardBackTheme = cardBackTheme,
            selectedAvatar = avatar,
            unlockedCosmetics = unlocked + CosmeticCatalog.DEFAULT_UNLOCKED_IDS,
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

    override suspend fun updateGameSpeed(speed: GameSpeed) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GAME_SPEED] = speed.name
        }
    }

    override suspend fun updateMaestroChattiness(chattiness: Chattiness) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAESTRO_CHATTINESS] = chattiness.name
        }
    }

    override suspend fun updateShowPointsRace(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_POINTS_RACE] = show
        }
    }

    override suspend fun updatePlayerName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYER_NAME] = name.trim().take(MAX_PLAYER_NAME)
            preferences[PreferencesKeys.PLAYER_NAME_ASKED] = true
        }
    }

    override suspend fun markPlayerNameAsked() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYER_NAME_ASKED] = true
        }
    }

    override suspend fun markHintUsed() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HINT_EVER_USED] = true
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

    override suspend fun updateSelectedTableTheme(tableThemeId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_TABLE_THEME] = tableThemeId
        }
    }

    override suspend fun updateSelectedCardBackTheme(cardBackThemeId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_CARD_BACK_THEME] = cardBackThemeId
        }
    }

    override suspend fun updateSelectedAvatar(avatarId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_AVATAR] = avatarId
        }
    }

    override suspend fun unlockCosmetic(cosmeticId: String) {
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.UNLOCKED_COSMETICS] ?: CosmeticCatalog.DEFAULT_UNLOCKED_IDS
            preferences[PreferencesKeys.UNLOCKED_COSMETICS] = current + cosmeticId
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

    override suspend fun updateGameSpeed(speed: GameSpeed) {
        _state.value = _state.value.copy(gameSpeed = speed)
    }

    override suspend fun updateMaestroChattiness(chattiness: Chattiness) {
        _state.value = _state.value.copy(maestroChattiness = chattiness)
    }

    override suspend fun updateShowPointsRace(show: Boolean) {
        _state.value = _state.value.copy(showPointsRace = show)
    }

    override suspend fun updatePlayerName(name: String) {
        _state.value = _state.value.copy(
            playerName = name.trim().take(MAX_PLAYER_NAME).takeIf { it.isNotBlank() },
            playerNameAsked = true,
        )
    }

    override suspend fun markPlayerNameAsked() {
        _state.value = _state.value.copy(playerNameAsked = true)
    }

    override suspend fun markHintUsed() {
        _state.value = _state.value.copy(hintEverUsed = true)
    }

    override suspend fun updatePreferredLevel(level: DifficultyLevel) {
        _state.value = _state.value.copy(preferredLevel = level)
    }

    override suspend fun updatePreferredTargetScore(targetScore: Int) {
        _state.value = _state.value.copy(preferredTargetScore = targetScore)
    }

    override suspend fun updateSelectedTableTheme(tableThemeId: String) {
        _state.value = _state.value.copy(selectedTableTheme = tableThemeId)
    }

    override suspend fun updateSelectedCardBackTheme(cardBackThemeId: String) {
        _state.value = _state.value.copy(selectedCardBackTheme = cardBackThemeId)
    }

    override suspend fun updateSelectedAvatar(avatarId: String) {
        _state.value = _state.value.copy(selectedAvatar = avatarId)
    }

    override suspend fun unlockCosmetic(cosmeticId: String) {
        _state.value = _state.value.copy(unlockedCosmetics = _state.value.unlockedCosmetics + cosmeticId)
    }
}

/** Lunghezza massima del nome del giocatore. */
public const val MAX_PLAYER_NAME: Int = 20
