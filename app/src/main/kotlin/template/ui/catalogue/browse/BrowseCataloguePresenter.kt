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
import template.utils.preference.PreferencesHelper
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Robin Yeung on 8/22/18.
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

        pagerDisposable = pager.results()
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
                .subscribeReplay({ view, error ->
                    Timber.e(error)
                }, { view, (page, mangas) ->

                })

        // Request first page.
        requestNext()
    }

    /**
     * Requests the next page for the active pager.
     */
    fun requestNext() {

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
                    Observable.fromIterable(it)
                }
                .filter {
                    it.thumbnail_url == null && !it.initialized
                }
                .concatMap {
                    getMangaDetailsObservable(it)
                }
                .toFlowable(BackpressureStrategy.LATEST)
                .onBackpressureDrop {

                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    //                    view.onMangaInitialized(manga)
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

    private fun getMangaDetailsObservable(manga: Manga): Observable<Manga> {
        return source.fetchMangaDetails(manga)
                .flatMap {
                    manga.copyFrom(it)
                    manga.initialized = true

                    // TODO("inser database")

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