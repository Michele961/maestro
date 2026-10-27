package it.maestro.app.shell.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import it.maestro.app.designsystem.audio.AndroidSoundManager
import it.maestro.app.designsystem.audio.SoundManager
import it.maestro.app.shell.crash.CrashReporter
import it.maestro.app.shell.crash.DefaultCrashReporter
import it.maestro.app.data.local.datastore.AcademyRepository
import it.maestro.app.data.local.datastore.DailyPuzzleRepository
import it.maestro.app.data.local.datastore.DataStoreAcademyRepository
import it.maestro.app.data.local.datastore.DataStoreDailyPuzzleRepository
import it.maestro.app.data.local.datastore.DataStoreMasteryRepository
import it.maestro.app.data.local.datastore.DataStoreRetentionRepository
import it.maestro.app.data.local.datastore.DataStoreSettingsRepository
import it.maestro.app.domain.mastery.MasteryRepository
import it.maestro.app.domain.retention.RetentionRepository
import it.maestro.app.data.local.datastore.SettingsRepository
import it.maestro.app.data.local.db.MaestroDatabase
import it.maestro.app.data.local.db.MaestroMigrations
import it.maestro.app.data.local.wallet.RoomWalletRepository
import it.maestro.app.domain.wallet.WalletRepository
import it.maestro.app.domain.match.MatchRepository
import it.maestro.app.data.repository.RoomMatchRepository
import it.maestro.app.feature.table.presentation.GameViewModel
import it.maestro.app.feature.missions.presentation.DailyMissionsViewModel
import it.maestro.app.feature.puzzle.presentation.DailyPuzzleViewModel
import it.maestro.app.feature.review.presentation.GameReviewViewModel
import it.maestro.app.feature.profile.presentation.ProfileViewModel
import it.maestro.app.feature.shop.presentation.ShopViewModel
import it.maestro.app.feature.academy.presentation.AcademyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import it.maestro.app.domain.rewards.RewardEngine
import it.maestro.app.domain.time.Clock
import it.maestro.app.domain.time.SystemClock
import it.maestro.app.shell.ShellViewModel
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "maestro_settings")
private val Context.dailyPuzzleDataStore by preferencesDataStore(name = "maestro_daily_puzzle")
private val Context.masteryDataStore by preferencesDataStore(name = "maestro_mastery")
private val Context.retentionDataStore by preferencesDataStore(name = "maestro_retention")
private val Context.academyDataStore by preferencesDataStore(name = "maestro_academy")

/**
 * Modulo Koin principale per l'iniezione delle dipendenze dell'app Android (ADR-0012, task F1-08, F1-14).
 */
public val appModule: Module = module {
    single<Clock> { SystemClock }

    single<MaestroDatabase> {
        Room.databaseBuilder(
            androidContext(),
            MaestroDatabase::class.java,
            "maestro_scopa.db",
        ).addMigrations(*MaestroMigrations.ALL).build()
    }

    single { get<MaestroDatabase>().savedMatchDao() }
    single { get<MaestroDatabase>().walletDao() }

    single<MatchRepository> {
        RoomMatchRepository(dao = get(), clock = get())
    }

    single<WalletRepository> {
        RoomWalletRepository(dao = get())
    }

    single<SettingsRepository> {
        DataStoreSettingsRepository(dataStore = androidContext().dataStore)
    }

    single<DailyPuzzleRepository> {
        DataStoreDailyPuzzleRepository(dataStore = androidContext().dailyPuzzleDataStore)
    }

    single { RewardEngine(wallet = get(), retention = get(), mastery = get(), clock = get()) }

    single<MasteryRepository> {
        DataStoreMasteryRepository(dataStore = androidContext().masteryDataStore, clock = get())
    }

    single<RetentionRepository> {
        DataStoreRetentionRepository(dataStore = androidContext().retentionDataStore)
    }

    single<AcademyRepository> {
        DataStoreAcademyRepository(dataStore = androidContext().academyDataStore, clock = get())
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
            gameSpeed = get<SettingsRepository>().settingsFlow.map { it.gameSpeed },
        )
    }

    viewModel {
        GameReviewViewModel()
    }

    viewModel {
        ShellViewModel(
            matchRepository = get(),
            rewardEngine = get(),
            soundManager = get(),
            clock = get(),
            savedStateHandle = get(),
        )
    }

    viewModel {
        DailyPuzzleViewModel(
            repository = get(),
            rewardEngine = get(),
            clock = get(),
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
            rewardEngine = get(),
            clock = get(),
        )
    }

    viewModel {
        AcademyViewModel(
            academyRepository = get(),
            rewardEngine = get(),
        )
    }

    viewModel {
        ShopViewModel(
            settingsRepository = get(),
            walletRepository = get(),
        )
    }
}
