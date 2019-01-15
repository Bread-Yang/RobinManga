package template.ui.catalogue.global_search

import android.os.Bundle
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import template.data.database.DatabaseHelper
import template.data.database.models.Manga
import template.extensions.getOrDefault
import template.source.CatalogueSource
import template.source.Source
import template.source.SourceManager
import template.source.model.FilterList
import template.source.model.SManga
import template.source.online.LoginSource
import template.ui.catalogue.browse.BrowseCataloguePresenter
import template.ui.common.mvp.BasePresenter
import template.utils.preference.PreferencesHelper
import javax.inject.Inject

/**
 * Presenter of [CatalogueSearchController]
 * Function calls should be done from here. UI calls should be done from the controller.
 */
open class CatalogueSearchPresenter : BasePresenter<CatalogueSearchController>() {

    /**
     * manages the different sources.
     */
    @Inject
    lateinit var sourceManager: SourceManager

    /**
     * manages the database calls.
     */
    @Inject
    lateinit var db: DatabaseHelper

    /**
     * manages the preference calls.
     */
    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    var initialQuery: String? = ""

    /**
     * Enabled sources.
     */
    val sources by lazy {
        getEnabledSources()
    }

    /**
     * Query from the view.
     */
    var query = ""
        private set

    /**
     * Fetches the different sources by user settings.
     */
    private var fetchSourcesDisposable: Disposable? = null

    /**
     * Subject which fetches image of given manga.
     */
    private val fetchImageProcessor = PublishProcessor.create<Pair<List<Manga>, Source>>()

    /**
     * Disposable which fetches image of given manga.
     */
    private var fetchImageDisposable: Disposable? = null

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        // Perform a search with previous or initial state
        search(savedState?.getString(BrowseCataloguePresenter::query.name)
                ?: initialQuery.orEmpty())
    }

    override fun onDestroy() {
        fetchSourcesDisposable?.dispose()
        fetchImageDisposable?.dispose()
        super.onDestroy()
    }

    override fun onSave(state: Bundle) {
        state.putString(BrowseCataloguePresenter::query.name, query)
        super.onSave(state)
    }

    /**
     * Initiates a search for manga per catalogue.
     *
     * @param query query on which to search.
     */
    fun search(query: String) {
        // Return if there's nothing to do
        if (this.query == query)
            return

        // Update query
        this.query = query

        // Create image fetch disposable
        initializeFetchImageDisposable()

        // Create items with the initial state
        val initialItems = sources.map {
            CatalogueSearchItem(it, null)
        }
        var items = initialItems

        fetchSourcesDisposable?.dispose()
        fetchSourcesDisposable = Observable.fromIterable(sources)
                .flatMap({ source ->
                    source.fetchSearchManga(1, query, FilterList())
                            .subscribeOn(Schedulers.io())
                            .onExceptionResumeNext(Observable.empty()) // Ignore timeouts.
                            .map {
                                it.mangas.take(10)     // Get at most 10 manga from search result.
                            }
                            .map {
                                it.map {
                                    networkToLocalManga(it, source.id)  // Convert to local image.
                                }
                            }
                            .doOnNext {
                                fetchImage(it, source)  // Load manga covers.
                            }
                            .map {
                                CatalogueSearchItem(source, it.map { CatalogueSearchCardItem(it) })
                            }
                }, 5)
                .observeOn(AndroidSchedulers.mainThread())
                // Update matching source with the obtained resultsPublishSubject
                .map { result ->
                    items.map { item ->
                        if (item.source == result.source) result else item
                    }
                }
                // Update current state
                .doOnNext {
                    items = it
                }
                // Deliver initial state
                .startWith(initialItems)
                .subscribeLatestCache { view, manga ->
                    view.setItems(manga)
                }
    }

    /**
     * Initialize a list of manga.
     *
     * @param manga the list of manga to initialize.
     */
    private fun fetchImage(manga: List<Manga>, source: Source) {
        fetchImageProcessor.onNext(Pair(manga, source))
    }

    /**
     * Subscribes to the initializer of manga details and updates the view if needed.
     */
    private fun initializeFetchImageDisposable() {
        fetchImageDisposable?.dispose()
        fetchImageDisposable = fetchImageProcessor
                .observeOn(Schedulers.io())
                .flatMap {
                    val source = it.second

                    Flowable.fromIterable(it.first)
                            .filter {
                                it.thumbnail_url == null && !it.initialized
                            }
                            .map {
                                Pair(it, source)
                            }
                            .concatMap {
                                getMangaDetailsFlowable(it.first, it.second)
                            }
                            .map {
                                Pair(source as CatalogueSource, it)
                            }
                }
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { (source, manga) ->
                    view?.onMangaInitialized(source, manga)
                }
    }

    /**
     * Returns a list of enabled sources ordered by language and name.
     *
     * @return list containing enabled sources.
     */
    protected open fun getEnabledSources(): List<CatalogueSource> {
        val languages = preferencesHelper.enabledLanguages().getOrDefault()
        val hiddenCatalogues = preferencesHelper.hiddenCatalogues().getOrDefault()

        return sourceManager.getCatalogueSources()
                .filter {
                    it.lang in languages
                }
                .filterNot {
                    it is LoginSource && !it.isLogged()
                }
                .filterNot {
                    it.id.toString() in hiddenCatalogues
                }
                .sortedBy {
                    // sortedBy的用法
                    "(${it.lang}) ${it.name}"
                }
    }

    /**
     * Returns an Flowable of manga that initializes the given manga.
     *
     * @param manga the manga to initialize.
     * @return an observable of the manga to initialize
     */
    private fun getMangaDetailsFlowable(manga: Manga, source: Source): Flowable<Manga> {
        return source.fetchMangaDetails(manga)
                .flatMap { networkManga ->
                    manga.copyFrom(networkManga)
                    manga.initialized = true
                    db.insertManga(manga).executeAsBlocking()
                    Observable.just(manga)
                }
                .toFlowable(BackpressureStrategy.BUFFER)
                .onErrorResumeNext(Flowable.just(manga))
    }

    /**
     * Returns a manga from the database for the given manga from network. It creates a new entry
     * if the manga is not yet in the database.
     *
     * @param sManga the manga from the source.
     * @return a manga from the database.
     */
    private fun networkToLocalManga(sManga: SManga, sourceId: Long): Manga {
        var localManga = db.getManga(sManga.url, sourceId).executeAsBlocking()
        if (localManga == null) {
            val newManga = Manga.create(sManga.url, sManga.title, sourceId)
            newManga.copyFrom(sManga)
            val result = db.insertManga(newManga).executeAsBlocking()
            newManga.id = result.insertedId()
            localManga = newManga
        }
        return localManga
    }
}
