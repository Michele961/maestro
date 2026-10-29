package it.maestro.app.domain.audio

/** Brani di musica dell'app (task F1R-20). */
public enum class MusicTrack { CIRCLE_DAY, CIRCLE_EVENING }

/** Rumori di fondo dell'app (task F1R-20). */
public enum class AmbientTrack { BAR, TABLE }

/**
 * Cosa suona sotto una schermata: un brano di [music] e un [ambient], uno dei due può mancare.
 * La musica non cambia da una schermata all'altra: resta lo stesso brano e non riparte (scelta di Michele,
 * 2026-10-29: niente cambi di musica di volta in volta).
 */
public data class Backdrop(
    val music: MusicTrack?,
    val ambient: AmbientTrack?,
)

/** Regole pure per scegliere lo sfondo sonoro (`docs/app/game-feel.md` §7, task F1R-20). */
public object Backdrops {
    /** Dalle 19 alle 5:59 il circolo suona la musica della sera. */
    public const val EVENING_FROM_HOUR: Int = 19
    public const val DAY_FROM_HOUR: Int = 6

    /** Brano del circolo per l'ora locale [hour] (0–23). */
    public fun circleMusic(hour: Int): MusicTrack =
        if (hour >= EVENING_FROM_HOUR || hour < DAY_FROM_HOUR) MusicTrack.CIRCLE_EVENING else MusicTrack.CIRCLE_DAY

    /** Circolo (Home, fine partita e schermate fuori partita): musica del momento e brusio del bar. */
    public fun circle(hour: Int): Backdrop = Backdrop(circleMusic(hour), AmbientTrack.BAR)

    /** Tavolo: la stessa musica del circolo, senza interruzioni, e il rumore del tavolo al posto del bar. */
    public fun table(hour: Int): Backdrop = Backdrop(circleMusic(hour), AmbientTrack.TABLE)
}
