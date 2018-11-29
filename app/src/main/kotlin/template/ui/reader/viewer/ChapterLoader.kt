package template.ui.reader.viewer

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import template.data.database.models.Manga
import template.data.download.DownloadManager
import template.extensions.fetchImageFromCacheThenNet
import template.extensions.fetchPageListFromCacheThenNet
import template.extensions.plusAssign
import template.source.Source
import template.source.model.Page
import template.source.online.HttpSource
import template.ui.reader.ReaderChapter
import timber.log.Timber
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
        disposables.clear()
        queue.clear()
    }

    /**
     * 监听[queue]，如果[queue]里面一旦有数据，立刻从里面拿出page来加载图片,数据通过[ChapterCache]获取(先检测本地缓存([DiskLruCache])，
     * 没有再从网络获取(获取成功再存入本地缓存))
     */
    private fun prepareOnlineReading() {
        if (source !is HttpSource) return

        disposables += Observable
                .defer {
                    Timber.e("不断从PriorityBlockingQueue队列中拿出数据,queue如果没有数据，take()操作会阻塞")
                    Observable.just(queue.take().page)
                }
                .filter {
                    it.status == Page.QUEUE
                }
                .concatMap {
                    source.fetchImageFromCacheThenNet(it)
                }
                .repeat()
                .subscribeOn(Schedulers.io())
                .subscribe({
                }, { error ->
                    if (error !is InterruptedException) {
                        Timber.e(error)
                    }
                })

    }

    /**
     * 加载chapter的所有pages，如果pages没有数据，则从ChapterCache中获取(现在本地缓存(DiskLruCache)查找，没有再从网络获取)
     */
    fun loadChapter(chapter: ReaderChapter) = Observable.just(chapter)
            .flatMap {
                if (chapter.pages == null) {
                    retrievePageList(chapter)
                } else {
                    Observable.just(chapter.pages!!)
                }
            }
            .doOnNext {
                if (it.isEmpty()) {
                    throw Exception("Page list is empty")
                }

                // Now that the number of pages is known, fix the requested page if the last one
                // was requested.
                if (chapter.requestedPage == -1) {
                    chapter.requestedPage = it.lastIndex
                }

                loadPages(chapter)
            }
            .map {
                chapter
            }

    /**
     * 获取Chapter的所有pages
     */
    private fun retrievePageList(chapter: ReaderChapter) = Observable.just(chapter)
            .flatMap {
                // Check if the chapter is downloaded.
                chapter.isDownloaded = downloadManager.isChapterDownloaded(chapter, manga, true)

                if (chapter.isDownloaded) {
                    // Fetch the page list from disk.
                    downloadManager.buildPageList(source, manga, chapter)
                } else {
                    (source as? HttpSource)?.fetchPageListFromCacheThenNet(chapter)
                            ?: source.fetchPageList(chapter)
                }
            }
            .doOnNext { pages ->
                chapter.pages = pages
                pages.forEach {
                    it.chapter = chapter
                }
            }

    private fun loadPages(chapter: ReaderChapter) {
        if (!chapter.isDownloaded) {
            loadOnlinePages(chapter)
        }
    }

    /**
     * 往[queue]里面添加数据，由于[prepareOnlineReading]之前有对queue监听，所以一往queue里面添加数据，则会立刻去获取manga图片
     */
    private fun loadOnlinePages(chapter: ReaderChapter) {
        chapter.pages?.let {
            val startPage = chapter.requestedPage
            val pagesToLoad = if (startPage == 0)
                it
            else
                it.drop(startPage)

            pagesToLoad.forEach {
                queue.offer(PriorityPage(it, 0))
            }
        }
    }

    fun loadPriorizedPage(page: Page) {
        queue.offer(PriorityPage(page, 1))
    }

    fun retryPage(page: Page) {
        queue.offer(PriorityPage(page, 2))
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
