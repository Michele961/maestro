package it.maestro.app.di

import it.maestro.app.game.GameViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Modulo Koin principale per l'iniezione delle dipendenze dell'app Android.
 */
public val appModule: Module = module {
    viewModelOf(::GameViewModel)
}
