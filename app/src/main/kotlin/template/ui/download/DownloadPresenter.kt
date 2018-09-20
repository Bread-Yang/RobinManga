package template.ui.download

import template.data.download.DownloadManager
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
     * Property to get eh queue from the download manager.
     */
//    val downloadQueue: DownloadQueue
//        get() = downloadManager.queue
}