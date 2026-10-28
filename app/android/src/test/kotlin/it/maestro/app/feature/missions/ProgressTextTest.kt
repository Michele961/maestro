package it.maestro.app.feature.missions

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import it.maestro.app.domain.progress.AchievementId
import it.maestro.app.domain.progress.Experience
import it.maestro.app.feature.missions.ui.descriptionRes
import it.maestro.app.feature.missions.ui.experienceLevelNameRes
import it.maestro.app.feature.missions.ui.nameRes
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Task F1R-29f: ogni obiettivo e ogni livello di esperienza ha un testo vero, diverso dagli altri (ADR-0007). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ProgressTextTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun everyAchievementHasAUniqueNameAndADescription() {
        val names = AchievementId.entries.map { context.getString(it.nameRes) }
        assertTrue(names.all { it.isNotBlank() })
        assertEquals(names.size, names.distinct().size, "Due obiettivi con lo stesso nome")
        assertTrue(AchievementId.entries.all { context.getString(it.descriptionRes).isNotBlank() })
    }

    @Test
    fun everyExperienceLevelHasAUniqueName() {
        val names = (1..Experience.MAX_LEVEL).map { context.getString(experienceLevelNameRes(it)) }
        assertTrue(names.all { it.isNotBlank() })
        assertEquals(Experience.MAX_LEVEL, names.distinct().size)
    }

    @Test
    fun levelsOutsideTheRangeUseTheNearestName() {
        assertEquals(experienceLevelNameRes(1), experienceLevelNameRes(0))
        assertEquals(experienceLevelNameRes(Experience.MAX_LEVEL), experienceLevelNameRes(99))
    }
}
