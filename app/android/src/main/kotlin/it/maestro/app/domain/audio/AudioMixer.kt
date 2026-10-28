package it.maestro.app.domain.audio

/**
 * Livelli audio dell'app (task F1R-31b): tre volumi indipendenti da 0 a 100 e l'interruttore generale.
 * Regole pure, senza Android: il `SoundManager` ne è l'implementazione sulla piattaforma.
 */
public object AudioMixer {

    /** Volume massimo di un livello. */
    public const val MAX_VOLUME: Int = 100

    /** Fattore di abbassamento della musica mentre parla il Maestro (`game-feel.md` §7). */
    public const val DUCK_FACTOR: Float = 0.35f

    /** Guadagno 0..1 per gli effetti sonori: nullo se l'audio è spento. */
    public fun effectsGain(effectsVolume: Int, soundEnabled: Boolean): Float = gain(effectsVolume, soundEnabled)

    /** Guadagno 0..1 per l'ambiente: nullo se l'audio è spento. */
    public fun ambientGain(ambientVolume: Int, soundEnabled: Boolean): Float = gain(ambientVolume, soundEnabled)

    /** Guadagno 0..1 per la musica, abbassato mentre il Maestro parla. */
    public fun musicGain(musicVolume: Int, soundEnabled: Boolean, maestroSpeaking: Boolean): Float =
        gain(musicVolume, soundEnabled) * duckMusicWhile(maestroSpeaking)

    /** Moltiplicatore della musica: abbassata mentre [maestroSpeaking], piena altrimenti. */
    public fun duckMusicWhile(maestroSpeaking: Boolean): Float = if (maestroSpeaking) DUCK_FACTOR else 1f

    private fun gain(volume: Int, soundEnabled: Boolean): Float =
        if (soundEnabled) volume.coerceIn(0, MAX_VOLUME) / MAX_VOLUME.toFloat() else 0f
}
