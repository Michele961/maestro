package it.maestro.app.domain.settings

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.domain.audio.MusicChoice
import it.maestro.app.domain.coach.Chattiness
import it.maestro.app.domain.cosmetics.CosmeticCatalog
import kotlinx.coroutines.flow.Flow

/**
 * Impostazioni utente dell'applicazione Maestro (task F1-08).
 */
public data class AppSettings(
    val language: String = "system",
    val soundEnabled: Boolean = true,
    /** Volume degli effetti sonori, 0–100 (task F1R-31b). */
    val effectsVolume: Int = 100,
    /** Volume della musica, 0–100; la musica arriva con F1R-20, il valore si conserva già. */
    val musicVolume: Int = 40,
    /** Volume dell'ambiente, 0–100; arriva con F1R-20. */
    val ambientVolume: Int = 40,
    /** Il brano scelto dal giocatore (task F1R-33); `AUTO` = giorno o sera secondo l'ora. */
    val musicChoice: MusicChoice = MusicChoice.AUTO,
    /** Vibrazione nelle celebrazioni (Scopa, riepilogo smazzata). */
    val hapticsEnabled: Boolean = true,
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
    public suspend fun updateEffectsVolume(volume: Int)
    public suspend fun updateMusicVolume(volume: Int)
    public suspend fun updateAmbientVolume(volume: Int)
    public suspend fun updateMusicChoice(choice: MusicChoice)
    public suspend fun updateHapticsEnabled(enabled: Boolean)
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
