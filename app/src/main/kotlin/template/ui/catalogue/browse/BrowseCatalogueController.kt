package template.ui.catalogue.browse

import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible
import kotlinx.android.synthetic.main.catalogue_controller.*
import nucleus5.factory.RequiresPresenter
import template.R
import template.data.database.models.Manga
import template.extensions.gone
import template.extensions.visible
import template.ui.common.annotation.Layout
import template.ui.common.mvp.controller.NucleusController
import timber.log.Timber

/**
 * Created by Robin Yeung on 8/22/18.
 */
@Layout(R.layout.catalogue_controller)
@RequiresPresenter(BrowseCataloguePresenter::class)
class BrowseCatalogueController : NucleusController<BrowseCataloguePresenter>() {

    /**
     * Adapter containing the list of manga from the catalogue.
     */
    private var adapter: FlexibleAdapter<IFlexible<*>>? = null

    /**
     * Recycler view with the list of resultsPublicSubject.
     */
    private var recycler: RecyclerView? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        adapter = FlexibleAdapter(null)
        setupRecycler(view)
    }

    override fun initPresenter() {
        progressBar?.visible()
        // 请求数据
        presenter.setSourceId(4)
        presenter.restartPager()
    }


    fun setupRecycler(view: View) {

        var oldPosition = RecyclerView.NO_POSITION
        val oldRecycler = lltCatalogue.getChildAt(1)
        if (oldRecycler is RecyclerView) {
            oldPosition = (oldRecycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            oldRecycler.adapter = null

            lltCatalogue.removeView(oldRecycler)
        }

        val recycler =
                RecyclerView(view.context).apply {
                    id = R.id.recyclerView
                    layoutManager = LinearLayoutManager(context)
                    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                }

        recycler.setHasFixedSize(true)
        recycler.adapter = adapter

        lltCatalogue.addView(recycler, 1)

        if (oldPosition != RecyclerView.NO_POSITION) {
            recycler.layoutManager.scrollToPosition(oldPosition)
        }
        this.recycler = recycler
    }

    fun onMangaInitialize(manga: Manga) {
        getHolder(manga)?.setImage(manga)
    }

    /**
     * Returns the view holder for the given manga.
     *
     * @param manga the manga to find.
     * @return the holder of the manga or null if it's not bound.
     */
    private fun getHolder(manga: Manga): CatalogueHolder? {
        val adapter = adapter ?: return null

        adapter.allBoundViewHolders.forEach {
            val item = adapter.getItem(it.adapterPosition) as? CatalogueItem
            if (item != null && item.manga.id!! == manga.id!!) {
                return it as CatalogueHolder
            }
        }

        return null
    }

    /**
     * Called from the presenter when the network request is received.
     *
     * @param page the current page.
     * @param mangas the list of manga of the page.
     */
    fun onAddPage(page: Int, mangas: List<CatalogueItem>) {
        val adapter = adapter ?: return
        hideProgressBar()
        // TODO()

        adapter.onLoadMoreComplete(mangas)
    }

    /**
     * Called from the presenter when the network request fails.
     *
     * @param error the error received.
     */
    fun onAddPageError(error: Throwable) {
        Timber.e(error)
        val adapter = adapter ?: return
        adapter.onLoadMoreComplete(null)
    }

    /**
     * Hides active progress bars.
     */
    private fun hideProgressBar() {
        progressBar?.gone()
    }
}