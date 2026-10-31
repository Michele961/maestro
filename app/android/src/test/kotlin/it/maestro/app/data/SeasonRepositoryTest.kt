package it.maestro.app.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import it.maestro.app.data.local.datastore.DataStoreSeasonRepository
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.season.SeasonFacts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Task F1G-07: sul disco si salvano solo i capitoli con ricompensa riscossa. */
class SeasonRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun repository(scope: TestScope, facts: MutableStateFlow<SeasonFacts>) = DataStoreSeasonRepository(
        PreferenceDataStoreFactory.create(scope = scope.backgroundScope) { folder.newFile("season.preferences_pb") },
        facts,
    )

    @Test
    fun claimingIsRememberedAndOnlyTheFirstTimeCounts() = runTest(UnconfinedTestDispatcher()) {
        val facts = MutableStateFlow(SeasonFacts(defeated = setOf(Opponent.GIGI)))
        val repo = repository(this, facts)
        assertEquals(listOf(2), repo.observeProgress().first().toClaim.map { it.number })
        assertTrue(repo.markClaimed(2))
        assertFalse(repo.markClaimed(2))
        assertTrue(repo.observeProgress().first().toClaim.isEmpty())
        // Anche se lo stato da cui deriva si azzera, il capitolo riscosso resta fatto.
        facts.value = SeasonFacts()
        assertEquals(listOf(2), repo.observeProgress().first().done.map { it.chapter.number })
    }
}
