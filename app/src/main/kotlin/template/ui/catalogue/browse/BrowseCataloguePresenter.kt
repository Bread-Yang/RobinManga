package template.ui.catalogue.browse

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import template.source.CatalogueSource
import template.source.SourceManager
import template.source.model.FilterList
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
     * Subscription for one request from the pager.
     */
    private var pageDisposable: Disposable? = null

    fun setSourceId(sourceId: Long) {
        this.sourceId = sourceId
        source = sourceManager.get(sourceId) as CatalogueSource
        pager = createPager(query, appliedFilters)
    }

    /**
     * Requests the next page for the active pager.
     */
    fun requestNext() {

        pageDisposable = Observable
                .defer {
                    pager.requestNext()
                }.subscribeFirst { _, _ ->
                    // Nothing to do when onNext is emitted.
                    /**
                     * 这里不处理，是因为由CataloguePager的PublicSubject[Pager.onPageReceived]负责再次转发事件出去
                     */
                }
    }

    open fun createPager(query: String, filters: FilterList): Pager {
        return CataloguePager(source, query, filters)
    }

}