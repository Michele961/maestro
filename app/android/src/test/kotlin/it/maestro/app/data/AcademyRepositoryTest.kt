package it.maestro.app.data

import it.maestro.app.data.local.academy.AcademyModuleId
import it.maestro.app.data.local.datastore.InMemoryAcademyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test unitari della logica di sblocco e persistenza della progressione dell'Accademia (task F2-06).
 */
class AcademyRepositoryTest {

    @Test
    fun initialProgressHasOnlyModuleOneUnlocked() = runTest {
        val repo = InMemoryAcademyRepository()
        val progress = repo.getProgress()

        val mod1 = progress[AcademyModuleId.MODULE_1_ABBECEDARIO]!!
        val mod2 = progress[AcademyModuleId.MODULE_2_PRIMIERA]!!
        val mod3 = progress[AcademyModuleId.MODULE_3_DISPARI]!!

        assertTrue(mod1.isUnlocked, "Il modulo 1 deve essere sbloccato di default")
        assertFalse(mod1.isCompleted)

        assertFalse(mod2.isUnlocked, "Il modulo 2 deve essere bloccato inizialmente")
        assertFalse(mod2.isCompleted)

        assertFalse(mod3.isUnlocked, "Il modulo 3 deve essere bloccato inizialmente")
        assertFalse(mod3.isCompleted)
    }

    @Test
    fun completingModuleOneUnlocksModuleTwo() = runTest {
        val repo = InMemoryAcademyRepository()
        repo.completeModule(AcademyModuleId.MODULE_1_ABBECEDARIO)

        val progress = repo.observeProgress().first()
        val mod1 = progress[AcademyModuleId.MODULE_1_ABBECEDARIO]!!
        val mod2 = progress[AcademyModuleId.MODULE_2_PRIMIERA]!!
        val mod3 = progress[AcademyModuleId.MODULE_3_DISPARI]!!

        assertTrue(mod1.isCompleted)
        assertTrue(mod2.isUnlocked, "Completare il modulo 1 deve sbloccare il modulo 2")
        assertFalse(mod2.isCompleted)
        assertFalse(mod3.isUnlocked, "Il modulo 3 deve rimanere bloccato finché il 2 non è completato")
    }

    @Test
    fun completingModuleTwoUnlocksModuleThree() = runTest {
        val repo = InMemoryAcademyRepository(
            initialCompleted = setOf(AcademyModuleId.MODULE_1_ABBECEDARIO),
        )
        repo.completeModule(AcademyModuleId.MODULE_2_PRIMIERA)

        val progress = repo.getProgress()
        val mod2 = progress[AcademyModuleId.MODULE_2_PRIMIERA]!!
        val mod3 = progress[AcademyModuleId.MODULE_3_DISPARI]!!

        assertTrue(mod2.isCompleted)
        assertTrue(mod3.isUnlocked, "Completare il modulo 2 deve sbloccare il modulo 3")
        assertFalse(mod3.isCompleted)
    }

    @Test
    fun completingModuleThreeMarksAllCompleted() = runTest {
        val repo = InMemoryAcademyRepository(
            initialCompleted = setOf(
                AcademyModuleId.MODULE_1_ABBECEDARIO,
                AcademyModuleId.MODULE_2_PRIMIERA,
            ),
        )
        repo.completeModule(AcademyModuleId.MODULE_3_DISPARI)

        val progress = repo.getProgress()
        assertTrue(progress.values.all { it.isCompleted })
        assertTrue(progress.values.all { it.isUnlocked })
    }

    @Test
    fun resetProgressRestoresInitialState() = runTest {
        val repo = InMemoryAcademyRepository(
            initialCompleted = setOf(
                AcademyModuleId.MODULE_1_ABBECEDARIO,
                AcademyModuleId.MODULE_2_PRIMIERA,
            ),
        )
        repo.resetProgress()

        val progress = repo.getProgress()
        assertTrue(progress[AcademyModuleId.MODULE_1_ABBECEDARIO]!!.isUnlocked)
        assertFalse(progress[AcademyModuleId.MODULE_1_ABBECEDARIO]!!.isCompleted)
        assertFalse(progress[AcademyModuleId.MODULE_2_PRIMIERA]!!.isUnlocked)
        assertFalse(progress[AcademyModuleId.MODULE_3_DISPARI]!!.isUnlocked)
    }
}
