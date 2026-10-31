package it.maestro.app.domain.home

/** Che cosa il Maestro suggerisce di premere in Home (`guide_home_*`, task F1G-16a). */
public enum class HomeGuide { NEW, FIRST_WIN, DAILY_OPEN, CHAPTER_READY, ALL_DONE }

/**
 * Ciò che serve a scegliere il suggerimento.
 *
 * @property matchesPlayed partite terminate.
 * @property matchesWon partite vinte.
 * @property puzzleEverSolved la Sfida del giorno è stata risolta almeno una volta.
 * @property puzzleSolvedToday la Sfida di oggi è risolta.
 * @property chapterToPlay la Scalata ha un capitolo da giocare.
 */
public data class GuideSignals(
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val puzzleEverSolved: Boolean = false,
    val puzzleSolvedToday: Boolean = false,
    val chapterToPlay: Boolean = false,
)

/** Regole del suggerimento in Home: dominio puro. Il Maestro nomina solo pulsanti che la Home sta mostrando. */
public object HomeGuides {
    /**
     * Il suggerimento per i [signals], tenuto conto delle funzioni [visible]. Ordine: nessuna partita, prima vittoria
     * senza Sfida, Sfida di oggi aperta, capitolo pronto, tutto fatto.
     */
    public fun choose(signals: GuideSignals, visible: Set<HomeFeature>): HomeGuide = when {
        signals.matchesPlayed == 0 -> HomeGuide.NEW
        signals.matchesWon >= 1 && !signals.puzzleEverSolved && HomeFeature.DAILY_PUZZLE in visible -> HomeGuide.FIRST_WIN
        HomeFeature.DAILY_PUZZLE in visible && !signals.puzzleSolvedToday -> HomeGuide.DAILY_OPEN
        signals.chapterToPlay && HomeFeature.SEASON in visible -> HomeGuide.CHAPTER_READY
        else -> HomeGuide.ALL_DONE
    }
}
