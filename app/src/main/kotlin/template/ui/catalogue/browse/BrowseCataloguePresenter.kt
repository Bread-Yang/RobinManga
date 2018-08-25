package template.ui.catalogue.browse

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import template.data.database.models.Manga
import template.source.CatalogueSource
import template.source.SourceManager
import template.source.model.FilterList
import template.source.model.SManga
import template.ui.common.mvp.BasePresenter
import template.utils.preference.PreferencesHelper
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
    lateinit var preferences: PreferencesHelper

    @Inject
    lateinit var sourceManager: SourceManager

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

        // Create a new pager.
        pager = createPager(query, filters)

        // Prepare the pager.
        pagerDisposable?.let {
            remove(it)
        }

        pagerDisposable = pager.results()
                .observeOn(Schedulers.io())
                .map {

                }

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

    /**
     * Returns a manga from the database for the given manga from network. It creates a new entry
     * if the manga is not yet in the database.
     *
     * @param sManga the manga from the source.
     * @return a manga from the database.
     */
    private fun networkToLocalManga(sManga: SManga, sourceId: Long): Manga {

    }
}