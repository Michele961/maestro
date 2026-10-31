package it.maestro.app.domain.rewards

import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.data.local.datastore.InMemoryRetentionRepository
import it.maestro.app.data.local.datastore.InMemorySeasonRepository
import it.maestro.app.data.local.wallet.InMemoryWalletRepository
import it.maestro.app.domain.season.SeasonChapters
import it.maestro.app.domain.time.FakeClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

/** Task F1G-07: la ricompensa di capitolo nasce solo da `RewardEngine` e una sola volta. */
class SeasonRewardTest {

    private val clock = FakeClock(LocalDate.of(2026, 10, 31))
    private val wallet = InMemoryWalletRepository()
    private val engine = RewardEngine(
        wallet, InMemoryRetentionRepository(), InMemoryMasteryRepository(), clock,
        season = InMemorySeasonRepository(),
    )
    private val chapter2 = SeasonChapters.ALL.first { it.number == 2 }

    @Test
    fun aChapterGivesItsCreditsOnlyOnce() = runTest {
        val before = wallet.observeBalance().first()
        assertEquals(chapter2.rewardCredits, engine.onSeasonChapterCompleted(chapter2).creditsEarned)
        assertEquals(0, engine.onSeasonChapterCompleted(chapter2).creditsEarned)
        assertEquals(before + chapter2.rewardCredits, wallet.observeBalance().first())
    }

    @Test
    fun withoutASeasonRepositoryNothingIsGiven() = runTest {
        val bare = RewardEngine(wallet, InMemoryRetentionRepository(), InMemoryMasteryRepository(), clock)
        assertEquals(0, bare.onSeasonChapterCompleted(chapter2).creditsEarned)
    }
}
