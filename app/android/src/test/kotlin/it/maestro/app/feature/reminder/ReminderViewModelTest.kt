package it.maestro.app.feature.reminder

import it.maestro.app.data.local.datastore.InMemoryMasteryRepository
import it.maestro.app.domain.mastery.MatchHistoryEntry
import it.maestro.app.domain.reminder.FakeReminderNotifier
import it.maestro.app.domain.reminder.FakeReminderScheduler
import it.maestro.app.domain.reminder.InMemoryReminderRepository
import it.maestro.app.domain.reminder.ReminderCoordinator
import it.maestro.app.domain.reminder.ReminderState
import it.maestro.app.domain.time.FakeClock
import it.maestro.app.feature.reminder.presentation.ReminderUiState
import it.maestro.app.feature.reminder.presentation.ReminderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

/** F1G-13: richiesta di consenso solo dopo la prima vittoria, una volta sola; interruttore nelle Impostazioni. */
@OptIn(ExperimentalCoroutinesApi::class)
class ReminderViewModelTest {
    private val clock = FakeClock(LocalDate.of(2026, 10, 31), hour = 10)
    private val repository = InMemoryReminderRepository()
    private val scheduler = FakeReminderScheduler()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun entry(win: Boolean) = MatchHistoryEntry(
        id = "m", timestampMillis = 0L, accuracy = 70, cognitiveElo = 1000, eloDelta = 0,
        humanScore = if (win) 11 else 5, botScore = if (win) 5 else 11, isWin = win, sparringObjective = null,
    )

    private fun viewModel(history: List<MatchHistoryEntry>) = ReminderViewModel(
        repository,
        InMemoryMasteryRepository(initialHistory = history),
        ReminderCoordinator(repository, scheduler, FakeReminderNotifier(), clock) { null },
    )

    @Test
    fun noConsentAndNoSwitchBeforeTheFirstWin() = runTest {
        val vm = viewModel(listOf(entry(win = false)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertEquals(ReminderUiState(), vm.uiState.value)
    }

    @Test
    fun consentIsAskedAfterTheFirstWinAndTheSwitchAppears() = runTest {
        val vm = viewModel(listOf(entry(win = true)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertEquals(ReminderUiState(askConsent = true, switchAvailable = true), vm.uiState.value)
    }

    @Test
    fun afterAcceptingTheConsentIsNeverAskedAgainAndTheReminderIsOn() = runTest {
        val vm = viewModel(listOf(entry(win = true)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        vm.onConsentAccepted()

        assertEquals(ReminderUiState(askConsent = false, switchAvailable = true, enabled = true), vm.uiState.value)
    }

    @Test
    fun afterDecliningNothingIsOnAndNobodyAsksAgain() = runTest {
        val vm = viewModel(listOf(entry(win = true)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        vm.onConsentDeclined()

        assertEquals(ReminderUiState(askConsent = false, switchAvailable = true, enabled = false), vm.uiState.value)
        assertEquals(null, scheduler.scheduled)
    }

    @Test
    fun aDeniedSystemPermissionTurnsTheReminderOffWithoutAskingAgain() = runTest {
        val vm = viewModel(listOf(entry(win = true)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        vm.onConsentAccepted()
        vm.onPermissionResult(granted = false)

        assertEquals(false, vm.uiState.value.enabled)
        assertEquals(false, vm.uiState.value.askConsent)
    }

    @Test
    fun theSwitchStaysAvailableWhenTheWinLeftTheRecentHistory() = runTest {
        repository.save(ReminderState(enabled = true, consentAsked = true))
        val vm = viewModel(emptyList())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertEquals(true, vm.uiState.value.switchAvailable)
    }
}
