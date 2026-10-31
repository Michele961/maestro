package it.maestro.app.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.maestro.app.data.local.datastore.DataStoreGuideRepository
import it.maestro.app.domain.guide.GuideProgress
import it.maestro.app.domain.guide.GuidedHandStatus
import it.maestro.app.domain.match.HouseRule
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

/** F1G-01 e F1G-16b: il flag «primo avvio completato» e il segnale di revisione hanno un default sicuro e sopravvivono. */
class GuideRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun open(scope: CoroutineScope) =
        PreferenceDataStoreFactory.create(scope = scope) { java.io.File(folder.root, "guide.preferences_pb") }

    @Test
    fun withoutDataTheGuideHasNotStartedAndNothingWasReviewed() = runTest {
        val scope = CoroutineScope(Job() + Dispatchers.Unconfined)
        assertEquals(GuideProgress(), DataStoreGuideRepository(open(scope)).observe().first())
        scope.cancel()
    }

    @Test
    fun theOutcomeOfTheFirstHandAndTheReviewSignalSurviveARestartOfTheProcess() = runTest {
        val firstRun = CoroutineScope(Job() + Dispatchers.Unconfined)
        val repository = DataStoreGuideRepository(open(firstRun))
        repository.setHand(GuidedHandStatus.SKIPPED)
        repository.markReviewed()
        repository.markReviewed()
        firstRun.cancel()

        val secondRun = CoroutineScope(Job() + Dispatchers.Unconfined)
        assertEquals(
            GuideProgress(GuidedHandStatus.SKIPPED, reviewed = true),
            DataStoreGuideRepository(open(secondRun)).observe().first(),
        )
        secondRun.cancel()
    }

    @Test
    fun theSeenRuleLessonsStartEmptyAndSurviveARestartOfTheProcess() = runTest {
        val firstRun = CoroutineScope(Job() + Dispatchers.Unconfined)
        val repository = DataStoreGuideRepository(open(firstRun))
        assertEquals(emptySet<HouseRule>(), repository.observe().first().seenRuleLessons)
        repository.markRuleLessonSeen(HouseRule.RE_BELLO)
        repository.markRuleLessonSeen(HouseRule.RE_BELLO)
        repository.markRuleLessonSeen(HouseRule.NAPOLA)
        firstRun.cancel()

        val secondRun = CoroutineScope(Job() + Dispatchers.Unconfined)
        assertEquals(
            setOf(HouseRule.RE_BELLO, HouseRule.NAPOLA),
            DataStoreGuideRepository(open(secondRun)).observe().first().seenRuleLessons,
        )
        secondRun.cancel()
    }

    @Test
    fun anUnknownValueFromAFutureVersionReadsAsNotDone() = runTest {
        val scope = CoroutineScope(Job() + Dispatchers.Unconfined)
        val store = open(scope)
        store.edit { it[stringPreferencesKey("guide_hand")] = "something_new" }
        assertEquals(GuidedHandStatus.NOT_DONE, DataStoreGuideRepository(store).observe().first().hand)
        scope.cancel()
    }
}
