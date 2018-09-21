package template.data.download

import android.content.Context
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import template.App
import template.data.download.model.Download
import template.data.download.model.DownloadQueue
import template.source.SourceManager

/**
 * This class is the one in charge of downloading chapters.
 *
 * Its [queue] contains the list of chapters
 */
class Downloader(
        private val context: Context,
        private val pathProvider: DownloadPathProvider,
        private val cache: DownloadCache
) {

    /**
     * Store for persisting downloads across restarts.
     */
    private val store = DownloadStore(context)

    /**
     * Queue where active downloads are kept.
     */
    val queue = DownloadQueue(store)

    /**
     * Source manager.
     */
    private val sourceManager: SourceManager = App.app.lazySourceManager.get()

    /**
     * Notifier for the downloader state and progress.
     */
    // TODO
//    private val notifier by lazy {
//        DownloadNotifier(context)
//    }

    /**
     * Downloader disposables.
     */
    private val disposables = CompositeDisposable()

    /**
     * Subject to send a list fo downloads to the downloader.
     */
    private val downloadSubject = PublishSubject.create<List<Download>>()

    /**
     * Subject to subscribe to the downloader status.
     */
    val runningSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    /**
     * Whether the downloader is running.
     */
    @Volatile
    private var isRunning: Boolean = false

    init {
    }
}

