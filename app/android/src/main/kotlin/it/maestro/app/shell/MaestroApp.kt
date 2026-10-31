package it.maestro.app.shell

import android.app.Application
import android.util.Log
import it.maestro.app.domain.reminder.ReminderCoordinator
import it.maestro.app.shell.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Classe Application dell'app Maestro. Inizializza il container di Dependency Injection Koin
 * e registra il gestore di crash per diagnostica accurata.
 */
public class MaestroApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("MaestroCrash", "CRASH DETECTED on thread ${thread.name}", throwable)
            try {
                filesDir.resolve("last_crash.txt").writeText(
                    "${throwable::class.qualifiedName}: ${throwable.message}\n" +
                    Log.getStackTraceString(throwable)
                )
            } catch (_: Throwable) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }

        if (org.koin.core.context.GlobalContext.getOrNull() == null) {
            startKoin {
                androidLogger()
                androidContext(this@MaestroApp)
                modules(appModule)
            }
        }

        // Il promemoria riparte a ogni avvio dell'app (F1G-13); al riavvio del telefono ci pensa il ricevitore.
        CoroutineScope(Dispatchers.Default).launch {
            runCatching { GlobalContext.get().get<ReminderCoordinator>().reschedule() }
        }
    }
}
