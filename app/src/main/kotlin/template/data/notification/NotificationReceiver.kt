package template.data.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.BuildConfig.APPLICATION_ID as ID

/**
 * Global [BroadcastReceiver] that runs on UI thread
 * Pending Broadcasts should be made from here.
 * NOTE: Use local broadcasts if possible.
 */
class NotificationReceiver : BroadcastReceiver() {

    companion object {

        private const val NAME = "NotificationReceiver"

        // Called to notify user shortcut is created.
        private const val ACTION_SHORTCUT_CREATED = "$ID.$NAME.ACTION_SHORTCUT_CREATED"

        // Called to resume downloads.
        private const val ACTION_RESUME_DOWNLOADS = "$ID.$NAME.ACTION_RESUME_DOWNLOADS"

        // Called to clear downloads.
        private const val ACTION_CLEAR_DOWNLOADS = "$ID.$NAME.ACTION_CLEAR_DOWNLOADS"

        // Called to open chapter
        private const val ACTION_OPEN_CHAPTER = "$ID.$NAME.ACTION_OPEN_CHAPTER"

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
    }

    override fun onReceive(context: Context?, intent: Intent?) {
    }

}
