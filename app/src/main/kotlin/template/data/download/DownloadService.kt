package template.data.download

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import template.App
import template.R
import template.data.notification.Notifications
import template.extensions.connectivityManager
import template.extensions.plusAssign
import template.extensions.powerManager
import template.extensions.toast
import template.data.preference.PreferencesHelper

/**
 * This service is used to manage the downloader. The system can decide to stop the service, in
 * which case the downloader is also stopped. It's also stopped while there's no network available.
 * While the downloader is running, a wake lock will be held.
 */
class DownloadService : Service() {

    companion object {

        /**
         * Subject used to know when the service is running.
         */
        val runningSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

        /**
         * Starts this service.
         *
         * @param context the application context.
         */
        fun start(context: Context) {
            val intent = Intent(context, DownloadService::class.java)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                context.startService(intent)
            } else {
                context.startForegroundService(intent)
            }
        }

        /**
         * Stops this service.
         *
         * @param context the application context.
         */
        fun stop(context: Context) {
            context.stopService(Intent(context, DownloadService::class.java))
        }
    }

    /**
     * Preferences helper.
     */
    private val preferences: PreferencesHelper = App.app.lazyPreferencesHelper.get()

    /**
     * Download manager.
     */
    private val downloadManager: DownloadManager = App.app.lazyDownloaderManager.get()

    /**
     * Wake lock to prevent the device to enter sleep mode.
     *
     * PowerManager.PARTIAL_WAKE_LOCK是一个标志位，标志位是用来控制获取的WakeLock对象的类型，主要控制CPU工作
     * 时屏幕是否需要亮着以及键盘灯需要亮着，标志位说明如下 :
     *
     *      levelAndFlags           CPU是否运行             屏幕是否亮着          	键盘灯是否亮着
     *
     *      PARTIAL_WAKE_LOCK       	是                   	否                   否
     *      SCREEN_DIM_WAKE_LOCK        是                     低亮度               	 否
     *      SCREEN_BRIGHT_WAKE_LOCK     是                     高亮度                 否
     *      FULL_WAKE_LOCK              是                      	是                   是
     *
     * WakeLock类可以用来控制设备的工作状态。使用该类中的acquire可以使CPU一直处于工作的状态，如果不需要使CPU处于工作状态就调用release来关闭
     */
    private val wakeLock by lazy {
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DownloadService:WakeLock")
    }

    /**
     * Disposables to store while the service is running.
     */
    private lateinit var disposables: CompositeDisposable

    /**
     * Called when the service is created.
     */
    override fun onCreate() {
        super.onCreate()
        startForeground(Notifications.ID_DOWNLOAD_CHAPTER, getPlaceholderNotification())
        runningSubject.onNext(true)
        disposables = CompositeDisposable()
        listenDownloaderStatus()
        listenNetworkChanges()
    }

    /**
     * Called when the service is destroyed.
     */
    override fun onDestroy() {
        runningSubject.onNext(false)
        disposables.dispose()
        downloadManager.stopDownloads()
        wakeLock.releaseIfNeeded()
        super.onDestroy()
    }

    /**
     * Not used.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    /**
     * Not used.
     */
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * Listens to network changes.
     *
     * @see onNetworkStateChanged
     */
    private fun listenNetworkChanges() {
        disposables += ReactiveNetwork.observeNetworkConnectivity(applicationContext)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onNetworkStateChanged(it)
                }, {
                    toast(R.string.download_queue_error)
                    stopSelf()
                })
    }

    /**
     * Called when the network state changes.
     *
     * @param connectivity the new network state.
     */
    private fun onNetworkStateChanged(connectivity: Connectivity) {
        when (connectivity.state()) {
            NetworkInfo.State.CONNECTED -> {
                if (preferences.downloadOnlyOverWifi() && connectivityManager.isActiveNetworkMetered) {
                    downloadManager.stopDownloads(getString(R.string.download_notifier_text_only_wifi))
                } else {
                    val started = downloadManager.startDownloads()
                    if (!started)
                        stopSelf()
                }
            }
            NetworkInfo.State.DISCONNECTED -> {
                downloadManager.stopDownloads(getString(R.string.download_notifier_no_network))
            }
            else -> {
                /* Do nothing */
            }
        }
    }

    /**
     * Listens to downloader status. Enables or disables the wake lock depending on the status.
     */
    private fun listenDownloaderStatus() {
        disposables += downloadManager.runningSubject.subscribe { running ->
            if (running)
                wakeLock.acquireIfNeeded()
            else
                wakeLock.releaseIfNeeded()
        }
    }

    /**
     * Releases the wake lock if it's held.
     */
    fun PowerManager.WakeLock.releaseIfNeeded() {
        if (isHeld)
            release()
    }

    /**
     * Acquires the wake lock if it's not held.
     */
    fun PowerManager.WakeLock.acquireIfNeeded() {
        if (!isHeld)
            acquire()
    }

    private fun getPlaceholderNotification(): Notification {
        return NotificationCompat.Builder(this, Notifications.CHANNEL_DOWNLOADER)
                .setContentTitle(getString(R.string.download_notifier_downloader_title))
                .build()
    }
}
