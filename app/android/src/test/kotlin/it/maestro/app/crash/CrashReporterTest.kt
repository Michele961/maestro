package it.maestro.app.crash

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import it.maestro.app.shell.crash.DefaultCrashReporter
import it.maestro.app.shell.crash.TestCrashException

class CrashReporterTest {

    @Test
    fun triggerTestCrashThrowsTestCrashException() {
        val reporter = DefaultCrashReporter()
        val exception = assertFailsWith<TestCrashException> {
            reporter.triggerTestCrash()
        }
        assertEquals("Test Crash Maestro F1-14 — simulazione per verifica Crashlytics", exception.message)
    }

    @Test
    fun defaultReporterHandlesLoggingWithoutThrowing() {
        val reporter = DefaultCrashReporter()
        // Non deve sollevare eccezioni
        reporter.log("Test log event")
        reporter.recordException(IllegalStateException("Handled test exception"))
    }
}
