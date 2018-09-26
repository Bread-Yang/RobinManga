package template.data.download

import android.app.Service
import android.content.Intent
import android.os.IBinder
import io.reactivex.subjects.BehaviorSubject

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
    }

    /**
     * Not used.
     */
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
