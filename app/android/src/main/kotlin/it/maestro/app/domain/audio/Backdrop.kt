package it.maestro.app.domain.audio

/** Brani di musica dell'app (task F1R-20). */
public enum class MusicTrack { CIRCLE_DAY, CIRCLE_EVENING, TENSE }

/**
 * La musica scelta dal giocatore (task F1R-33): [AUTO] lascia fare all'app (giorno o sera), gli altri fissano
 * un brano per tutta l'app (solo brani abbastanza lunghi da non stancare in loop: oltre 45 secondi), [OFF] toglie la musica lasciando gli altri suoni.
 */
public enum class MusicChoice(public val track: MusicTrack?) {
    AUTO(null),
    CIRCLE_DAY(MusicTrack.CIRCLE_DAY),
    CIRCLE_EVENING(MusicTrack.CIRCLE_EVENING),
    TENSE(MusicTrack.TENSE),
    OFF(null),
}

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

    /** Il brano che suona davvero: quello scelto dal giocatore, o quello del momento con [MusicChoice.AUTO]; `null` se spenta. */
    public fun music(hour: Int, choice: MusicChoice): MusicTrack? = when (choice) {
        MusicChoice.AUTO -> circleMusic(hour)
        MusicChoice.OFF -> null
        else -> choice.track
    }

    /** Circolo (Home, fine partita e schermate fuori partita): musica del momento e brusio del bar. */
    public fun circle(hour: Int, choice: MusicChoice = MusicChoice.AUTO): Backdrop = Backdrop(music(hour, choice), AmbientTrack.BAR)

    /** Tavolo: la stessa musica del circolo, senza interruzioni, e il rumore del tavolo al posto del bar. */
    public fun table(hour: Int, choice: MusicChoice = MusicChoice.AUTO): Backdrop = Backdrop(music(hour, choice), AmbientTrack.TABLE)
}
