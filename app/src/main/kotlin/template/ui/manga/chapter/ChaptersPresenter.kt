package template.ui.manga.chapter

import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import template.data.database.DatabaseHelper
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.data.download.DownloadManager
import template.data.download.model.Download
import template.extensions.isNullOrDisposed
import template.source.Source
import template.ui.common.mvp.BasePresenter
import template.ui.manga.MangaController
import template.utils.syncChaptersWithSource
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by Robin Yeung on 9/7/18.
 */
class ChaptersPresenter : BasePresenter<ChaptersController>() {

    @Inject
    lateinit var db: DatabaseHelper

    @Inject
    lateinit var downloadManager: DownloadManager

    lateinit var manga: Manga

    lateinit var source: Source

    lateinit var chapterCountSubject: BehaviorSubject<Float>

    lateinit var lastUpdateSubject: BehaviorSubject<Date>

    lateinit var mangaFavoriteSubject: PublishSubject<Boolean>

    /**
     * List of chapters of the manga. It's always unfiltered and unsorted.
     */
    var chapterItems: List<ChapterItem> = emptyList()
        private set

    /**
     * Subject of list of chapters to allow updating the view without going to DB.
     */
    val chapterItemsSubject: PublishSubject<List<ChapterItem>>
            by lazy { PublishSubject.create<List<ChapterItem>>() }

    /**
     * Whether the chapter list has been requested to the source.
     */
    var hasRequested = false
        private set

    /**
     * Disposable to retrieve the new list of chapters from the source.
     */
    private var fetchChaptersDisposable: Disposable? = null

    /**
     * Disposable to observe download status changes.
     */
    private var observeDownloadsDisposable: Disposable? = null

    fun init(parentcontroller: MangaController) {
        this.manga = parentcontroller.manga!!
        this.source = parentcontroller.source!!
        this.chapterCountSubject = parentcontroller.chapterCountSubject
        this.lastUpdateSubject = parentcontroller.lastUpdateSubject
        this.mangaFavoriteSubject = parentcontroller.mangaFavoriteSubject

        chapterItemsSubject
                .flatMap {
                    applyChapterFilters(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeLatestCache({ _, error ->
                    Timber.e(error)
                }, ChaptersController::onNextChapters)

        // Add the disposable that retrieves the chapters from the database, keeps subscribed to
        // changes, and sends the list of chapters to the chapterItemsSubject.
        add(db.getChapters(manga)
                .asRxFlowable(BackpressureStrategy.BUFFER)
                .map { chapters ->
                    // Convert every chapter to a model.
                    chapters.map {
                        it.toModel()
                    }
                }
                .doOnNext { chapterItems ->
                    // find downloaded chapters
                    setDownloadedChapters(chapterItems)

                    // Store the last emission
                    this.chapterItems = chapterItems

                    // Listen for download status changes
                    observeDownloads()

                    // Emit the number of chapters to the info tab.
                    chapterCountSubject.onNext(
                            chapterItems.maxBy {
                                it.chapter_number
                            }?.chapter_number ?: 0f
                    )

                    // Emit the upload date of the most recent chapter
                    lastUpdateSubject.onNext(
                            Date(
                                    chapterItems.maxBy {
                                        it.date_upload
                                    }?.date_upload ?: 0
                            )
                    )
                }
                .subscribe {
                    chapterItemsSubject.onNext(it)
                }
        )
    }

    /**
     * Applies the view filters to the list of chapters obtained from the database.
     * @param chapters the list of chapters from the database
     * @return an observable of the list of chapters filtered and sorted.
     */
    private fun applyChapterFilters(chapters: List<ChapterItem>): Observable<List<ChapterItem>> {
        var observable = Observable
                .fromIterable(chapters)
                .subscribeOn(Schedulers.io())
                .filter {
                    if (onlyUnread()) {
                        it.read
                    } else if (onlyRead()) {
                        it.read
                    } else if (onlyDownloaded()) {
                        it.isDownloaded
                    } else if (onlyBookmarked()) {
                        it.bookmark
                    } else {
                        true
                    }
                }
        val sortFunction: (Chapter, Chapter) -> Int = when (manga.sorting) {
            Manga.SORTING_SOURCE -> when (sortDescending()) {
                true -> { c1, c2 -> c1.source_order.compareTo(c2.source_order) }
                false -> { c1, c2 -> c2.source_order.compareTo(c1.source_order) }
            }
            Manga.SORTING_NUMBER -> when (sortDescending()) {
                true -> { c1, c2 -> c1.chapter_number.compareTo(c2.chapter_number) }
                false -> { c1, c2 -> c2.chapter_number.compareTo(c1.chapter_number) }
            }
            else -> throw NotImplementedError("Unimplemented sorting method")
        }
        return observable.toSortedList(sortFunction).toObservable()
    }

    private fun observeDownloads() {
        observeDownloadsDisposable?.let {
            remove(it)
        }

        observeDownloadsDisposable = downloadManager.queue.getSingleDownloadStatusFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .filter { download ->
                    download.manga.id == manga.id
                }
                .doOnNext {
                    onDownloadStatusChange(it)
                }
                .subscribeLatestCache { view, download ->
                    view.onChapterStatusChange(download)
                }
    }

    /**
     * Called when a download for the active manga changes status.
     * @param download the download whose status changed.
     */
    private fun onDownloadStatusChange(download: Download) {
        // Assign the download to the model object.
        if (download.status == Download.QUEUE) {
            chapterItems.find {
                it.id == download.chapter.id
            }?.let {
                if (it.download == null) {
                    it.download = download
                }
            }
        }

        // Force UI update if downloaded filter active and download finished.
        if (onlyDownloaded() && download.status == Download.DOWNLOADED)
            refreshChapters()
    }

    /**
     * Downloads the given list of chapters with the manager.
     *
     * @param chapters the list of chapters to download.
     */
    fun downloadChapters(chapters: List<ChapterItem>) {
        downloadManager.downloadChapters(manga, chapters)
    }

    /**
     * Updates the UI after applying the filters.
     */
    private fun refreshChapters() {
        chapterItemsSubject.onNext(chapterItems)
    }

    /**
     * Converts a chapter from the database to an extended model, allowing to store new fields.
     */
    private fun Chapter.toModel(): ChapterItem {
        // Create the model object.
        val model = ChapterItem(this, manga)

        // TODO
        // Find an active download for this chapter.
//        val download = downloadManager.queue.find { it.chapter.id == id }
//
//        if (download != null) {
//            // If there's an active download, assign it.
//            model.download = download
//        }
        return model
    }

    /**
     * Finds and assigns the list of downloaded chapters.
     *
     * @param chapters the list of chapter from the database.
     */
    private fun setDownloadedChapters(chapters: List<ChapterItem>) {
        //TODO
//        for (chapter in chapters) {
//            if (downloadManager.isChapterDownloaded(chapter, manga)) {
//                chapter.status = Download.DOWNLOADED
//            }
//        }
    }

    /**
     * Request an updated list of chapters from the source.
     */
    fun fetchChaptersFromSource() {
        hasRequested = true

        if (!fetchChaptersDisposable.isNullOrDisposed()) return
        fetchChaptersDisposable = Observable.defer { source.fetchChapterList(manga) }
                .subscribeOn(Schedulers.io())
                .map {
                    syncChaptersWithSource(db, it, manga, source)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeFirst(ChaptersController::onFetchChaptersError) { view, pair ->
                    view.onFetchChaptersDone()
                }
    }

    /**
     * Whether the display only downloaded filter is enabled.
     */
    fun onlyDownloaded(): Boolean {
        return manga.downloadedFilter == Manga.SHOW_DOWNLOADED
    }

    /**
     * Whether the display only downloaded filter is enabled.
     */
    fun onlyBookmarked(): Boolean {
        return manga.bookmarkedFilter == Manga.SHOW_BOOKMARKED
    }

    /**
     * Whether the display only unread filter is enabled.
     */
    fun onlyUnread(): Boolean {
        return manga.readFilter == Manga.SHOW_UNREAD
    }

    /**
     * Whether the display only read filter is enabled.
     */
    fun onlyRead(): Boolean {
        return manga.readFilter == Manga.SHOW_READ
    }

    /**
     * Whether the sorting method is descending or ascending.
     */
    fun sortDescending(): Boolean {
        return manga.sortDescending()
    }
}