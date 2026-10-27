package it.maestro.app.shell.crash

import android.util.Log

/**
 * Eccezione generata intenzionalmente per testare la ricezione di crash (task F1-14).
 */
public class TestCrashException(
    message: String = "Test Crash Maestro F1-14 — simulazione per verifica Crashlytics",
) : RuntimeException(message)

/**
 * Interfaccia per la registrazione di eccezioni e log diagnostici (task F1-14).
 */
public interface CrashReporter {
    public fun log(message: String)
    public fun recordException(throwable: Throwable)
    public fun triggerTestCrash(): Nothing
}

/**
 * Implementazione standard per diagnostica locale e Crashlytics (task F1-14).
 */
public class DefaultCrashReporter : CrashReporter {

    override fun log(message: String) {
        Log.i("MaestroCrashReporter", message)
    }

    override fun recordException(throwable: Throwable) {
        Log.e("MaestroCrashReporter", "Exception recorded", throwable)
    }

    override fun triggerTestCrash(): Nothing {
        throw TestCrashException()
    }
}
