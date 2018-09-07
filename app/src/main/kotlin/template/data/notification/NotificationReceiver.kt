package template.data.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import template.BuildConfig.APPLICATION_ID as ID

/**
 * Global [BroadcastReceiver] that runs on UI thread
 * Pending Broadcasts should be made from here.
 * NOTE: Use local broadcasts if possible.
 */
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
    }

    companion object {

        private const val NAME = "NotificationReceiver"

        // Called to notify user shortcut is created.
        private const val ACTION_SHORTCUT_CREATED = "$ID.$NAME.ACTION_SHORTCUT_CREATED"

        internal fun shortcutCreatedBroadcast(context: Context) : PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_SHORTCUT_CREATED
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}
