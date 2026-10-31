package it.maestro.app.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import it.maestro.app.domain.reminder.ReminderCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/**
 * Riceve l'allarme del promemoria e il riavvio del telefono (`RECEIVE_BOOT_COMPLETED`, solo per ripianificare).
 * Non è esportato: l'allarme arriva dal nostro `PendingIntent`, il riavvio dal sistema. Il lavoro vero è del
 * [ReminderCoordinator].
 */
public class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val coordinator = GlobalContext.getOrNull()?.get<ReminderCoordinator>() ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if (intent.action == ACTION_FIRE) coordinator.onAlarm() else coordinator.reschedule()
            } finally {
                pending.finish()
            }
        }
    }

    public companion object {
        /** Azione dell'allarme pianificato dall'app. */
        public const val ACTION_FIRE: String = "it.maestro.app.action.REMINDER_FIRE"
    }
}
