package it.maestro.app.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import it.maestro.app.domain.reminder.ReminderScheduler
import it.maestro.app.domain.reminder.ReminderSlot
import java.time.ZoneId

/**
 * Pianifica il promemoria con un allarme **inesatto** della piattaforma: nessun permesso di allarme esatto, il sistema
 * può spostarlo di poco per risparmiare batteria. Un solo allarme alla volta: pianificarne un altro sostituisce il
 * precedente (stesso `PendingIntent`).
 */
public class AlarmReminderScheduler(private val context: Context) : ReminderScheduler {

    override fun schedule(slot: ReminderSlot) {
        val triggerAt = slot.date.atTime(slot.hour, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager()?.setAndAllowWhileIdle(AlarmManager.RTC, triggerAt, pendingIntent())
    }

    override fun cancel() {
        alarmManager()?.cancel(pendingIntent())
    }

    private fun alarmManager(): AlarmManager? = context.getSystemService(AlarmManager::class.java)

    private fun pendingIntent(): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).setAction(ReminderReceiver.ACTION_FIRE)
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private companion object {
        const val REQUEST_CODE = 13
    }
}
