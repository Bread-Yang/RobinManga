package template

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import template.data.cache.CoverCache
import template.di.component.ApplicationComponent
import template.di.component.DaggerApplicationComponent
import template.di.module.ApplicationModule
import template.network.NetworkHelper
import template.source.SourceManager
import timber.log.Timber
import javax.inject.Inject

class App : Application() {

    companion object {
        lateinit var app: App
    }

    @Inject
    lateinit var lazyCoverCache: dagger.`Lazy`<CoverCache>

    @Inject
    lateinit var lazySourceManager : dagger.`Lazy`<SourceManager>

    @Inject
    lateinit var lazyNetworkHelper: dagger.`Lazy`<NetworkHelper>

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
}