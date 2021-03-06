package template.data.download

import android.content.Context
import com.hippo.unifile.UniFile
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.data.download.model.DownloadQueue
import template.source.Source
import template.source.model.Page

/**
 * This class is used to manage chapter downloads in the application. It must be instantiated once
 * and retrieved through dependency injection. You can use this class to queue new chapters or query
 * downloaded chapters.
 *
 * @param context the application context.
 */
class DownloadManager(context: Context) {

    /**
     * Downloads path provider, used to retrieve the folders where the chapters are or should be stored.
     */
    private val pathProvider = DownloadPathProvider(context)

    /**
     * Cache of downloaded chapters.
     */
    private val cache = DownloadCache(context, pathProvider)

    /**
     * Downloader whose only task is to donwload chapters.
     */
    private val downloader = Downloader(context, pathProvider, cache)

    /**
     * Downloads queue, where the pending chapters are stored.
     */
    val queue: DownloadQueue
        get() = downloader.queue

    /**
     * Subject for subscribing to downloader status.
     */
    val runningSubject: BehaviorSubject<Boolean>
        get() = downloader.runningSubject

    /**
     * Tells the downloader to begin downloads.
     *
     * @return true if it's started, false otherwise (empty queue).
     */
    fun startDownloads(): Boolean {
        return downloader.start()
    }

    /**
     * Tells the downloader to stop downloads.
     *
     * @param reason an optional reason for being stopped, used to notify the user.
     */
    fun stopDownloads(reason: String? = null) {
        downloader.stop(reason)
    }

    /**
     * Tells the downloader to pause downloads.
     */
    fun pauseDownloads() {
        downloader.pause()
    }

    /**
     * Empties the download queue.
     *
     * @param isNotification value that determines if status is set (needed for view updates)
     */
    fun clearQueue(isNotification: Boolean = false) {
        downloader.clearQueue(isNotification)
    }

    /**
     * Tells the downloader to enqueue the given list of chapters.
     *
     * @param manga the manga of the chapters.
     * @param chapters the list of chapters to enqueue.
     * @param autoStart whether to start the downloader after enqueuing the chapters.
     */
    fun downloadChapters(manga: Manga, chapters: List<Chapter>, autoStart: Boolean = true) {
        downloader.queueChapters(manga, chapters, autoStart)
    }

    /**
     * Builds the page list of a downloaded chapters.
     *
     * @param source the source of the chapter.
     * @param manga the manga of the chapter.
     * @param chapter the downloaded chapter.
     * @return an observable containing the list of pages from the chapter.
     */
    fun buildPageList(source: Source, manga: Manga, chapter: Chapter): Observable<List<Page>> {
        return buildPageList(pathProvider.findChapterDir(chapter, manga, source))
    }

    /**
     * Builds the page list of a download chapter.
     *
     * @param chapterDir the file where the chapter is downloaded.
     * @param an observable containing the list of pages from chapter.
     */
    private fun buildPageList(chapterDir: UniFile?): Observable<List<Page>> {
        return Observable.fromCallable {
            val files = chapterDir?.listFiles().orEmpty()
                    .filter {
                        "image" in it.type.orEmpty()
                    }

            if (files.isEmpty()) {
                throw Exception("Page list is empty")
            }

            files
                    .sortedBy {
                        it.name
                    }
                    .mapIndexed { index, uniFile ->
                        Page(index, uri = uniFile.uri)
                                .apply {
                                    status = Page.READY
                                }
                    }
        }
    }

    /**
     * Returns true if the chapter is download.
     *
     * @param chapter the chapter to check.
     * @param manga the manga of the chapter.
     * @param skipCache whether to skip the directory cache and check in the filesystem.
     */
    fun isChapterDownloaded(chapter: Chapter, manga: Manga, skipCache: Boolean = false): Boolean {
        return cache.isChapterDownloaded(chapter, manga, skipCache)
    }

    /**
     * Returns the amount of downloaded chapters for a manga.
     *
     * @param manga the manga to check.
     */
    fun getDownloadCount(manga: Manga): Int {
        return cache.getDownloadCount(manga)
    }

    /**
     * Deletes the directory of a downloaded chapter.
     *
     * @param chapter the chapter to delete.
     * @param manga the manga of the chapter.
     * @param source the source of the chapter.
     */
    fun deleteChapter(chapter: Chapter, manga: Manga, source: Source) {
        pathProvider.findChapterDir(chapter, manga, source)?.delete()
        cache.removeChapter(chapter, manga)
    }

    /**
     * Deletes the directory of a downloaded manga.
     *
     * @param manga the manga to delete.
     * @param source the source of the manga.
     */
    fun deleteManga(manga: Manga, source: Source) {
        pathProvider.findMangaDir(manga, source)?.delete()
        cache.removeManga(manga)
    }
}