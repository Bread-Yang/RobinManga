package template.ui.catalogue.browse

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import template.source.CatalogueSource
import template.source.model.FilterList
import template.source.model.MangasPage

/**
 * Created by Robin Yeung on 8/25/18.
 */
open class CataloguePager(val source: CatalogueSource, val query: String, val filters: FilterList) : Pager() {

    override fun requestNext(): Observable<MangasPage> {
        val page = currentPage

        val observable =
                if (query.isBlank() && filters.isEmpty())
                    source.fetchPopularManga(page)
                else
                    source.fetchSearchManga(page, query, filters)

        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it.mangas.isNotEmpty()) {
                        onPageReceived(it)
                    } else {
                        throw NoResultsException()
                    }
                }
    }

}