package template.data.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.data.library.LibraryUpdateService
import template.extensions.notificationManager
import template.BuildConfig.APPLICATION_ID as ID

/**
 * Global [BroadcastReceiver] that runs on UI thread
 * Pending Broadcasts should be made from here.
 * NOTE: Use local broadcasts if possible.
 */
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            // Cancel library update and dismiss notification
            ACTION_CANCEL_LIBRARY_UPDATE -> cancelLibraryUpdate(context, Notifications.ID_LIBRARY_PROGRESS)
        }
    }

    /**
     * Method called when user wants to stop a library update
     *
     * @param context context of application
     * @param notificationId id of notification
     */
    private fun cancelLibraryUpdate(context: Context, notificationId: Int) {
        LibraryUpdateService.stop(context)
        Handler().post {
            dismissNotification(context, notificationId)
        }
    }

    /**
     * Dismiss the notification
     *
     * @param notificationId the id of the notification
     */
    private fun dismissNotification(context: Context, notificationId: Int) {
        context.notificationManager.cancel(notificationId)
    }

    companion object {

        private const val NAME = "NotificationReceiver"

        // Called to launch share intent.
        private const val ACTION_SHARE_IMAGE = "$ID.$NAME.SHARE_IMAGE"

        // Called to delete image.
        private const val ACTION_DELETE_IMAGE = "$ID.$NAME.DELETE_IMAGE"

        // Called to cancel library update.
        private const val ACTION_CANCEL_LIBRARY_UPDATE = "$ID.$NAME.CANCEL_LIBRARY_UPDATE"

        // Called to notify user shortcut is created.
        private const val ACTION_SHORTCUT_CREATED = "$ID.$NAME.ACTION_SHORTCUT_CREATED"

        // Called to resume downloads.
        private const val ACTION_RESUME_DOWNLOADS = "$ID.$NAME.ACTION_RESUME_DOWNLOADS"

        // Called to clear downloads.
        private const val ACTION_CLEAR_DOWNLOADS = "$ID.$NAME.ACTION_CLEAR_DOWNLOADS"

        // Called to open chapter
        private const val ACTION_OPEN_CHAPTER = "$ID.$NAME.ACTION_OPEN_CHAPTER"

        // Value containing file location.
        private const val EXTRA_FILE_LOCATION = "$ID.$NAME.FILE_LOCATION"

        // Value containing notification id.
        private const val EXTRA_NOTIFICATION_ID = "$ID.$NAME.NOTIFICATION_ID"

        // Value containing manga id.
        private const val EXTRA_MANGA_ID = "$ID.$NAME.EXTRA_MANGA_ID"

        // Value containing chapter id.
        private const val EXTRA_CHAPTER_ID = "$ID.$NAME.EXTRA_CHAPTER_ID"

        /**
         * Returns a [PendingIntent] that resumes the download of a chapter
         *
         * @param context context of application
         * @return [PendingIntent]
         */
        internal fun resumeDownloadsPendingBroadcast(context: Context): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_RESUME_DOWNLOADS
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        /**
         * Returns a [PendingIntent] that clears the download queue
         *
         * @param context context of application
         * @return [PendingIntent]
         */
        internal fun clearDownloadsPendingBroadcast(context: Context): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_CLEAR_DOWNLOADS
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        /**
         * Returns [PendingIntent] that start a reader activity containing chapter.
         *
         * @param context context of application
         * @param manga manga of chapter
         * @param chapter chapter that needs to be opened
         */
        internal fun openChapterPendingBroadcast(context: Context, manga: Manga, chapter: Chapter): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_OPEN_CHAPTER
                putExtra(EXTRA_MANGA_ID, manga.id)
                putExtra(EXTRA_CHAPTER_ID, chapter.id)
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        internal fun shortcutCreatedBroadcast(context: Context): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_SHORTCUT_CREATED
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        /**
         * Return [PendingIntent] that starts a service which cancels the notification and starts a share activity
         *
         * @param context context of application
         * @param path location path of file
         * @param notificationId id of notification
         * @return [PendingIntent]
         */
        internal fun shareImagePendingBroadcast(context: Context, path: String, notificationId: Int): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_SHARE_IMAGE
                putExtra(EXTRA_FILE_LOCATION, path)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        /**
         * Returns [PendingIntent] that starts a service which removes an image from disk
         *
         * @param context context of application
         * @param path location path of file
         * @param notificationId id of notification
         * @return [PendingIntent]
         */
        internal fun deleteImagePendingBroadcast(context: Context, path: String, notificationId: Int): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_DELETE_IMAGE
                putExtra(EXTRA_FILE_LOCATION, path)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        /**
         * Returns [PendingIntent] that starts a service which stops the library update
         *
         * @param context context of appliation
         * @return [PendingIntent]
         */
        internal fun cancelLibraryUpdatePendingBroadcast(context: Context): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_CANCEL_LIBRARY_UPDATE
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}
