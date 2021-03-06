package template

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.google.gson.Gson
import template.data.cache.ChapterCache
import template.data.cache.CoverCache
import template.data.database.DatabaseHelper
import template.data.download.DownloadManager
import template.data.notification.Notifications
import template.data.preference.PreferencesHelper
import template.data.track.TrackManager
import template.di.component.ApplicationComponent
import template.di.component.DaggerApplicationComponent
import template.di.module.ApplicationModule
import template.network.NetworkHelper
import template.source.SourceManager
import timber.log.Timber
import javax.inject.Inject

class App : Application() {

    // 静态变量
    companion object {
        lateinit var app: App
    }

    @Inject
    lateinit var lazyCoverCache: dagger.`Lazy`<CoverCache>

    @Inject
    lateinit var lazyChapterCache: dagger.`Lazy`<ChapterCache>

    @Inject
    lateinit var lazySourceManager: dagger.`Lazy`<SourceManager>

    @Inject
    lateinit var lazyNetworkHelper: dagger.`Lazy`<NetworkHelper>

    @Inject
    lateinit var lazyDatabaseHelper: dagger.`Lazy`<DatabaseHelper>

    @Inject
    lateinit var lazyPreferencesHelper: dagger.`Lazy`<PreferencesHelper>

    @Inject
    lateinit var lazyDownloaderManager: dagger.`Lazy`<DownloadManager>

    @Inject
    lateinit var lazyGson: dagger.`Lazy`<Gson>

    @Inject
    lateinit var lazyTrackManager: dagger.`Lazy`<TrackManager>

    val component: ApplicationComponent by lazy {
        DaggerApplicationComponent
                .builder()
                .applicationModule(ApplicationModule(this))
                .build()
    }

    override fun onCreate() {
        super.onCreate()

        app = this
        component.inject(this)

        setupNotificationChannels()

        timber()
    }

    private fun timber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                @SuppressLint("DefaultLocale")
                override fun createStackElementTag(element: StackTraceElement): String {
                    return String.format("@@ %s.%s:%d thread[%s]",
                            super.createStackElementTag(element),
                            element.methodName, element.lineNumber, Thread.currentThread().name)
                }
            })
        } else {
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String, message: String, throwable: Throwable?) {
                    if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                        return
                    }
                    //CrashLibrary.log(priority, tag, message)
                }
            })
        }
    }

    private fun setupNotificationChannels() {
        Notifications.createChannel(this)
    }
}