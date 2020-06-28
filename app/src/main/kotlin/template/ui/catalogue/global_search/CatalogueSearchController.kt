package template.ui.catalogue.global_search

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.SearchView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.jakewharton.rxbinding2.support.v7.widget.queryTextChangeEvents
import kotlinx.android.synthetic.main.catalogue_global_search_controller.*
import nucleus5.factory.RequiresPresenter
import template.R
import template.annotation.Layout
import template.data.database.models.Manga
import template.extensions.withFadeTransaction
import template.source.CatalogueSource
import template.ui.common.mvp.controller.NucleusDaggerController
import template.ui.manga.MangaController

/**
 * This controller shows and manages the different search result in global search.
 * This controller should only handle UI actions, IO actions should be done by [CatalogueSearchPresenter]
 * [CatalogueSearchCardAdapter.OnMangaClickListener] called when manga is clicked in global search
 */
@Layout(R.layout.catalogue_global_search_controller)
@RequiresPresenter(CatalogueSearchPresenter::class)
open class CatalogueSearchController(protected val initialQuery: String? = null) :
        NucleusDaggerController<CatalogueSearchPresenter>(),
        CatalogueSearchCardAdapter.OnMangaClickListener {

    /**
     * Adapter containing search resultsPublishSubject grouped by lang.
     */
    protected var adapter: CatalogueSearchAdapter? = null

    /**
     * Called when controller is initialized.
     */
    init {
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View) {
        adapter = CatalogueSearchAdapter(this)

        // Create recycler and set adapter.
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(view.context)
        recyclerView.adapter = adapter
    }

    override fun initPresenterOnce() {
        presenter.initialQuery = initialQuery
    }

    /**
     * Set the title of controller.
     *
     * @return title.
     */
    override fun getTitle(): String? {
        return presenter.query
    }

    /**
     * Called when manga in global search is clicked, opens manga.
     *
     * @param manga clicked item containing manga information.
     */
    override fun onMangaClick(manga: Manga) {
        // Open MangaController.
        router.pushController(MangaController(manga, true).withFadeTransaction())
    }

    /**
     * Called when manga in global search is long clicked.
     *
     * @param manga clicked item containing manga information.
     */
    override fun onMangaLongClick(manga: Manga) {
        // Delegate to single click by default.
        onMangaClick(manga)
    }

    /**
     * Adds items to the options menu.
     *
     * @param menu menu containing options.
     * @param inflater used to load the menu xml.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate menu.
        inflater.inflate(R.menu.catalogue_new_list, menu)

        // Initialize search menu
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchView.onActionViewExpanded() // Required to show the query in the view
                searchView.setQuery(presenter.query, false)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                return true
            }
        })


        searchView.queryTextChangeEvents()
                .filter {
                    it.isSubmitted
                }
                .subscribeUntilDestroy {
                    presenter.search(it.queryText().toString())
                    searchItem.collapseActionView()
//                    setTitle() // Update toolbar title
                }
    }

    override fun onDestroyView(view: View) {
        adapter = null
        super.onDestroyView(view)
    }

    override fun onSaveViewState(view: View, outState: Bundle) {
        super.onSaveViewState(view, outState)
        adapter?.onSaveInstanceState(outState)
    }

    override fun onRestoreViewState(view: View, savedViewState: Bundle) {
        super.onRestoreViewState(view, savedViewState)
        adapter?.onRestoreInstanceState(savedViewState)
    }

    /**
     * Returns the view holder for the given manga.
     *
     * @param source used to find holder containing source
     * @return the holder of the manga or null if it's not bound.
     */
    private fun getHolder(source: CatalogueSource): CatalogueSearchHolder? {
        val adapter = adapter ?: return null

        adapter.allBoundViewHolders.forEach { holder ->
            val item = adapter.getItem(holder.adapterPosition)
            if (item != null && source.id == item.source.id) {
                return holder as CatalogueSearchHolder
            }
        }

        return null
    }

    /**
     * Add search result to adapter.
     *
     * @param searchResult result of search.
     */
    fun setItems(searchResult: List<CatalogueSearchItem>) {
        adapter?.updateDataSet(searchResult)
    }

    /**
     * Called from the presenter when a manga is initialized.
     *
     * @param manga the initialized manga.
     */
    fun onMangaInitialized(source: CatalogueSource, manga: Manga) {
        getHolder(source)?.setImage(manga)
    }
}
