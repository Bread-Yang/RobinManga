package template.ui.catalogue.global_search

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.catalogue_global_search_controller_card.*
import template.R
import template.data.database.models.Manga
import template.extensions.getResourceColor
import template.extensions.gone
import template.extensions.setVectorCompat
import template.extensions.visible
import template.ui.base.holder.BaseFlexibleViewHolder

/**
 * Holder that binds the [CatalogueSearchItem] containing catalogue cards.
 *
 * @param view view of [CatalogueSearchItem]
 * @param adapter instance of [CatalogueSearchAdapter]
 */
class CatalogueSearchHolder(view: View, val adapter: CatalogueSearchAdapter) :
        BaseFlexibleViewHolder(view, adapter) {

    /**
     * Adapter containing manga from search resultsPublicSubject.
     */
    private val mangaAdapter = CatalogueSearchCardAdapter(adapter.controller)

    private var lastBoundResults: List<CatalogueSearchCardItem>? = null

    init {
        // Set layout horizontal.
        recyclerView.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = mangaAdapter

        nothing_found_icon.setVectorCompat(R.drawable.ic_search_black_112dp,
                view.context.getResourceColor(android.R.attr.textColorHint))
    }

    /**
     * Show the loading of source search result.
     *
     * @param item item of card.
     */
    fun bind(item: CatalogueSearchItem) {
        val source = item.source
        val results = item.results

        // Set Title witch country code if available.
        ptvTitle.text = if (!source.lang.isEmpty()) "${source.name} (${source.lang})" else source.name

        when {
            results == null -> {
                progressBar.visible()
                nothing_found.gone()
            }
            results.isEmpty() -> {
                progressBar.gone()
                nothing_found.visible()
            }
            else -> {
                progressBar.gone()
                nothing_found.gone()
            }
        }
        if (results !== lastBoundResults) {
            mangaAdapter.updateDataSet(results)
            lastBoundResults = results
        }
    }

    /**
     * Called from the presenter when a manga is initialized.
     *
     * @param manga the initialized manga.
     */
    fun setImage(manga: Manga) {
        getHolder(manga)?.setImage(manga)
    }

    /**
     * Returns the view holder for the given manga.
     *
     * @param manga the manga to find.
     * @return the holder of the manga or null if it's not bound.
     */
    private fun getHolder(manga: Manga): CatalogueSearchCardHolder? {
        mangaAdapter.allBoundViewHolders.forEach { holder ->
            val item = mangaAdapter.getItem(holder.adapterPosition)
            if (item != null && item.manga.id!! == manga.id!!) {
                return holder as CatalogueSearchCardHolder
            }
        }

        return null
    }

}
