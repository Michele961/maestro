package it.maestro.app.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import it.maestro.app.data.local.datastore.DataStoreHomeFeaturesRepository
import it.maestro.app.domain.home.HomeFeature
import it.maestro.app.domain.home.PresentedFeatures
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** F1G-03: lo stato «funzioni presentate» ha un default sicuro e sopravvive alla chiusura del processo. */
class HomeFeaturesRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun open(scope: CoroutineScope) =
        PreferenceDataStoreFactory.create(scope = scope) { java.io.File(folder.root, "home.preferences_pb") }

    @Test
    fun withoutDataTheStateIsTheSafeDefault() = runTest {
        val scope = CoroutineScope(Job() + Dispatchers.Unconfined)
        assertEquals(PresentedFeatures(), DataStoreHomeFeaturesRepository(open(scope)).load())
        scope.cancel()
    }

    @Test
    fun theStateSurvivesARestartOfTheProcess() = runTest {
        val firstRun = CoroutineScope(Job() + Dispatchers.Unconfined)
        val saved = PresentedFeatures(
            initialized = true,
            revealed = setOf(HomeFeature.DAILY_PUZZLE, HomeFeature.GAME_TILES),
            introduced = setOf(HomeFeature.DAILY_PUZZLE),
            closingDay = "2026-10-31",
        )
        DataStoreHomeFeaturesRepository(open(firstRun)).save(saved)
        firstRun.cancel()

        val secondRun = CoroutineScope(Job() + Dispatchers.Unconfined)
        assertEquals(saved, DataStoreHomeFeaturesRepository(open(secondRun)).load())
        secondRun.cancel()
    }

    @Test
    fun unknownNamesFromAFutureVersionAreIgnored() = runTest {
        val scope = CoroutineScope(Job() + Dispatchers.Unconfined)
        val store = open(scope)
        store.edit { it[stringSetPreferencesKey("home_features_revealed")] = setOf("MISSIONS", "FUTURE_THING") }
        assertEquals(setOf(HomeFeature.MISSIONS), DataStoreHomeFeaturesRepository(store).load().revealed)
        scope.cancel()
    }
}
