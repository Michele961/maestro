package it.maestro.app.domain.story

import it.maestro.app.R
import it.maestro.app.domain.academy.AcademyChapterId
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.season.SeasonFacts
import it.maestro.app.domain.season.SeasonProgress
import it.maestro.game.training.TrainingFundamental
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Task F1G-16a: quali scene compaiono, in che ordine, e una sola volta. */
class StoryDirectorTest {

    private fun progress(facts: SeasonFacts = SeasonFacts()) = SeasonProgress.derive(facts, claimed = emptySet())

    private fun ids(progress: SeasonProgress, seen: Set<String> = emptySet()) =
        StoryDirector.pending(progress, seen).map { it.id }

    private val afterGigi = SeasonFacts(defeated = setOf(Opponent.GIGI))
    private val everything = SeasonFacts(
        defeated = Opponent.entries.toSet(),
        dailyPuzzleSolvedToday = true,
        completedAcademyChapters = AcademyChapterId.entries.toSet(),
        sparringChecksPlayed = TrainingFundamental.entries.toSet(),
    )

    @Test
    fun openingTheClimbForTheFirstTimeShowsTheWelcomeThenTheCurrentChapterOpening() {
        assertEquals(listOf("welcome", "ch02_open"), ids(progress()))
    }

    @Test
    fun aSceneAlreadySeenNeverComesBack() {
        assertEquals(listOf("ch02_open"), ids(progress(), seen = setOf("welcome")))
        assertEquals(emptyList(), ids(progress(), seen = setOf("welcome", "ch02_open")))
    }

    @Test
    fun aCompletedChapterShowsItsClosingThenTheNextOpening() {
        assertEquals(listOf("ch02_close", "ch03_open"), ids(progress(afterGigi)))
        assertEquals(listOf("ch03_open"), ids(progress(afterGigi), seen = setOf("ch02_close")))
    }

    @Test
    fun whoAlreadyFinishedChaptersBeforeTheScenesExistedSeesNeitherTheWelcomeNorOlderClosings() {
        val facts = SeasonFacts(
            defeated = setOf(Opponent.GIGI, Opponent.NONNA_ROSA),
            sparringChecksPlayed = setOf(TrainingFundamental.SEVENS_COUNT),
            dailyPuzzleSolvedToday = true,
        )
        // Fatti i capitoli 2-5: solo la chiusura dell'ultimo e l'apertura del corrente.
        assertEquals(listOf("ch05_close", "ch06_open"), ids(progress(facts)))
    }

    @Test
    fun theMidSceneReplacesTheOpeningOnceTheFirstStepIsDone() {
        val atAlfonso = SeasonFacts(
            defeated = setOf(Opponent.GIGI, Opponent.NONNA_ROSA, Opponent.TONINO, Opponent.DON_ALFONSO),
            sparringChecksPlayed = TrainingFundamental.entries.toSet(),
            dailyPuzzleSolvedToday = true,
            completedAcademyChapters = AcademyChapterId.entries.toSet(),
        )
        assertEquals(listOf("ch09_close", "ch10_mid"), ids(progress(atAlfonso)))
    }

    @Test
    fun theFinishedClimbShowsTheLastClosingThenTheEpilogue() {
        assertEquals(listOf("ch10_close", "epilogue"), ids(progress(everything)))
        assertEquals(emptyList(), ids(progress(everything), seen = setOf("ch10_close", "epilogue")))
    }

    @Test
    fun nothingIsShownBeforeTheProgressIsKnown() {
        assertEquals(emptyList(), ids(SeasonProgress(emptyList())))
    }

    @Test
    fun everyVisibleChapterHasAnOpeningAndAClosing() {
        for (n in 1..10) {
            assertNotNull(StoryDirector.open(n), "apertura $n")
            assertNotNull(StoryDirector.close(n), "chiusura $n")
        }
        assertNotNull(StoryDirector.mid(9))
        assertNotNull(StoryDirector.mid(10))
        assertNull(StoryDirector.mid(2))
    }

    @Test
    fun onlyTheWelcomeTheTenthClosingAndTheEpilogueHaveANamedVariant() {
        val named = (listOf(StoryDirector.WELCOME, StoryDirector.EPILOGUE) + (1..10).mapNotNull { StoryDirector.close(it) })
            .flatMap { scene -> scene.beats.filter { it.namedTextRes != null }.map { scene.id } }
        assertEquals(listOf("welcome", "epilogue", "ch10_close"), named)
        // La versione senza nome esiste sempre: è `textRes`.
        assertTrue(StoryDirector.WELCOME.beats.all { it.textRes != 0 })
    }

    @Test
    fun scenesAreSpokenByTheRightCharacters() {
        assertEquals(Speaker.GIGI, StoryDirector.open(2)?.beats?.first()?.speaker)
        assertEquals(Speaker.ALFONSO, StoryDirector.open(10)?.beats?.first()?.speaker)
        assertEquals(Speaker.MAESTRO, StoryDirector.WELCOME.beats.first().speaker)
    }

    // --- Saluto di rientro ---

    private val today = LocalDate.of(2026, 10, 31)

    @Test
    fun theReturnGreetingNeedsAtLeastThreeDaysAway() {
        val p = progress()
        assertNull(StoryDirector.returnGreeting(p, today.minusDays(2), today, emptySet()))
        assertNotNull(StoryDirector.returnGreeting(p, today.minusDays(3), today, emptySet()))
    }

    @Test
    fun theReturnGreetingNeedsAPastActivity() {
        assertNull(StoryDirector.returnGreeting(progress(), null, today, emptySet()))
    }

    @Test
    fun theReturnGreetingIsGivenOncePerAbsence() {
        val lastPlayed = today.minusDays(5)
        val scene = StoryDirector.returnGreeting(progress(), lastPlayed, today, emptySet())
        assertNotNull(scene)
        assertNull(StoryDirector.returnGreeting(progress(), lastPlayed, today, setOf(scene.id)))
        // Una nuova assenza, dopo aver giocato di nuovo, è un'altra occasione.
        assertNotNull(StoryDirector.returnGreeting(progress(), today.minusDays(4), today, setOf(scene.id)))
    }

    @Test
    fun theReturnGreetingNamesHowManyTablesAreLeftAndTheNextChapter() {
        val scene = requireNotNull(StoryDirector.returnGreeting(progress(), today.minusDays(9), today, emptySet()))
        val middle = scene.beats[1]
        assertEquals(R.string.guide_return_scene2_text, middle.textRes)
        assertEquals(listOf(BeatArg.Number(8), BeatArg.Text(R.string.story_ch02_title)), middle.args)
    }

    @Test
    fun withOneTableLeftTheGreetingUsesTheSingularForm() {
        val facts = everything.copy(defeated = Opponent.entries.toSet() - Opponent.MAESTRO)
        val scene = requireNotNull(StoryDirector.returnGreeting(progress(facts), today.minusDays(9), today, emptySet()))
        assertEquals(R.string.guide_return_scene2_text_one, scene.beats[1].textRes)
        assertEquals(listOf<BeatArg>(BeatArg.Text(R.string.story_ch10_title)), scene.beats[1].args)
    }

    @Test
    fun noGreetingWhenTheClimbIsFinished() {
        assertNull(StoryDirector.returnGreeting(progress(everything), today.minusDays(9), today, emptySet()))
    }
}
