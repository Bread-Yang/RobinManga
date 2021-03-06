package template.data.library

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import io.reactivex.disposables.Disposable
import template.App
import template.R
import template.data.database.DatabaseHelper
import template.data.database.models.Category
import template.data.download.DownloadManager
import template.data.library.LibraryUpdateService.Companion.start
import template.data.notification.NotificationReceiver
import template.data.notification.Notifications
import template.data.preference.PreferencesHelper
import template.data.track.TrackManager
import template.extensions.isServiceRunning
import template.source.SourceManager

/**
 * This class will take care of updating the chapters of the manga from the library. It can be
 * started calling the [start] method. If it's already running, it won't do anything.
 * While the library is updating, a [PowerManager.WakeLock] will be held until the update is
 * completed, preventing the device from going to sleep mode. A notification will display the
 * progress of the update, and if case of an unexpected error, this service will be silently
 * destroyed.
 */
class LibraryUpdateService(
        val databaseHelper: DatabaseHelper = App.app.lazyDatabaseHelper.get(),
        val sourceManager: SourceManager = App.app.lazySourceManager.get(),
        val preferencesHelper: PreferencesHelper = App.app.lazyPreferencesHelper.get(),
        val downloadManager: DownloadManager = App.app.lazyDownloaderManager.get(),
        val trackManager: TrackManager = App.app.lazyTrackManager.get()
) : Service() {

    /**
     * Defines what should be updated within a service execution.
     */
    enum class Target {
        CHAPTERS, // Manga chapters
        DETAILS,  // Manga metadata
        TRACKING  // Tracking metadata
    }

    /**
     * Wake lock that will be held unitl the service is destroyed.
     */
    private lateinit var wakeLock: PowerManager.WakeLock

    /**
     * Disposable where the update is donw.
     */
    private var disposable: Disposable? = null

    /**
     * Pending intent of action that cancels the library update
     */
    private val cancelIntent by lazy {
        NotificationReceiver.cancelLibraryUpdatePendingBroadcast(this)
    }

    /**
     * Bitmap of the app for notification.
     */
    private val notificationBitmap by lazy {
        BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
    }

    /**
     * Cached progress notification to avoid creating a lot.
     */
    private val progressNotification by lazy {
        NotificationCompat.Builder(this, Notifications.CHANNEL_LIBRARY)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_refresh_white_24dp_img)
                .setLargeIcon(notificationBitmap)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_clear_grey_24dp_img, getString(android.R.string.cancel), cancelIntent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {

        /**
         * Key for category to update.
         */
        const val KEY_CATEGORY = "category"

        /**
         * Key that defines what should be updated.
         */
        const val KEY_TARGET = "target"

        /**
         * Returns the status of the service.
         *
         * @param context the application context.
         * @return true if the service is running, false otherwise.
         */
        fun isRunning(context: Context): Boolean {
            return context.isServiceRunning(LibraryUpdateService::class.java)
        }

        /**
         * Starts the service. It will be started only if there isn't another instance already
         * running.
         *
         * @param context the application context.
         * @param category a specific category to update, or null for global update.
         * @param target defines what should be updated.
         */
        fun start(context: Context, category: Category? = null, target: Target = Target.CHAPTERS) {
            if (!isRunning(context)) {
                val intent = Intent(context, LibraryUpdateService::class.java).apply {
                    putExtra(KEY_TARGET, target)
                    category?.let {
                        putExtra(KEY_CATEGORY, it.id)
                    }
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    context.startService(intent)
                } else {
                    context.startForegroundService(intent)
                }
            }
        }

        /**
         * Stops the service.
         *
         * @param context the application context.
         */
        fun stop(context: Context) {
            context.stopService(Intent(context, LibraryUpdateService::class.java))
        }
    }
}