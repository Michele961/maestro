package it.maestro.app.data

import it.maestro.app.data.local.datastore.InMemoryRetentionRepository
import it.maestro.app.data.local.missions.DailyMissionType
import it.maestro.app.data.local.missions.StreakState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Test unitari della logica di retention, streak quotidiana, gettone riposo e missioni (task F2-05).
 */
class RetentionRepositoryTest {

    private val day1 = LocalDate.of(2026, 10, 1)
    private val day2 = LocalDate.of(2026, 10, 2)
    private val day3 = LocalDate.of(2026, 10, 3)
    private val day4 = LocalDate.of(2026, 10, 4)
    private val day5 = LocalDate.of(2026, 10, 5)
    private val day6 = LocalDate.of(2026, 10, 6)

    @Test
    fun streakIncrement_consecutiveDaysIncreasesStreakAndBestStreak() = runTest {
        val repo = InMemoryRetentionRepository()

        // Giorno 1: prima attività
        val s1 = repo.recordGameActivity(day1)
        assertEquals(1, s1.currentStreak)
        assertEquals(1, s1.bestStreak)
        assertEquals(1, s1.freezeTokens)

        // Giocare di nuovo nello stesso giorno non incrementa ulteriormente
        val s1Repeat = repo.recordGameActivity(day1)
        assertEquals(1, s1Repeat.currentStreak)

        // Giorno 2: giocato consecutivamente -> streak 2
        val s2 = repo.recordGameActivity(day2)
        assertEquals(2, s2.currentStreak)
        assertEquals(2, s2.bestStreak)
        assertEquals(1, s2.freezeTokens)
    }

    @Test
    fun streakFreeze_consumesFreezeTokenWhenSkippingSingleDay() = runTest {
        val repo = InMemoryRetentionRepository()

        // Giorno 1 e 2 giocati
        repo.recordGameActivity(day1)
        repo.recordGameActivity(day2)

        // Giorno 3: NON GIOCATO!
        // Giorno 4: utente apre l'app (passati 2 giorni)
        val s4Before = repo.getStreak(day4)
        // Il gettone riposo è stato consumato per salvare la streak
        assertEquals(0, s4Before.freezeTokens)
        assertEquals(2, s4Before.currentStreak)
        assertEquals(day3.toString(), s4Before.lastFreezeUsedDate)

        // Utente gioca al Giorno 4 -> streak incrementa a 3!
        val s4After = repo.recordGameActivity(day4)
        assertEquals(3, s4After.currentStreak)
        assertEquals(3, s4After.bestStreak)
        assertEquals(0, s4After.freezeTokens)
    }

    @Test
    fun streakReset_resetsToZeroWhenSkippingWithoutFreezeTokens() = runTest {
        val repo = InMemoryRetentionRepository(
            initialStreak = StreakState(
                currentStreak = 5,
                bestStreak = 5,
                lastPlayedDate = day1.toString(),
                freezeTokens = 0, // Nessun gettone riposo
            ),
        )

        // Giorno 2: saltato. Al giorno 3 senza gettoni la streak è azzerata!
        val s3 = repo.getStreak(day3)
        assertEquals(0, s3.currentStreak)
        assertEquals(5, s3.bestStreak)

        // Se gioca al giorno 3, ricomincia da 1
        val s3Played = repo.recordGameActivity(day3)
        assertEquals(1, s3Played.currentStreak)
        assertEquals(5, s3Played.bestStreak)
    }

    @Test
    fun streakReset_resetsWhenSkippingTwoConsecutiveDaysEvenWithTokens() = runTest {
        val repo = InMemoryRetentionRepository()

        // Giorno 1 giocato (ha 1 freeze token)
        repo.recordGameActivity(day1)

        // Giorno 2 e Giorno 3 NON GIOCATI (2 giorni consecutivi saltati)
        // Giorno 4: controlla stato -> streak azzerata perché il gettone copre al massimo 1 giorno
        val s4 = repo.getStreak(day4)
        assertEquals(0, s4.currentStreak)

        // Giocando al giorno 4 ricomincia da 1
        val s4Played = repo.recordGameActivity(day4)
        assertEquals(1, s4Played.currentStreak)
    }

    @Test
    fun dailyMissions_deterministicGenerationAndProgressTracking() = runTest {
        val repo = InMemoryRetentionRepository()

        val missionsDay1 = repo.getDailyMissions(day1)
        assertEquals(3, missionsDay1.missions.size)
        assertEquals(day1.toString(), missionsDay1.date)

        val targetMission = missionsDay1.missions.first()
        val type = targetMission.type

        // Aggiorna progresso
        repo.updateMissionProgress(day1, type, increment = 1)
        val updated = repo.getDailyMissions(day1)
        val updatedMission = updated.missions.first { it.type == type }
        assertEquals(1, updatedMission.progress)

        // Completa missione
        repo.setMissionCompleted(day1, type)
        val completedState = repo.getDailyMissions(day1)
        assertTrue(completedState.missions.first { it.type == type }.isCompleted)
        assertFalse(completedState.missions.first { it.type == type }.isClaimed)

        // Riscatta ricompensa
        val claimed = repo.claimMissionReward(day1, type)
        assertTrue(claimed)
        val claimedState = repo.getDailyMissions(day1)
        assertTrue(claimedState.missions.first { it.type == type }.isClaimed)

        // Un secondo riscatto non va a buon fine
        val doubleClaim = repo.claimMissionReward(day1, type)
        assertFalse(doubleClaim)

        // Il giorno successivo genera 3 missioni per la nuova data
        val missionsDay2 = repo.getDailyMissions(day2)
        assertEquals(day2.toString(), missionsDay2.date)
        assertEquals(0, missionsDay2.completedCount)
    }
}
