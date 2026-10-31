package it.maestro.app.feature.season

import it.maestro.app.domain.academy.AcademyChapterId
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.season.SeasonFacts
import it.maestro.app.domain.season.SeasonProgress
import it.maestro.app.feature.season.ui.OlympusChair
import it.maestro.app.feature.season.ui.olympusChair
import it.maestro.game.training.TrainingFundamental
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Task F1G-22: la sedia dell'Olimpo è vuota col capitolo 10 in corso, occupata a Scalata completa, assente prima. */
class OlympusChairTest {
    private val allButTheLastTable = SeasonFacts(
        defeated = Opponent.entries.toSet() - setOf(Opponent.DON_ALFONSO, Opponent.MAESTRO),
        dailyPuzzleSolvedToday = true,
        completedAcademyChapters = AcademyChapterId.entries.toSet(),
        sparringChecksPlayed = TrainingFundamental.entries.toSet(),
        reviewed = true,
    )

    private fun chair(facts: SeasonFacts) = olympusChair(SeasonProgress.derive(facts, claimed = emptySet()))

    @Test
    fun thereIsNoChairBeforeTheLastChapter() {
        assertNull(chair(SeasonFacts()))
    }

    @Test
    fun theChairIsEmptyWhileChapterTenIsInProgress() {
        assertEquals(OlympusChair.EMPTY, chair(allButTheLastTable))
    }

    @Test
    fun theChairIsTakenWhenTheClimbIsComplete() {
        assertEquals(OlympusChair.TAKEN, chair(allButTheLastTable.copy(defeated = Opponent.entries.toSet())))
    }
}
