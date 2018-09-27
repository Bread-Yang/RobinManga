package template.data.download.model

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import template.data.database.models.Chapter
import template.data.download.DownloadStore
import template.source.model.Page
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Robin Yeung on 9/20/18.
 */
class DownloadQueue(
        private val store: DownloadStore,
        private val queue: MutableList<Download> = CopyOnWriteArrayList<Download>())
    : List<Download> by queue {

    /**
     * 单个[Download.status]状态变更时，用来通知adapter更新item界面[template.ui.download.DownloadController.onSingleDownloadStatusChange]
     */
    private val singleDownloadStatusProcessor = PublishProcessor.create<Download>()

    /**
     * 队列中新增download或者删除download时，用来通知UI更新adapter的download List数据(传this过去 this by List<Download>)
     * [template.ui.download.DownloadController.onDownloadListUpdate]
     */
    private val downloadListUpdatedProcessor = PublishProcessor.create<Unit>()

    fun addAll(downloads: List<Download>) {
        downloads.forEach {
            it.setStatusProcessor(singleDownloadStatusProcessor)
            it.status = Download.QUEUE
        }
        queue.addAll(downloads)
        store.addAll(downloads)
        downloadListUpdatedProcessor.onNext(Unit)
    }

    fun remove(download: Download) {
        val removed = queue.remove(download)
        store.remove(download)
        download.setStatusProcessor(null)
        if (removed) {
            downloadListUpdatedProcessor.onNext(Unit)
        }
    }

    fun remove(chapter: Chapter) {
        find {
            it.chapter.id == chapter.id
        }?.let {
            remove(it)
        }
    }

    fun clear() {
        queue.forEach {
            it.setStatusProcessor(null)
        }
        queue.clear()
        store.clear()
        downloadListUpdatedProcessor.onNext(Unit)
    }

    fun getActiveDownloads(): Flowable<Download> =
            Flowable.fromIterable(this)
                    .filter {
                        it.status == Download.DOWNLOADING
                    }

    fun getSingleDownloadStatusFlowable(): Flowable<Download> = singleDownloadStatusProcessor.onBackpressureBuffer()

    fun getDownloadListUpdatedFlowable(): Flowable<List<Download>> = downloadListUpdatedProcessor.onBackpressureBuffer()
            .startWith(Unit)
            .map {
                this
            }

    /**
     * 当Download里面的Pages任何一个page status发生变化时(如 状态DOWNLOAD_IMAGE -> 状态READY),
     * singleDownloadStatusProcessor就会通知订阅它的observable
     */
    fun getDownloadProgressFlowable(): Flowable<Download> {
        return singleDownloadStatusProcessor.onBackpressureBuffer()
                .startWith(getActiveDownloads())
                .flatMap { download ->
                    if (download.status == Download.DOWNLOADING) {
                        val pageStatusProcessor = PublishProcessor.create<Int>()
                        setPagesStatusProcessor(download.pages, pageStatusProcessor)
                        return@flatMap pageStatusProcessor
                                .onBackpressureBuffer()
                                .filter {
                                    it == Page.READY
                                }
                                .map {
                                    download
                                }
                    } else if (download.status == Download.DOWNLOADED || download.status == Download.ERROR) {
                        setPagesStatusProcessor(download.pages, null)
                    }
                    Flowable.just(download)
                }
                .filter {
                    it.status == Download.DOWNLOADING
                }
    }

    private fun setPagesStatusProcessor(pages: List<Page>?, processor: PublishProcessor<Int>?) {
        if (pages != null) {
            for (page in pages) {
                page.setStatusProcessor(processor)
            }
        }
    }
}