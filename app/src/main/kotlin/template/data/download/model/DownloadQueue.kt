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

    private val statusProcessor = PublishProcessor.create<Download>()

    private val updatedProcessor = PublishProcessor.create<Unit>()

    fun addAll(downloads: List<Download>) {
        downloads.forEach {
            it.setStatusProcessor(statusProcessor)
            it.status = Download.QUEUE
        }
        queue.addAll(downloads)
        store.addAll(downloads)
        updatedProcessor.onNext(Unit)
    }

    fun remove(download: Download) {
        val removed = queue.remove(download)
        store.remove(download)
        download.setStatusProcessor(null)
        if (removed) {
            updatedProcessor.onNext(Unit)
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
        updatedProcessor.onNext(Unit)
    }

    fun getActiveDownloads(): Flowable<Download> =
            Flowable.fromIterable(this)
                    .filter {
                        it.status == Download.DOWNLOADING
                    }

    fun getStatusFlowable(): Flowable<Download> = statusProcessor.onBackpressureBuffer()

    fun getUpdatedFlowable(): Flowable<List<Download>> = updatedProcessor.onBackpressureBuffer()
            .startWith(Unit)
            .map {
                this
            }

    fun getProgressFlowable(): Flowable<Download> {
        return statusProcessor.onBackpressureBuffer()
                .startWith(getActiveDownloads())
                .flatMap { download ->
                    if (download.status == Download.DOWNLOADING) {
                        val pageStatusProcessor = PublishProcessor.create<Int>()
                        setPagesProcessor(download.pages, pageStatusProcessor)
                        return@flatMap pageStatusProcessor
                                .onBackpressureBuffer()
                                .filter {
                                    it == Page.READY
                                }
                                .map {
                                    download
                                }
                    } else if (download.status == Download.DOWNLOADED || download.status == Download.ERROR) {
                        setPagesProcessor(download.pages, null)
                    }
                    Flowable.just(download)
                }
                .filter {
                    it.status == Download.DOWNLOADING
                }
    }

    private fun setPagesProcessor(pages: List<Page>?, processor: PublishProcessor<Int>?) {
        if (pages != null) {
            for (page in pages) {
                page.setStatusProcessor(processor)
            }
        }
    }
}