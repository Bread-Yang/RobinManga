package template.ui.download

import android.os.Bundle
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import template.data.download.DownloadManager
import template.data.download.model.Download
import template.data.download.model.DownloadQueue
import template.ui.common.mvp.BasePresenter
import javax.inject.Inject

/**
 * Presenter of [DownloadController].
 */
class DownloadPresenter : BasePresenter<DownloadController>() {

    /**
     * Download manager.
     */
    @Inject
    lateinit var downloadManager: DownloadManager

    /**
     * Property to get the queue from the download manager.
     */
    val downloadQueue: DownloadQueue
        get() = downloadManager.queue

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        downloadQueue
                .getDownloadListUpdatedFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    ArrayList(it)
                }
                .subscribeLatestCache { downloadController, arrayList ->
                    downloadController.onDownloadListUpdate(arrayList)
                }
    }

    fun getSingleDownloadStatusFlowable(): Flowable<Download> {
        return downloadQueue.getSingleDownloadStatusFlowable()
                .startWith(downloadQueue.getActiveDownloads())
    }

    fun getDownloadProgressFlowable(): Flowable<Download> {
        return downloadQueue.getDownloadProgressFlowable()
    }

    /**
     * Pauses the download queue.
     */
    fun pauseDownloads() {
        downloadManager.pauseDownloads()
    }

    /**
     * Clears the download queue.
     */
    fun clearQueue() {
        downloadManager.clearQueue()
    }
}