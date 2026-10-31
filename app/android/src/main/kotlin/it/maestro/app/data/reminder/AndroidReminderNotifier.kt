package it.maestro.app.data.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import it.maestro.app.R
import it.maestro.app.domain.reminder.ReminderNotifier

/**
 * Notifica del promemoria con il [NotificationManager] della piattaforma. Il canale è a bassa importanza: nessun
 * suono, nessuna vibrazione, nessun avviso a schermo. Toccarla apre l'app. I testi vengono dalle risorse (ADR-0007).
 */
public class AndroidReminderNotifier(private val context: Context) : ReminderNotifier {

    override fun canNotify(): Boolean = manager()?.areNotificationsEnabled() == true

    override fun show() {
        val manager = manager() ?: return
        val name = context.getString(R.string.reminder_channel_name)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
                .apply { description = context.getString(R.string.reminder_channel_description) },
        )
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val tap = launch?.let { PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE) }
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(context.applicationInfo.icon)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(context.getString(R.string.reminder_notification_text))
            .setContentIntent(tap)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun manager(): NotificationManager? = context.getSystemService(NotificationManager::class.java)

    private companion object {
        const val CHANNEL_ID = "daily_reminder"
        const val NOTIFICATION_ID = 13
    }
}
