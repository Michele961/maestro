package it.maestro.app.domain.season

import it.maestro.app.R
import it.maestro.app.domain.academy.AcademyChapterId
import it.maestro.app.domain.opponents.Opponent
import it.maestro.game.training.TrainingFundamental

/**
 * I Circoli della Scalata (`docs/design/stagione-storia.md`, mappa dei Circoli); i nomi stanno nelle risorse.
 * [backgroundRes] è lo sfondo illustrato del luogo, senza persone né testo (F1G-21, `story_bg_*`).
 */
public enum class Circle(public val nameRes: Int, public val backgroundRes: Int) {
    BAR_TABLE(R.string.story_circle_bar_table, R.drawable.story_bg_bar_table),
    NEIGHBORHOOD_CLUB(R.string.story_circle_neighborhood_club, R.drawable.story_bg_neighborhood_club),
    NOBLE_LOUNGE(R.string.story_circle_noble_lounge, R.drawable.story_bg_noble_lounge),
    GRAND_CASINO(R.string.story_circle_grand_casino, R.drawable.story_bg_grand_casino),
    OLYMPUS(R.string.story_circle_olympus, R.drawable.story_bg_olympus),
}

/**
 * Che cosa completa un capitolo. Ogni obiettivo si legge da stato che esiste già ([SeasonFacts]): la Stagione
 * non ha una sua fonte di verità sui progressi.
 */
public sealed interface ChapterGoal {
    /** Tutti gli [opponents] battuti in Classica. */
    public data class DefeatOpponents(val opponents: List<Opponent>) : ChapterGoal

    /** Almeno uno Sparring giocato con il Check [fundamental]. */
    public data class SparringCheck(val fundamental: TrainingFundamental) : ChapterGoal

    /** La Sfida del giorno di oggi è risolta. */
    public data object DailyPuzzle : ChapterGoal

    /** Tutte le lezioni del capitolo [chapter] dell'Accademia completate. */
    public data class AcademyChapter(val chapter: AcademyChapterId) : ChapterGoal

    /** Mano d'esordio guidata (F1G-02): la funzione non esiste ancora, il capitolo resta nascosto. */
    public data object GuidedHand : ChapterGoal

    /** Revisione dell'ultima partita: non c'è ancora un segnale registrato, il capitolo resta nascosto. */
    public data object ReviewLastMatch : ChapterGoal

    /** Tutti gli obiettivi di [goals]. */
    public data class All(val goals: List<ChapterGoal>) : ChapterGoal
}

/**
 * Un capitolo della Scalata, come dato. I testi (`story_chNN_*`) sono risorse; [midRes] e [midQuipRes] compaiono solo nei
 * capitoli con due partite, dopo la prima.
 */
public data class SeasonChapter(
    val number: Int,
    val circle: Circle,
    val goal: ChapterGoal,
    val titleRes: Int,
    val openRes: Int,
    val closeRes: Int,
    val quipRes: Int? = null,
    val midRes: Int? = null,
    val midQuipRes: Int? = null,
    /** Crediti dati una sola volta alla chiusura del capitolo, tramite `RewardEngine`. */
    val rewardCredits: Int = CHAPTER_CREDITS,
) {
    public companion object {
        public const val CHAPTER_CREDITS: Int = 20
        public const val FINAL_CHAPTER_CREDITS: Int = 50
    }
}

/** I dieci capitoli della Scalata dei Circoli, nell'ordine della storia approvata. */
public object SeasonChapters {
    public val ALL: List<SeasonChapter> = listOf(
        SeasonChapter(
            1, Circle.BAR_TABLE, ChapterGoal.GuidedHand,
            R.string.story_ch01_title, R.string.story_ch01_open, R.string.story_ch01_close, R.string.story_ch01_quip,
        ),
        SeasonChapter(
            2, Circle.BAR_TABLE, ChapterGoal.DefeatOpponents(listOf(Opponent.GIGI)),
            R.string.story_ch02_title, R.string.story_ch02_open, R.string.story_ch02_close, R.string.story_ch02_quip,
        ),
        SeasonChapter(
            3, Circle.BAR_TABLE, ChapterGoal.SparringCheck(TrainingFundamental.SEVENS_COUNT),
            R.string.story_ch03_title, R.string.story_ch03_open, R.string.story_ch03_close,
        ),
        SeasonChapter(
            4, Circle.NEIGHBORHOOD_CLUB, ChapterGoal.DailyPuzzle,
            R.string.story_ch04_title, R.string.story_ch04_open, R.string.story_ch04_close,
        ),
        SeasonChapter(
            5, Circle.NEIGHBORHOOD_CLUB, ChapterGoal.DefeatOpponents(listOf(Opponent.NONNA_ROSA)),
            R.string.story_ch05_title, R.string.story_ch05_open, R.string.story_ch05_close, R.string.story_ch05_quip,
        ),
        SeasonChapter(
            6, Circle.NEIGHBORHOOD_CLUB, ChapterGoal.AcademyChapter(AcademyChapterId.CHAPTER_1_FONDAMENTI),
            R.string.story_ch06_title, R.string.story_ch06_open, R.string.story_ch06_close,
        ),
        SeasonChapter(
            7, Circle.NOBLE_LOUNGE, ChapterGoal.ReviewLastMatch,
            R.string.story_ch07_title, R.string.story_ch07_open, R.string.story_ch07_close,
        ),
        SeasonChapter(
            8, Circle.NOBLE_LOUNGE, ChapterGoal.DefeatOpponents(listOf(Opponent.TONINO)),
            R.string.story_ch08_title, R.string.story_ch08_open, R.string.story_ch08_close, R.string.story_ch08_quip,
        ),
        SeasonChapter(
            9, Circle.GRAND_CASINO,
            ChapterGoal.All(
                listOf(
                    ChapterGoal.AcademyChapter(AcademyChapterId.CHAPTER_2_PRIMIERA),
                    ChapterGoal.SparringCheck(TrainingFundamental.PRIMIERA_LEAD),
                ),
            ),
            R.string.story_ch09_title, R.string.story_ch09_open, R.string.story_ch09_close,
        ),
        SeasonChapter(
            10, Circle.OLYMPUS, ChapterGoal.DefeatOpponents(listOf(Opponent.DON_ALFONSO, Opponent.MAESTRO)),
            R.string.story_ch10_title, R.string.story_ch10_open, R.string.story_ch10_close, R.string.story_ch10_quip,
            midRes = R.string.story_ch10_mid, midQuipRes = R.string.story_ch10_maestro_quip,
            rewardCredits = SeasonChapter.FINAL_CHAPTER_CREDITS,
        ),
    )
}
