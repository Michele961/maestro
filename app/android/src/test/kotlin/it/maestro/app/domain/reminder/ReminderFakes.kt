package it.maestro.app.domain.reminder

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Repository in memoria per i test del promemoria. */
class InMemoryReminderRepository(initial: ReminderState = ReminderState()) : ReminderRepository {
    val state = MutableStateFlow(initial)

    override fun observe(): Flow<ReminderState> = state

    override suspend fun load(): ReminderState = state.value

    override suspend fun save(state: ReminderState) {
        this.state.value = state
    }
}

/** Ricorda l'ultima pianificazione richiesta. */
class FakeReminderScheduler : ReminderScheduler {
    var scheduled: ReminderSlot? = null
    var cancelCount = 0

    override fun schedule(slot: ReminderSlot) {
        scheduled = slot
    }

    override fun cancel() {
        scheduled = null
        cancelCount++
    }
}

/** Conta le notifiche mostrate. */
class FakeReminderNotifier(var allowed: Boolean = true) : ReminderNotifier {
    var shown = 0

    override fun canNotify(): Boolean = allowed

    override fun show() {
        shown++
    }
}
