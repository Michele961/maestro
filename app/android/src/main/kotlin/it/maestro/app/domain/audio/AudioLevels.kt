package it.maestro.app.domain.audio

/** I tre canali regolabili dal giocatore. */
public enum class AudioChannel { EFFECTS, MUSIC, AMBIENT }

/** Audio com'è ora: l'interruttore generale e i tre volumi, 0–100. Lo mostrano il tavolo e la sua pausa (task F1R-32). */
public data class AudioLevels(
    val soundEnabled: Boolean = true,
    val effects: Int = AudioMixer.MAX_VOLUME,
    val music: Int = 40,
    val ambient: Int = 40,
) {
    /** Il volume di [channel]. */
    public fun volume(channel: AudioChannel): Int = when (channel) {
        AudioChannel.EFFECTS -> effects
        AudioChannel.MUSIC -> music
        AudioChannel.AMBIENT -> ambient
    }
}
