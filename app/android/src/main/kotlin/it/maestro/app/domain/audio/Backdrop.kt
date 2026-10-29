package it.maestro.app.domain.audio

/** Brani di musica dell'app (task F1R-20). */
public enum class MusicTrack { CIRCLE_DAY, CIRCLE_EVENING, TENSE, VICTORY, DEFEAT }

/** Rumori di fondo dell'app (task F1R-20). */
public enum class AmbientTrack { BAR, TABLE }

/**
 * Cosa suona sotto una schermata: un brano di [music] e un [ambient], uno dei due può mancare.
 * La musica ricomincia da capo in loop se [musicLoops], altrimenti suona una volta sola (vittoria e sconfitta).
 */
public data class Backdrop(
    val music: MusicTrack?,
    val ambient: AmbientTrack?,
    val musicLoops: Boolean = true,
)

/** Regole pure per scegliere lo sfondo sonoro (`docs/app/game-feel.md` §7, task F1R-20). */
public object Backdrops {
    /** Dalle 19 alle 5:59 il circolo suona la musica della sera. */
    public const val EVENING_FROM_HOUR: Int = 19
    public const val DAY_FROM_HOUR: Int = 6

    /** Brano del circolo per l'ora locale [hour] (0–23). */
    public fun circleMusic(hour: Int): MusicTrack =
        if (hour >= EVENING_FROM_HOUR || hour < DAY_FROM_HOUR) MusicTrack.CIRCLE_EVENING else MusicTrack.CIRCLE_DAY

    /** Circolo (Home e schermate fuori partita): musica del momento e brusio del bar. */
    public fun circle(hour: Int): Backdrop = Backdrop(circleMusic(hour), AmbientTrack.BAR)

    /** Tavolo: musica del circolo e rumore del tavolo; nell'ultima smazzata [lastDeal] la musica diventa tesa. */
    public fun table(hour: Int, lastDeal: Boolean): Backdrop =
        Backdrop(if (lastDeal) MusicTrack.TENSE else circleMusic(hour), AmbientTrack.TABLE)

    /** Fine partita: lo stacchetto di vittoria o di sconfitta, una volta sola, senza rumori di fondo. */
    public fun result(won: Boolean): Backdrop =
        Backdrop(if (won) MusicTrack.VICTORY else MusicTrack.DEFEAT, ambient = null, musicLoops = false)
}
