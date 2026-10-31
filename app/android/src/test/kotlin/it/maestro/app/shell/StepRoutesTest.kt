package it.maestro.app.shell

import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.season.SeasonChapters
import it.maestro.app.domain.season.ChapterState
import it.maestro.app.domain.season.ChapterStatus
import it.maestro.app.domain.story.StepAction
import it.maestro.app.domain.story.nextStep
import it.maestro.game.training.TrainingFundamental
import kotlin.test.Test
import kotlin.test.assertEquals

/** F1G-16a: dal «prossimo passo» di ogni capitolo alla rotta esistente giusta. */
class StepRoutesTest {

    @Test
    fun playingAnOpponentOpensTheirPreGame() {
        assertEquals(Route.PreGame("NONNA_ROSA"), StepAction.PlayOpponent(Opponent.NONNA_ROSA).toRoute())
    }

    @Test
    fun aSparringCheckOpensThePreGameWithTheObjectiveSelected() {
        assertEquals(
            Route.PreGame("GIGI", "SEVENS_COUNT"),
            StepAction.Sparring(Opponent.GIGI, TrainingFundamental.SEVENS_COUNT).toRoute(),
        )
    }

    @Test
    fun theOtherStepsOpenTheirScreen() {
        assertEquals(Route.DailyPuzzle, StepAction.DailyPuzzle.toRoute())
        assertEquals(Route.Academy, StepAction.Academy.toRoute())
        assertEquals(Route.Profile, StepAction.Profile.toRoute())
    }

    @Test
    fun everyChapterStepLeadsToARoute() {
        val destinations = SeasonChapters.ALL.associate { chapter ->
            chapter.number to ChapterState(chapter, ChapterStatus.CURRENT).nextStep?.action?.toRoute()
        }
        assertEquals(Route.PreGame("GIGI"), destinations[2])
        assertEquals(Route.PreGame("GIGI", "SEVENS_COUNT"), destinations[3])
        assertEquals(Route.DailyPuzzle, destinations[4])
        assertEquals(Route.PreGame("NONNA_ROSA"), destinations[5])
        assertEquals(Route.Academy, destinations[6])
        assertEquals(Route.PreGame("TONINO"), destinations[8])
        assertEquals(Route.Academy, destinations[9])
        assertEquals(Route.PreGame("DON_ALFONSO"), destinations[10])
    }
}
