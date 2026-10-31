package it.maestro.app.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import it.maestro.app.data.local.datastore.DataStoreStoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** F1G-16a: le scene viste hanno un default sicuro (nessuna) e sopravvivono alla chiusura del processo. */
class StoryRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun open(scope: CoroutineScope) =
        PreferenceDataStoreFactory.create(scope = scope) { java.io.File(folder.root, "story.preferences_pb") }

    @Test
    fun withoutDataNoSceneIsSeen() = runTest {
        val scope = CoroutineScope(Job() + Dispatchers.Unconfined)
        assertEquals(emptySet<String>(), DataStoreStoryRepository(open(scope)).observeSeen().first())
        scope.cancel()
    }

    @Test
    fun seenScenesSurviveARestartOfTheProcess() = runTest {
        val firstRun = CoroutineScope(Job() + Dispatchers.Unconfined)
        val repository = DataStoreStoryRepository(open(firstRun))
        repository.markSeen("welcome")
        repository.markSeen("ch02_open")
        repository.markSeen("welcome")
        firstRun.cancel()

        val secondRun = CoroutineScope(Job() + Dispatchers.Unconfined)
        assertEquals(setOf("welcome", "ch02_open"), DataStoreStoryRepository(open(secondRun)).observeSeen().first())
        secondRun.cancel()
    }
}
