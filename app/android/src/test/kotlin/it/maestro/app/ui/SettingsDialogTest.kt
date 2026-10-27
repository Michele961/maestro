package it.maestro.app.ui

import it.maestro.app.shell.crash.TestCrashException
import it.maestro.app.designsystem.components.MaestroMood
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsDialogTest {

    @Test
    fun maestroMoodEnumContainsExpectedMoods() {
        val expected = setOf(
            MaestroMood.NEUTRAL,
            MaestroMood.THINKING,
            MaestroMood.PLEASED,
            MaestroMood.WARNING,
            MaestroMood.CELEBRATING,
            MaestroMood.APPLAUDING,
            MaestroMood.TRIUMPHANT,
            MaestroMood.DESPAIR,
            MaestroMood.ENCOURAGING,
        )
        assertEquals(expected, MaestroMood.entries.toSet())
    }

    @Test
    fun soundToggleCallbackInvokedCorrectly() {
        var currentSound = true
        val onToggle = { enabled: Boolean -> currentSound = enabled }

        onToggle(false)
        assertFalse(currentSound)

        onToggle(true)
        assertTrue(currentSound)
    }

    @Test
    fun triggerCrashCallbackPropagatesTestCrash() {
        val onCrash = { throw TestCrashException() }

        assertFailsWith<TestCrashException> {
            onCrash()
        }
    }

    @Test
    fun maestroChattinessIsRememberedByTheSettingsRepository() = kotlinx.coroutines.test.runTest {
        val repo = it.maestro.app.data.local.datastore.InMemorySettingsRepository()
        assertEquals(it.maestro.app.domain.coach.Chattiness.NORMAL, repo.settingsFlow.first().maestroChattiness)
        repo.updateMaestroChattiness(it.maestro.app.domain.coach.Chattiness.QUIET)
        assertEquals(it.maestro.app.domain.coach.Chattiness.QUIET, repo.settingsFlow.first().maestroChattiness)
        assertFalse(repo.settingsFlow.first().hintEverUsed)
        repo.markHintUsed()
        assertTrue(repo.settingsFlow.first().hintEverUsed)
    }
}
