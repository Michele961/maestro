package it.maestro.app.data

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.data.local.datastore.InMemorySettingsRepository
import it.maestro.app.data.local.db.SavedMatchEntity
import it.maestro.app.data.repository.InMemoryMatchRepository
import it.maestro.app.domain.match.RestoreMatchResult
import it.maestro.app.feature.table.presentation.GameIntent
import it.maestro.app.feature.table.presentation.GameViewModel
import it.maestro.game.CoreGame
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ScopaConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test per il salvataggio, ripresa e gestione della versione delle regole (ADR-0003, task F1-08):
 * - Chiudere l'app a metà partita e riaprirla porta allo stesso identico stato.
 * - Un log con rulesVersion diverso è gestito senza crash (WrongRulesVersion).
 * - Persistenza delle impostazioni utente.
 */
class MatchPersistenceTest {

    @Test
    fun closingAppMidGameAndReopeningRestoresIdenticalState() = runBlocking {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val repository = InMemoryMatchRepository()

        // 1. Sessione iniziale: avvio partita e giocata di alcune mosse
        val viewModel1 = GameViewModel(
            matchRepository = repository,
            ioDispatcher = Dispatchers.Unconfined,
            coroutineScope = testScope,
        )

        viewModel1.onIntent(
            GameIntent.StartMatch(
                config = ScopaConfig(targetScore = 11),
                level = DifficultyLevel.PRINCIPIANTE,
                seed = 42L,
                humanSeat = Seat.P1,
            ),
        )

        // Giochiamo 4 mosse umane per avanzare lo stato a metà partita
        var movesPlayed = 0
        while (movesPlayed < 4 && viewModel1.uiState.value.phase == DealPhase.PLAYING) {
            val state = viewModel1.uiState.value
            if (state.pendingCaptureChoice != null) {
                viewModel1.onIntent(
                    GameIntent.SelectCapture(
                        state.pendingCaptureChoice!!.card,
                        state.pendingCaptureChoice!!.options.first(),
                    ),
                )
                continue
            }
            if (state.isHumanTurn && state.humanHand.isNotEmpty()) {
                viewModel1.onIntent(GameIntent.PlayCard(state.humanHand.first()))
                movesPlayed++
            }
        }

        val stateBeforeClosing = viewModel1.uiState.value

        // 2. Simuliamo la chiusura dell'app (distruzione del ViewModel) e riapertura
        val restoreResult = repository.restoreActiveMatch()
        assertTrue(restoreResult is RestoreMatchResult.Success, "Il ripristino deve avere successo")

        // 3. Verifichiamo che lo stato ripristinato sia identico al momento della chiusura
        val restoredMatch = restoreResult as RestoreMatchResult.Success
        assertEquals(DifficultyLevel.PRINCIPIANTE, restoredMatch.level)
        assertEquals(11, restoredMatch.targetScore)

        // 4. Inizializziamo un nuovo ViewModel con lo stato ripristinato
        val viewModel2 = GameViewModel(
            matchRepository = repository,
            ioDispatcher = Dispatchers.Unconfined,
            coroutineScope = testScope,
        )

        viewModel2.onIntent(
            GameIntent.RestoreMatch(
                session = restoredMatch.session,
                level = restoredMatch.level,
            ),
        )

        val stateAfterReopening = viewModel2.uiState.value
        assertEquals(stateBeforeClosing.phase, stateAfterReopening.phase)
        assertEquals(stateBeforeClosing.humanHand, stateAfterReopening.humanHand)
        assertEquals(stateBeforeClosing.tableCards, stateAfterReopening.tableCards)
        assertEquals(stateBeforeClosing.deckCardCount, stateAfterReopening.deckCardCount)
        assertEquals(stateBeforeClosing.opponentCardCount, stateAfterReopening.opponentCardCount)
        assertEquals(stateBeforeClosing.humanCapturedCount, stateAfterReopening.humanCapturedCount)
        assertEquals(stateBeforeClosing.opponentCapturedCount, stateAfterReopening.opponentCapturedCount)
        assertEquals(stateBeforeClosing.humanScore, stateAfterReopening.humanScore)
        assertEquals(stateBeforeClosing.opponentScore, stateAfterReopening.opponentScore)
        assertEquals(stateBeforeClosing.isHumanTurn, stateAfterReopening.isHumanTurn)

        // 5. Continuiamo a giocare senza errori
        if (stateAfterReopening.isHumanTurn && stateAfterReopening.humanHand.isNotEmpty()) {
            viewModel2.onIntent(GameIntent.PlayCard(stateAfterReopening.humanHand.first()))
            val nextState = viewModel2.uiState.value
            assertEquals(stateAfterReopening.humanHand.size - 1, nextState.humanHand.size)
        }
    }

    @Test
    fun logWithDifferentRulesVersionIsHandledGracefullyWithoutCrash() = runBlocking {
        val repository = InMemoryMatchRepository()

        // Creiamo un log valido, poi modifichiamo la rulesVersion a un valore futuro incompatibile (es. 99)
        val validLog = MatchLog(
            rulesVersion = CoreGame.RULES_VERSION,
            config = ScopaConfig(),
            seed = 42L,
            moves = emptyList(),
        )
        val validText = validLog.toText()
        // Sostituiamo la riga della rulesVersion (formato "rules <version>")
        val wrongVersionText = validText.replace("rules ${CoreGame.RULES_VERSION}", "rules 99")

        repository.setActiveMatchDirectly(
            SavedMatchEntity(
                id = "active_match",
                matchLogText = wrongVersionText,
                levelName = DifficultyLevel.INTERMEDIO.name,
                targetScore = 11,
                status = "IN_PROGRESS",
                winnerSeat = null,
                p1Score = 0,
                p2Score = 0,
                createdAt = 1000L,
                updatedAt = 2000L,
            ),
        )

        // Nessun crash: deve restituire WrongRulesVersion tipizzato (criterio F1-08)
        val result = repository.restoreActiveMatch()
        assertTrue(result is RestoreMatchResult.WrongRulesVersion)
        val wrongRules = result as RestoreMatchResult.WrongRulesVersion
        assertEquals(99, wrongRules.found)
        assertEquals(CoreGame.RULES_VERSION, wrongRules.expected)
    }

    @Test
    fun logWithCorruptedMovesIsHandledGracefullyWithoutCrash() = runBlocking {
        val repository = InMemoryMatchRepository()

        val validLog = MatchLog(
            rulesVersion = CoreGame.RULES_VERSION,
            config = ScopaConfig(),
            seed = 42L,
            moves = emptyList(),
        )
        // Log con mossa illeggibile/corrotta
        val corruptedText = validLog.toText() + "\nmove 99_X -"

        repository.setActiveMatchDirectly(
            SavedMatchEntity(
                id = "active_match",
                matchLogText = corruptedText,
                levelName = DifficultyLevel.INTERMEDIO.name,
                targetScore = 11,
                status = "IN_PROGRESS",
                winnerSeat = null,
                p1Score = 0,
                p2Score = 0,
                createdAt = 1000L,
                updatedAt = 2000L,
            ),
        )

        val result = repository.restoreActiveMatch()
        assertTrue(result is RestoreMatchResult.Corrupted)
    }

    @Test
    fun completedMatchIsNotRestoredAsActive() = runBlocking {
        val repository = InMemoryMatchRepository()
        val log = MatchLog(rulesVersion = CoreGame.RULES_VERSION, config = ScopaConfig(), seed = 42L, moves = emptyList())
        repository.saveActiveMatch(log = log, level = DifficultyLevel.MAESTRO, targetScore = 11)
        repository.markCompleted(winner = Seat.P1, p1Score = 12, p2Score = 8)

        val result = repository.restoreActiveMatch()
        assertEquals(RestoreMatchResult.None, result)
    }

    @Test
    fun settingsRepositoryPersistsAndEmitsPreferencesCorrectly() = runBlocking {
        val settingsRepo = InMemorySettingsRepository()

        val initial = settingsRepo.settingsFlow.first()
        assertEquals("system", initial.language)
        assertTrue(initial.soundEnabled)
        assertEquals(DifficultyLevel.INTERMEDIO, initial.preferredLevel)
        assertEquals(11, initial.preferredTargetScore)

        settingsRepo.updateLanguage("it")
        settingsRepo.updateSoundEnabled(false)
        settingsRepo.updatePreferredLevel(DifficultyLevel.MAESTRO)
        settingsRepo.updatePreferredTargetScore(21)

        val updated = settingsRepo.settingsFlow.first()
        assertEquals("it", updated.language)
        assertFalse(updated.soundEnabled)
        assertEquals(DifficultyLevel.MAESTRO, updated.preferredLevel)
        assertEquals(21, updated.preferredTargetScore)
    }
}
