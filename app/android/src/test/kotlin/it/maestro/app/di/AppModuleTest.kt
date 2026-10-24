package it.maestro.app.di

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.data.repository.MatchRepository
import it.maestro.app.data.repository.RestoreMatchResult
import it.maestro.app.game.GameViewModel
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertNotNull

class AppModuleTest {

    private class FakeMatchRepository : MatchRepository {
        override suspend fun saveActiveMatch(
            log: MatchLog,
            level: DifficultyLevel,
            targetScore: Int,
            p1Score: Int,
            p2Score: Int,
        ) {}
        override suspend fun restoreActiveMatch(): RestoreMatchResult = RestoreMatchResult.None
        override suspend fun clearActiveMatch() {}
        override suspend fun markCompleted(id: String, winner: Seat?, p1Score: Int, p2Score: Int) {}
    }

    @Test
    fun testGameViewModelResolutionWithExplicitConstructor() {
        val testApp = koinApplication {
            modules(
                module {
                    single<MatchRepository> { FakeMatchRepository() }
                    viewModel {
                        GameViewModel(
                            matchRepository = get(),
                            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
                        )
                    }
                },
            )
        }
        val vm = testApp.koin.get<GameViewModel>()
        assertNotNull(vm)
    }
}
