package template.ui.catalogue.browse

import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import template.data.database.DatabaseHelper
import template.data.database.models.Manga
import template.source.CatalogueSource
import template.source.SourceManager
import template.source.model.FilterList
import template.source.model.SManga
import template.ui.common.mvp.BasePresenter
import template.data.preference.PreferencesHelper
import timber.log.Timber
import javax.inject.Inject

/**
 * Presenter of [BrowseCatalogueController].
 */
//class BrowseCataloguePresenter @Inject constructor(
//        sourceId: Long,
//        preferences: Lazy<PreferencesHelper>
//) : BasePresenter<BrowseCatalogueController>() {
//
//}
class BrowseCataloguePresenter : BasePresenter<BrowseCatalogueController>() {

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    @Inject
    lateinit var sourceManager: SourceManager

    @Inject
    lateinit var databaseHelper: DatabaseHelper

    var sourceId: Long? = null

    /**
     * Selected source.
     */
    lateinit var source: CatalogueSource

    /**
     * Pager containing a list of manga results.
     */
    private lateinit var pager: Pager

    /**
     * Subject that initializes a list of manga detail.
     */
    private val mangaDetailSubject = PublishSubject.create<List<Manga>>()

    /**
     * List of filters used by the [Pager]. If empty alongside [query], the popular query is used.
     */
    var appliedFilters = FilterList()

    /**
     * Query from the view.
     */
    var query = ""
        private set

    /**
     * Disposable for the pager.
     */
    private var pagerDisposable: Disposable? = null

    /**
     * Disposable for one request from the pager.
     */
    private var pageDisposable: Disposable? = null

    /**
     * Disposable for initialize manga details.
     */
    private var mangaDetailDisposable: Disposable? = null

    fun setSourceId(sourceId: Long) {
        this.sourceId = sourceId
        source = sourceManager.get(sourceId) as CatalogueSource
    }

    /**
     * Restarts the pager for the active source with the provided query and filters.
     *
     * @param query the query.
     * @param filters the current state of the filters (for search mode).
     */
    fun restartPager(query: String = this.query, filters: FilterList = this.appliedFilters) {
        this.query = query
        this.appliedFilters = filters

        subscribeToMangaDetails()

        // Create a new pager.
        pager = createPager(query, filters)

        val catalogueAsList = preferencesHelper.catalogueAsList()

        // Prepare the pager.
        pagerDisposable?.let {
            remove(it)
        }

        pagerDisposable = pager.resultsPublicSubject()
                .observeOn(Schedulers.io())
                .map {
                    it.first to it.second.map {
                        networkToLocalManga(it, source.id)
                    }
                }
                .doOnNext {
                    initializeMangas(it.second)
                }
                .map {
                    it.first to it.second.map {
                        CatalogueItem(it, catalogueAsList)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeReplay({ view, error ->           // 屏幕rotate之后事件会重新发送
                    Timber.e(error)
                }, { view, (page, mangas) ->
                    view.onAddPage(page, mangas)
                })

        // Request first page.
        requestNext()
    }

    /**
     * Requests the next page for the active pager.
     */
    fun requestNext() {
        if (!hasNextPage()) return

        pageDisposable?.let {
            remove(it)
        }

        pageDisposable = Observable
                .defer {
                    pager.requestNext()
                }.subscribeFirst(BrowseCatalogueController::onAddPageError) { _, _ ->
                    // Nothing to do when onNext is emitted.
                    /**
                     * 这里不处理，是因为由CataloguePager的PublicSubject[Pager.onPageReceived]负责再次转发事件出去
                     */
                }
    }

    fun hasNextPage() : Boolean {
        return pager.hasNextPage
    }

    open fun createPager(query: String, filters: FilterList): Pager {
        return CataloguePager(source, query, filters)
    }

    private fun subscribeToMangaDetails() {
        mangaDetailDisposable?.let {
            remove(it)
        }

        mangaDetailDisposable = mangaDetailSubject
                .observeOn(Schedulers.io())
                .flatMap {
                    // When to use flatMap operator :
                    // 1. When you're working with a list of data within a page, activity, or fragment and want to send some data to a server or a database per item of the list. The concatMap operator will also do here; however, as the flatMap operator works asynchronously, it'll be faster, and, as you're sending data, the order doesn't really matter.
                    // 2. Whenever you want to perform any operation on list items asynchronously and in a comparatively short time period.
                    Observable.fromIterable(it)
                }
                .filter {
                    it.thumbnail_url == null && !it.initialized
                }
                .concatMap {
                    // When to use concatMap operator :
                    // 1. When you are downloading the list of data to display to the user. The order really matters here, you will surely not want to load and display the second item of the list after the third and fourth one are already displayed, would you?
                    // 2. Performing some operation on a sorted list, making sure the list stays the same.
                    Timber.e("getMangaDetailsObservable")
                    getMangaDetailsObservable(it)
                }
                .toFlowable(BackpressureStrategy.MISSING)
                .onBackpressureDrop() {
                    Timber.e("BackpressureDrop : ${it.thumbnail_url}" )
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view?.onMangaInitialize(it)
                }, {
                    Timber.e(it)
                })
                .apply {
                    add(this)
                }
    }

    /**
     * Initialize a list of manga.
     *
     * @param mangas the list of manga to initialize.
     */
    fun initializeMangas(mangas: List<Manga>) {
        mangaDetailSubject.onNext(mangas)
    }

    /**
     * Returns an observable of manga that initializes the given manga.
     *
     * @param manga the manga to initialize.
     * @return an observable of the manga to initialize
     */
    private fun getMangaDetailsObservable(manga: Manga): Observable<Manga> {
        return source.fetchMangaDetails(manga)
                .flatMap {
                    manga.copyFrom(it)
                    manga.initialized = true

                    databaseHelper.insertManga(manga).executeAsBlocking()
                    Observable.just(manga)
                }
                .onErrorResumeNext { _: Throwable ->
                    Observable.just(manga)
                }
    }

    /**
     * Returns a manga from the database for the given manga from network. It creates a new entry
     * if the manga is not yet in the database.
     *
     * @param sManga the manga from the source.
     * @return a manga from the database.
     */
    private fun networkToLocalManga(sManga: SManga, sourceId: Long): Manga {
        var localManga = databaseHelper.getManga(sManga.url, sourceId).executeAsBlocking()
        if (localManga == null) {
            val newManga = Manga.create(sManga.url, sManga.title, sourceId)
            newManga.copyFrom(sManga)
            val result = databaseHelper.insertManga(newManga).executeAsBlocking()
            newManga.id = result.insertedId()
            localManga = newManga
        }
        return localManga
    }
}