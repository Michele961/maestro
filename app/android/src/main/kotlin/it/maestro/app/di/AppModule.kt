package it.maestro.app.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import it.maestro.app.audio.AndroidSoundManager
import it.maestro.app.audio.SoundManager
import it.maestro.app.crash.CrashReporter
import it.maestro.app.crash.DefaultCrashReporter
import it.maestro.app.data.local.datastore.DailyPuzzleRepository
import it.maestro.app.data.local.datastore.DataStoreDailyPuzzleRepository
import it.maestro.app.data.local.datastore.DataStoreMasteryRepository
import it.maestro.app.data.local.datastore.DataStoreRetentionRepository
import it.maestro.app.data.local.datastore.DataStoreSettingsRepository
import it.maestro.app.data.local.datastore.MasteryRepository
import it.maestro.app.data.local.datastore.RetentionRepository
import it.maestro.app.data.local.datastore.SettingsRepository
import it.maestro.app.data.local.db.MaestroDatabase
import it.maestro.app.data.repository.MatchRepository
import it.maestro.app.data.repository.RoomMatchRepository
import it.maestro.app.game.GameViewModel
import it.maestro.app.ui.screens.DailyMissionsViewModel
import it.maestro.app.ui.screens.DailyPuzzleViewModel
import it.maestro.app.ui.screens.GameReviewViewModel
import it.maestro.app.ui.screens.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore(name = "maestro_settings")
private val Context.dailyPuzzleDataStore by preferencesDataStore(name = "maestro_daily_puzzle")
private val Context.masteryDataStore by preferencesDataStore(name = "maestro_mastery")
private val Context.retentionDataStore by preferencesDataStore(name = "maestro_retention")

/**
 * Modulo Koin principale per l'iniezione delle dipendenze dell'app Android (ADR-0012, task F1-08, F1-14).
 */
public val appModule: Module = module {
    single<MaestroDatabase> {
        Room.databaseBuilder(
            androidContext(),
            MaestroDatabase::class.java,
            "maestro_scopa.db",
        ).fallbackToDestructiveMigration().build()
    }

    single { get<MaestroDatabase>().savedMatchDao() }

    single<MatchRepository> {
        RoomMatchRepository(dao = get())
    }

    single<SettingsRepository> {
        DataStoreSettingsRepository(dataStore = androidContext().dataStore)
    }

    single<DailyPuzzleRepository> {
        DataStoreDailyPuzzleRepository(dataStore = androidContext().dailyPuzzleDataStore)
    }

    single<MasteryRepository> {
        DataStoreMasteryRepository(dataStore = androidContext().masteryDataStore)
    }

    single<RetentionRepository> {
        DataStoreRetentionRepository(dataStore = androidContext().retentionDataStore)
    }

    single<SoundManager> {
        AndroidSoundManager(
            context = androidContext(),
            settingsRepository = get(),
            scope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
        )
    }

    single<CrashReporter> {
        DefaultCrashReporter()
    }

    viewModel {
        GameViewModel(
            matchRepository = get(),
        )
    }

    viewModel {
        GameReviewViewModel()
    }

    viewModel {
        DailyPuzzleViewModel(
            repository = get(),
            retentionRepository = get(),
        )
    }

    viewModel {
        ProfileViewModel(
            masteryRepository = get(),
        )
    }

    viewModel {
        DailyMissionsViewModel(
            retentionRepository = get(),
        )
    }
}
