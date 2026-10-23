package it.maestro.app

import android.app.Application
import it.maestro.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Classe Application dell'app Maestro. Inizializza il container di Dependency Injection Koin.
 */
public class MaestroApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MaestroApp)
            modules(appModule)
        }
    }
}
