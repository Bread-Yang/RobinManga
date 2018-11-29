package template.data.library

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import template.App
import template.data.database.DatabaseHelper
import template.source.SourceManager
import template.utils.preference.PreferencesHelper

/**
 * This class will take care of updating the chapters of the manga from the library. It can be
 * started calling the [start] method. If it's already running, it won't do anything.
 * While the library is updating, a [PowerManager.WakeLock] will be held until the update is
 * completed, preventing the device from going to sleep mode. A notification will display the
 * progress of the update, and if case of an unexpected error, this service will be silently
 * destroyed.
 */
class LibraryUpdateService() : Service() {

    val databaseHelper: DatabaseHelper = App.app.lazyDatabaseHelper.get()

    val sourceManager: SourceManager = App.app.lazySourceManager.get()

    val preferencesHelper: PreferencesHelper = App.app.lazyPreferencesHelper.get()

    val trackManager: TrackManager = App.app.lazy

    override fun onBind(intent: Intent?): IBinder {
    }

}