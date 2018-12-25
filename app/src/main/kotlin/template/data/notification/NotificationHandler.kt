package template.data.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import template.extensions.getUriCompat
import template.ui.main.MainActivity
import java.io.File

/**
 * Class that manages [PendingIntent] of activity's
 */
object NotificationHandler {

    /**
     * Returns [PendingIntent] that starts a download activity.
     *
     * @param context context of application
     */
    internal fun openDownloadManagerPendingActivity(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            action = MainActivity.SHORTCUT_DOWNLOADS
        }
        return PendingIntent.getActivity(context, 0, intent, 0)
    }

    /**
     * Returns [PendingIntent] that starts a gallery activity
     *
     * @param context context of application
     * @param file file containing image
     */
    internal fun openImagePendingActivity(context: Context, file: File): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val uri = file.getUriCompat(context)
            setDataAndType(uri, "image/*")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}