package it.maestro.app.domain.story

import it.maestro.app.R
import it.maestro.app.domain.season.SeasonProgress
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Sceglie quali scene mostrare (task F1G-16a): dominio puro, nessun orologio e nessun I/O. Una scena già vista non
 * si ripropone mai.
 */
public object StoryDirector {
    /** Giorni senza giocare dopo i quali il Maestro dà il bentornato. */
    public const val RETURN_AFTER_DAYS: Int = 3

    public val WELCOME: StoryScene = StoryScene("welcome", StoryTexts.WELCOME_BEATS, SceneEnd(R.string.story_welcome_next_button))
    public val EPILOGUE: StoryScene = StoryScene(
        "epilogue", StoryTexts.EPILOGUE_BEATS, SceneEnd(R.string.story_epilogue_next_button, R.string.story_epilogue_next_text),
    )

    /** Introduzione della mano d'esordio guidata (`guide_intro_1`). Non si ricorda: la mano si gioca una volta. */
    public val GUIDE_INTRO: StoryScene = StoryScene("guide_intro", listOf(Beat(Speaker.MAESTRO, R.string.guide_intro_1_text)))

    /** Saluto a fine mano guidata: dopo i punti, il Maestro manda da Gigi. */
    public val GUIDE_END: StoryScene = StoryScene(
        "guide_end",
        listOf(Beat(Speaker.MAESTRO, R.string.guide_end_ok), Beat(Speaker.MAESTRO, R.string.guide_end_1_text)),
        SceneEnd(R.string.guide_end_button),
    )

    /** Scena di «So già giocare» e «Salta la guida» (`guide_skip_scene*`). */
    public val GUIDE_SKIP: StoryScene = StoryScene(
        "guide_skip",
        listOf(
            Beat(Speaker.MAESTRO, R.string.guide_skip_scene1_text),
            Beat(Speaker.MAESTRO, R.string.guide_skip_scene2_text),
            Beat(Speaker.MAESTRO, R.string.guide_skip_scene3_text),
        ),
        SceneEnd(R.string.guide_skip_scene_button),
    )

    private fun sceneId(number: Int, kind: String) = "ch${number.toString().padStart(2, '0')}_$kind"

    /** Apertura del capitolo [number], se la storia ne ha una. */
    public fun open(number: Int): StoryScene? = StoryTexts.OPEN[number]?.let { StoryScene(sceneId(number, "open"), it) }

    /** Scena a metà dei capitoli a due passi (9 e 10). */
    public fun mid(number: Int): StoryScene? = StoryTexts.MID[number]?.let { StoryScene(sceneId(number, "mid"), it) }

    /** Chiusura del capitolo [number]. */
    public fun close(number: Int): StoryScene? =
        StoryTexts.CLOSE[number]?.let { StoryScene(sceneId(number, "close"), it) }

    /**
     * Le scene da mostrare, in ordine, quando si apre la Scalata: il benvenuto a chi non ha ancora finito nulla, la
     * chiusura dell'ultimo capitolo finito, poi l'apertura (o la scena di metà) del capitolo corrente oppure
     * l'epilogo a Scalata finita. Chi ha già finito dei capitoli prima di questa funzione non rivede il benvenuto né
     * le chiusure più vecchie. Le scene in [seen] sono escluse.
     */
    public fun pending(progress: SeasonProgress, seen: Set<String>): List<StoryScene> {
        if (progress.chapters.isEmpty()) return emptyList()
        val scenes = buildList {
            if (progress.done.isEmpty()) add(WELCOME)
            progress.done.lastOrNull()?.let { last -> close(last.chapter.number)?.let(::add) }
            val current = progress.current
            when {
                progress.isComplete -> add(EPILOGUE)
                current == null -> Unit
                current.midReached -> (mid(current.chapter.number) ?: open(current.chapter.number))?.let(::add)
                else -> open(current.chapter.number)?.let(::add)
            }
        }
        return scenes.filter { it.id !in seen }
    }

    /**
     * Il saluto di rientro (`guide_return_*`): c'è se [lastPlayed] dista da [today] almeno [RETURN_AFTER_DAYS]
     * giorni, se c'è un capitolo da giocare e se per quell'assenza non è già stato dato. Il numero di tavoli e il
     * nome del prossimo sono quelli di [progress].
     */
    public fun returnGreeting(progress: SeasonProgress, lastPlayed: LocalDate?, today: LocalDate, seen: Set<String>): StoryScene? {
        val next = progress.current ?: return null
        if (lastPlayed == null || ChronoUnit.DAYS.between(lastPlayed, today) < RETURN_AFTER_DAYS) return null
        val id = "return_$lastPlayed"
        if (id in seen) return null
        val remaining = progress.remaining
        val title = BeatArg.Text(next.chapter.titleRes)
        val middle = if (remaining == 1) {
            Beat(Speaker.MAESTRO, R.string.guide_return_scene2_text_one, args = listOf(title))
        } else {
            Beat(Speaker.MAESTRO, R.string.guide_return_scene2_text, args = listOf(BeatArg.Number(remaining), title))
        }
        return StoryScene(
            id,
            listOf(Beat(Speaker.MAESTRO, R.string.guide_return_scene1_text), middle, Beat(Speaker.MAESTRO, R.string.guide_return_scene3_text)),
        )
    }
}
