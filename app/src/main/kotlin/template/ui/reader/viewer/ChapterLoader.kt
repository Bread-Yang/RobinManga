package template.ui.reader.viewer

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import template.data.database.models.Manga
import template.data.download.DownloadManager
import template.extensions.plusAssign
import template.source.Source
import template.source.model.Page
import template.source.online.HttpSource
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

class ChapterLoader(
        private val downloadManager: DownloadManager,
        private val manga: Manga,
        private val source: Source
) {

    private val queue = PriorityBlockingQueue<PriorityPage>()
    private val disposables = CompositeDisposable()

    fun init() {
        prepareOnlineReading()
    }

    fun restart() {
        cleanup()
        init()
    }

    fun cleanup() {
        disposables.dispose()
        disposables.clear()
    }

    private fun prepareOnlineReading() {
        if (source !is HttpSource) return

        disposables += Observable
                .defer {
                    Observable.just(queue.take().page)
                }
                .filter {
                    it.status == Page.QUEUE
                }
                .concatMap {
                    source.fetchImageFromCacheThenNet(it)
                }

    }

    private data class PriorityPage(val page: Page, val priority: Int) : Comparable<PriorityPage> {

        companion object {
            private val idGenerator = AtomicInteger()
        }

        private val identifier = idGenerator.incrementAndGet()

        override fun compareTo(other: PriorityPage): Int {
            val p = other.priority.compareTo(priority)
            return if (p != 0) p else identifier.compareTo(other.identifier)
        }

    }
}
