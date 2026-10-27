package it.maestro.app

import android.content.pm.ApplicationInfo
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import it.maestro.app.shell.DeveloperTools

/** Task F1R-05: gli strumenti di sviluppo dipendono solo dal flag di build debuggabile. */
class DeveloperToolsTest {

    @Test
    fun releaseFlagsDisableDeveloperTools() {
        assertFalse(DeveloperTools.isDebuggable(0))
        assertTrue(DeveloperTools.isDebuggable(ApplicationInfo.FLAG_DEBUGGABLE))
    }
}
